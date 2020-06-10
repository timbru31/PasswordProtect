package de.dustplanet.passwordprotect.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.commands.PasswordProtectLoginCommand;
import de.dustplanet.passwordprotect.commands.PasswordProtectPasswordCommand;
import de.dustplanet.passwordprotect.commands.PasswordProtectSetJailLocationCommand;
import de.dustplanet.passwordprotect.commands.PasswordProtectSetPasswordCommand;
import de.dustplanet.passwordprotect.jail.JailHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

/**
 * PasswordProtect utility functions.
 *
 * @author timbru31
 */
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "ClassFanOutComplexity", "PMD.DataflowAnomalyAnalysis" })
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class PasswordProtectUtilities {
    private static final int BUFFER_SIZE = 1024;

    private final PasswordProtect plugin;
    private final String[] commands = { "help", "rules", "motd" };
    @Getter
    @Setter
    private List<String> commandList = new ArrayList<>();
    @Getter
    @Setter
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private String hashAlgorithm = "SHA-512";

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY" })
    public PasswordProtectUtilities(final PasswordProtect instance) {
        plugin = instance;
    }

    /**
     * Copies a given YML formatted string to a target file.
     *
     * @param yml the YML string
     * @param file the file to write to
     */
    @SuppressWarnings({ "PMD.AssignmentInOperand", "PMD.DataflowAnomalyAnalysis" })
    @SuppressFBWarnings({ "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", "RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE",
            "NP_LOAD_OF_KNOWN_NULL_VALUE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE" })
    public void copy(final String yml, final File file) {
        try (OutputStream out = Files.newOutputStream(file.toPath()); InputStream inputStream = plugin.getResource(yml)) {
            final byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy the default config!", e);
        }
    }

    /**
     * Loads the config and the default values.
     */
    @SuppressWarnings({ "checkstyle:ExecutableStatementCount", "checkstyle:MagicNumber", "PMD.AvoidDuplicateLiterals",
            "PMD.AvoidPrintStackTrace" })
    @SuppressFBWarnings("IMC_IMMATURE_CLASS_PRINTSTACKTRACE")
    public void loadConfig() {
        final FileConfiguration config = plugin.getConfig();
        config.options().header("For help please refer to https://dev.bukkit.org/projects/passwordprotect");
        config.addDefault("hash", "SHA-512");
        config.addDefault("disableJailArea", Boolean.FALSE);
        config.addDefault("opsRequirePassword", Boolean.TRUE);
        config.addDefault("cleanPassword", Boolean.FALSE);
        config.addDefault("password", "");
        config.addDefault("passwordClean", "");
        config.addDefault("prevent.movement", Boolean.TRUE);
        config.addDefault("prevent.interaction", Boolean.TRUE);
        config.addDefault("prevent.interactionMobs", Boolean.TRUE);
        config.addDefault("prevent.itemPickup", Boolean.TRUE);
        config.addDefault("prevent.itemDrop", Boolean.TRUE);
        config.addDefault("prevent.portal", Boolean.TRUE);
        config.addDefault("prevent.blockPlace", Boolean.TRUE);
        config.addDefault("prevent.blockBreak", Boolean.TRUE);
        config.addDefault("prevent.triggering", Boolean.TRUE);
        config.addDefault("prevent.attacks", Boolean.TRUE);
        config.addDefault("prevent.damage", Boolean.TRUE);
        config.addDefault("prevent.chat", Boolean.TRUE);
        config.addDefault("prevent.deathDrops", Boolean.TRUE);
        config.addDefault("prevent.flying", Boolean.TRUE);
        config.addDefault("wrongAttempts.kick", 3);
        config.addDefault("wrongAttempts.ban", 5);
        config.addDefault("wrongAttempts.banIP", Boolean.TRUE);
        config.addDefault("broadcast.kick", Boolean.TRUE);
        config.addDefault("broadcast.ban", Boolean.TRUE);
        config.addDefault("darkness", Boolean.TRUE);
        config.addDefault("slowness", Boolean.TRUE);
        config.addDefault("teleportBack", Boolean.TRUE);
        config.addDefault("loginMessage", Boolean.TRUE);
        config.addDefault("allowedCommands", Arrays.asList(commands));
        setCommandList(config.getStringList("allowedCommands"));
        if (config.contains("hash")) {
            hashAlgorithm = config.getString("hash");
        } else {
            hashAlgorithm = config.getString("encryption");
        }

        try {
            MessageDigest.getInstance(hashAlgorithm);
        } catch (final NoSuchAlgorithmException e) {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "PasswordProtect can't use this hash! FATAL!");
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Falling back to SHA-512!");
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Report this IMMEDIATELY!");
            hashAlgorithm = "SHA-512";
            config.set("hash", "SHA-512");
            e.printStackTrace();
        }
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Loads the localization YML and writes the default to a file.
     *
     * @param localization the YML localization object
     * @param localizationFile the file to save the defaults to
     */
    public void loadLocalization(final FileConfiguration localization, final File localizationFile) {
        localization.options().header("The underscores are used for the different lines!");
        localization.addDefault("permission_denied", "&4You don't have the permission to do this!");
        localization.addDefault("enter_password",
                "&eThis server is password-protected\n&eEnter the password with &a/login &4<password> &eto play");
        localization.addDefault("set_password",
                "&ePasswordProtect is enabled but no password has been set\n&eUse &a/setpassword &4<password> &eto set it");
        localization.addDefault("password_accepted", "&aServer password accepted, you can now play");
        localization.addDefault("attempts_left_kick", "&4Server password incorrect! &e%attempts &4attempts left until kick...");
        localization.addDefault("attempts_left_ban", "&4Server password incorrect! &e%attempts &4attempts left until ban...");
        localization.addDefault("kick_message", "&4Kicked by &ePasswordProtect &4for too many wrong attempts...");
        localization.addDefault("ban_message", "&4Banned by &ePasswordProtect &4for too many wrong attempts...");
        localization.addDefault("kick_broadcast", "&e%player &4kicked by &ePasswordProtect &4for too many wrong attempts...");
        localization.addDefault("ban_broadcast", "&e%player &4banned by &ePasswordProtect &4for too many wrong attempts...");
        localization.addDefault("radius_not_number", "&4The radius was not a number! Using standard (4) instead!");
        localization.addDefault("jail_set", "&aJail location set");
        localization.addDefault("password_set", "&aServer password set!");
        localization.addDefault("only_ingame", "&4The command can only be used ingame!");
        localization.addDefault("config_invalid", "&4It seems like this server config invalid. Please re-set the password!");
        localization.addDefault("only_hashed", "&4Server password is only stored hashed...");
        localization.addDefault("password_not_set", "&eServer password is not set. Use /setpassword <password>");
        localization.addDefault("password", "&eServer password is &4%password");
        localization.addDefault("set_jail_area", "&eYou can set the jail area by going somewhere and using &a/setjaillocation &4[radius]");
        localization.addDefault("already_logged_in", "&eYou are already logged in!");
        localization.addDefault("no_login_console", "&eThe console can't login into the server!");
        localization.options().copyDefaults(true);
        saveLocalization(localization, localizationFile);
    }

    private void saveLocalization(final FileConfiguration localization, final File localizationFile) {
        try {
            localization.save(localizationFile);
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save the localization.yml", e);
        }
    }

    /**
     * Registers the commands.
     */
    public void registerCommands(final JailHelper jailHelper) {
        final PluginCommand loginCommand = plugin.getCommand("login");
        if (loginCommand != null) {
            loginCommand.setExecutor(new PasswordProtectLoginCommand(plugin));
        }
        final PluginCommand passwordCommand = plugin.getCommand("password");
        if (passwordCommand != null) {
            passwordCommand.setExecutor(new PasswordProtectPasswordCommand(plugin));
        }
        final PluginCommand setPasswordCommand = plugin.getCommand("setpassword");
        if (setPasswordCommand != null) {
            setPasswordCommand.setExecutor(new PasswordProtectSetPasswordCommand(plugin));
        }
        final PluginCommand setJailLocationPassword = plugin.getCommand("setjaillocation");
        if (setJailLocationPassword != null) {
            setJailLocationPassword.setExecutor(new PasswordProtectSetJailLocationCommand(plugin, jailHelper));
        }
    }

    /**
     * Messages a command sender a message with replacements.
     *
     * @param sender the sender to receive the message
     * @param message the message
     * @param value the replacement that should take place
     */
    public void message(final CommandSender sender, final String message, final String value) {
        final PluginDescriptionFile pdfFile = plugin.getDescription();
        if (message == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Somehow this message is not defined. Please check your localization.yml");
        } else {
            String valueToSend = value;
            if (value == null) {
                valueToSend = "";
            }
            final String replacedMessage = message.replace("%attempts", valueToSend).replace("%password", valueToSend).replace("%version",
                    pdfFile.getVersion());
            final String[] messageToSend = ChatColor.translateAlternateColorCodes('&', replacedMessage).split("\n");
            if (sender != null) {
                sender.sendMessage(messageToSend);
            }
        }
    }

    /**
     * Saves a password, hashed if enabled.
     *
     * @param password the clean text password
     */
    public void setPassword(final String password) {
        final String hashedPassword = hash(password);
        plugin.getConfig().set("password", hashedPassword);
        if (plugin.getConfig().getBoolean("cleanPassword", false)) {
            plugin.getConfig().set("passwordClean", password);
        }
        plugin.saveConfig();
    }

    /**
     * Gets the password in cleartext.
     *
     * @return the password or null if cleanPassword is false
     */
    @Nullable
    public String getCleanPassword() {
        if (plugin.getConfig().getBoolean("cleanPassword", false)) {
            return plugin.getConfig().getString("passwordClean");
        }
        return null;
    }

    /**
     * Gets the password (hash).
     *
     * @return the password or null
     */
    @Nullable
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String getPassword() {
        return plugin.getConfig().getString("password");
    }

    /**
     * Checks if the password is set in the config.
     *
     * @return the result
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean isPasswordSet() {
        return !plugin.getConfig().getString("password", "").isEmpty();
    }

    /**
     * Tries to hash a given password.
     *
     * @param password the password to hash
     * @return the result hash or null
     */
    @Nullable
    public String hash(final String password) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            messageDigest.update(password.getBytes(Charset.defaultCharset()));
            final byte[] byteData = messageDigest.digest();
            return String.format("%0" + (byteData.length << 1) + "x", new BigInteger(1, byteData));
        } catch (final NoSuchAlgorithmException e) {
            plugin.getServer().getLogger().log(Level.SEVERE, "The algorithm is NOT known: " + hashAlgorithm.replaceAll("[\r\n]", ""), e);
            return null;
        }
    }

    /**
     * Sends a password required message to a player.
     *
     * @param player the player to receive the message
     */
    public void sendPasswordRequiredMessage(final Player player) {
        if (plugin.getConfig().getBoolean("loginMessage", true)) {
            final String messageLocalization = plugin.getLocalization().getString("enter_password");
            message(player, messageLocalization, null);
        }
    }

}
