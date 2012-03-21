package de.xghostkillerx.passwordprotect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import de.xghostkillerx.passwordprotect.JailLocation;

public class PasswordProtect extends JavaPlugin  {
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
	public HashMap<Player, Integer> jailedPlayers = new HashMap<Player, Integer>();
	private HashMap<World, JailLocation> jailLocations = new HashMap<World, JailLocation>();
	public List<String> commandList = new ArrayList<String>();
	private String[] commands = {"help", "rules", "motd",};

	// Shutdown
	public void onDisable() {
		jailedPlayers.clear();
		jailLocations.clear();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion()	+ " has been disabled!");
	}

	// Start
	public void onEnable() {
		// Events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(blockListener, this);
		pm.registerEvents(playerListener, this);
		pm.registerEvents(entityListener, this);

		// Clear lists
		jailedPlayers.clear();
		jailLocations.clear();

		// Jails config		
		jailFile = new File(getDataFolder(), "jails.yml");
		// Copy if the config doesn't exist
		if (!jailFile.exists()) {
			jailFile.getParentFile().mkdirs();
			copy(getResource("jails.yml"), jailFile);
		}
		// Try to load
		try {
			jails = YamlConfiguration.loadConfiguration(jailFile);
		}
		// Log if failed
		catch (Exception e) {
			log.warning("PasswordProtect failed to load the jails.yml! Please report this!");
		}

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
		if(!localizationFile.exists()){
			localizationFile.getParentFile().mkdirs();
			copy(getResource("localization.yml"), localizationFile);
		}
		// Try to load
		try {
			localization = YamlConfiguration.loadConfiguration(localizationFile);
			loadLocalization();
		}
		// If it failed, tell it
		catch (Exception e) {
			log.warning("PasswordProtect failed to load the localization!");
		}

		// Commands
		executor = new PasswordProtectCommands(this);
		getCommand("password").setExecutor(executor);
		getCommand("setpassword").setExecutor(executor);
		getCommand("setpasswordjail").setExecutor(executor);

		// Message
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");
	}

	// Loads the config at start
	private void loadConfig() {
		config.options().header("For help please refer to");
		config.addDefault("cleanPassword", false);
		config.addDefault("password", "");
		config.addDefault("passwordClean", "");
		config.addDefault("prevent.Movement", true);
		config.addDefault("prevent.Interaction", true);
		config.addDefault("prevent.InteractionMobs", true);
		config.addDefault("prevent.ItemPickup", true);
		config.addDefault("prevent.ItemDrops", true);
		config.addDefault("prevent.Portal", true);
		config.addDefault("prevent.BlockPlace", true);
		config.addDefault("prevent.BlockBreak", true);
		config.addDefault("prevent.Triggering", true);
		config.addDefault("prevent.Attacks", true);
		config.addDefault("prevent.Damage", true);
		config.addDefault("prevent.Chat", true);
		config.addDefault("wrongAttempts.kick", 3);
		config.addDefault("wrongAttempts.ban", 5);
		config.addDefault("darkness", true);
		config.addDefault("slowness", true);
		config.addDefault("commands", Arrays.asList(commands));
		commandList = config.getStringList("commands");
		config.options().copyDefaults(true);
		saveConfig();
	}

	// Loads the localization
	private void loadLocalization() {
		localization.options().header("The underscores are used for the different lines!");
		localization.addDefault("permission_denied", "&4You don't have the permission to do this!");
		localization.options().copyDefaults(true);
		saveLocalization();
	}

	// Try to save the players.yml
	private void saveJails() {
		try {
			jails.save(jailFile);
		} catch (Exception e) {
			log.warning("PasswordProtect failed to save the jails.yml! Please report this!");
		}
	}

	// Saves the localization
	private void saveLocalization() {
		try {
			localization.save(localizationFile);
		}
		catch (IOException e) {
			log.warning("PasswordProtect failed to save the localization.yml! Please report this!");
		}
	}

	// If no config is found, copy the default one!
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len=in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
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
		}
		else {
			// Try to load and add to the list
			jailLocation = loadJailLocation(world);
			if (jailLocation == null)  {
				jailLocation = new JailLocation(world.getSpawnLocation(), 4);
				jailLocations.put(world, jailLocation);
			}
		}
		// Is the jailLocation null? Yes -> Make one at spawn, No -> return jailLocation
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
		String encryptedPassword = encrypt(password);
		config.set("password", encryptedPassword);
		if (config.getBoolean("cleanPassword")) {
			config.set("passwordClean", password);
		}
		saveConfig();
	}

	// Check for the password, return null or the password
	public String getPasswordClean() {
		if (config.getBoolean("cleanPassword")) {
			return config.getString("passwordClean");
		}
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

	// SHA256 encryption. Stores only hex format
	public String encrypt(String password) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(password.getBytes());
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}