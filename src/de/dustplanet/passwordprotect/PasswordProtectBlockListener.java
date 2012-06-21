package de.dustplanet.passwordprotect;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * PasswordProtect for CraftBukkit/Bukkit
 * Handles some block related events
 * 
 * 
 * Refer to the forum thread:
 * http://bit.ly/ppbukkit
 * Refer to the dev.bukkit.org page:
 * http://bit.ly/ppbukktidev
 *
 * @author xGhOsTkiLLeRx
 * @thanks to brianewing alias DisabledHamster for the original plugin!
 * 
 */

public class PasswordProtectBlockListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectBlockListener(PasswordProtect instance) {
		plugin = instance;
	}

	// If a block is placed, cancel it
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (plugin.config.getBoolean("prevent.BlockPlace")) {
			String playerName = event.getPlayer().getName();
			if (plugin.jailedPlayers.containsKey(playerName)) {
				event.setBuild(false);
				event.setCancelled(true);
			}
		}
	}

	// If a player is in jail, he can't break a block
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (plugin.config.getBoolean("prevent.BlockBreak")) {
			String playerName = event.getPlayer().getName();
			if (plugin.jailedPlayers.containsKey(playerName)) {
				event.setCancelled(true);
			}
		}
	}

}