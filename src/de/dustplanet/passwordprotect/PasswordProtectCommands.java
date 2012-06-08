package de.dustplanet.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * PasswordProtect for CraftBukkit/Bukkit
 * Handles commands
 * 
 * 
 * Refer to the forum thread:
 * http://bit.ly/ppbukkit
 * Refer to the dev.bukkit.org page:
 * http://bit.ly/ppbukktidev
 *
 * @author xGhOsTkiLLeRx
 * @thanks to brianewing alias DisabledHamster for the original plugin!
 * 
 */

public class PasswordProtectCommands implements CommandExecutor {
	PasswordProtect plugin;
	public PasswordProtectCommands(PasswordProtect instance) {
		plugin = instance;
	}

	public boolean onCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
		// Send the password back, if the sender is the console or if the player has got the permission
		if (command.getName().equalsIgnoreCase("password")) {
			// If a player send the command
			if (sender instanceof Player) {
				Player player = (Player) sender;
				// If the player hasn't got the permission, don't display the password
				if (!player.hasPermission("passwordprotect.getpassword")) {
					String messageLocalization = plugin.localization.getString("permission_denied");
					plugin.message(null, player, messageLocalization, null);
					return true;
				}
			}
			// Could be null...
			String cleanServerPassword = plugin.getPasswordClean();
			// If password is only stored encrypted
			if (cleanServerPassword == null && plugin.passwordSet() && !plugin.config.getBoolean("cleanPassword")) {
				String messageLocalization = plugin.localization.getString("only_encypted");
				plugin.message(sender, null, messageLocalization, null);
				return true;
			}
			// If no password is set, tell the sender the instructions
			else if (cleanServerPassword == null && !plugin.passwordSet()) {
				String messageLocalization = plugin.localization.getString("password_not_set");
				plugin.message(sender, null, messageLocalization, null);
				return true;
			}
			// Tell the sender the password
			else if (cleanServerPassword != null && plugin.passwordSet()){
				String messageLocalization = plugin.localization.getString("password");
				plugin.message(sender, null, messageLocalization, cleanServerPassword);
				return true;
			}
			// If password is not set, but a clean one is set and cleanPassword is enabled
			if (cleanServerPassword != null && !plugin.passwordSet() && plugin.config.getBoolean("cleanPassword")) {
				String messageLocalization = plugin.localization.getString("config_invalid");
				plugin.message(sender, null, messageLocalization, null);
				return true;
			}
			// Else debug
			else {
				sender.sendMessage(ChatColor.DARK_RED + "You shouldn't see this message. Please report this issue, including a copy of the config!");
				return true;
			}
		}
		// Sets the jail location
		if (command.getName().equalsIgnoreCase("setjaillocation")) {
			// If the console send this -> Not possible
			if (!(sender instanceof Player)) {
				String messageLocalization = plugin.localization.getString("only_ingame");
				plugin.message(sender, null, messageLocalization, null);
				return true;
			} else if (sender instanceof Player){
				Player player = (Player)sender;
				// if the player hasn't got the permission, cancel it
				if (!player.hasPermission("passwordprotect.setjailarea")) {
					String messageLocalization = plugin.localization.getString("permission_denied");
					plugin.message(null, player, messageLocalization, null);
					return true;
				}

				// Gets the players location
				World world = player.getWorld();
				// Default radius of four
				int radius = 4;
				if (args.length >= 1) {
					try {
						radius = Integer.valueOf(args[0]);
					}
					// Radius not a number? Use default!
					catch (Exception e) {
						String messageLocalization = plugin.localization.getString("radius_not_number");
						plugin.message(null, player, messageLocalization, null);
					}
				}
				// Make a new jail location and store it
				JailLocation loc = new JailLocation(player.getLocation(), radius);
				plugin.setJailLocation(world, loc);
				String messageLocalization = plugin.localization.getString("jail_set");
				plugin.message(null, player, messageLocalization, null);
				return true;
			}
		}
		// If someone tries to set a password
		if (command.getName().equalsIgnoreCase("setpassword")) {
			if (args.length != 1)
				return false;
			// The password is the first argument
			String password = args[0];
			// If the player hasn't got the permission, cancel it
			if (!sender.hasPermission("passwordprotect.setpassword")) {
				String messageLocalization = plugin.localization.getString("permission_denied");
				plugin.message(sender, null, messageLocalization, null);
				return true;
			}
			// Set password
			try {
				plugin.setPassword(password);
			} 
			catch (Exception e) {}
			String messageLocalization = plugin.localization.getString("password_set");
			plugin.message(sender, null, messageLocalization, null);
			// Reminder for the jail area
			if (sender instanceof Player) {
				messageLocalization = plugin.localization.getString("set_jail_area");
				plugin.message(sender, null, messageLocalization, null);
			}
			return true;
		}
		// Console tries to login -> Deny
		if (command.getName().equalsIgnoreCase("login")) {
			if (!(sender instanceof Player)) {
				String messageLocalization = plugin.localization.getString("no_login_console");
				plugin.message(sender, null, messageLocalization, null);
				return true;
			}
		}
		return false;
	}
}
