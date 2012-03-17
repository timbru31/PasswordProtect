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

public class PasswordProtect extends JavaPlugin  {
	public static final Logger log = Logger.getLogger("Minecraft");
	private final PasswordProtectPlayerListener playerListener = new PasswordProtectPlayerListener(this);
	private final PasswordProtectBlockListener blockListener = new PasswordProtectBlockListener(this);
	private final PasswordProtectEntityListener entityListener = new PasswordProtectEntityListener(this);
	private PasswordProtectCommands executor;
	public FileConfiguration config;
	public File configFile;
	public ArrayList<Player> jailedPlayers = new ArrayList<Player>();
	private HashMap<World, JailLocation> jailLocations = new HashMap<World, JailLocation>();
	private String password;

	// Shutdown
	public void onDisable() {
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
		jailLocations.put(world, location);

		ArrayList<Double> data = new ArrayList<Double>();
		data.add(location.getX());
		data.add(location.getY());
		data.add(location.getZ());
		data.add(new Double(location.getYaw()));
		data.add(new Double(location.getPitch()));
		data.add(new Double(location.getRadius()));

		String worldName = world.getName();
		config.set(worldName + ".jailLocation", data);
		saveConfig();
	}

	public JailLocation getJailLocation(Player player) {
		World world = player.getWorld();

		JailLocation jailLocation;
		if (jailLocations.containsKey(world)) {
			jailLocation = jailLocations.get(world);
		} else {
			jailLocation = loadJailLocation(world);
			jailLocations.put(world, jailLocation);
		}

		return jailLocation == null ? new JailLocation(world.getSpawnLocation(), 2) : jailLocation;
	}

	private JailLocation loadJailLocation(World world) {
		String worldName = world.getName();
		config.addDefault(worldName + ".jailLocation", null);
		saveConfig();
		List<Double> data = config.getDoubleList(worldName + ".jailLocation");

		if (data == null || data.size() != 6) { // [x, y, z, yaw, pitch, radius]
			return null;
		}

		Double x = data.get(0);
		Double y = data.get(1);
		Double z = data.get(2);
		Float yaw = new Float(data.get(3));
		Float pitch = new Float(data.get(4));
		int radius = data.get(5).intValue();

		JailLocation jailLocation = new JailLocation(world, x, y, z, yaw, pitch, radius);
		return jailLocation;
	}

	public void setPassword(String password) throws Exception {
		password = encrypt(password);
		config.set("password", password);
		saveConfig();
	}

	// Check for the password, return null or the password
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

	public String encrypt(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte byteData[] = md.digest();
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
		return sb.toString();
	}
}