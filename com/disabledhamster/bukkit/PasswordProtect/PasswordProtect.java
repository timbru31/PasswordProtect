package com.disabledhamster.bukkit.PasswordProtect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.disabledhamster.bukkit.PasswordProtect.commands.*;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

public class PasswordProtect extends JavaPlugin  {
    private final PPPlayerListener playerListener = new PPPlayerListener(this);
    private final PPBlockListener blockListener = new PPBlockListener(this);

    private Configuration configuration;

    public ArrayList<Player> jailedPlayers = new ArrayList<Player>();
    private HashMap<World, JailLocation> jailLocations = new HashMap<World, JailLocation>();

    private String password;
    private Boolean requireOpsPassword;

    public void onEnable() {
        configuration = getConfiguration();

        System.out.println(getDescription().getName() + " " + getDescription().getVersion() + " enabled");
        String serverPassword = getPassword();
        if (serverPassword != null)
            System.out.println("Server password is " + getPassword());
        else {
            System.out.println("Server password is not set. Use /setpassword <password>");
        }

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.High, this);
        pluginManager.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.High, this);
        pluginManager.registerEvent(Type.PLAYER_DROP_ITEM, playerListener, Priority.High, this);

        pluginManager.registerEvent(Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);

        pluginManager.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Monitor, this);

        getCommand("password").setExecutor(new PasswordCommand(this));
        getCommand("setpasswordjail").setExecutor(new SetJailCommand(this));
        getCommand("setpassword").setExecutor(new SetPasswordCommand(this));
        getCommand("opsrequirepassword").setExecutor(new OpsRequirePasswordCommand(this));
    }

    public void onDisable() {}

    public Configuration getConfig() {
        return configuration;
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
        configuration.setProperty(worldName + ".jailLocation", data);
        configuration.save();
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
        List<Double> data = configuration.getDoubleList(worldName + ".jailLocation", null);
        
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
        
        configuration.setProperty("password", password);
        configuration.save();
    }

    public String getPassword() {
        if (password == null) {
            password = configuration.getString("password", null);
        }
        
        return password;
    }

    public void setRequireOpsPassword(boolean requireOpsPassword) {
        this.requireOpsPassword = requireOpsPassword;

        configuration.setProperty("requireOpsPassword", requireOpsPassword);
        configuration.save();
    }

    public boolean getRequireOpsPassword() {
        if (requireOpsPassword == null) {
            requireOpsPassword = configuration.getBoolean("requireOpsPassword", false);
        }

        return requireOpsPassword;
    }
}