package de.dustplanet.passwordprotect.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.jail.JailHelper;
import de.dustplanet.passwordprotect.jail.JailLocation;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PasswordProtect for CraftBukkit/Spgiot. Handles the setjaillocation command.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class PasswordProtectSetJailLocationCommand implements CommandExecutor {
    private static final int DEFAULT_RADIUS = 4;
    private final PasswordProtect plugin;
    private final PasswordProtectUtilities utils;
    private final JailHelper jailHelper;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public PasswordProtectSetJailLocationCommand(final PasswordProtect instance, final JailHelper jailHelper) {
        plugin = instance;
        utils = instance.getUtils();
        this.jailHelper = jailHelper;
    }

    @Override
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.DataflowAnomalyAnalysis", "PMD.AvoidLiteralsInIfCondition" })
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (!player.hasPermission("passwordprotect.setjailarea")) {
                final String messageLocalization = plugin.getLocalization().getString("permission_denied");
                utils.message(player, messageLocalization, null);
                return true;
            }

            final World world = player.getWorld();
            int radius = DEFAULT_RADIUS;
            if (args.length >= 1) {
                try {
                    radius = Integer.parseInt(args[0]);
                } catch (@SuppressWarnings("unused") final NumberFormatException e) {
                    final String messageLocalization = plugin.getLocalization().getString("radius_not_number");
                    utils.message(player, messageLocalization, null);
                    radius = DEFAULT_RADIUS;
                }
            }
            final JailLocation loc = new JailLocation(player.getLocation(), radius);
            jailHelper.setJailLocation(world, loc);
            final String messageLocalization = plugin.getLocalization().getString("jail_set");
            utils.message(player, messageLocalization, null);
        } else {
            final String messageLocalization = plugin.getLocalization().getString("only_ingame");
            utils.message(sender, messageLocalization, null);

        }
        return true;
    }
}
