/**
 * File:   IBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.isolates.base.bundles;

/**
 * Represents a bundle file finder service
 * 
 * @author Thomas Calmant
 */
public interface IBundleFinderSvc {

    /**
     * Searches for a bundle according to the given possible names. It looks in
     * platform repositories, then in the local directory if needed.
     * 
     * @param aPossibleNames
     *            Possible file names for the bundle
     * @return A file reference to the bundle, null if not found
     */
    BundleRef findBundle(String... aBundlePossibleNames);
}
