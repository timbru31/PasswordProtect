package de.xghostkillerx.passwordprotect;

import org.bukkit.Location;
import org.bukkit.World;

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
}
