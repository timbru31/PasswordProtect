package de.dustplanet.passwordprotect;

import org.bukkit.Location;
import org.bukkit.World;

import lombok.Getter;
import lombok.Setter;

/**
 * The JailLocation is a square around a specific location provided by the exact location or coordinates. The radius is the value after
 * which the player should be teleported back into the middle of the JailLocation Refer to the dev.bukkit.org page:
 * https://dev.bukkit.org/projects/passwordprotect
 *
 * @author xGhOsTkiLLeRx thanks to brianewing alias DisabledHamster for the original plugin!
 */

public class JailLocation extends Location implements IJailLocation {
    /**
     * Radius used.
     */
    @Getter
    @Setter
    private int radius;

    /**
     * Creates a new JailLocation.
     *
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
     *
     * @param location given location as the center
     * @param radius radius of the JailLocation
     */
    public JailLocation(Location location, int radius) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.radius = radius;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + radius;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JailLocation other = (JailLocation) obj;
        if (this.radius != other.radius) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString() + "; Radius{radius = " + this.radius + "}";
    }
}
