/**
 * File:   IPlatformConfiguration.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.commons;

import java.util.List;

/**
 * Describes the platform configuration
 * 
 * @author Thomas Calmant
 */
public interface IPlatformConfiguration {

    /**
     * Adds the given bundle to the ones needed by all isolates
     * 
     * @param aBundle
     *            New common bundle
     */
    void addCommonBundle(final String aBundle);

    /**
     * Retrieves bundles needed by all isolates
     * 
     * @return bundles needed by all isolates
     */
    String[] getCommonBundles();

    /**
     * Retrieves references to the bundles needed by all isolates
     * 
     * @return references to bundles needed by all isolates
     */
    IBundleRef[] getCommonBundlesRef();

    /**
     * Retrieves the command line to start the forker script using
     * {@link ProcessBuilder}. The command is ready to be used for the host
     * operating system.
     * 
     * @return The forker start script command
     */
    List<String> getForkerStartCommand();

    /**
     * Retrieves the root directory where the platform runs the isolates.
     * 
     * @return The isolates working directories root
     */
    String getIsolatesDirectory();

    /**
     * Retrieves the java interpreter path, based on java.home property
     * 
     * @return The path to the java interpreter
     */
    String getJavaExecutable();

    /**
     * Retrieves the platform base directory
     * 
     * @return The platform base directory
     */
    String getPlatformBaseDirectory();

    /**
     * Retrieves the platform home directory
     * 
     * @return The platform home directory
     */
    String getPlatformHomeDirectory();

    /**
     * Retrieves the <b>full</b> path of the platform bundles repository.
     * 
     * @return The full path of the platform bundles repository.
     */
    String getRepositoryDirectory();

    /**
     * Removes the given bundle from the ones needed by all isolates
     * 
     * @param aBundle
     *            Removed common bundle
     */
    void removeCommonBundle(final String aBundle);
}
