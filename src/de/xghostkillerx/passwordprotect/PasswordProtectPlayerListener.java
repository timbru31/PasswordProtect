package de.xghostkillerx.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class PasswordProtectPlayerListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectPlayerListener(PasswordProtect instance) {
		plugin = instance;
	}

	// When the player joins, force a password and check permissions
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!plugin.passwordSet()) {
			if (player.hasPermission("passwordprotect.setpassword")) {
				player.sendMessage(ChatColor.YELLOW + "PasswordProtect is enabled but no password has been set");
				player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/setpassword " + ChatColor.RED + "<password>" + ChatColor.YELLOW + " to set it");
			}
		}
		else if (!player.hasPermission("passwordprotect.nopassword")) {
			sendToJail(player);
			if (!plugin.jailedPlayers.containsKey(player)) plugin.jailedPlayers.put(player, 1);
			if (plugin.config.getBoolean("darkness")) player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
			if (plugin.config.getBoolean("slowness")) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 86400, 5));
		}
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
				stayInJail(player);
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
				String password = message.replaceFirst("\\/password ", "");
				password = plugin.encrypt(password);
				if (password.equals(plugin.getPassword())) {
					player.sendMessage(ChatColor.GREEN + "Server password accepted, you can now play");
					plugin.jailedPlayers.remove(player);
					if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) 
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 0, 0), true);
					if (player.hasPotionEffect(PotionEffectType.SLOW)) 
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 0, 0), true);
				}
				else {
					int attempts = plugin.jailedPlayers.get(player);
					System.out.println(attempts);
					int kickAfter = plugin.config.getInt("wrongAttempts.kick");
					int banAfter = plugin.config.getInt("wrongAttempts.ban");
					int attemptsLeftKick = kickAfter - attempts;
					int attemptsLeftBan = banAfter - attempts;

					if (attemptsLeftKick == 0 || attemptsLeftBan == 0) {
						if (attemptsLeftBan == 0) {
							player.sendMessage(ChatColor.RED + "Server password incorrect! You're will be banned by now...");
							player.kickPlayer("Banned by PasswordProtect for too many wrong attempts...");
							player.setBanned(true);
							return;
						}
						else if(attemptsLeftKick == 0) {
							player.kickPlayer("Kicked by PasswordProtect for too many wrong attempts...");
						}
					}
					if (attemptsLeftKick > 0 || attemptsLeftBan > 0) {
						if (attemptsLeftKick > 0) {
							player.sendMessage(ChatColor.RED + "Server password incorrect! " + attemptsLeftKick + " attempts left until kick...");
						}
						player.sendMessage(ChatColor.RED + "Server password incorrect! " + attemptsLeftBan + " attempts left until ban...");
						attempts++;
						plugin.jailedPlayers.put(player, attempts);
					}
				}
			}
			else {
				sendPasswordRequiredMessage(player);
			}
			event.setCancelled(true);
		}
	}

	private void stayInJail(Player player) {
		JailLocation jailLocation = plugin.getJailLocation(player);
		Location playerLocation = player.getLocation();
		int radius = jailLocation.getRadius();
		// If player is within radius^2 blocks of jail location...
		if (Math.abs(jailLocation.getBlockX() - playerLocation.getBlockX()) <= radius
				&& Math.abs(jailLocation.getBlockY() - playerLocation.getBlockY()) <= radius
				&& Math.abs(jailLocation.getBlockZ() - playerLocation.getBlockZ()) <= radius) {
			return;
		}
		sendToJail(player);
	}

	private void sendToJail(Player player) {
		JailLocation jailLocation = plugin.getJailLocation(player);
		player.teleport(jailLocation);
		sendPasswordRequiredMessage(player);
	}

	private void sendPasswordRequiredMessage(Player player) {
		player.sendMessage(ChatColor.YELLOW + "This server is password-protected");
		player.sendMessage(ChatColor.YELLOW + "Enter the password with " + ChatColor.GREEN + "/password " + ChatColor.RED + " <password>" + ChatColor.YELLOW + " to play");
	}
}