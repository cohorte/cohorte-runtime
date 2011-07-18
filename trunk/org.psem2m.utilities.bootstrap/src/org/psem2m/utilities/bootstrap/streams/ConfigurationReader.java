/**
 * 
 */
package org.psem2m.utilities.bootstrap.streams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the bootstrap configuration from the given input stream
 * 
 * @author Thomas Calmant
 */
public class ConfigurationReader {

    /**
     * Converts the given strings array into an URL array. Uses the file
     * protocol for malformed URLs.
     * 
     * @param aStringArray
     *            Strings to be converted
     * @return URL array corresponding to the strings
     */
    public static URL[] stringsToURLs(final String[] aStringArray) {

        List<URL> result = new ArrayList<URL>(aStringArray.length);

        for (String value : aStringArray) {

            URL valueUrl = null;
            try {
                // Try a direct conversion
                valueUrl = new URL(value);

            } catch (MalformedURLException e) {
                // Try using the file protocol
                File file = new File(value);

                try {
                    if (file.exists()) {
                        valueUrl = file.toURI().toURL();
                    }

                } catch (MalformedURLException ex) {
                    // Abandon this string
                }
            }

            if (valueUrl != null) {
                result.add(valueUrl);
            }
        }

        return result.toArray(new URL[0]);
    }

    /** Configuration input stream */
    private InputStream pInputStream;

    /**
     * Prepares to read the given input stream
     * 
     * @param aInputStream
     *            The configuration input stream
     */
    public ConfigurationReader(final InputStream aInputStream) {
        pInputStream = aInputStream;
    }

    /**
     * Reads the first serialized object from the input stream. Return null if
     * it wasn't an array of URL.
     * 
     * @return The deserialized URL array, null on error.
     */
    public URL[] readSerializedConfiguration() {

        Object readData = null;

        try {
            // Try to read some data
            ObjectInputStream objectStream = new ObjectInputStream(pInputStream);
            readData = objectStream.readObject();
            objectStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Try to understand what we read (instanceof is false if null)

        if (readData instanceof URL[]) {
            // Direct array
            return (URL[]) readData;

        } else if (readData instanceof String[]) {
            // String to URL conversion needed
            return stringsToURLs((String[]) readData);
        }

        // Unknown format
        return null;
    }

    /**
     * Read lines, removing duplications, from the standard input until the
     * first empty line.
     * 
     * @return The read lines content
     */
    public String[] readStringLines() {

        // Use a set to avoid duplications
        Set<String> linesSet = new HashSet<String>();

        // Read from standard input
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                pInputStream));

        // One bundle URL per line
        try {
            String readLine = reader.readLine();

            while (!readLine.isEmpty()) {
                // Transform the line into an URL
                linesSet.add(readLine);

                // Next step
                readLine = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return linesSet.toArray(new String[0]);
    }

    /**
     * Utility method that calls {@link #readStringLines()} and converts its
     * result into an array of URLs with {@link #stringsToURLs(String[])}.
     * 
     * @return An array of URLs.
     */
    public URL[] readURLLines() {
        return stringsToURLs(readStringLines());
    }
}
