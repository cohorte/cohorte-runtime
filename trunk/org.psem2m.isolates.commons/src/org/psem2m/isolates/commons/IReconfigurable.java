/**
 * File:   IReconfigurable.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons;

/**
 * Description of an object that can reload its configuration while running
 */
public interface IReconfigurable {

    /**
     * Asks the object to reload its configuration.
     * 
     * @param aPath
     *            Configuration file path
     * @param aForce
     *            Force dangerous actions (kill elements no more in the
     *            configuration)
     */
    void reloadConfiguration(final String aPath, final boolean aForce);
}
