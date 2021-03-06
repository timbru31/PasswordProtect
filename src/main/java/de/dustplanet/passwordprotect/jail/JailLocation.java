package de.dustplanet.passwordprotect.jail;

import org.bukkit.Location;
import org.bukkit.World;

import lombok.Getter;
import lombok.Setter;

/**
 * The JailLocation is a square around a specific location provided by the exact location or coordinates. The radius is the value after
 * which the player should be teleported back into the middle of the JailLocation.
 *
 * @author timbru31
 * @author brianewing
 */

@SuppressWarnings("PMD.CommentSize")
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
    @SuppressWarnings("PMD.ShortVariable")
    public JailLocation(final World world, final double x, final double y, final double z, final float yaw, final float pitch,
            final int radius) {
        super(world, x, y, z, yaw, pitch);
        this.radius = radius;
    }

    /**
     * Creates a new JailLocation.
     *
     * @param location given location as the center
     * @param radius radius of the JailLocation
     */
    public JailLocation(final Location location, final int radius) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.radius = radius;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + radius;
    }

    @Override
    public boolean equals(final Object obj) {
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
