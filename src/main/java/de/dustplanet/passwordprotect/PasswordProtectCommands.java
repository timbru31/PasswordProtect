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
 * https://dev.bukkit.org/projects/passwordprotect
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
        if (command.getName().equalsIgnoreCase("password")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("passwordprotect.getpassword")) {
                    String messageLocalization = plugin.getLocalization().getString("permission_denied");
                    plugin.message(player, messageLocalization, null);
                    return true;
                }
            }
            String cleanServerPassword = plugin.getCleanPassword();
            if (cleanServerPassword == null && plugin.passwordSet() && !plugin.getConfig().getBoolean("cleanPassword", false)) {
                String messageLocalization = plugin.getLocalization().getString("only_hashed");
                plugin.message(sender, messageLocalization, null);
            } else if (cleanServerPassword == null && !plugin.passwordSet()) {
                String messageLocalization = plugin.getLocalization().getString("password_not_set");
                plugin.message(sender, messageLocalization, null);
            } else if (cleanServerPassword != null && plugin.passwordSet()) {
                String messageLocalization = plugin.getLocalization().getString("password");
                plugin.message(sender, messageLocalization, cleanServerPassword);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "You shouldn't see this message. Please report this issue, including a copy of the config!");
            }
            if (cleanServerPassword != null && !plugin.passwordSet() && plugin.getConfig().getBoolean("cleanPassword", false)) {
                String messageLocalization = plugin.getLocalization().getString("config_invalid");
                plugin.message(sender, messageLocalization, null);
            }
        } else if (command.getName().equalsIgnoreCase("setjaillocation")) {
            if (!(sender instanceof Player)) {
                String messageLocalization = plugin.getLocalization().getString("only_ingame");
                plugin.message(sender, messageLocalization, null);
            } else {
                Player player = (Player) sender;
                if (!player.hasPermission("passwordprotect.setjailarea")) {
                    String messageLocalization = plugin.getLocalization().getString("permission_denied");
                    plugin.message(player, messageLocalization, null);
                    return true;
                }

                World world = player.getWorld();
                int radius = 4;
                if (args.length >= 1) {
                    try {
                        radius = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        String messageLocalization = plugin.getLocalization().getString("radius_not_number");
                        plugin.message(player, messageLocalization, null);
                        radius = 4;
                    }
                }
                JailLocation loc = new JailLocation(player.getLocation(), radius);
                plugin.setJailLocation(world, loc);
                String messageLocalization = plugin.getLocalization().getString("jail_set");
                plugin.message(player, messageLocalization, null);
            }
        } else if (command.getName().equalsIgnoreCase("setpassword")) {
            if (args.length != 1) {
                return false;
            }
            String password = args[0];
            if (!sender.hasPermission("passwordprotect.setpassword")) {
                String messageLocalization = plugin.getLocalization().getString("permission_denied");
                plugin.message(sender, messageLocalization, null);
                return true;
            }
            plugin.setPassword(password);
            String messageLocalization = plugin.getLocalization().getString("password_set");
            plugin.message(sender, messageLocalization, null);
            if (sender instanceof Player) {
                messageLocalization = plugin.getLocalization().getString("set_jail_area");
                plugin.message(sender, messageLocalization, null);
            }
        } else if (command.getName().equalsIgnoreCase("login") && !(sender instanceof Player)) {
            String messageLocalization = plugin.getLocalization().getString("no_login_console");
            plugin.message(sender, messageLocalization, null);
        }
        return true;
    }
}
