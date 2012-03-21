package de.xghostkillerx.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PasswordProtectCommands implements CommandExecutor {
	PasswordProtect plugin;
	public PasswordProtectCommands(PasswordProtect instance) {
		plugin = instance;
	}

	public boolean onCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
		// Send the password back, if the sender is the console or got the permission
		if (command.getName().equalsIgnoreCase("password")) {
			// If a player send the command
			if (sender instanceof Player) {
				Player player = (Player) sender;
				// If the player hasn't got the permission, don't display the password
				if (!player.hasPermission("passwordprotect.getpassword")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
					return true;
				}
			}
			// Could be null...
			String cleanServerPassword = plugin.getPasswordClean();
			// If password is only stored encrypted
			if (cleanServerPassword == null && plugin.passwordSet() && !plugin.config.getBoolean("cleanPassword")) {
				sender.sendMessage(ChatColor.YELLOW + "Server password is only stored encrypted...");
				return true;
			}
			// If no password is set, tell the sender the instructions
			else if (cleanServerPassword == null && !plugin.passwordSet()) {
				sender.sendMessage(ChatColor.YELLOW + "Server password is not set. Use /setpassword <password>");
				return true;
			}
			// Tell the sender the password
			else if (cleanServerPassword != null && plugin.passwordSet()){
				sender.sendMessage(ChatColor.YELLOW + "Server password is " + ChatColor.DARK_RED + cleanServerPassword);
				return true;
			}
			// If password is not set, but a clean one is set and cleanPassword is enabled
			if (cleanServerPassword != null && !plugin.passwordSet() && plugin.config.getBoolean("cleanPassword")) {
				sender.sendMessage(ChatColor.YELLOW + "It seems like this is server config invalid. Please re-set the password!");
				return true;
			}
			// Else debug
			else {
				sender.sendMessage(ChatColor.DARK_RED + "You shouldn't see this message. Please report this issue, including a copy of the config!");
				return true;
			}
		}
		// Sets the jail radius
		if (command.getName().equalsIgnoreCase(/* TODO Change this ugly command */"setpasswordjail")) {
			// If the console send this -> Not possible
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command must be used in-game");
				return true;
			} else if (sender instanceof Player){
				Player player = (Player)sender;
				// if the player hasn't got the permission, cancel it
				if (!player.hasPermission("passwordprotect.setjailarea")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
					return true;
				}

				// Gets the players location
				World world = player.getWorld();
				int radius = 0;
				if (args.length >= 1) {
					try {
						radius = Integer.valueOf(args[0]);
					}
					catch (Exception nfe) {
						player.sendMessage(ChatColor.RED + "The radius wasn't a number!");
					}
				}
				JailLocation loc = new JailLocation(player.getLocation(), radius);
				plugin.setJailLocation(world, loc);
				player.sendMessage(ChatColor.GREEN + "Jail location set");
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
			if (sender instanceof Player) {
				Player player = (Player)sender;
				if (!player.hasPermission("passwordprotect.setpassword")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
					return true;
				}
			}
			try {
				plugin.setPassword(password);
			} 
			catch (Exception e) {}
			sender.sendMessage(ChatColor.GREEN + "Server password set!");
			sender.sendMessage(ChatColor.YELLOW + "You can set the jail area by going somewhere and using " + ChatColor.GREEN + "/setpasswordjail " + ChatColor.RED + "[radius]");
			return true;
		}
		return false;
	}
}
