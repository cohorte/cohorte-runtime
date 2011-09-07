/**
 * File:   IBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.isolates.base.bundles;

import java.io.File;

/**
 * Represents a bundle file finder service
 * 
 * @author Thomas Calmant
 */
public interface IBundleFinderSvc {

    /** The bootstrap main class */
    String BOOTSTRAP_MAIN_CLASS = "org.psem2m.utilities.bootstrap.Main";

    /** The bootstrap symbolic name */
    String BOOTSTRAP_SYMBOLIC_NAME = "org.psem2m.utilities.bootstrap";

    /**
     * Searches for a bundle according to the given possible names. It looks in
     * platform repositories, then in the local directory if needed.
     * 
     * @param aPossibleNames
     *            Possible file names for the bundle
     * @return A file reference to the bundle, null if not found
     */
    BundleRef findBundle(String... aBundlePossibleNames);

    /**
     * Finds the bootstrap JAR file using its symbolic name (internal constant).
     * 
     * @return The bootstrap JAR file, null if not found.
     */
    File getBootstrap();
}
