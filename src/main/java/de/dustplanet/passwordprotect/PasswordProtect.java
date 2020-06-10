package de.dustplanet.passwordprotect;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import de.dustplanet.passwordprotect.jail.JailHelper;
import de.dustplanet.passwordprotect.listeners.PasswordProtectBlockListener;
import de.dustplanet.passwordprotect.listeners.PasswordProtectEntityListener;
import de.dustplanet.passwordprotect.listeners.PasswordProtectPlayerListener;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import de.dustplanet.passwordprotect.utils.ScalarYamlConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

/**
 * PasswordProtect for CraftBukkit/Spigot. Handles some general stuff. Refer to the dev.bukkit.org page:
 * https://dev.bukkit.org/projects/passwordprotect
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "CD_CIRCULAR_DEPENDENCY" })
@SuppressWarnings({ "checkstyle:MissingCtor", "checkstyle:MultipleStringLiterals", "checkstyle:ClassDataAbstractionCoupling",
        "PMD.AtLeastOneConstructor" })
public class PasswordProtect extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 2038;
    private JailHelper jailHelper;
    @Getter
    @Setter
    private FileConfiguration localization;
    private File jailedPlayersFile;
    @Getter
    private final PasswordProtectUtilities utils = new PasswordProtectUtilities(this);

    @Override
    @SuppressFBWarnings({ "NP_UNWRITTEN_FIELD", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
    public void onDisable() {
        for (final UUID playerUUID : jailHelper.getJailedPlayers().keySet()) {
            final Player player = getServer().getPlayer(playerUUID);
            if (player != null) {
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
                if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                    player.removePotionEffect(PotionEffectType.SLOW);
                }
            }
        }

        try (ObjectOutputStream obj = new ObjectOutputStream(Files.newOutputStream(jailedPlayersFile.toPath()))) {
            obj.writeObject(jailHelper.getJailedPlayers());
        } catch (final IOException e) {
            getLogger().log(Level.INFO, "Couldn't find the 'jailedPlayers.dat' file!", e);
        }

        jailHelper.clear();
        utils.getCommandList().clear();
    }

    @Override
    public void onEnable() {
        final File jailFile = new File(getDataFolder(), "jails.yml");
        if (!jailFile.exists()) {
            utils.copy("jails.yml", jailFile);
        }
        final ScalarYamlConfiguration jails = ScalarYamlConfiguration.loadConfiguration(jailFile);
        final PluginManager pluginManager = getServer().getPluginManager();

        jailHelper = new JailHelper(this, jailFile, jails);
        pluginManager.registerEvents(new PasswordProtectBlockListener(this, jailHelper), this);
        pluginManager.registerEvents(new PasswordProtectPlayerListener(this, jailHelper), this);
        pluginManager.registerEvents(new PasswordProtectEntityListener(this, jailHelper), this);

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("The config folder could NOT be created, make sure it's writable!");
            getLogger().severe("Disabling now!");
            setEnabled(false);
            return;
        }

        final File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            utils.copy("config.yml", configFile);
        }
        utils.loadConfig();

        final File localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            utils.copy("localization.yml", localizationFile);
        }
        setLocalization(ScalarYamlConfiguration.loadConfiguration(localizationFile));
        utils.loadLocalization(getLocalization(), localizationFile);

        loadJailedPlayers();

        utils.registerCommands(jailHelper);

        registerMetrics();
    }

    @SuppressWarnings("unchecked")
    @SuppressFBWarnings("OBJECT_DESERIALIZATION")
    private void loadJailedPlayers() {
        jailedPlayersFile = new File(getDataFolder(), "jailedPlayers.dat");
        if (jailedPlayersFile.exists()) {
            try (ObjectInputStream obj = new ObjectInputStream(Files.newInputStream(jailedPlayersFile.toPath()))) {
                jailHelper.setJailedPlayers((HashMap<UUID, Integer>) obj.readObject());
            } catch (IOException | ClassNotFoundException e) {
                getLogger().log(Level.INFO, "Couldn't read the 'jailedPlayers.dat' file!", e);
            }
        } else {
            try {
                if (!jailedPlayersFile.createNewFile()) {
                    getLogger().info("Creating the 'jailedPlayers.dat' file failed!");
                }
            } catch (final IOException e) {
                getLogger().log(Level.INFO, "Couldn't create the 'jailedPlayers.dat' file!", e);
            }

        }
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings("SEC_SIDE_EFFECT_CONSTRUCTOR")
    private void registerMetrics() {
        new Metrics(this, BSTATS_PLUGIN_ID);
    }

}
