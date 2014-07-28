package de.dustplanet.passwordprotect;

import java.util.UUID;

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
 * Refer to the dev.bukkit.org page:
 * http://dev.bukkit.org/bukkit-plugins/passwordprotect/
 *
 * @author xGhOsTkiLLeRx
 * thanks to brianewing alias DisabledHamster for the original plugin!
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
            if (target == null) {
                return;
            }
            if (target.getType().equals(EntityType.PLAYER)) {
                UUID playerUUID = ((Player) target).getUniqueId();
                if (plugin.jailedPlayers.containsKey(playerUUID)) {
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
                UUID playerUUID = ((Player) damager).getUniqueId();
                if (plugin.jailedPlayers.containsKey(playerUUID)) {
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
                UUID playerUUID = ((Player) entity).getUniqueId();
                Player player = (Player) entity;
                if (plugin.jailedPlayers.containsKey(playerUUID)) {
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
                UUID playerUUID = ((Player) entity).getUniqueId();
                if (plugin.jailedPlayers.containsKey(playerUUID)) {
                    // Exp and items
                    event.setDroppedExp(0);
                    event.getDrops().clear();
                }
            }
        }
    }
}