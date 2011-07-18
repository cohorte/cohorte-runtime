/**
 * 
 */
package org.psem2m.isolates.commons;

import java.io.Serializable;

/**
 * Represents a serializable bundle information. It provides access to the basic
 * informations given by {@link org.osgi.framework.Bundle}.
 * 
 * @author Thomas Calmant
 */
public interface IBundleInfo extends Serializable {

    /**
     * Retrieves the bundle's unique ID
     * 
     * @return The bundle's UID
     */
    long getId();

    /**
     * Retrieves the bundle's location identifier
     * 
     * @return the bundle's location
     */
    String getLocation();

    /**
     * Returns this bundle's current state
     * 
     * @return The bundle's state
     */
    int getState();

    /**
     * Returns the symbolic name of this bundle as specified by its
     * Bundle-SymbolicName manifest header.
     * 
     * @return The bundle's symbolic name
     */
    String getSymbolicName();

    /**
     * Returns the bundle's version string representation
     * 
     * @return The bundle's version
     */
    String getVersion();
}
