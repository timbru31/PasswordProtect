package de.xghostkillerx.passwordprotect;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * PasswordProtect for CraftBukkit/Bukkit
 * Handles player activities.
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

public class PasswordProtectPlayerListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectPlayerListener(PasswordProtect instance) {
		plugin = instance;
	}

	// When the player joins, force a password and check permissions
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		plugin.check(player);
	}

	// Don't cancel movement, teleport back instead
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (plugin.config.getBoolean("prevent.Movement")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				if (!player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
					if (plugin.config.getBoolean("darkness")) player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
				}
				if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
					if (plugin.config.getBoolean("slowness")) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 86400, 15));
				}
				plugin.stayInJail(player);
			}
		}
	}

	// Don't let him interact
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (plugin.config.getBoolean("prevent.Interaction")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				event.setCancelled(true);
			}
		}
	}

	// Don't let him interact with mobs
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (plugin.config.getBoolean("prevent.InteractionMobs")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				event.setCancelled(true);
			}
		}
	}

	// Don't let him drop items
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (plugin.config.getBoolean("prevent.ItemDrop")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				event.setCancelled(true);
			}
		}
	}

	// Don't let him pickup stuff
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (plugin.config.getBoolean("prevent.ItemPickup")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				event.setCancelled(true);
			}
		}
	}

	// Sorry, no nether ;)
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (plugin.config.getBoolean("prevent.Portal")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				event.setCancelled(true);
			}
		}
	}

	// Sorry, no chat
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(PlayerChatEvent event) {
		if (plugin.config.getBoolean("prevent.Chat")) {
			Player player = event.getPlayer();
			if (plugin.jailedPlayers.containsKey(player)) {
				event.setCancelled(true);
			}
		}
	}

	// Listening for commands
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws Exception {
		Player player = event.getPlayer();
		String message = event.getMessage();
		String command = message.replaceFirst("/", "");
		if (command.contains(" "))
			command = command.substring(0, command.indexOf(' '));

		if (plugin.jailedPlayers.containsKey(player)) {
			if (plugin.commandList.contains(command)) {
				return;
			}
			else if (message.startsWith("/password")) {
				String password = message.replaceFirst("/password ", "");
				password = plugin.encrypt(password);
				if (password.equals(plugin.getPassword())) {
					String messageLocalization = plugin.localization.getString("password_accepted");
					plugin.message(null, player, messageLocalization, null);
					plugin.jailedPlayers.remove(player);
					if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) 
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 0, 0), true);
					if (player.hasPotionEffect(PotionEffectType.SLOW)) 
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 0, 0), true);
				}
				else {
					int attempts = plugin.jailedPlayers.get(player);
					int kickAfter = plugin.config.getInt("wrongAttempts.kick");
					int banAfter = plugin.config.getInt("wrongAttempts.ban");
					int attemptsLeftKick = kickAfter - attempts;
					int attemptsLeftBan = banAfter - attempts;
					if (attemptsLeftKick <= 0 || attemptsLeftBan <= 0) {
						if (attemptsLeftBan <= 0) {
							String ip = player.getAddress().getAddress().toString().replaceAll("/", "");
							String messageLocalization = plugin.localization.getString("ban_message");
							player.kickPlayer(messageLocalization.replaceAll("&([0-9a-fk])", "\u00A7$1"));
							player.setBanned(true);
							if (plugin.config.getBoolean("wrongAttempts.banIP")) plugin.getServer().banIP(ip);
							return;
						}
						else if(attemptsLeftKick == 0) {
							String messageLocalization = plugin.localization.getString("kick_message");
							player.kickPlayer(messageLocalization.replaceAll("&([0-9a-fk])", "\u00A7$1"));
						}
					}
					if (attemptsLeftKick > 0 || attemptsLeftBan > 0) {
						if (attemptsLeftKick > 0) {
							String messageLocalization = plugin.localization.getString("attempts_left_kick");
							plugin.message(null, player, messageLocalization, Integer.toString(attemptsLeftKick));
						}
						String messageLocalization = plugin.localization.getString("attempts_left_ban");
						plugin.message(null, player, messageLocalization, Integer.toString(attemptsLeftBan));
						attempts++;
						plugin.jailedPlayers.put(player, attempts);
					}
				}
			}
			else {
				plugin.sendPasswordRequiredMessage(player);
			}
			event.setCancelled(true);
		}
	}
}