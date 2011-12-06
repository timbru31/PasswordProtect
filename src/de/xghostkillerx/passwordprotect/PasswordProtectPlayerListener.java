package de.xghostkillerx.passwordprotect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PasswordProtectPlayerListener extends PlayerListener {
	private PasswordProtect plugin;
	public PasswordProtectPlayerListener(PasswordProtect plugin) {
		this.plugin = plugin;
	}

	// When the player joins, force a password and check permissions
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (this.plugin.getPassword() == null) {
			if (player.hasPermission("passwordprotect.setpassword")) {
				player.sendMessage(ChatColor.YELLOW + "PasswordProtect is enabled but no password has been set");
				player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/setpassword " + ChatColor.RED + "<password>" + ChatColor.YELLOW + " to set it");
			}
		} else if (!player.hasPermission("passwordprotect.nopassword")) {
			sendToJail(player);
			this.plugin.jailedPlayers.add(player);
		}
	}

	public void stayInJail(Player player) {
		JailLocation jailLocation = this.plugin.getJailLocation(player);
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

	public void sendToJail(Player player) {
		JailLocation jailLocation = this.plugin.getJailLocation(player);
		player.teleport(jailLocation);
		sendPasswordRequiredMessage(player);
	}

	public void sendPasswordRequiredMessage(Player player) {
		player.sendMessage(ChatColor.YELLOW + "This server is password-protected");
		player.sendMessage(ChatColor.YELLOW + "Enter the password with " + ChatColor.GREEN + "/password " + ChatColor.RED + " <password>" + ChatColor.YELLOW + " to play");
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		if (this.plugin.jailedPlayers.contains(player)) {
			stayInJail(player);
			event.setCancelled(true);
		}
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		if (this.plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		if (this.plugin.jailedPlayers.contains(player)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		if (this.plugin.jailedPlayers.contains(player)) {
			if (message.startsWith("/password")) {
				String password = message.replaceFirst("\\/password ", "");

				if (password.equals(this.plugin.getPassword())) {
					player.sendMessage(ChatColor.GREEN + "Server password accepted, you can now play");
					this.plugin.jailedPlayers.remove(player);
				}
				else {
					player.sendMessage(ChatColor.RED + "Server password incorrect, try again");
				}
			}
			else if (message.startsWith("/rules") || message.startsWith("/help")) {
				
			}
			else {
				sendPasswordRequiredMessage(player);
			}
			event.setCancelled(true);
		}
	}
}