package de.dustplanet.passwordprotect.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PasswordProtect for CraftBukkit/Spgiot. Handles the setpassword command.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class PasswordProtectSetPasswordCommand implements CommandExecutor {
    private final PasswordProtect plugin;
    private final PasswordProtectUtilities utils;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public PasswordProtectSetPasswordCommand(final PasswordProtect instance) {
        plugin = instance;
        utils = instance.getUtils();
    }

    @Override
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidLiteralsInIfCondition" })
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (args.length != 1) {
            return false;
        }
        if (!sender.hasPermission("passwordprotect.setpassword")) {
            final String messageLocalization = plugin.getLocalization().getString("permission_denied");
            utils.message(sender, messageLocalization, null);
            return true;
        }
        final String password = args[0];
        utils.setPassword(password);
        String messageLocalization = plugin.getLocalization().getString("password_set");
        utils.message(sender, messageLocalization, null);
        if (sender instanceof Player) {
            messageLocalization = plugin.getLocalization().getString("set_jail_area");
            utils.message(sender, messageLocalization, null);
        }

        return true;
    }
}
