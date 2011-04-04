package com.disabledhamster.bukkit.PasswordProtect.commands;

import com.disabledhamster.bukkit.PasswordProtect.PasswordProtect;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetPasswordCommand implements CommandExecutor {

    private final PasswordProtect plugin;

    public SetPasswordCommand(PasswordProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1)
            return false;

        String password = args[0];

        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (!plugin.permissions.canSetPassword(player)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                return true;
            }
        }

        plugin.setPassword(password);
        sender.sendMessage(ChatColor.GREEN + "Server password set!");
        sender.sendMessage(ChatColor.YELLOW + "You can set the jail area by going somewhere and using " + ChatColor.GREEN + "/setpasswordjail " + ChatColor.RED + "[radius]");
        return true;
    }
}