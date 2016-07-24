package de.dustplanet.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * PasswordProtect for CraftBukkit/Spgiot.
 * Handles commands.
 *
 * Refer to the dev.bukkit.org page:
 * http://dev.bukkit.org/bukkit-plugins/passwordprotect/
 *
 * @author xGhOsTkiLLeRx
 * thanks to brianewing alias DisabledHamster for the original plugin!
 *
 */

public class PasswordProtectCommands implements CommandExecutor {
    private PasswordProtect plugin;
    public PasswordProtectCommands(PasswordProtect instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        // Send the password back, if the sender is the console or if the player has got the permission
        if (command.getName().equalsIgnoreCase("password")) {
            // If a player send the command
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // If the player hasn't got the permission, don't display the password
                if (!player.hasPermission("passwordprotect.getpassword")) {
                    String messageLocalization = plugin.getLocalization().getString("permission_denied");
                    plugin.message(player, messageLocalization, null);
                    return true;
                }
            }
            // Could be null, additional checks required
            String cleanServerPassword = plugin.getPasswordClean();
            // If password is only stored encrypted
            if (cleanServerPassword == null && plugin.passwordSet() && !plugin.getConfig().getBoolean("cleanPassword", false)) {
                String messageLocalization = plugin.getLocalization().getString("only_hashed");
                plugin.message(sender, messageLocalization, null);
            } else if (cleanServerPassword == null && !plugin.passwordSet()) {
                // If no password is set, tell the sender the instructions
                String messageLocalization = plugin.getLocalization().getString("password_not_set");
                plugin.message(sender, messageLocalization, null);
            } else if (cleanServerPassword != null && plugin.passwordSet()) {
                // Tell the sender the password
                String messageLocalization = plugin.getLocalization().getString("password");
                plugin.message(sender, messageLocalization, cleanServerPassword);
            } else {
                // Else debug
                sender.sendMessage(ChatColor.DARK_RED + "You shouldn't see this message. Please report this issue, including a copy of the config!");
            }
            // If password is not set, but a clean one is set and cleanPassword is enabled
            if (cleanServerPassword != null && !plugin.passwordSet() && plugin.getConfig().getBoolean("cleanPassword", false)) {
                String messageLocalization = plugin.getLocalization().getString("config_invalid");
                plugin.message(sender, messageLocalization, null);
            }
        } else if (command.getName().equalsIgnoreCase("setjaillocation")) {
            // Sets the jail location
            // Console can't define jaillocation
            if (!(sender instanceof Player)) {
                String messageLocalization = plugin.getLocalization().getString("only_ingame");
                plugin.message(sender, messageLocalization, null);
            } else {
                Player player = (Player) sender;
                // If the player hasn't got the permission, cancel it
                if (!player.hasPermission("passwordprotect.setjailarea")) {
                    String messageLocalization = plugin.getLocalization().getString("permission_denied");
                    plugin.message(player, messageLocalization, null);
                    return true;
                }

                // Gets the players location
                World world = player.getWorld();
                // Default radius of four
                int radius = 4;
                if (args.length >= 1) {
                    try {
                        radius = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        // Radius not a number? Use default!
                        String messageLocalization = plugin.getLocalization().getString("radius_not_number");
                        plugin.message(player, messageLocalization, null);
                        radius = 4; // Better safe than sorry
                    }
                }
                // Make a new jail location and store it
                JailLocation loc = new JailLocation(player.getLocation(), radius);
                plugin.setJailLocation(world, loc);
                String messageLocalization = plugin.getLocalization().getString("jail_set");
                plugin.message(player, messageLocalization, null);
            }
        } else if (command.getName().equalsIgnoreCase("setpassword")) {
            // If someone tries to set a password
            if (args.length != 1) {
                return false;
            }
            // The password is the first argument
            String password = args[0];
            // If the player hasn't got the permission, cancel it
            if (!sender.hasPermission("passwordprotect.setpassword")) {
                String messageLocalization = plugin.getLocalization().getString("permission_denied");
                plugin.message(sender, messageLocalization, null);
                return true;
            }
            // Set password
            plugin.setPassword(password);
            String messageLocalization = plugin.getLocalization().getString("password_set");
            plugin.message(sender, messageLocalization, null);
            // Reminder for the jail area
            if (sender instanceof Player) {
                messageLocalization = plugin.getLocalization().getString("set_jail_area");
                plugin.message(sender, messageLocalization, null);
            }
        } else if (command.getName().equalsIgnoreCase("login") && !(sender instanceof Player)) {
            // Console logins should be denied
            String messageLocalization = plugin.getLocalization().getString("no_login_console");
            plugin.message(sender, messageLocalization, null);
        }
        return true;
    }
}
