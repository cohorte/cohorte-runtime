/**
 * 
 */
package org.psem2m.utilities.bootstrap.streams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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

    /** Bundle finder */
    private FileFinder pFileFinder;

    /** Configuration input stream */
    private InputStream pInputStream;

    /** Message sender (mainly for errors) */
    private IMessageSender pMessageSender;

    /**
     * Prepares to read the given input stream
     * 
     * @param aInputStream
     *            The configuration input stream
     * @param aMessageSender
     *            Log message sender
     */
    public ConfigurationReader(final InputStream aInputStream,
	    final IMessageSender aMessageSender) {

	pInputStream = aInputStream;
	pMessageSender = aMessageSender;
	pFileFinder = new FileFinder(pMessageSender);
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

	    File file = pFileFinder.find(fileName);
	    if (file != null) {
		return new FileInputStream(file);
	    }

	    return null;

	} catch (FileNotFoundException e) {
	    return null;
	}
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
     * Read lines, removing duplications, from the input given in the
     * constructor until the first empty line.
     * 
     * @return The read lines content
     */
    public String[] readStringLines() {
	return readStringLines(pInputStream);
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
	Set<String> linesSet = new LinkedHashSet<String>();

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
     * Utility method that calls {@link #readStringLines()} and converts its
     * result into an array of URLs with {@link #stringsToURLs(String[])}.
     * 
     * @return An array of URLs.
     */
    public URL[] readURLLines() {
	return stringsToURLs(readStringLines());
    }

    /**
     * Converts the given strings array into an URL array. Uses the file
     * protocol for malformed URLs.
     * 
     * @param aStringArray
     *            Strings to be converted
     * @return URL array corresponding to the strings
     */
    public URL[] stringsToURLs(final String[] aStringArray) {

	List<URL> result = new ArrayList<URL>(aStringArray.length);

	for (String value : aStringArray) {
	    URL valueUrl = null;
	    try {
		// Try a direct conversion
		valueUrl = new URL(value);

	    } catch (MalformedURLException e) {
		// Try using the file protocol
		File file = pFileFinder.find(value);

		if (file != null) {
		    try {
			// Accept the URL in any case at this time
			// File existence will be tested later
			valueUrl = file.toURI().toURL();

		    } catch (MalformedURLException ex) {
			// Abandon this string
		    }
		}
	    }

	    if (valueUrl != null) {
		result.add(valueUrl);
	    }
	}

	return result.toArray(new URL[0]);
    }
}
