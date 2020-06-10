package de.dustplanet.passwordprotect.jail;

/**
 * Interface for the JailLocation.
 *
 * @author timbru31
 */
public interface IJailLocation {

    /**
     * Returns current radius for the JailLocation.
     *
     * @return the radius
     */
    int getRadius();

    /**
     * Sets an updated radius of the JailLocation.
     *
     * @param radius new radius
     */
    void setRadius(int radius);
}
