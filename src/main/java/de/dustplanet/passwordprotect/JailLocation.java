package de.dustplanet.passwordprotect;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * PasswordProtect for CraftBukkit/Bukkit
 * Custom JailLocation
 * 
 * 
 * Refer to the forum thread: http://bit.ly/ppbukkit
 * 
 * Refer to the dev.bukkit.org page:
 * http://bit.ly/ppbukktidev
 * 
 * @author xGhOsTkiLLeRx
 * @thanks to brianewing alias DisabledHamster for the original plugin!
 * 
 */

/*
 * 
 * The JailLocation is a square around a specific location provided by the exact
 * location or coordinates. The radius is the value after which the player
 * should be teleported back into the middle of the JailLocation
 */

public class JailLocation extends Location {
    private int radius;

    public JailLocation(World world, double x, double y, double z, float yaw, float pitch, int radius) {
	super(world, x, y, z, yaw, pitch);
	this.radius = radius;
    }

    public JailLocation(Location location, int radius) {
	super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	this.radius = radius;
    }

    public int getRadius() {
	return radius;
    }
    
    @Override
    public boolean equals(Object obj) {
	return super.equals(super.clone());
    }
}
