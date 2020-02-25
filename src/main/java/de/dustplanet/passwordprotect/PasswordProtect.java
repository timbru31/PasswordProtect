package de.dustplanet.passwordprotect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import lombok.Getter;
import lombok.Setter;

/**
 * PasswordProtect for CraftBukkit/Spigot. Handles some general stuff. Refer to the dev.bukkit.org page:
 * https://dev.bukkit.org/projects/passwordprotect
 *
 * @author xGhOsTkiLLeRx thanks to brianewing alias DisabledHamster for the original plugin!
 */

public class PasswordProtect extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 2038;
    private FileConfiguration config;
    @Getter
    @Setter
    private FileConfiguration localization;
    private FileConfiguration jails;
    private File configFile;
    private File jailFile;
    private File localizationFile;
    @Getter
    @Setter
    private HashMap<UUID, Integer> jailedPlayers = new HashMap<>();
    private HashMap<World, JailLocation> jailLocations = new HashMap<>();
    @Getter
    private HashMap<UUID, Location> playerLocations = new HashMap<>();
    @Getter
    @Setter
    private List<String> commandList = new ArrayList<>();
    private String[] commands = { "help", "rules", "motd" };
    private String hash = "SHA-512";
    private File jailedPlayersFile;

    @Override
    public void onDisable() {
        for (UUID playerUUID : getJailedPlayers().keySet()) {
            Player player = getServer().getPlayer(playerUUID);
            if (player != null) {
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
                if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                    player.removePotionEffect(PotionEffectType.SLOW);
                }
            }
        }

        try (ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(jailedPlayersFile))) {
            obj.writeObject(getJailedPlayers());
        } catch (IOException e) {
            getLogger().info("Couldn't find the 'jailedPlayers.dat' file!");
            e.printStackTrace();
        }

        getJailedPlayers().clear();
        jailLocations.clear();
        getPlayerLocations().clear();
        getCommandList().clear();
    }

    @Override
    @SuppressWarnings({ "unchecked", "unused" })
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PasswordProtectBlockListener(this), this);
        pm.registerEvents(new PasswordProtectPlayerListener(this), this);
        pm.registerEvents(new PasswordProtectEntityListener(this), this);

        getJailedPlayers().clear();
        jailLocations.clear();
        getPlayerLocations().clear();

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("The config folder could NOT be created, make sure it's writable!");
            getLogger().severe("Disabling now!");
            setEnabled(false);
            return;
        }

        jailFile = new File(getDataFolder(), "jails.yml");
        if (!jailFile.exists()) {
            copy("jails.yml", jailFile);
        }
        jails = ScalarYamlConfiguration.loadConfiguration(jailFile);

        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            copy("config.yml", configFile);
        }
        config = this.getConfig();
        loadConfig();

        localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            copy("localization.yml", localizationFile);
        }
        setLocalization(ScalarYamlConfiguration.loadConfiguration(localizationFile));
        loadLocalization();

        jailedPlayersFile = new File(getDataFolder(), "jailedPlayers.dat");
        if (!jailedPlayersFile.exists()) {
            try {
                if (!jailedPlayersFile.createNewFile()) {
                    getLogger().info("Creating the 'jailedPlayers.dat' file failed!");
                }
            } catch (IOException e) {
                getLogger().info("Couldn't create the 'jailedPlayers.dat' file!");
                e.printStackTrace();
            }
        } else {
            try (ObjectInputStream obj = new ObjectInputStream(new FileInputStream(jailedPlayersFile))) {
                setJailedPlayers((HashMap<UUID, Integer>) obj.readObject());
            } catch (IOException | ClassNotFoundException e) {
                getLogger().info("Couldn't read the 'jailedPlayers.dat' file!");
                e.printStackTrace();
            }
        }

        PasswordProtectCommands executor = new PasswordProtectCommands(this);
        getCommand("login").setExecutor(executor);
        getCommand("password").setExecutor(executor);
        getCommand("setpassword").setExecutor(executor);
        getCommand("setjaillocation").setExecutor(executor);

        new Metrics(this, BSTATS_PLUGIN_ID);
    }

    private void loadConfig() {
        config.options().header("For help please refer to https://dev.bukkit.org/projects/passwordprotect");
        config.addDefault("hash", "SHA-512");
        config.addDefault("disableJailArea", false);
        config.addDefault("opsRequirePassword", true);
        config.addDefault("cleanPassword", false);
        config.addDefault("password", "");
        config.addDefault("passwordClean", "");
        config.addDefault("prevent.movement", true);
        config.addDefault("prevent.interaction", true);
        config.addDefault("prevent.interactionMobs", true);
        config.addDefault("prevent.itemPickup", true);
        config.addDefault("prevent.itemDrop", true);
        config.addDefault("prevent.portal", true);
        config.addDefault("prevent.blockPlace", true);
        config.addDefault("prevent.blockBreak", true);
        config.addDefault("prevent.triggering", true);
        config.addDefault("prevent.attacks", true);
        config.addDefault("prevent.damage", true);
        config.addDefault("prevent.chat", true);
        config.addDefault("prevent.deathDrops", true);
        config.addDefault("prevent.flying", true);
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
        setCommandList(config.getStringList("allowedCommands"));
        if (config.contains("hash")) {
            hash = config.getString("hash");
        } else {
            hash = config.getString("encryption");
        }

        try {
            MessageDigest.getInstance(hash);
        } catch (@SuppressWarnings("unused") NoSuchAlgorithmException e) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "PasswordProtect can't use this hash! FATAL!");
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Falling back to SHA-512!");
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Report this IMMEDIATELY!");
            hash = "SHA-512";
            config.set("hash", "SHA-512");
        }
        config.options().copyDefaults(true);
        saveConfig();
    }

    private void loadLocalization() {
        getLocalization().options().header("The underscores are used for the different lines!");
        getLocalization().addDefault("permission_denied", "&4You don't have the permission to do this!");
        getLocalization().addDefault("enter_password",
                "&eThis server is password-protected\n&eEnter the password with &a/login &4<password> &eto play");
        getLocalization().addDefault("set_password",
                "&ePasswordProtect is enabled but no password has been set\n&eUse &a/setpassword &4<password> &eto set it");
        getLocalization().addDefault("password_accepted", "&aServer password accepted, you can now play");
        getLocalization().addDefault("attempts_left_kick", "&4Server password incorrect! &e%attempts &4attempts left until kick...");
        getLocalization().addDefault("attempts_left_ban", "&4Server password incorrect! &e%attempts &4attempts left until ban...");
        getLocalization().addDefault("kick_message", "&4Kicked by &ePasswordProtect &4for too many wrong attempts...");
        getLocalization().addDefault("ban_message", "&4Banned by &ePasswordProtect &4for too many wrong attempts...");
        getLocalization().addDefault("kick_broadcast", "&e%player &4kicked by &ePasswordProtect &4for too many wrong attempts...");
        getLocalization().addDefault("ban_broadcast", "&e%player &4banned by &ePasswordProtect &4for too many wrong attempts...");
        getLocalization().addDefault("radius_not_number", "&4The radius was not a number! Using standard (4) instead!");
        getLocalization().addDefault("jail_set", "&aJail location set");
        getLocalization().addDefault("password_set", "&aServer password set!");
        getLocalization().addDefault("only_ingame", "&4The command can only be used ingame!");
        getLocalization().addDefault("config_invalid", "&4It seems like this server config invalid. Please re-set the password!");
        getLocalization().addDefault("only_hashed", "&4Server password is only stored hashed...");
        getLocalization().addDefault("password_not_set", "&eServer password is not set. Use /setpassword <password>");
        getLocalization().addDefault("password", "&eServer password is &4%password");
        getLocalization().addDefault("set_jail_area",
                "&eYou can set the jail area by going somewhere and using &a/setjaillocation &4[radius]");
        getLocalization().addDefault("already_logged_in", "&eYou are already logged in!");
        getLocalization().addDefault("no_login_console", "&eThe console can't login into the server!");
        getLocalization().options().copyDefaults(true);
        saveLocalization();
    }

    private void saveJails() {
        try {
            jails.save(jailFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save the jails.yml!");
            e.printStackTrace();
        }
    }

    private void saveLocalization() {
        try {
            getLocalization().save(localizationFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save the localization.yml");
            e.printStackTrace();
        }
    }

    private void copy(String yml, File file) {
        try (OutputStream out = new FileOutputStream(file); InputStream in = getResource(yml)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            getLogger().warning("Failed to copy the default config!");
            e.printStackTrace();
        }
    }

    public void message(CommandSender sender, String message, String value) {
        PluginDescriptionFile pdfFile = this.getDescription();
        if (message != null) {
            String valueToSend = value;
            if (value == null) {
                valueToSend = "";
            }
            String replacedMessage = message.replace("%attempts", valueToSend).replace("%password", valueToSend).replace("%version",
                    pdfFile.getVersion());
            String[] messageToSend = ChatColor.translateAlternateColorCodes('&', replacedMessage).split("\n");
            if (sender != null) {
                sender.sendMessage(messageToSend);
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Somehow this message is not defined. Please check your localization.yml");
        }
    }

    public void check(Player player) {
        if (!passwordSet()) {
            if (player.hasPermission("passwordprotect.setpassword")) {
                String messageLocalization = getLocalization().getString("set_password");
                message(player, messageLocalization, null);
            }
        } else if (!player.hasPermission("passwordprotect.nopassword") && player.isOp() && config.getBoolean("opsRequirePassword", true)
                || !player.isOp()) {
            if (config.getBoolean("teleportBack", true) && !getPlayerLocations().containsKey(player.getUniqueId())) {
                getPlayerLocations().put(player.getUniqueId(), player.getLocation());
            }

            if (!config.getBoolean("disableJailArea", false)) {
                sendToJail(player);
            }

            if (!getJailedPlayers().containsKey(player.getUniqueId())) {
                getJailedPlayers().put(player.getUniqueId(), 1);
            }
            if (config.getBoolean("prevent.Flying", true)) {
                player.setAllowFlight(false);
            }
            if (config.getBoolean("darkness", true)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 86400, 15));
            }
            if (config.getBoolean("slowness", true)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 86400, 5));
            }
        }
    }

    public void stayInJail(Player player) {
        if (config.getBoolean("disableJailArea", false)) {
            return;
        }
        JailLocation jailLocation = getJailLocation(player);
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
        JailLocation jailLocation = getJailLocation(player);
        player.teleport(jailLocation);
        sendPasswordRequiredMessage(player);
    }

    public void sendPasswordRequiredMessage(Player player) {
        if (config.getBoolean("loginMessage", true)) {
            String messageLocalization = getLocalization().getString("enter_password");
            message(player, messageLocalization, null);
        }
    }

    public void setJailLocation(World world, JailLocation location) {
        jailLocations.put(world, location);
        ArrayList<Double> data = new ArrayList<>();
        data.add(location.getX());
        data.add(location.getY());
        data.add(location.getZ());
        data.add(Double.valueOf(location.getYaw()));
        data.add(Double.valueOf(location.getPitch()));
        data.add(Double.valueOf(location.getRadius()));
        String worldName = world.getName();
        jails.set(worldName + ".jailLocation", data);
        saveJails();
    }

    public JailLocation getJailLocation(Player player) {
        World world = player.getWorld();
        JailLocation jailLocation;

        if (jailLocations.containsKey(world)) {
            jailLocation = jailLocations.get(world);
        } else {
            jailLocation = loadJailLocation(world);
            Location spawnLocation = world.getSpawnLocation();
            if (jailLocation == null) {
                jailLocation = new JailLocation(world, spawnLocation.getX(),
                        spawnLocation.getWorld().getHighestBlockYAt(spawnLocation.getBlockX(), spawnLocation.getBlockZ()) + 1,
                        spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch(), 4);
                jailLocations.put(world, jailLocation);
                setJailLocation(world, jailLocation);
            }
        }
        return jailLocation;
    }

    private JailLocation loadJailLocation(World world) {
        String worldName = world.getName();
        List<Double> data = jails.getDoubleList(worldName + ".jailLocation");
        if (data == null || data.size() != 6) { // [x, y, z, yaw, pitch, radius]
            return null;
        }
        double x = data.get(0);
        double y = data.get(1);
        double z = data.get(2);
        float yaw = data.get(3).floatValue();
        float pitch = data.get(4).floatValue();
        int radius = data.get(5).intValue();

        JailLocation jailLocation = new JailLocation(world, x, y, z, yaw, pitch, radius);
        return jailLocation;
    }

    public void setPassword(String password) {
        String encryptedPassword = hash(password);
        config.set("password", encryptedPassword);
        if (config.getBoolean("cleanPassword", false)) {
            config.set("passwordClean", password);
        }
        saveConfig();
    }

    public String getCleanPassword() {
        if (config.getBoolean("cleanPassword", false)) {
            return config.getString("passwordClean");
        }
        return null;
    }

    public String getPassword() {
        return config.getString("password");
    }

    public boolean passwordSet() {
        return !config.getString("password").isEmpty();
    }

    public String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(hash);
            md.update(password.getBytes(Charset.defaultCharset()));
            byte[] byteData = md.digest();
            return String.format("%0" + (byteData.length << 1) + "x", new BigInteger(1, byteData));
        } catch (@SuppressWarnings("unused") NoSuchAlgorithmException e) {
            getServer().getLogger().severe("The algorithm is NOT known: " + hash);
            return null;
        }
    }
}
