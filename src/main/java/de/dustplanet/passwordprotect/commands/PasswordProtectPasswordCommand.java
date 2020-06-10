package de.dustplanet.passwordprotect.commands;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PasswordProtect for CraftBukkit/Spigot. Handles the password command.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class PasswordProtectPasswordCommand implements CommandExecutor {
    private final PasswordProtect plugin;
    private final PasswordProtectUtilities utils;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public PasswordProtectPasswordCommand(final PasswordProtect instance) {
        plugin = instance;
        utils = instance.getUtils();
    }

    @Override
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (!player.hasPermission("passwordprotect.getpassword")) {
                final String messageLocalization = plugin.getLocalization().getString("permission_denied");
                utils.message(player, messageLocalization, null);
                return true;
            }
        }
        final String cleanServerPassword = getPasswordIfPossible(sender);
        if (cleanServerPassword != null && !utils.isPasswordSet() && plugin.getConfig().getBoolean("cleanPassword", false)) {
            final String messageLocalization = plugin.getLocalization().getString("config_invalid");
            utils.message(sender, messageLocalization, null);
        }

        return true;
    }

    @Nullable
    private String getPasswordIfPossible(final CommandSender sender) {
        final String cleanServerPassword = utils.getCleanPassword();
        if (cleanServerPassword == null && utils.isPasswordSet() && !plugin.getConfig().getBoolean("cleanPassword", false)) {
            final String messageLocalization = plugin.getLocalization().getString("only_hashed");
            utils.message(sender, messageLocalization, null);
        } else if (cleanServerPassword == null && !utils.isPasswordSet()) {
            final String messageLocalization = plugin.getLocalization().getString("password_not_set");
            utils.message(sender, messageLocalization, null);
        } else if (cleanServerPassword != null && utils.isPasswordSet()) {
            final String messageLocalization = plugin.getLocalization().getString("password");
            utils.message(sender, messageLocalization, cleanServerPassword);
        } else {
            sender.sendMessage(
                    ChatColor.DARK_RED + "You shouldn't see this message. Please report this issue, including a copy of the config!");
        }
        return cleanServerPassword;
    }
}
