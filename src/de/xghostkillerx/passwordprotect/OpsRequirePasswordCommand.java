package de.xghostkillerx.passwordprotect;


import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;

public class OpsRequirePasswordCommand implements CommandExecutor {

    private final PasswordProtect plugin;

    public OpsRequirePasswordCommand(PasswordProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Only server ops can use this command");
            return true;
        }

        boolean requireOpsPassword;
        String requiredMsg;
        if (args.length == 0) {
            requireOpsPassword = plugin.getRequireOpsPassword();
            requiredMsg = requireOpsPassword ? "required" : "not required";
        } else {
            String yesOrNo = args[0];
            if (yesOrNo.startsWith("n") || yesOrNo.startsWith("false")) {
                requireOpsPassword = false;
            } else {
                requireOpsPassword = true;
            }
            requiredMsg = requireOpsPassword ? "now required" : "no longer required";

            plugin.setRequireOpsPassword(requireOpsPassword);
        }

        sender.sendMessage(ChatColor.YELLOW + "Server operators are " + requiredMsg + " to provide the server password");
        return true;
    }
}