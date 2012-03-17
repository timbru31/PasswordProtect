package de.xghostkillerx.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PasswordProtectPlayerListener implements Listener {
	public PasswordProtect plugin;
	public PasswordProtectPlayerListener(PasswordProtect instance) {
		plugin = instance;
	}

	// When the player joins, force a password and check permissions
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (this.plugin.getPassword() == null) {
			if (player.hasPermission("passwordprotect.setpassword")) {
				player.sendMessage(ChatColor.YELLOW + "PasswordProtect is enabled but no password has been set");
				player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/setpassword " + ChatColor.RED + "<password>" + ChatColor.YELLOW + " to set it");
			}
		} else if (!player.hasPermission("passwordprotect.nopassword")) {
			sendToJail(player);
			plugin.jailedPlayers.add(player);
		}
	}
	
	// When quitting remove from list
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			plugin.jailedPlayers.remove(player);
		}
	}
	
	// When kicked remove from list
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(final PlayerKickEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			plugin.jailedPlayers.remove(player);
		}
	}

	// Don't cancel movement, teleport back instead
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(final PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			stayInJail(player);
		}
	}

	// Don't let him interact
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}
	
	// Don't let him interact with mobs
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}

	// Don't let him drop items
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}
	
	// Don't let him pickup stuff
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}
	
	// Sorry, no nether ;)
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPortal(final PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if (plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) throws Exception {
		Player player = event.getPlayer();
		String message = event.getMessage();

		if (plugin.jailedPlayers.contains(player)) {
			if (message.startsWith("/password")) {
				String password = message.replaceFirst("\\/password ", "");
				password = plugin.encrypt(password);

				if (password.equals(plugin.getPassword())) {
					player.sendMessage(ChatColor.GREEN + "Server password accepted, you can now play");
					plugin.jailedPlayers.remove(player);
				}
				else {
					player.sendMessage(ChatColor.RED + "Server password incorrect, try again");
				}
			}
			else if (message.startsWith("/rules") || message.startsWith("/help")) {
				return;
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