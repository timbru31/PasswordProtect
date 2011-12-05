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

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (plugin.jailedPlayers.contains(player)) {
            event.setBuild(false);
            event.setCancelled(true);
        }
    }

    @Override
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