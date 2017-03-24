package de.dustplanet.passwordprotect;

import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * PasswordProtect for CraftBukkit/Spgiot.
 * Handles player activities.
 *
 * Refer to the dev.bukkit.org page:
 * https://dev.bukkit.org/projects/passwordprotect
 *
 * @author xGhOsTkiLLeRx
 * thanks to brianewing alias DisabledHamster for the original plugin!
 *
 */

public class PasswordProtectPlayerListener implements Listener {
    private PasswordProtect plugin;

    public PasswordProtectPlayerListener(PasswordProtect instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.check(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();
        if (plugin.getJailedPlayers().containsKey(playerUUID)) {
            if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                player.removePotionEffect(PotionEffectType.SLOW);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Movement", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            Player player = event.getPlayer();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                if (!player.hasPotionEffect(PotionEffectType.BLINDNESS) && plugin.getConfig().getBoolean("darkness", true)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
                }
                if (!player.hasPotionEffect(PotionEffectType.SLOW) && plugin.getConfig().getBoolean("slowness", true)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 86400, 15));
                }
                plugin.stayInJail(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Interaction", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (plugin.getConfig().getBoolean("prevent.InteractionMobs", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getConfig().getBoolean("prevent.ItemDrop", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (plugin.getConfig().getBoolean("prevent.ItemPickup", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Portal", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Chat", true)) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws Exception {
        Player player = event.getPlayer();
        UUID playerUUID = event.getPlayer().getUniqueId();
        String message = event.getMessage();
        String command = message.replaceFirst("/", "");
        if (command.contains(" ")) {
            command = command.substring(0, command.indexOf(' '));
        }
        if (plugin.getJailedPlayers().containsKey(playerUUID)) {
            if (plugin.getCommandList().contains(command)) {
                return;
            } else if (command.equalsIgnoreCase("login")) {
                if (message.length() > 7) {
                    String password = message.substring(7, message.length());
                    password = plugin.hash(password);
                    if (password.equals(plugin.getPassword())) {
                        String messageLocalization = plugin.getLocalization().getString("password_accepted");
                        plugin.message(player, messageLocalization, null);
                        plugin.getJailedPlayers().remove(playerUUID);
                        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                        }
                        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                            player.removePotionEffect(PotionEffectType.SLOW);
                        }
                        if (player.getGameMode().equals(GameMode.CREATIVE)) {
                            player.setAllowFlight(true);
                        }
                        if (plugin.getConfig().getBoolean("teleportBack", true) && plugin.getPlayerLocations().containsKey(playerUUID)) {
                            player.teleport(plugin.getPlayerLocations().get(playerUUID));
                            plugin.getPlayerLocations().remove(playerUUID);
                        }
                    } else {
                        int attempts = plugin.getJailedPlayers().get(playerUUID);
                        int kickAfter = plugin.getConfig().getInt("wrongAttempts.kick");
                        int banAfter = plugin.getConfig().getInt("wrongAttempts.ban");
                        int attemptsLeftKick = kickAfter - attempts;
                        int attemptsLeftBan = banAfter - attempts;
                        if (attemptsLeftKick <= 0 || attemptsLeftBan <= 0) {
                            if (attemptsLeftBan <= 0) {
                                String ip = player.getAddress().getAddress().toString().replace("/", "");
                                String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("ban_message"));
                                player.kickPlayer(messageLocalization);
                                plugin.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), "AutoBan - PasswordProtect", null, "PasswordProtect");
                                if (plugin.getConfig().getBoolean("broadcast.ban", true)) {
                                    messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("ban_broadcast"));
                                    plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
                                }
                                if (plugin.getConfig().getBoolean("wrongAttempts.banIP", true)) {
                                    plugin.getServer().banIP(ip);
                                }
                                plugin.getJailedPlayers().remove(playerUUID);
                                if (plugin.getPlayerLocations().containsKey(playerUUID)) {
                                    plugin.getPlayerLocations().remove(playerUUID);
                                }
                                return;
                            } else if (attemptsLeftKick == 0) {
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
                        }
                        if (attemptsLeftKick > 0 || attemptsLeftBan > 0) {
                            if (attemptsLeftKick > 0) {
                                String messageLocalization = plugin.getLocalization().getString("attempts_left_kick");
                                plugin.message(player, messageLocalization, Integer.toString(attemptsLeftKick));
                            }
                            String messageLocalization = plugin.getLocalization().getString("attempts_left_ban");
                            plugin.message(player, messageLocalization, Integer.toString(attemptsLeftBan));
                            attempts++;
                            plugin.getJailedPlayers().put(playerUUID, attempts);
                        }
                    }
                } else {
                    plugin.sendPasswordRequiredMessage(player);
                }
            } else {
                plugin.sendPasswordRequiredMessage(player);
            }
            event.setCancelled(true);
        } else if (message.toLowerCase().startsWith("/login")) {
            String messageLocalization = plugin.getLocalization().getString("already_logged_in");
            plugin.message(player, messageLocalization, null);
            event.setCancelled(true);
        }
    }
}
