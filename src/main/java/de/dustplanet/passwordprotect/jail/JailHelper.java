package de.dustplanet.passwordprotect.jail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class to handle jail locations and players that are jailed. File handling is not part of this helper.
 *
 * @author timbru31
 */
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "PMD.UseConcurrentHashMap", "PMD.DataflowAnomalyAnalysis" })
@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "CD_CIRCULAR_DEPENDENCY" })
public class JailHelper {
    private static final int SLOWNESS_AMPLIFIER = 5;
    private static final int DARKNESS_AMPLIFIER = 15;
    private static final int DEFAULT_RADIUS = 4;
    @SuppressWarnings("PMD.LongVariable")
    private static final int TWENTY_FOUR_HOURS_IN_TICKS = 20 * 60 * 60 * 24;
    @Getter
    private final Map<World, JailLocation> jailLocations = new HashMap<>();
    private final FileConfiguration jails;
    private final File jailFile;
    private final PasswordProtect plugin;
    private final PasswordProtectUtilities utils;
    @Getter
    @Setter
    private Map<UUID, Integer> jailedPlayers = new HashMap<>();
    @Getter
    private final Map<UUID, Location> playerLocations = new HashMap<>();

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public JailHelper(final PasswordProtect instance, final File jailFile, final FileConfiguration jails) {
        plugin = instance;
        this.jailFile = jailFile;
        this.jails = jails;
        utils = instance.getUtils();
    }

    /**
     * Clears all maps.
     */
    public void clear() {
        getJailedPlayers().clear();
        getJailLocations().clear();
        getPlayerLocations().clear();
    }

    /**
     * Sets a new jail location in the given world.
     *
     * @param world the world to update the jail location in
     * @param location the new jail location
     */
    @SuppressFBWarnings("OPM_OVERLY_PERMISSIVE_METHOD")
    public void setJailLocation(final World world, final JailLocation location) {
        jailLocations.put(world, location);
        final ArrayList<Double> data = new ArrayList<>();
        data.add(location.getX());
        data.add(location.getY());
        data.add(location.getZ());
        data.add(Double.valueOf(location.getYaw()));
        data.add(Double.valueOf(location.getPitch()));
        data.add(Double.valueOf(location.getRadius()));
        final String worldName = world.getName();
        jails.set(worldName + ".jailLocation", data);
        saveJails();
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private JailLocation getJailLocation(final Player player) {
        final World world = player.getWorld();
        JailLocation jailLocation;

        jailLocation = jailLocations.get(world);
        if (jailLocation == null) {
            jailLocation = loadJailLocation(world);
            final Location spawnLocation = world.getSpawnLocation();
            if (jailLocation == null) {
                jailLocation = new JailLocation(world, spawnLocation.getX(),
                        spawnLocation.getWorld().getHighestBlockYAt(spawnLocation.getBlockX(), spawnLocation.getBlockZ()) + 1,
                        spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch(), DEFAULT_RADIUS);
                jailLocations.put(world, jailLocation);
                setJailLocation(world, jailLocation);
            }
        }
        return jailLocation;
    }

    /**
     * Ensures a player is within the jail boundaries. May teleport him back.
     *
     * @param player the player to check
     */
    public void stayInJail(final Player player) {
        if (plugin.getConfig().getBoolean("disableJailArea", false)) {
            return;
        }
        final JailLocation jailLocation = getJailLocation(player);
        final Location playerLocation = player.getLocation();
        final int radius = jailLocation.getRadius();
        // If player is within radius^2 blocks of jail location...
        if (Math.abs(jailLocation.getBlockX() - playerLocation.getBlockX()) <= radius
                && Math.abs(jailLocation.getBlockY() - playerLocation.getBlockY()) <= radius
                && Math.abs(jailLocation.getBlockZ() - playerLocation.getBlockZ()) <= radius) {
            return;
        }
        sendToJail(player);
    }

    /**
     * Checks if a player needs to be jailed.
     *
     * @param player the player to check
     */
    @SuppressWarnings({ "checkstyle:CyclomaticComplexity", "checkstyle:NPathComplexity", "PMD.CyclomaticComplexity",
            "PMD.NPathComplexity" })
    public void check(final Player player) {
        if (!utils.isPasswordSet()) {
            if (player.hasPermission("passwordprotect.setpassword")) {
                final String messageLocalization = plugin.getLocalization().getString("set_password");
                utils.message(player, messageLocalization, null);
            }
            return;
        }

        if (checkPlayerPermissions(player)) {
            if (plugin.getConfig().getBoolean("teleportBack", true) && !getPlayerLocations().containsKey(player.getUniqueId())) {
                getPlayerLocations().put(player.getUniqueId(), player.getLocation());
            }

            if (!plugin.getConfig().getBoolean("disableJailArea", false)) {
                sendToJail(player);
            }

            if (!getJailedPlayers().containsKey(player.getUniqueId())) {
                getJailedPlayers().put(player.getUniqueId(), 1);
            }
            if (plugin.getConfig().getBoolean("prevent.flying", true)) {
                player.setAllowFlight(false);
            }
            if (plugin.getConfig().getBoolean("darkness", true)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, TWENTY_FOUR_HOURS_IN_TICKS, DARKNESS_AMPLIFIER));
            }
            if (plugin.getConfig().getBoolean("slowness", true)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, TWENTY_FOUR_HOURS_IN_TICKS, SLOWNESS_AMPLIFIER));
            }

            utils.sendPasswordRequiredMessage(player);
        }
    }

    private boolean checkPlayerPermissions(final Player player) {
        if (player.isOp()) {
            return plugin.getConfig().getBoolean("opsRequirePassword", true);
        }
        return !player.hasPermission("passwordprotect.nopassword");
    }

    private void sendToJail(final Player player) {
        final JailLocation jailLocation = getJailLocation(player);
        player.teleport(jailLocation);
    }

    @Nullable
    @SuppressWarnings({ "checkstyle:MagicNumber", "PMD.ShortVariable", "PMD.AvoidLiteralsInIfCondition" })
    private JailLocation loadJailLocation(final World world) {
        final String worldName = world.getName();
        final List<Double> data = jails.getDoubleList(worldName + ".jailLocation");
        // [x, y, z, yaw, pitch, radius]
        if (data.size() != 6) {
            return null;
        }
        final double x = data.get(0);
        final double y = data.get(1);
        final double z = data.get(2);
        final float yaw = data.get(3).floatValue();
        final float pitch = data.get(4).floatValue();
        final int radius = data.get(5).intValue();

        return new JailLocation(world, x, y, z, yaw, pitch, radius);
    }

    private void saveJails() {
        try {
            jails.save(jailFile);
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save the jails.yml!", e);
        }
    }

}
