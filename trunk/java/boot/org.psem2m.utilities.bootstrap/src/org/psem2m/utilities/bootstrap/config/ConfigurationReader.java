/**
 * 
 */
package org.psem2m.utilities.bootstrap.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.psem2m.isolates.constants.boot.IBootstrapConstants;
import org.psem2m.utilities.bootstrap.IMessageSender;

/**
 * Reads the bootstrap configuration from the given input stream
 * 
 * @author Thomas Calmant
 */
public class ConfigurationReader {

    /** Comment marker in configuration file */
    public static final String COMMENT_MARKER = "#";

    /** File include command */
    public static final String INCLUDE_COMMAND = "include:";

    /** Configuration file finder */
    private final FileFinder pFileFinder = new FileFinder();

    /** Message sender (mainly for errors) */
    private final IMessageSender pMessageSender;

    /**
     * Prepares to read the configuration file
     * 
     * @param aMessageSender
     *            Log message sender
     */
    public ConfigurationReader(final IMessageSender aMessageSender) {

        pMessageSender = aMessageSender;
    }

    /**
     * Tries to open the file indicated in the include directive. Returns null
     * if the file is not found.
     * 
     * @param aIncludeLine
     *            Complete include directive line
     * @return The file input stream, null on error
     */
    private FileInputStream getIncludedFileStream(final String aIncludeLine) {

        try {
            final String fileName = aIncludeLine.substring(
                    INCLUDE_COMMAND.length()).trim();

            final File file = pFileFinder.findInConfiguration(fileName);
            if (file != null) {
                return new FileInputStream(file);
            }

            return null;

        } catch (final FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Reads and parses the base configuration file
     * 
     * @throws IOException
     *             An error occurred reading this file
     */
    public URL[] readConfiguration() throws IOException {

        // Find bundle file
        final File platformFile = pFileFinder
                .findInConfiguration(IBootstrapConstants.FILE_PLATFORM_BUNDLES);
        if (platformFile == null) {
            throw new IOException(MessageFormat.format(
                    "Can''t find the base file ''{0}''",
                    IBootstrapConstants.FILE_PLATFORM_BUNDLES));
        }

        // Parse them all
        final String[] platformBundles = readStringLines(new FileInputStream(
                platformFile));

        // Linked set, to avoid duplication and keep order
        final Set<URL> bundlesUrls = new LinkedHashSet<URL>();
        bundlesUrls.addAll(stringsToURLs(platformBundles));
        return bundlesUrls.toArray(new URL[0]);
    }

    /**
     * Read lines, removing duplications, from the given input until the first
     * empty line.
     * 
     * @param aInputStream
     *            The input stream to be used
     * 
     * @return The read lines content
     */
    private String[] readStringLines(final InputStream aInputStream) {

        // Use a set to avoid duplications
        final Set<String> linesSet = new LinkedHashSet<String>();

        // Read from standard input
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                aInputStream));

        // One bundle URL per line
        try {
            String readLine = reader.readLine();

            // readLine can be null if the end of file is reached
            while (readLine != null) {

                // Trim the line
                readLine = readLine.trim();

                if (!(readLine.isEmpty() || readLine.startsWith(COMMENT_MARKER))) {
                    // Non-commented, non-empty line
                    if (readLine.startsWith(INCLUDE_COMMAND)) {
                        // File include
                        final InputStream includedStream = getIncludedFileStream(readLine);

                        if (includedStream != null) {
                            linesSet.addAll(Arrays
                                    .asList(readStringLines(includedStream)));

                        } else {
                            // Error opening file
                            final String fileName = readLine
                                    .substring(INCLUDE_COMMAND.length());
                            pMessageSender.sendMessage(Level.WARNING,
                                    "ConfigurationReader", "readStringLines",
                                    MessageFormat.format(
                                            "Can''t open file: {0}", fileName));
                        }

                    } else {
                        // Consider the line as an URL
                        linesSet.add(readLine);
                    }
                }

                // Next step
                readLine = reader.readLine();
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }

        return linesSet.toArray(new String[0]);
    }

    /**
     * Converts the given strings array into an URL array. Uses the file
     * protocol for malformed URLs.
     * 
     * @param aStringArray
     *            Strings to be converted
     * @return URL collection corresponding to the strings
     * @throws FileNotFoundException
     *             A bundle file is missing
     */
    public Collection<URL> stringsToURLs(final String[] aStringArray)
            throws FileNotFoundException {

        final List<URL> result = new ArrayList<URL>(aStringArray.length);

        for (final String value : aStringArray) {
            // Try URL parsing
            URL valueUrl = tryParseUrl(value);
            if (valueUrl == null) {
                // Conversion returned nothing
                // Try the file name
                final File file = tryFindFile(value);
                if (file != null) {
                    try {
                        // Accept the URL in any case at this time
                        // File existence will be tested later
                        valueUrl = file.toURI().toURL();

                    } catch (final MalformedURLException ex) {
                        // Abandon this string
                    }

                } else {
                    throw new FileNotFoundException(value);
                }
            }

            if (valueUrl != null) {
                result.add(valueUrl);
            }
        }

        return result;
    }

    /**
     * Tries to find the given bundle file, by file or bundle symbolic name
     * 
     * @param aName
     *            A file name or a symbolic name
     * @return The found file object, or null
     */
    private File tryFindFile(final String aName) {

        // Try the file name
        File file = pFileFinder.findInRepositories(aName);
        if (file == null) {
            // Try the bundle symbolic name
            file = pFileFinder.findBundle(aName);
        }

        return file;
    }

    /**
     * Tries to convert the given URL, returns null on parsing error
     * 
     * @param aUrlString
     *            A URL string
     * @return The URL object or null
     */
    private URL tryParseUrl(final String aUrlString) {

        try {
            // Try a direct conversion
            return new URL(aUrlString);

        } catch (final MalformedURLException e) {
            // Invalid URL
            return null;
        }
    }
}
