/**
 * File:   Utilities.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.base;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Various utility methods
 * 
 * @author Thomas Calmant
 */
public final class Utilities {

    /**
     * Returns a list if the given object is an array, else returns the given
     * object.
     * 
     * @param aObject
     *            An object, can be null
     * @return A list if aObject is an array, else aObject
     */
    public static Object arrayToIterable(final Object aObject) {

        if (aObject != null && aObject.getClass().isArray()) {
            // Convert arrays into list
            return Arrays.asList((Object[]) aObject);
        }

        return aObject;
    }

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

        for (final Bundle bundle : aBundles) {

            // Only work with RESOLVED and ACTIVE bundles
            final int bundleState = bundle.getState();
            if (bundleState == Bundle.ACTIVE || bundleState == Bundle.RESOLVED) {
                try {
                    return bundle.loadClass(aClassName);

                } catch (final ClassNotFoundException e) {
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
    public static Map<String, Object> getServiceProperties(
            final ServiceReference aServiceReference) {

        final Map<String, Object> serviceProperties = new HashMap<String, Object>();

        final String[] propertyKeys = aServiceReference.getPropertyKeys();
        for (final String key : propertyKeys) {
            serviceProperties.put(key, aServiceReference.getProperty(key));
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

        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (final Object object : aJoinedObjects) {

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

        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (final String string : aJoinedStrings) {

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

        final File directory = new File(aPath.toString());

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
     * Tests if the given string matches the filter.
     * 
     * The filter is the regular filename filter and not a regular expression.
     * Allowed are *.* or ???.xml, etc.
     * 
     * Found at : <a href="http://blogs.igalia.com/eocanha/?p=67">blogs.igalia
     * .com/eocanha/?p=67</a>
     * 
     * @param aTested
     *            Tested string
     * @param aFilter
     *            Filename filter-like string
     * 
     * @return True if matches and false if either null or no match
     */
    public static boolean matchFilter(final String aTested, final String aFilter) {

        if (aTested == null || aFilter == null) {
            return false;
        }

        final StringBuffer f = new StringBuffer();

        for (final StringTokenizer st = new StringTokenizer(aFilter, "?*", true); st
                .hasMoreTokens();) {
            final String t = st.nextToken();
            if (t.equals("?")) {
                f.append(".");
            } else if (t.equals("*")) {
                f.append(".*");
            } else {
                f.append(Pattern.quote(t));
            }
        }
        return aTested.matches(f.toString());
    }

    /**
     * Hide the constructor of a utility class
     */
    private Utilities() {

        // Hide constructor
    }
}
