/**
 * File:   IBundleDescr.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import java.io.Serializable;
import java.util.Properties;

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
     * Tests if the bundle is declared optional in the configuration file. If
     * true, the isolate state is valid even if this bundle is not present.
     * 
     * @return True if the bundle is optional
     */
    boolean getOptional();

    /**
     * Retrieves the set of properties declared in the bundle description.
     * 
     * @return an instance of Properties else null.
     */
    Properties getProperties();

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
     * Tests if the bundle has a set of properties.
     * 
     * @return true if the bundle description has a set of properties
     */
    boolean hasProperties();
}
