/**
 * File:   BundlesClassLoader.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.remote.client.rose;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.commons.Utilities;

/**
 * "Fake" Class loader, trying to load a class that could be in one of the
 * active/resolved bundles
 * 
 * @author Thomas Calmant
 */
public class BundlesClassLoader extends ClassLoader {

    /** The bundle context */
    private BundleContext pBundleContext;

    /**
     * Prepares the "class loader"
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public BundlesClassLoader(final BundleContext aBundleContext) {

	super();
	pBundleContext = aBundleContext;
    }

    /**
     * Tries to load the given class in the 'local' class loader, then in all
     * bundles.
     * 
     * @param aName
     *            The class to be loaded
     * @return The loaded class
     * @throws ClassNotFoundException
     *             The class could not be found
     * 
     * @see Utilities#findClassInBundles(org.osgi.framework.Bundle[], String)
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(final String aName) throws ClassNotFoundException {

	// Try the "local" class loader first
	try {
	    return Class.forName(aName);
	} catch (ClassNotFoundException ex) {
	    // Ignore it
	}

	// Try the bundles loaders
	Class<?> foundClass = Utilities.findClassInBundles(
		pBundleContext.getBundles(), aName);

	if (foundClass == null) {
	    throw new ClassNotFoundException("Class not found : " + aName);
	}

	return foundClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "BundeLoader(" + super.toString() + ")";
    }
}
