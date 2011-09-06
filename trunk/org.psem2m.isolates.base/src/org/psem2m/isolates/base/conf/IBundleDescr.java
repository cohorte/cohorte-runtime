/**
 * File:   IBundleDescr.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.base.conf;

import java.io.Serializable;

/**
 * Describes a bundle
 * 
 * @author Thomas Calmant
 */
public interface IBundleDescr extends Serializable {

    /**
     * Retrieves the file path indicated in the configuration file. Can be null.
     * 
     * @return The bundle file path, or null
     */
    String getFile();

    /**
     * Retrieves the bundle symbolic name. Can't be null.
     * 
     * @return The bundle symbolic name
     */
    String getSymbolicName();

    /**
     * Retrieves the bundle version indicated in the configuration file. Can be
     * null.
     * 
     * @return The bundle version
     */
    String getVersion();

    /**
     * Tests if the bundle is declared optional in the configuration file. If
     * true, the isolate state is valid even if this bundle is not present.
     * 
     * @return True if the bundle is optional
     */
    boolean isOptional();
}
