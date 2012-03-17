package de.xghostkillerx.passwordprotect;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class PasswordProtectInventoryListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectInventoryListener(PasswordProtect instance) {
		plugin = instance;
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event){
		PasswordProtect.log.info("Test");
		//		Player player = (Player) event.getPlayer();
		//		if (plugin.jailedPlayers.contains(player)) {
		//event.setCancelled(true);
		//}
	}
}