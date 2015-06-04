package de.dustplanet.passwordprotect;

public interface IJailLocation {

    /**
     * Returns current radius for the JailLocation.
     * @return the radius
     */
    abstract int getRadius();

    /**
     * Sets an updated radius of the JailLocation.
     * @param radius new radius
     */
    abstract void setRadius(int radius);
}
