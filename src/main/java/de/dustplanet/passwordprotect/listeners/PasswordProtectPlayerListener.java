package de.dustplanet.passwordprotect.listeners;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.dustplanet.passwordprotect.PasswordProtect;
import de.dustplanet.passwordprotect.jail.JailHelper;
import de.dustplanet.passwordprotect.utils.PasswordProtectUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Protection listener. Cancels various damage events and re-jails the player.
 *
 * @author timbru31
 */
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:ClassFanOutComplexity", "PMD.TooManyMethods",
        "PMD.DataflowAnomalyAnalysis" })
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class PasswordProtectPlayerListener implements Listener {
    private static final int POTION_AMPLIFIER = 15;
    private static final int TICKS_PER_SECOND = 20;
    private final PasswordProtect plugin;
    private final PasswordProtectUtilities utils;
    private final JailHelper jailHelper;

    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidDuplicateLiterals" })
    public PasswordProtectPlayerListener(final PasswordProtect instance, final JailHelper jailHelper) {
        plugin = instance;
        utils = instance.getUtils();
        this.jailHelper = jailHelper;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        jailHelper.check(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        final Player player = event.getPlayer();
        if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
            if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                player.removePotionEffect(PotionEffectType.SLOW);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("prevent.movement", true)) {
            return;
        }

        final UUID playerUUID = event.getPlayer().getUniqueId();
        final Player player = event.getPlayer();
        if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
            if (!player.hasPotionEffect(PotionEffectType.BLINDNESS) && plugin.getConfig().getBoolean("darkness", true)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (TICKS_PER_SECOND * TimeUnit.DAYS.toSeconds(1L)),
                        POTION_AMPLIFIER));
            }
            if (!player.hasPotionEffect(PotionEffectType.SLOW) && plugin.getConfig().getBoolean("slowness", true)) {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.SLOW, (int) (TICKS_PER_SECOND * TimeUnit.DAYS.toSeconds(1L)), POTION_AMPLIFIER));
            }
            jailHelper.stayInJail(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerInteract(final PlayerInteractEvent event) {
        checkBasicEvent(event, "prevent.interaction");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        checkBasicEvent(event, "prevent.interactionMobs");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        checkBasicEvent(event, "prevent.itemDrop");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerPickupItem(final EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (plugin.getConfig().getBoolean("prevent.itemPickup", true)) {
            final UUID playerUUID = event.getEntity().getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerPortal(final PlayerPortalEvent event) {
        checkBasicEvent(event, "prevent.portal");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        checkBasicEvent(event, "prevent.chat");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "checkstyle:ReturnCount", "checkstyle:ExecutableStatementCount",
            "checkstyle:NestedIfDepth", "checkstyle:NPathComplexity", "checkstyle:CyclomaticComplexity", "PMD.CyclomaticComplexity",
            "PMD.SignatureDeclareThrowsException", "PMD.NPathComplexity" })
    @SuppressFBWarnings("UC_USELESS_CONDITION")
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) throws Exception {
        final Player player = event.getPlayer();
        final UUID playerUUID = event.getPlayer().getUniqueId();
        final String message = event.getMessage();
        String command = message.replaceFirst("/", "");
        if (command.contains(" ")) {
            command = command.substring(0, command.indexOf(' '));
        }
        if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
            if (utils.getCommandList().contains(command)) {
                return;
            }

            final int loginTextWithSlashLength = 7;
            if (!"login".equalsIgnoreCase(command) || message.length() < loginTextWithSlashLength) {
                utils.sendPasswordRequiredMessage(player);
                event.setCancelled(true);
                return;
            }

            String password = message.substring(loginTextWithSlashLength, message.length());
            password = utils.hash(password);
            if (password.equals(utils.getPassword())) {
                loginPlayer(player, playerUUID);
                event.setCancelled(true);
                return;
            }

            int attempts = jailHelper.getJailedPlayers().get(playerUUID);
            final int kickAfter = plugin.getConfig().getInt("wrongAttempts.kick");
            final int banAfter = plugin.getConfig().getInt("wrongAttempts.ban");
            final int attemptsLeftKick = kickAfter - attempts;
            final int attemptsLeftBan = banAfter - attempts;
            if (attemptsLeftKick <= 0 || attemptsLeftBan <= 0) {
                if (attemptsLeftBan <= 0) {
                    banPlayer(player, playerUUID);
                    return;
                } else if (attemptsLeftKick == 0) {
                    kickPlayer(player);
                }
            }
            if (attemptsLeftKick > 0 || attemptsLeftBan > 0) {
                if (attemptsLeftKick > 0) {
                    final String messageLocalization = plugin.getLocalization().getString("attempts_left_kick");
                    utils.message(player, messageLocalization, Integer.toString(attemptsLeftKick));
                }
                final String messageLocalization = plugin.getLocalization().getString("attempts_left_ban");
                utils.message(player, messageLocalization, Integer.toString(attemptsLeftBan));
                attempts++;
                jailHelper.getJailedPlayers().put(playerUUID, attempts);
            }
            event.setCancelled(true);
        } else if (message.toLowerCase(Locale.ENGLISH).startsWith("/login")) {
            final String messageLocalization = plugin.getLocalization().getString("already_logged_in");
            utils.message(player, messageLocalization, null);
            event.setCancelled(true);
        }
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void banPlayer(final Player player, final UUID playerUUID) {
        final String ipAddress = player.getAddress().getAddress().toString().replace("/", "");
        String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("ban_message"));
        player.kickPlayer(messageLocalization);
        plugin.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), "AutoBan - PasswordProtect", null, "PasswordProtect");
        if (plugin.getConfig().getBoolean("broadcast.ban", true)) {
            messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("ban_broadcast"));
            plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
        }
        if (plugin.getConfig().getBoolean("wrongAttempts.banIP", true)) {
            plugin.getServer().banIP(ipAddress);
        }
        jailHelper.getJailedPlayers().remove(playerUUID);
        if (jailHelper.getPlayerLocations().containsKey(playerUUID)) {
            jailHelper.getPlayerLocations().remove(playerUUID);
        }
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void kickPlayer(final Player player) {
        String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("kick_message"));
        player.kickPlayer(messageLocalization);
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            player.removePotionEffect(PotionEffectType.SLOW);
        }
        if (plugin.getConfig().getBoolean("broadcast.kick", true)) {
            messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("kick_broadcast"));
            plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
        }
    }

    private void loginPlayer(final Player player, final UUID playerUUID) {
        final String messageLocalization = plugin.getLocalization().getString("password_accepted");
        utils.message(player, messageLocalization, null);
        jailHelper.getJailedPlayers().remove(playerUUID);
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            player.removePotionEffect(PotionEffectType.SLOW);
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setAllowFlight(true);
        }
        if (plugin.getConfig().getBoolean("teleportBack", true) && jailHelper.getPlayerLocations().containsKey(playerUUID)) {
            final Location location = jailHelper.getPlayerLocations().remove(playerUUID);
            player.teleport(location);
        }
    }

    private void checkBasicEvent(final PlayerEvent event, final String configKey) {
        if (plugin.getConfig().getBoolean(configKey, true)) {
            final UUID playerUUID = event.getPlayer().getUniqueId();
            if (jailHelper.getJailedPlayers().containsKey(playerUUID)) {
                ((Cancellable) event).setCancelled(true);
            }
        }
    }

}
