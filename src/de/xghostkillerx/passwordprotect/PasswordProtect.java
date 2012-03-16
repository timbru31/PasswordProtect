package de.xghostkillerx.passwordprotect;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

public class PasswordProtect extends JavaPlugin  {

	public static final Logger log = Logger.getLogger("Minecraft");
	private final PasswordProtectPlayerListener playerListener = new PasswordProtectPlayerListener(this);
	private final PasswordProtectBlockListener blockListener = new PasswordProtectBlockListener(this);

	public FileConfiguration config;
	public File configFile;

	public ArrayList<Player> jailedPlayers = new ArrayList<Player>();
	private HashMap<World, JailLocation> jailLocations = new HashMap<World, JailLocation>();

	private String password;
	private Boolean requireOpsPassword;
	

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
		
		// Config
		config = this.getConfig();
		
		// Check for the server password
		String serverPassword = getPassword();
		if (serverPassword != null) log.info("Server password is " + getPassword());
		else log.info("Server password is not set. Use /setpassword <password>");
		
		// Message
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");

		getCommand("opsrequirepassword").setExecutor(new OpsRequirePasswordCommand(this));
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

	public void setPassword(String password) {
		this.password = password;
		config.addDefault("password", password);
		config.options().copyDefaults(true);
		saveConfig();
	}

	// Check for the password, return null or the password
	public String getPassword() {
		if (password == null) {
			password = config.getString("password", null);
		}
		return password;
	}

	public void setRequireOpsPassword(boolean requireOpsPassword) {
		this.requireOpsPassword = requireOpsPassword;

		config.addDefault("requireOpsPassword", requireOpsPassword);
		config.options().copyDefaults(true);
		saveConfig();
	}

	public boolean getRequireOpsPassword() {
		if (requireOpsPassword == null) {
			requireOpsPassword = config.getBoolean("requireOpsPassword", false);
		}

		return requireOpsPassword;
	}


	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		PasswordProtectCommands cmd = new PasswordProtectCommands(this);
		return cmd.PasswordProtectCommand(sender, command, commandLabel, args);
	}
}