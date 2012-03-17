package de.xghostkillerx.passwordprotect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class PasswordProtectEntityListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectEntityListener(PasswordProtect instance) {
		plugin = instance;
	}

	// Cancel triggering of jailed player
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
	
	// Cancel hitting mobs
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager.getType() == EntityType.PLAYER) {
			Player player = (Player) damager;
			if (plugin.jailedPlayers.contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	// Cancel incoming damage
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(final EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() == EntityType.PLAYER) {
			Player player = (Player) entity;
			if (plugin.jailedPlayers.contains(player)) {
				event.setCancelled(true);
			}
		}
	}
}