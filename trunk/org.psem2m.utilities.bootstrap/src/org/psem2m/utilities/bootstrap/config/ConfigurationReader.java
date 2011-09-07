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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.psem2m.utilities.bootstrap.IBootstrapConstants;
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
    protected FileInputStream getIncludedFileStream(final String aIncludeLine) {

	try {
	    String fileName = aIncludeLine.substring(INCLUDE_COMMAND.length())
		    .trim();

	    File file = pFileFinder.findInConfiguration(fileName);
	    if (file != null) {
		return new FileInputStream(file);
	    }

	    return null;

	} catch (FileNotFoundException e) {
	    return null;
	}
    }

    /**
     * Reads and parses the base configuration files
     * 
     * @throws IOException
     *             An error occurred reading those files
     */
    public URL[] readConfiguration() throws IOException {

	// Find bundle files
	final File osgiFile = pFileFinder
		.findInConfiguration(IBootstrapConstants.FILE_OSGI_BUNDLES);
	if (osgiFile == null) {
	    throw new IOException("Can't find the base file '"
		    + IBootstrapConstants.FILE_OSGI_BUNDLES + "'");
	}

	final File platformFile = pFileFinder
		.findInConfiguration(IBootstrapConstants.FILE_PLATFORM_BUNDLES);
	if (platformFile == null) {
	    throw new IOException("Can't find the base file '"
		    + IBootstrapConstants.FILE_PLATFORM_BUNDLES + "'");
	}

	// Parse them all
	final String[] osgiBundles = readStringLines(new FileInputStream(
		osgiFile));
	final String[] platformBundles = readStringLines(new FileInputStream(
		platformFile));

	// Linked set, to avoid duplication and keep order
	final Set<URL> bundlesUrls = new LinkedHashSet<URL>();
	bundlesUrls.addAll(stringsToURLs(osgiBundles));
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
    protected String[] readStringLines(final InputStream aInputStream) {

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

		// Test the comment marker
		if (readLine.isEmpty() || readLine.startsWith(COMMENT_MARKER)) {
		    // Do nothing (ignore the other commands

		} else if (readLine.startsWith(INCLUDE_COMMAND)) {
		    // File include
		    InputStream includedStream = getIncludedFileStream(readLine);

		    if (includedStream != null) {
			linesSet.addAll(Arrays
				.asList(readStringLines(includedStream)));

		    } else {

			pMessageSender.sendMessage(
				Level.WARNING,
				"ConfigurationReader",
				"readStringLines",
				"Can't open file : "
					+ readLine.substring(INCLUDE_COMMAND
						.length()));
		    }

		} else {
		    // Consider the line as an URL
		    linesSet.add(readLine);
		}

		// Next step
		readLine = reader.readLine();
	    }

	} catch (IOException e) {
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
     */
    public Collection<URL> stringsToURLs(final String[] aStringArray) {

	List<URL> result = new ArrayList<URL>(aStringArray.length);

	for (String value : aStringArray) {
	    URL valueUrl = null;
	    try {
		// Try a direct conversion
		valueUrl = new URL(value);

	    } catch (MalformedURLException e) {
		// Try the file name
		File file = pFileFinder.findInRepositories(value);

		if (file == null) {
		    // Try the symbolic name
		    file = pFileFinder.findBundle(value);
		}

		if (file != null) {
		    try {
			// Accept the URL in any case at this time
			// File existence will be tested later
			valueUrl = file.toURI().toURL();

		    } catch (MalformedURLException ex) {
			// Abandon this string
		    }

		} else {
		    pMessageSender.sendMessage(Level.WARNING,
			    "Configuration Reader", "stringsToURLs",
			    "Bundle not found : " + value);
		}
	    }

	    if (valueUrl != null) {
		result.add(valueUrl);
	    }
	}

	return result;
    }
}
