package de.dustplanet.passwordprotect.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PasswordProtect for CraftBukkit/Spgiot. Handles the login command.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class PasswordProtectLoginCommand implements CommandExecutor {
    private final PasswordProtect plugin;
    private final PasswordProtectUtilities utils;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public PasswordProtectLoginCommand(final PasswordProtect instance) {
        plugin = instance;
        utils = instance.getUtils();
    }

    @Override
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (!(sender instanceof Player)) {
            final String messageLocalization = plugin.getLocalization().getString("no_login_console");
            utils.message(sender, messageLocalization, null);
        }
        return true;
    }
}
