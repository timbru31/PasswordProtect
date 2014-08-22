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
 * PasswordProtect for CraftBukkit/Bukkit.
 * Handles player activities.
 *
 * Refer to the dev.bukkit.org page:
 * http://dev.bukkit.org/bukkit-plugins/passwordprotect/
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

    // When the player joins, force a password and check permissions
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.check(player);
    }

    // When the player leaves, remove potion effects if jailed
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

    // Don't cancel movement, teleport back instead
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Movement")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            Player player = event.getPlayer();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                if (!player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    if (plugin.getConfig().getBoolean("darkness")) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
                    }
                }
                if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
                    if (plugin.getConfig().getBoolean("slowness")) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 86400, 15));
                    }
                }
                plugin.stayInJail(player);
            }
        }
    }

    // Don't let him interact
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Interaction")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    // Don't let him interact with mobs
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (plugin.getConfig().getBoolean("prevent.InteractionMobs")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    // Don't let him drop items
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getConfig().getBoolean("prevent.ItemDrop")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    // Don't let him pickup stuff
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (plugin.getConfig().getBoolean("prevent.ItemPickup")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    // Sorry, no nether ;)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Portal")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    // Sorry, no chat
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (plugin.getConfig().getBoolean("prevent.Chat")) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (plugin.getJailedPlayers().containsKey(playerUUID)) {
                event.setCancelled(true);
            }
        }
    }

    // Listening for commands
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws Exception {
        Player player = event.getPlayer();
        UUID playerUUID = event.getPlayer().getUniqueId();
        String message = event.getMessage();
        // Separate commands from the message
        String command = message.replaceFirst("/", "");
        if (command.contains(" ")) {
            command = command.substring(0, command.indexOf(' '));
        }
        // If jailed
        if (plugin.getJailedPlayers().containsKey(playerUUID)) {
            // Command on the list? Stop here
            if (plugin.getCommandList().contains(command)) {
                return;
            } else if (command.equalsIgnoreCase("login")) {
                // Don't count only /login -> 7 characters
                if (message.length() > 7) {
                    // Get the password
                    String password = message.substring(7, message.length());
                    password = plugin.encrypt(password);
                    // Is the password right?
                    if (password.equals(plugin.getPassword())) {
                        String messageLocalization = plugin.getLocalization().getString("password_accepted");
                        plugin.message(null, player, messageLocalization, null);
                        // Remove from jail & remove effects
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
                        // Teleport back to logout location? (really: teleport back to login location before jailing ;) )
                        if (plugin.getConfig().getBoolean("teleportBack")) {
                            if (plugin.getPlayerLocations().containsKey(playerUUID)) {
                                player.teleport(plugin.getPlayerLocations().get(playerUUID));
                                plugin.getPlayerLocations().remove(playerUUID);
                            }
                        }
                    } else {
                        // Increase wrong counters and kick/ban if necessary.
                        // Attempts from the HashMap
                        int attempts = plugin.getJailedPlayers().get(playerUUID);
                        // After how many attempts?
                        int kickAfter = plugin.getConfig().getInt("wrongAttempts.kick");
                        int banAfter = plugin.getConfig().getInt("wrongAttempts.ban");
                        // Attempts left until action
                        int attemptsLeftKick = kickAfter - attempts;
                        int attemptsLeftBan = banAfter - attempts;
                        // If attempts are 0
                        if (attemptsLeftKick <= 0 || attemptsLeftBan <= 0) {
                            // Ban
                            if (attemptsLeftBan <= 0) {
                                String ip = player.getAddress().getAddress().toString().replace("/", "");
                                String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("ban_message"));
                                player.kickPlayer(messageLocalization);
                                plugin.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), "AutoBan - PasswordProtect", null, "PasswordProtect");
                                // Broadcast message
                                if (plugin.getConfig().getBoolean("broadcast.ban")) {
                                    messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("ban_broadcast"));
                                    plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
                                }
                                // Ban IP
                                if (plugin.getConfig().getBoolean("wrongAttempts.banIP")) {
                                    plugin.getServer().banIP(ip);
                                }
                                // Remove from all lists!
                                plugin.getJailedPlayers().remove(playerUUID);
                                if (plugin.getPlayerLocations().containsKey(playerUUID)) {
                                    plugin.getPlayerLocations().remove(playerUUID);
                                }
                                return;
                            } else if (attemptsLeftKick == 0) {
                                // Kick
                                String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("kick_message"));
                                player.kickPlayer(messageLocalization);
                                if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                                }
                                if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                                    player.removePotionEffect(PotionEffectType.SLOW);
                                }
                                // Broadcast message
                                if (plugin.getConfig().getBoolean("broadcast.kick")) {
                                    messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("kick_broadcast"));
                                    plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
                                }
                            }
                        }
                        // Warn player
                        if (attemptsLeftKick > 0 || attemptsLeftBan > 0) {
                            // Leave kick out later
                            if (attemptsLeftKick > 0) {
                                String messageLocalization = plugin.getLocalization().getString("attempts_left_kick");
                                plugin.message(null, player, messageLocalization, Integer.toString(attemptsLeftKick));
                            }
                            String messageLocalization = plugin.getLocalization().getString("attempts_left_ban");
                            plugin.message(null, player, messageLocalization, Integer.toString(attemptsLeftBan));
                            // Increase HashMap value and attempts variable
                            attempts++;
                            plugin.getJailedPlayers().put(playerUUID, attempts);
                        }
                    }
                } else {
                    // You need a password
                    plugin.sendPasswordRequiredMessage(player);
                }
            } else {
                // You need a password
                plugin.sendPasswordRequiredMessage(player);
            }
            // Cancel event anyway, no commands
            event.setCancelled(true);
        } else if (message.toLowerCase().startsWith("/login")) {
            // Else already logged in
            String messageLocalization = plugin.getLocalization().getString("already_logged_in");
            plugin.message(null, player, messageLocalization, null);
            event.setCancelled(true);
        }
    }
}
