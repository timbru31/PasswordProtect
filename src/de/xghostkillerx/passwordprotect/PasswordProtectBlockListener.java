package de.xghostkillerx.passwordprotect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PasswordProtectBlockListener implements Listener {
    private PasswordProtect plugin;
    public PasswordProtectBlockListener(PasswordProtect plugin) {
        this.plugin = plugin;
    }

   // If a place is placed, cancel it
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        // If the player is in "jail", cancel it
        Player player = event.getPlayer();
        if (plugin.jailedPlayers.contains(player)) {
            event.setBuild(false);
            event.setCancelled(true);
        }
    }

    // If a player is in jail, he can't break a block
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.jailedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

}