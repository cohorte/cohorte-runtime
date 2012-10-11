/**
 * File:   Utilities.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * Various utility methods
 * 
 * @author Thomas Calmant
 */
public final class Utilities {

    /**
     * Converts the given object to a one-dimension array of the given type
     * 
     * @param aArrayObject
     *            An array object
     * @param aTypeArray
     *            An object to determine the kind of result.
     * @return The converted array
     * @throws ClassCastException
     *             Error casting an entry
     * @throws IllegalArgumentException
     *             Invalid array object or result type
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayObjectToArray(final Object aArrayObject,
            final Class<? extends T> aType) throws ClassCastException {

        if (aArrayObject == null) {
            // Nothing to do
            return null;
        }

        if (aType == null) {
            // Invalid type
            throw new IllegalArgumentException("Can't convert to a null type");
        }

        if (aArrayObject.getClass().isArray()) {
            // We got an array
            final int size = Array.getLength(aArrayObject);
            final T[] resultArray = (T[]) Array.newInstance(aType, size);

            // Copy the array
            for (int i = 0; i < size; i++) {
                resultArray[i] = (T) Array.get(aArrayObject, i);
            }
            return resultArray;

        } else if (aArrayObject instanceof Collection) {
            // We got a collection instead of an array, don't worry
            try {
                final Collection<T> collection = (Collection<T>) aArrayObject;
                return collection.toArray((T[]) Array.newInstance(aType,
                        collection.size()));

            } catch (final ArrayStoreException ex) {
                // Convert the exception class
                throw new ClassCastException(MessageFormat.format(
                        "Invalid array object content type: {0}", ex));
            }
        }

        // Neither an array nor a collection
        throw new IllegalArgumentException(MessageFormat.format(
                "Given object is not an array: {0}", aArrayObject.getClass()
                        .getName()));
    }

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
     * Tries to convert the given array or collection into an array of the given
     * type.
     * 
     * If the conversion can't be done, the method returns null.
     * 
     * @param aObject
     *            An array or a collection to be converted
     * @param aRequestedClass
     *            The requested result array content type
     * @return The typed array, null if the conversion is impossible
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] getArray(final Object aObject,
            final Class<T> aRequestedClass) {

        if (aObject == null || aRequestedClass == null) {
            // Bad arguments
            return null;
        }

        final Class<?> objClass = aObject.getClass();
        if (objClass.isArray()) {
            // Got an array
            if (aRequestedClass.isAssignableFrom(objClass.getComponentType())) {
                // Direct match
                return (T[]) aObject;

            } else {
                // Try a conversion
                final Object[] array = (Object[]) aObject;
                final T[] resultArray = (T[]) Array.newInstance(
                        aRequestedClass, array.length);
                int i = 0;

                for (final Object element : array) {

                    if (element == null) {
                        // Null object, nothing to do
                        resultArray[i++] = null;

                    } else if (aRequestedClass.isAssignableFrom(element
                            .getClass())) {
                        // Compatible type
                        resultArray[i++] = (T) element;

                    } else {
                        // Invalid type, abandon
                        return null;
                    }
                }

                return resultArray;
            }

        } else if (aObject instanceof Collection) {
            // Got a collection, let Java do the job
            try {
                return ((Collection<?>) aObject).toArray((T[]) Array
                        .newInstance(aRequestedClass, 0));

            } catch (final ArrayStoreException e) {
                // Invalid content type
                return null;
            }
        }

        // Can't convert
        return null;
    }

    /**
     * Computes the current host name, by using the system properties or socket
     * information
     */
    public static String getHostName() {

        // Try with system property
        String hostName = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_HOST_NAME);

        if (hostName == null || hostName.trim().isEmpty()) {
            try {
                // Try with local host name
                hostName = InetAddress.getLocalHost().getHostName();

            } catch (final UnknownHostException e) {

                hostName = null;
            }
        }

        if (hostName == null || hostName.trim().isEmpty()) {
            // Still null : use the standard local host name
            return "localhost";
        }

        return hostName;
    }

    /**
     * Tries to convert the given array or collection into a list of the given
     * type.
     * 
     * If the conversion can't be done, the method returns null.
     * 
     * @param aObject
     *            An array or a collection to be converted
     * @param aRequestedClass
     *            The requested result list content type
     * @return The type-validated list, null if the conversion is impossible
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(final Object aObject,
            final Class<T> aContentType) {

        if (aObject == null || aContentType == null) {
            // Invalid args
            return null;
        }

        final Class<?> objectClass = aObject.getClass();
        if (objectClass.isArray()) {
            // Convert to an array
            final Object[] array = (Object[]) aObject;
            final List<T> result = new ArrayList<T>(array.length);

            for (final Object element : array) {
                if (element == null) {
                    // Nothing to do
                    result.add(null);

                } else if (aContentType.isAssignableFrom(element.getClass())) {
                    // Compatible type
                    result.add((T) element);

                } else {
                    // Type error, abandon
                    return null;
                }
            }

        } else if (aObject instanceof Collection) {
            // Collection
            final Collection<?> collection = (Collection<?>) aObject;

            // Type validation loop
            for (final Object element : collection) {
                if (element != null
                        && !aContentType.isAssignableFrom(element.getClass())) {
                    // Found an incompatible element, abandon
                    return null;
                }
            }

            if (aObject instanceof List) {
                // No container conversion needed
                return (List<T>) aObject;

            } else {
                // The result must be a list
                return new ArrayList<T>((Collection<T>) aObject);
            }
        }

        // Can't work
        return null;
    }

    /**
     * Retrieves the service properties as a map
     * 
     * @param aServiceReference
     *            A reference to the service
     * @return The service properties
     */
    public static Map<String, Object> getServiceProperties(
            final ServiceReference<?> aServiceReference) {

        final Map<String, Object> serviceProperties = new HashMap<String, Object>();

        final String[] propertyKeys = aServiceReference.getPropertyKeys();
        for (final String key : propertyKeys) {
            serviceProperties.put(key, aServiceReference.getProperty(key));
        }

        return serviceProperties;
    }

    /**
     * Converts an input stream into a byte array
     * 
     * @param aInputStream
     *            An input stream
     * @return The input stream content, null on error
     * @throws IOException
     *             Something went wrong
     */
    public static byte[] inputStreamToBytes(final InputStream aInputStream)
            throws IOException {

        if (aInputStream == null) {
            return null;
        }

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];
        int read = 0;

        do {
            read = aInputStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }

        } while (read > 0);

        outStream.close();
        return outStream.toByteArray();
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
     * Deletes the given directory recursively
     * 
     * @param aDirectory
     *            Directory to delete
     */
    public static void removeDirectory(final File aDirectory) {

        if (!aDirectory.isDirectory()) {
            // Not a directory...
            return;
        }

        for (final File subDir : aDirectory.listFiles()) {

            if (subDir.isDirectory()) {
                // Recursive call
                removeDirectory(subDir);

            } else {
                // Delete the file
                subDir.delete();
            }
        }

        // Delete the directory
        aDirectory.delete();
    }

    /**
     * Hide the constructor of a utility class
     */
    private Utilities() {

        // Hide constructor
    }
}
