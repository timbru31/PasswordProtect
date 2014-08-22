package de.dustplanet.passwordprotect;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * PasswordProtect for CraftBukkit/Bukkit.
 * The JailLocation is a square around a specific location provided by the exact
 * location or coordinates. The radius is the value after which the player
 * should be teleported back into the middle of the JailLocation
 *
 * Refer to the dev.bukkit.org page:
 * http://dev.bukkit.org/bukkit-plugins/passwordprotect/
 *
 * @author xGhOsTkiLLeRx
 * thanks to brianewing alias DisabledHamster for the original plugin!
 *
 */

public class JailLocation extends Location {
    /**
     * Radius used.
     */
    private int radius;

    /**
     * Creates a new JailLocation.
     * @param world where the JailLocation exists
     * @param x x-coordinate of the center
     * @param y y-coordinate of the center
     * @param z z-coordinate of the center
     * @param yaw yaw of the center
     * @param pitch pitch of the center
     * @param radius radius of the JailLocation
     */
    public JailLocation(World world, double x, double y, double z, float yaw, float pitch, int radius) {
        super(world, x, y, z, yaw, pitch);
        this.radius = radius;
    }

    /**
     * Creates a new JailLocation.
     * @param location given location as the center
     * @param radius radius of the JailLocation
     */
    public JailLocation(Location location, int radius) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.radius = radius;
    }

    /**
     * Returns current radius for the JailLocation.
     * @return
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Sets an updated radius of the JailLocation.
     * @param radius new radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    /**
     * Tests if two JailLocation are equal.
     */
    public boolean equals(Object obj) {
        return super.equals(super.clone());
    }

    @Override
    /**
     * Stringifies the JailLocation.
     */
    public String toString() {
        return super.toString() + "; Radius{radius = " + this.radius + "}";
    }
}
