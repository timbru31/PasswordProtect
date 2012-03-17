package de.xghostkillerx.passwordprotect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
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
	private final PasswordProtectInventoryListener inventoryListener = new PasswordProtectInventoryListener(this);
	private PasswordProtectCommands executor;
	public FileConfiguration config;
	public File configFile;
	public ArrayList<Player> jailedPlayers = new ArrayList<Player>();
	private HashMap<World, JailLocation> jailLocations = new HashMap<World, JailLocation>();
	private String password;

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
		pm.registerEvents(inventoryListener, this);

		jailedPlayers.clear();
		jailLocations.clear();
		
		// Config
		configFile = new File(getDataFolder(), "config.yml");
		if(!configFile.exists()){
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
		config = this.getConfig();
		loadConfig();

		// Check for the server password
		if (config.getBoolean("printPasswordOnStart")) {
			String serverPassword = getPassword();
			if (serverPassword != null) log.info("Server password is " + getPassword());
			else log.info("Server password is not set. Use /setpassword <password>");
		}

		// Message
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");

		executor = new PasswordProtectCommands(this);
		getCommand("password").setExecutor(executor);
		getCommand("setpassword").setExecutor(executor);
		getCommand("setpasswordjail").setExecutor(executor);
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
		config.set(worldName + ".jailLocation", data);
		saveConfig();
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
		List<Double> data = config.getDoubleList(worldName + ".jailLocation");
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
		password = encrypt(password);
		config.set("password", password);
		saveConfig();
	}

	// Check for the password, return null or the password
	// TODO
	public String getPassword() {
		if (password == null) {
			password = config.getString("password", null);
		}
		return password;
	}

	// Loads the config at start
	public void loadConfig() {
		config.options().header("For help please refer to");
		config.options().copyDefaults(true);
		saveConfig();
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