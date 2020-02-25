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
 * PasswordProtect for CraftBukkit/Spgiot. Handles entity activities. Refer to the dev.bukkit.org page:
 * https://dev.bukkit.org/projects/passwordprotect
 *
 * @author xGhOsTkiLLeRx thanks to brianewing alias DisabledHamster for the original plugin!
 */

public class PasswordProtectEntityListener implements Listener {
    private PasswordProtect plugin;

    public PasswordProtectEntityListener(PasswordProtect instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Triggering", true)) {
            Entity target = event.getTarget();
            if (target == null) {
                return;
            }
            if (target.getType() == EntityType.PLAYER) {
                UUID playerUUID = ((Player) target).getUniqueId();
                if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Attacks", true)) {
            Entity damager = event.getDamager();
            if (damager.getType() == EntityType.PLAYER) {
                UUID playerUUID = ((Player) damager).getUniqueId();
                if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Damage", true)) {
            Entity entity = event.getEntity();
            if (entity.getType() == EntityType.PLAYER) {
                UUID playerUUID = ((Player) entity).getUniqueId();
                Player player = (Player) entity;
                if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                    player.setFoodLevel(20);
                    player.setFireTicks(0);
                    player.setRemainingAir(player.getMaximumAir());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (plugin.getConfig().getBoolean("prevent.DeathDrops", true)) {
            Entity entity = event.getEntity();
            if (entity.getType() == EntityType.PLAYER) {
                UUID playerUUID = ((Player) entity).getUniqueId();
                if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                    event.setDroppedExp(0);
                    event.getDrops().clear();
                }
            }
        }
    }
}
