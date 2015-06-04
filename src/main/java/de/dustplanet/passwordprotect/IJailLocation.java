package de.dustplanet.passwordprotect;

public interface IJailLocation {

    /**
     * Returns current radius for the JailLocation.
     * @return the radius
     */
    public abstract int getRadius();

    /**
     * Sets an updated radius of the JailLocation.
     * @param radius new radius
     */
    public abstract void setRadius(int radius);
}
