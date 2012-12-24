package de.dustplanet.passwordprotect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * PasswordProtect for CraftBukkit/Bukkit
 * Handles entity activities.
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

public class PasswordProtectEntityListener implements Listener {
	private PasswordProtect plugin;
	public PasswordProtectEntityListener(PasswordProtect instance) {
		plugin = instance;
	}

	// Cancel triggering of jailed player
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityTarget(EntityTargetEvent event) {
		if (plugin.config.getBoolean("prevent.Triggering")) {
			Entity target = event.getTarget();
			if (target == null) return;
			if (target.getType().equals(EntityType.PLAYER)) {
				String playerName = ((Player) target).getName();
				if (plugin.jailedPlayers.containsKey(playerName)) {
					event.setCancelled(true);
				}
			}
		}
	}

	// Cancel hitting mobs
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (plugin.config.getBoolean("prevent.Attacks")) {
			Entity damager = event.getDamager();
			if (damager.getType() == EntityType.PLAYER) {
				String playerName = ((Player) damager).getName();
				if (plugin.jailedPlayers.containsKey(playerName)) {
					event.setCancelled(true);
				}
			}
		}
	}

	// Cancel incoming damage
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (plugin.config.getBoolean("prevent.Damage")) {
			Entity entity = event.getEntity();
			if (entity.getType() == EntityType.PLAYER) {
				String playerName = ((Player) entity).getName();
				Player player = (Player) entity;
				if (plugin.jailedPlayers.containsKey(playerName)) {
					// Restore food and air. Remove fire
					player.setFoodLevel(20);
					player.setFireTicks(0);
					player.setRemainingAir(player.getMaximumAir());
					event.setCancelled(true);
				}
			}
		}
	}
	
	// Cancel item drops on death
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (plugin.config.getBoolean("prevent.DeathDrops")) {
			Entity entity = event.getEntity();
			if (entity.getType() == EntityType.PLAYER) {
				String playerName = ((Player) entity).getName();
				if (plugin.jailedPlayers.containsKey(playerName)) {
					// Exp and items
					event.setDroppedExp(0);
					event.getDrops().clear();
				}
			}
		}
	}
}