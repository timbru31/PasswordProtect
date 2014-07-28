package de.dustplanet.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * PasswordProtect for CraftBukkit/Bukkit
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
	String playerName = event.getPlayer().getName();
	Player player = event.getPlayer();
	if (plugin.jailedPlayers.containsKey(playerName)) {
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
	if (plugin.config.getBoolean("prevent.Movement")) {
	    Player player = event.getPlayer();
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		if (!player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
		    if (plugin.config.getBoolean("darkness")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
		    }
		}
		if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
		    if (plugin.config.getBoolean("slowness")) {
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
	if (plugin.config.getBoolean("prevent.Interaction")) {
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		event.setCancelled(true);
	    }
	}
    }

    // Don't let him interact with mobs
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
	if (plugin.config.getBoolean("prevent.InteractionMobs")) {
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		event.setCancelled(true);
	    }
	}
    }

    // Don't let him drop items
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
	if (plugin.config.getBoolean("prevent.ItemDrop")) {
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		event.setCancelled(true);
	    }
	}
    }

    // Don't let him pickup stuff
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
	if (plugin.config.getBoolean("prevent.ItemPickup")) {
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		event.setCancelled(true);
	    }
	}
    }

    // Sorry, no nether ;)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
	if (plugin.config.getBoolean("prevent.Portal")) {
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		event.setCancelled(true);
	    }
	}
    }

    // Sorry, no chat
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
	if (plugin.config.getBoolean("prevent.Chat")) {
	    String playerName = event.getPlayer().getName();
	    if (plugin.jailedPlayers.containsKey(playerName)) {
		event.setCancelled(true);
	    }
	}
    }

    // Listening for commands
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws Exception {
	Player player = event.getPlayer();
	String playerName = event.getPlayer().getName();
	String message = event.getMessage();
	// Separate commands from the message
	String command = message.replaceFirst("/", "");
	if (command.contains(" ")) {
	    command = command.substring(0, command.indexOf(' '));
	}
	// If jailed
	if (plugin.jailedPlayers.containsKey(playerName)) {
	    // Command on the list? Stop here
	    if (plugin.commandList.contains(command)) {
		return;
	    } else if (command.equalsIgnoreCase("login")) {
		// Don't count only /login -> 7 characters
		if (message.length() > 7) {
		    // Get the password
		    String password = message.substring(7, message.length());
		    password = plugin.encrypt(password);
		    // Is the password right?
		    if (password.equals(plugin.getPassword())) {
			String messageLocalization = plugin.localization.getString("password_accepted");
			plugin.message(null, player, messageLocalization, null);
			// Remove from jail & remove effects
			plugin.jailedPlayers.remove(playerName);
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
			if (plugin.config.getBoolean("teleportBack")) {
			    if (plugin.playerLocations.containsKey(playerName)) {
				player.teleport(plugin.playerLocations.get(playerName));
				plugin.playerLocations.remove(playerName);
			    }
			}
		    }
		    // Increase wrong counters and kick/ban if necessary.
		    else {
			// Attempts from the HashMap
			int attempts = plugin.jailedPlayers.get(playerName);
			// After how many attempts?
			int kickAfter = plugin.config.getInt("wrongAttempts.kick");
			int banAfter = plugin.config.getInt("wrongAttempts.ban");
			// Attempts left until action
			int attemptsLeftKick = kickAfter - attempts;
			int attemptsLeftBan = banAfter - attempts;
			// If attempts are 0
			if (attemptsLeftKick <= 0 || attemptsLeftBan <= 0) {
			    // Ban
			    if (attemptsLeftBan <= 0) {
				String ip = player.getAddress().getAddress().toString().replace("/", "");
				String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.localization.getString("ban_message"));
				player.kickPlayer(messageLocalization);
				player.setBanned(true);
				// Broadcast message
				if (plugin.config.getBoolean("broadcast.ban")) {
				    messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.localization.getString("ban_broadcast"));
				    plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
				}
				// Ban IP
				if (plugin.config.getBoolean("wrongAttempts.banIP")) {
				    plugin.getServer().banIP(ip);
				}
				// Remove from all lists!
				plugin.jailedPlayers.remove(playerName);
				if (plugin.playerLocations.containsKey(playerName)) {
				    plugin.playerLocations.remove(playerName);
				}
				return;
			    }
			    // Kick
			    else if(attemptsLeftKick == 0) {
				String messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.localization.getString("kick_message"));
				player.kickPlayer(messageLocalization);
				if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
				    player.removePotionEffect(PotionEffectType.BLINDNESS);
				}
				if (player.hasPotionEffect(PotionEffectType.SLOW)) {
				    player.removePotionEffect(PotionEffectType.SLOW);
				}
				// Broadcast message
				if (plugin.config.getBoolean("broadcast.kick")) {
				    messageLocalization = ChatColor.translateAlternateColorCodes('&', plugin.localization.getString("kick_broadcast"));
				    plugin.getServer().broadcastMessage(messageLocalization.replace("%player", player.getName()));
				}
			    }
			}
			// Warn player
			if (attemptsLeftKick > 0 || attemptsLeftBan > 0) {
			    // Leave kick out later
			    if (attemptsLeftKick > 0) {
				String messageLocalization = plugin.localization.getString("attempts_left_kick");
				plugin.message(null, player, messageLocalization, Integer.toString(attemptsLeftKick));
			    }
			    String messageLocalization = plugin.localization.getString("attempts_left_ban");
			    plugin.message(null, player, messageLocalization, Integer.toString(attemptsLeftBan));
			    // Increase HashMap value and attempts variable
			    attempts++;
			    plugin.jailedPlayers.put(playerName, attempts);
			}
		    }
		}
		// You need a password
		else {
		    plugin.sendPasswordRequiredMessage(player);
		}
	    }
	    // You need a password
	    else {
		plugin.sendPasswordRequiredMessage(player);
	    }
	    // Cancel event anyway, no commands
	    event.setCancelled(true);
	}
	// Else already logged in
	else if (message.toLowerCase().startsWith("/login")) {
	    String messageLocalization = plugin.localization.getString("already_logged_in");
	    plugin.message(null, player, messageLocalization, null);
	    event.setCancelled(true);
	}
    }
}