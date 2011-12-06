package de.xghostkillerx.passwordprotect;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PasswordProtectBlockListener extends BlockListener {
    private PasswordProtect plugin;
    public PasswordProtectBlockListener(PasswordProtect plugin) {
        this.plugin = plugin;
    }

   // If a place is placed, cancel it
    public void onBlockPlace(BlockPlaceEvent event) {
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
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.jailedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

}