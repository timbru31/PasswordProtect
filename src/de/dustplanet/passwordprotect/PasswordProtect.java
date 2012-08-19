package de.dustplanet.passwordprotect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import de.dustplanet.passwordprotect.JailLocation;

/**
 * PasswordProtect for CraftBukkit/Bukkit Handles some general stuff.
 * 
 * 
 * Refer to the forum thread: http://bit.ly/ppbukkit Refer to the dev.bukkit.org
 * page: http://bit.ly/ppbukktidev
 * 
 * @author xGhOsTkiLLeRx
 * @thanks to brianewing alias DisabledHamster for the original plugin!
 * 
 */

public class PasswordProtect extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	private final PasswordProtectPlayerListener playerListener = new PasswordProtectPlayerListener(this);
	private final PasswordProtectBlockListener blockListener = new PasswordProtectBlockListener(this);
	private final PasswordProtectEntityListener entityListener = new PasswordProtectEntityListener(this);
	private PasswordProtectCommands executor;
	public FileConfiguration config;
	public FileConfiguration jails;
	public FileConfiguration localization;
	private File configFile;
	private File jailFile;
	private File localizationFile;
	// Integer = attempts to login!
	public HashMap<String, Integer> jailedPlayers = new HashMap<String, Integer>();
	private HashMap<World, JailLocation> jailLocations = new HashMap<World, JailLocation>();
	public HashMap<String, Location> playerLocations = new HashMap<String, Location>();
	public List<String> commandList = new ArrayList<String>();
	private String[] commands = { "help", "rules", "motd", };
	private String encryption = "SHA-256";
	private File jailedPlayersFile;

	// Shutdown
	public void onDisable() {
		// Remove potion effect
		for (String playerName : jailedPlayers.keySet()) {
			Player player = getServer().getPlayerExact(playerName);
			if (player != null) {
				if (player.hasPotionEffect(PotionEffectType.BLINDNESS))
					player.removePotionEffect(PotionEffectType.BLINDNESS);
				if (player.hasPotionEffect(PotionEffectType.SLOW))
					player.removePotionEffect(PotionEffectType.SLOW);
			}
		}
		// Save the HashMap of the jailedPlayers
		try {
			ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(jailedPlayersFile));
			obj.writeObject(jailedPlayers);
			obj.close();
		} catch (FileNotFoundException e) {
			log.info("PasswordProtect couldn't find the 'jailedPlayers.dat' file!");
		} catch (IOException e) {
			log.info("PasswordProtect couldn't save the 'jailedPlayers.dat' (I/O Exception)!");
		}
		// Clear lists
		jailedPlayers.clear();
		jailLocations.clear();
		playerLocations.clear();

		// Log
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been disabled!");
	}

	// Start
	@SuppressWarnings("unchecked")
	public void onEnable() {
		// Events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(blockListener, this);
		pm.registerEvents(playerListener, this);
		pm.registerEvents(entityListener, this);

		// Clear lists
		jailedPlayers.clear();
		jailLocations.clear();
		playerLocations.clear();

		// Jails config
		jailFile = new File(getDataFolder(), "jails.yml");
		// Copy if the config doesn't exist
		if (!jailFile.exists()) {
			jailFile.getParentFile().mkdirs();
			copy(getResource("jails.yml"), jailFile);
		}
		// Try to load
		jails = YamlConfiguration.loadConfiguration(jailFile);

		// Config
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
		config = this.getConfig();
		loadConfig();

		// Localization
		localizationFile = new File(getDataFolder(), "localization.yml");
		if (!localizationFile.exists()) {
			localizationFile.getParentFile().mkdirs();
			copy(getResource("localization.yml"), localizationFile);
		}
		localization = YamlConfiguration.loadConfiguration(localizationFile);
		loadLocalization();

		// Read the jailedPlayersFile
		jailedPlayersFile = new File(getDataFolder(), "jailedPlayers.dat");
		if (!jailedPlayersFile.exists()) {
			try {
				jailedPlayersFile.createNewFile();
			} catch (IOException e) {
				log.info("PasswordProtect couldn't create the 'jailedPlayers.dat' file! (I/O Exception)");
			}
			jailedPlayersFile.getParentFile().mkdirs();
		}
		// Read into Memory
		try {
			ObjectInputStream obj = new ObjectInputStream(new FileInputStream(jailedPlayersFile));
			jailedPlayers = (HashMap<String, Integer>) obj.readObject();
			obj.close();
		} catch (FileNotFoundException e) {
			log.info("PasswordProtect couldn't find the 'jailedPlayers.dat' file!");
		} catch (IOException e) {
			log.info("PasswordProtect couldn't read the 'jailedPlayers.dat' file! (I/O Exception)");
		} catch (ClassNotFoundException e) {
			log.info("PasswordProtect couldn't read the 'jailedPlayers.dat' file! (Class not found Exception)");
		}

		// Commands
		executor = new PasswordProtectCommands(this);
		getCommand("login").setExecutor(executor);
		getCommand("password").setExecutor(executor);
		getCommand("setpassword").setExecutor(executor);
		getCommand("setjaillocation").setExecutor(executor);

		// Message
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " is enabled!");

		// Stats
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {}
	}

	// Loads the config at start
	private void loadConfig() {
		config.options().header("For help please refer to");
		config.addDefault("encryption", "SHA-256");
		config.addDefault("OpsRequirePassword", true);
		config.addDefault("cleanPassword", false);
		config.addDefault("password", "");
		config.addDefault("passwordClean", "");
		config.addDefault("prevent.Movement", true);
		config.addDefault("prevent.Interaction", true);
		config.addDefault("prevent.InteractionMobs", true);
		config.addDefault("prevent.ItemPickup", true);
		config.addDefault("prevent.ItemDrop", true);
		config.addDefault("prevent.Portal", true);
		config.addDefault("prevent.BlockPlace", true);
		config.addDefault("prevent.BlockBreak", true);
		config.addDefault("prevent.Triggering", true);
		config.addDefault("prevent.Attacks", true);
		config.addDefault("prevent.Damage", true);
		config.addDefault("prevent.Chat", true);
		config.addDefault("prevent.DeathDrops", true);
		config.addDefault("prevent.Flying", true);
		config.addDefault("wrongAttempts.kick", 3);
		config.addDefault("wrongAttempts.ban", 5);
		config.addDefault("wrongAttempts.banIP", true);
		config.addDefault("broadcast.kick", true);
		config.addDefault("broadcast.ban", true);
		config.addDefault("darkness", true);
		config.addDefault("slowness", true);
		config.addDefault("teleportBack", true);
		config.addDefault("loginMessage", true);		
		config.addDefault("allowedCommands", Arrays.asList(commands));
		commandList = config.getStringList("allowedCommands");
		encryption = config.getString("encryption");
		// Lets see if the encryption is valid, if not fallback!
		try {
			@SuppressWarnings("unused")
			MessageDigest md = MessageDigest.getInstance(encryption);
		} catch (NoSuchAlgorithmException e) {
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "PasswordProtect can't use this encryption! FATAL!");
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "Falling back to SHA-256!");
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "Report this IMMEDIATELY!");
			encryption = "SHA-256";
			config.set("encryption", "SHA-256");
		}
		config.options().copyDefaults(true);
		saveConfig();
	}

	// Loads the localization
	private void loadLocalization() {
		localization.options().header("The underscores are used for the different lines!");
		localization.addDefault("permission_denied", "&4You don't have the permission to do this!");
		localization.addDefault("enter_password_1", "&eThis server is password-protected");
		localization.addDefault("enter_password_2", "&eEnter the password with &a/login &4<password> &eto play");
		localization.addDefault("set_password_1", "&ePasswordProtect is enabled but no password has been set");
		localization.addDefault("set_password_2", "&eUse &a/setpassword &4<password> &eto set it");
		localization.addDefault("password_accepted", "&aServer password accepted, you can now play");
		localization.addDefault("attempts_left_kick", "&4Server password incorrect! &e%attempts &4attempts left until kick...");
		localization.addDefault("attempts_left_ban", "&4Server password incorrect! &e%attempts &4attempts left until ban...");
		localization.addDefault("kick_message", "&4Kicked by &ePasswordProtect &4for too many wrong attempts...");
		localization.addDefault("ban_message", "&4Banned by &ePasswordProtect &4for too many wrong attempts...");
		localization.addDefault("kick_broadcast", "&e%player &4kicked by &ePasswordProtect &4for too many wrong attempts...");
		localization.addDefault("ban_broadcast", "&e%player &4banned by &ePasswordProtect &4for too many wrong attempts...");
		localization.addDefault("radius_not_number", "&4The radius was not a number! Using standard (4) instead!");
		localization.addDefault("jail_set", "&aJail location set");
		localization.addDefault("password_set", "&aServer password set!");
		localization.addDefault("only_ingame", "&4The command can only be used ingame!");
		localization.addDefault("config_invalid", "&4It seems like this server config invalid. Please re-set the password!");
		localization.addDefault("only_encypted", "&4Server password is only stored encrypted...");
		localization.addDefault("password_not_set", "&eServer password is not set. Use /setpassword <password>");
		localization.addDefault("password", "&eServer password is &4%password");
		localization.addDefault("set_jail_area", "&eYou can set the jail area by going somewhere and using &a/setjaillocation &4[radius]");
		localization.addDefault("already_logged_in", "&eYou are already logged in!");
		localization.addDefault("no_login_console", "&eThe console can't login into the server!");
		localization.options().copyDefaults(true);
		saveLocalization();
	}

	// Try to save the players.yml
	private void saveJails() {
		try {
			jails.save(jailFile);
		} catch (IOException e) {
			log.warning("PasswordProtect failed to save the jails.yml! Please report this!");
		}
	}

	// Saves the localization
	private void saveLocalization() {
		try {
			localization.save(localizationFile);
		} catch (IOException e) {
			log.warning("PasswordProtect failed to save the localization.yml! Please report this!");
		}
	}

	// If no config is found, copy the default one!
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Message sender
	public void message(CommandSender sender, Player player, String message,
			String value) {
		PluginDescriptionFile pdfFile = this.getDescription();
		if (message != null) {
			message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%attempts", value)
					.replaceAll("%password", value)
					.replaceAll("%version", pdfFile.getVersion());
			if (player != null) {
				player.sendMessage(message);
			} else if (sender != null) {
				sender.sendMessage(message);
			}
		}
		// If message is null. Should NOT occur.
		else {
			if (player != null) {
				player.sendMessage(ChatColor.DARK_RED + "Somehow this message is not defined. Please check your localization.yml");
			} else if (sender != null) {
				sender.sendMessage(ChatColor.DARK_RED + "Somehow this message is not defined. Please check your localization.yml");
			}
		}
	}

	public void check(Player player) {
		// Password not set
		if (!passwordSet()) {
			// Message player to set the password.
			if (player.hasPermission("passwordprotect.setpassword")) {
				for (int i = 1; i < 3; i++) {
					String messageLocalization = localization.getString("set_password_" + i);
					message(null, player, messageLocalization, null);
				}
			}
		}
		// If he has not the permission to bypass
		else if (!player.hasPermission("passwordprotect.nopassword")) {
			// If he isn't an OP or OPs are required, too
			if ((player.isOp() && config.getBoolean("OpsRequirePassword"))
					|| !player.isOp()) {
				// Remember position?
				if (config.getBoolean("teleportBack")) {
					if (!playerLocations.containsKey(player.getName()))
						playerLocations.put(player.getName(), player.getLocation());
				}
				// Jail!
				sendToJail(player);
				// Add him & do bad stuff
				if (!jailedPlayers.containsKey(player.getName())) jailedPlayers.put(player.getName(), 1);
				if (config.getBoolean("prevent.Flying")) player.setAllowFlight(false);
				if (config.getBoolean("darkness"))
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
				if (config.getBoolean("slowness"))
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 86400, 5));
			}
		}
	}

	// Teleport back to the middle if player leaves area
	public void stayInJail(Player player) {
		JailLocation jailLocation = getJailLocation(player);
		Location playerLocation = player.getLocation();
		int radius = jailLocation.getRadius();
		// If player is within radius^2 blocks of jail location...
		if (Math.abs(jailLocation.getBlockX() - playerLocation.getBlockX()) <= radius
				&& Math.abs(jailLocation.getBlockY()
						- playerLocation.getBlockY()) <= radius
				&& Math.abs(jailLocation.getBlockZ()
						- playerLocation.getBlockZ()) <= radius) {
			return;
		}
		sendToJail(player);
	}

	// Jail the player. Teleport and message
	public void sendToJail(Player player) {
		JailLocation jailLocation = getJailLocation(player);
		player.teleport(jailLocation);
		sendPasswordRequiredMessage(player);
	}

	// Message
	public void sendPasswordRequiredMessage(Player player) {
		for (int i = 1; i < 3; i++) {
			if (config.getBoolean("loginMessage")) {
				String messageLocalization = localization.getString("enter_password_" + i);
				message(null, player, messageLocalization, null);
			}
		}
	}

	public void setJailLocation(World world, JailLocation location) {
		// Set it to the hashmap
		jailLocations.put(world, location);
		// Make data array
		ArrayList<Double> data = new ArrayList<Double>();
		data.add(location.getX());
		data.add(location.getY());
		data.add(location.getZ());
		data.add(new Double(location.getYaw()));
		data.add(new Double(location.getPitch()));
		data.add(new Double(location.getRadius()));
		// Write to the config
		String worldName = world.getName();
		jails.set(worldName + ".jailLocation", data);
		saveJails();
	}

	public JailLocation getJailLocation(Player player) {
		World world = player.getWorld();
		JailLocation jailLocation;
		// If world is already on the list
		if (jailLocations.containsKey(world)) {
			jailLocation = jailLocations.get(world);
		} else {
			// Try to load and add to the list
			jailLocation = loadJailLocation(world);
			if (jailLocation == null) {
				jailLocation = JailLocation.SpawnJailLocation(world.getSpawnLocation(), 4);
				jailLocations.put(world, jailLocation);
				setJailLocation(world, jailLocation);
			}
		}
		// Is the jailLocation null? Yes -> Make one at spawn, No -> return
		// jailLocation
		return jailLocation;
	}

	private JailLocation loadJailLocation(World world) {
		String worldName = world.getName();
		List<Double> data = jails.getDoubleList(worldName + ".jailLocation");
		// If no data is stored, return null
		if (data == null || data.size() != 6) { // [x, y, z, yaw, pitch, radius]
			return null;
		}
		// Else load them
		Double x = data.get(0);
		Double y = data.get(1);
		Double z = data.get(2);
		Float yaw = new Float(data.get(3));
		Float pitch = new Float(data.get(4));
		int radius = data.get(5).intValue();
		// Return location
		JailLocation jailLocation = new JailLocation(world, x, y, z, yaw, pitch, radius);
		return jailLocation;
	}

	public void setPassword(String password) throws Exception {
		// Encrypt
		String encryptedPassword = encrypt(password);
		config.set("password", encryptedPassword);
		// Additionally store "clean"
		if (config.getBoolean("cleanPassword")) config.set("passwordClean", password);
		saveConfig();
	}

	// Check for the password, return null or the password
	public String getPasswordClean() {
		if (config.getBoolean("cleanPassword")) return config.getString("passwordClean");
		return null;
	}

	// Check for the password, return null or the password
	public String getPassword() {
		return config.getString("password");
	}

	// Is the password set?
	public boolean passwordSet() {
		return config.getString("password").trim().length() > 1 ? true : false;
	}

	// Encryption. Stores only hex format
	public String encrypt(String password) throws Exception {
		try {
			MessageDigest md = MessageDigest.getInstance(encryption);
			md.update(password.getBytes());
			byte byteData[] = md.digest();
			return String.format("%0" + (byteData.length << 1) + "x", new BigInteger(1, byteData));
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}