package de.xghostkillerx.passwordprotect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class PasswordProtectEntityListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectEntityListener(PasswordProtect instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityTarget(final EntityTargetEvent event) {
		Entity target = event.getTarget();
		if (target == null) return;
		if (target.getType().equals(EntityType.PLAYER)) {
			Player player = (Player) target;
			if (plugin.jailedPlayers.contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent  event) {
		Entity damager = event.getDamager();
		if (damager.getType() == EntityType.PLAYER) {
			Player player = (Player) damager;
			if (plugin.jailedPlayers.contains(player)) {
				event.setCancelled(true);
			}
		}
	}
}