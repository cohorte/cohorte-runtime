/**
 * File:   IBundleRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.commons;

import java.io.File;
import java.net.URI;

/**
 * Description of a bundle reference
 * 
 * @author Thomas Calmant
 */
public interface IBundleRef {

    /**
     * Retrieves the bundle file
     * 
     * @return The bundle file
     */
    File getFile();

    /**
     * Retrieves the bundle name
     * 
     * @return The bundle name
     */
    String getName();

    /**
     * Retrieves the bundle URI
     * 
     * @return The bundle URI
     */
    URI getUri();
}
