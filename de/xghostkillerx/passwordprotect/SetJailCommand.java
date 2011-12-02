package de.xghostkillerx.passwordprotect;


import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetJailCommand implements CommandExecutor {

    private final PasswordProtect plugin;

    public SetJailCommand(PasswordProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command must be used in-game");
            return true;
        } else {
            Player player = (Player)sender;
            if (!player.hasPermission("passwordprotect.setjailarea")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                return true;
            }
        }
        
        Player player = (Player)sender;
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