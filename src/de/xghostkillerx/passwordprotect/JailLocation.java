package de.xghostkillerx.passwordprotect;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * PasswordProtect for CraftBukkit/Bukkit
 * Custom JailLocation
 * 
 * 
 * Refer to the forum thread:
 * http://bit.ly/ppbukkit
 * Refer to the dev.bukkit.org page:
 * http://bit.ly/ppbukktidev
 *
 * @author xGhOsTkiLLeRx
 * @thanks to brianewing alias DisabledHamster for the original plugin!
 * 
 */

public class JailLocation extends Location {
    private int radius;

    public JailLocation(World world, double x, double y, double z, float yaw, float pitch, int radius) {
        super(world, x, y, z, yaw, pitch);
        this.radius = radius;
    }

    public JailLocation(Location location, int radius) {
    	// location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1 = Highest block for Y at location (x,z), because spawn can be like 65. +1 for head
        super(location.getWorld(), location.getX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1, location.getZ(), location.getYaw(), location.getPitch());
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }
}
