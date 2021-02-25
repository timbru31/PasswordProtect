package de.dustplanet.passwordprotect.listeners;

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

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.jail.JailHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Handles entity activities such as dying and damage.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class PasswordProtectEntityListener implements Listener {
    private static final int MAX_FOOD_LEVEL = 20;
    private final PasswordProtect plugin;
    private final JailHelper jailHelper;

    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidDuplicateLiterals" })
    public PasswordProtectEntityListener(final PasswordProtect instance, final JailHelper jailHelper) {
        plugin = instance;
        this.jailHelper = jailHelper;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onEntityTarget(final EntityTargetEvent event) {
        if (!plugin.getConfig().getBoolean("prevent.triggering", true)) {
            return;
        }

        final Entity target = event.getTarget();
        if (target == null) {
            return;
        }
        if (target.getType() == EntityType.PLAYER) {
            final UUID playerUUID = ((Player) target).getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("prevent.attacks", true)) {
            return;
        }

        final Entity damager = event.getDamager();
        if (damager.getType() == EntityType.PLAYER) {
            final UUID playerUUID = ((Player) damager).getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!plugin.getConfig().getBoolean("prevent.damage", true)) {
            return;
        }

        final Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER) {
            final UUID playerUUID = ((Player) entity).getUniqueId();
            final Player player = (Player) entity;
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                player.setFoodLevel(MAX_FOOD_LEVEL);
                player.setFireTicks(0);
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onEntityDeath(final EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("prevent.deathDrops", true)) {
            return;
        }

        final Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER) {
            final UUID playerUUID = ((Player) entity).getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                event.setDroppedExp(0);
                event.getDrops().clear();
            }
        }

    }
}
