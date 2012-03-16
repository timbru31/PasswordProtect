package de.xghostkillerx.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PasswordProtectCommands {
	PasswordProtect plugin;
	public PasswordProtectCommands(PasswordProtect instance) {
		plugin = instance;
	}

	public boolean PasswordProtectCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
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

			String serverPassword = plugin.getPassword();
			// If no password is set, tell the sender the instructions
			if (serverPassword == null) {
				sender.sendMessage(ChatColor.YELLOW + "Server password is not set. Use /setpassword <password>");
				return true;
			}
			// Tell the sender the password
			else {
				sender.sendMessage(ChatColor.YELLOW + "Server password is " + ChatColor.DARK_RED + plugin.getPassword());
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
						radius = Integer.parseInt(args[0]);
					} catch (NumberFormatException nfe) {}
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

			plugin.setPassword(password);
			sender.sendMessage(ChatColor.GREEN + "Server password set!");
			sender.sendMessage(ChatColor.YELLOW + "You can set the jail area by going somewhere and using " + ChatColor.GREEN + "/setpasswordjail " + ChatColor.RED + "[radius]");
			return true;
		}
		return false;
	}
}
