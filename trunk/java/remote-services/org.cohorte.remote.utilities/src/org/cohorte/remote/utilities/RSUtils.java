/**
 * File:   RSUtils.java
 * Author: Thomas Calmant
 * Date:   28 juin 2013
 */
package org.cohorte.remote.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.osgi.framework.BundleContext;

/**
 * Utility methods for Remote Services
 * 
 * @author Thomas Calmant
 */
public class RSUtils {

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
     * Retrieves the UID found in the framework or the system properties. If no
     * value is found, a new UID is generated and stored in the system
     * properties.
     * 
     * @param aBundleContext
     *            The bundle context, to look into the framework properties
     * @param aPropertyKey
     *            The name of the property containing the UID
     * @return The found or generated UID
     */
    public static String setupUID(final BundleContext aBundleContext,
            final String aPropertyKey) {

        // Try with the framework properties
        String uid = null;
        if (aBundleContext != null) {
            uid = aBundleContext.getProperty(aPropertyKey);
        }

        if (uid == null || uid.isEmpty()) {
            // Try with the system properties
            System.getProperty(aPropertyKey);
        }

        if (uid == null || uid.isEmpty()) {
            // No UID found, generate one
            uid = UUID.randomUUID().toString();

            // Store it
            System.setProperty(aPropertyKey, uid);
        }

        return uid;
    }

    /**
     * Hidden constructor (this is a utility class)
     */
    private RSUtils() {

        // Hidden constructor
    }
}
