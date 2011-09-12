/**
 * File:   Utilities.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.base;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Various utility methods
 * 
 * @author Thomas Calmant
 */
public final class Utilities {

    /**
     * Tries to load the given class by looking into all available bundles.
     * 
     * @param aBundles
     *            An array containing all bundles to search into
     * @param aClassName
     *            Name of the class to load
     * @return The searched class, null if not found
     */
    public static Class<?> findClassInBundles(final Bundle[] aBundles,
	    final String aClassName) {

	if (aBundles == null) {
	    return null;
	}

	for (Bundle bundle : aBundles) {

	    // Only work with RESOLVED and ACTIVE bundles
	    int bundleState = bundle.getState();
	    if (bundleState == Bundle.ACTIVE || bundleState == Bundle.RESOLVED) {
		try {
		    return bundle.loadClass(aClassName);

		} catch (ClassNotFoundException e) {
		    // Class not found, try next bundle...
		}
	    }
	}

	return null;
    }

    /**
     * The retrieves the URL of the Jar file containing the given class
     * 
     * @param aClass
     *            Class to look for
     * @return The Jar file containing the class
     */
    public static URL findClassJar(final Class<?> aClass) {

	return aClass.getResource('/' + aClass.getName().replace('.', '/')
		+ ".class");
    }

    /**
     * Retrieves the service properties as a map
     * 
     * @param aServiceReference
     *            A reference to the service
     * @return The service properties
     */
    public static Map<String, String> getServiceProperties(
	    final ServiceReference aServiceReference) {

	Map<String, String> serviceProperties = new HashMap<String, String>();

	String[] propertyKeys = aServiceReference.getPropertyKeys();
	for (String key : propertyKeys) {
	    serviceProperties.put(key,
		    String.valueOf(aServiceReference.getProperty(key)));
	}

	return serviceProperties;
    }

    /**
     * Objects joining operation, using the {@link String#valueOf(Object)}
     * method.
     * 
     * @param aJoinSequence
     *            Sequence between strings
     * @param aJoinedObjects
     *            Objects to be joined
     * @return Joined strings
     */
    public static String join(final String aJoinSequence,
	    final Object[] aJoinedObjects) {

	StringBuilder builder = new StringBuilder();
	boolean first = true;

	for (Object object : aJoinedObjects) {

	    if (!first) {
		builder.append(aJoinSequence);
	    } else {
		first = false;
	    }

	    builder.append(String.valueOf(object));
	}

	return builder.toString();
    }

    /**
     * String joining operation, like in Python str.join().
     * 
     * @param aJoinSequence
     *            Sequence between strings
     * @param aJoinedStrings
     *            Objects to be joined
     * @return Joined strings
     */
    public static String join(final String aJoinSequence,
	    final String... aJoinedStrings) {

	StringBuilder builder = new StringBuilder();
	boolean first = true;

	for (String string : aJoinedStrings) {

	    if (!first) {
		builder.append(aJoinSequence);
	    } else {
		first = false;
	    }

	    builder.append(string);
	}

	return builder.toString();
    }

    /**
     * Returns a valid directory from the given path (it may return the parent
     * directory).
     * 
     * @param aPath
     *            base directory path
     * @return A valid directory path
     * 
     * @throws IOException
     *             The directory already exists or can't be created
     */
    public static File makeDirectory(final CharSequence aPath)
	    throws IOException {

	File directory = new File(aPath.toString());

	if (!directory.exists()) {
	    // Create directory if needed
	    if (!directory.mkdirs()) {
		throw new IOException(
			"Directory not created. Already existing ?");
	    }

	} else if (!directory.isDirectory()) {
	    // A node already has this name
	    throw new IOException("'" + aPath + "' is not a valid directory.");
	}

	return directory;
    }

    /**
     * Hide the constructor of a utility class
     */
    private Utilities() {
	// Hide constructor
    }
}
