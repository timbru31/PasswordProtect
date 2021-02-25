package de.dustplanet.passwordprotect.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.jail.JailHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PasswordProtect for CraftBukkit/Spigot. Handles some block related events.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class PasswordProtectBlockListener implements Listener {
    private final PasswordProtect plugin;
    private final JailHelper jailHelper;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public PasswordProtectBlockListener(final PasswordProtect instance, final JailHelper jailHelper) {
        plugin = instance;
        this.jailHelper = jailHelper;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (plugin.getConfig().getBoolean("prevent.blockPlace", true)) {
            final UUID playerUUID = event.getPlayer().getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                event.setBuild(false);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onBlockBreak(final BlockBreakEvent event) {
        if (plugin.getConfig().getBoolean("prevent.blockBreak", true)) {
            final UUID playerUUID = event.getPlayer().getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }
}
