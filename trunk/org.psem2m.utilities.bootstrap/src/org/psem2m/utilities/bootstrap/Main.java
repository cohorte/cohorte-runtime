/**
 * File:   Main.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Bootstrap entry point
 * 
 * @author Thomas Calmant
 */
public class Main {

    /** Ignored characters for normalization. Order matters ! */
    public static final String[] IGNORED_CHARACTERS = { "--", "-D", "-" };

    /** Test command (writes a serialized URL array in a file) */
    public static final String TEST_COMMAND = "--test";

    /**
     * Bootstrap entry point
     * 
     * @param aArgs
     *            Program arguments
     */
    public static void main(final String[] aArgs) {

	// Special test command
	if (aArgs[0].equalsIgnoreCase(TEST_COMMAND)) {
	    writeSerializationTest("./test.dat");
	    return;
	}

	Main program = new Main();
	program.run(aArgs);
    }

    /**
     * Writes a serialized sample file
     * 
     * @param aFileName
     *            Output file name
     */
    protected static void writeSerializationTest(final String aFileName) {

	try {
	    URL[] urls = new URL[] {
		    new File(
			    "../../platforms/felix/org.apache.felix.shell-1.4.2.jar")
			    .toURI().toURL(),
		    new File(
			    "../../platforms/felix/org.apache.felix.shell.tui-1.4.1.jar")
			    .toURI().toURL() };

	    ObjectOutputStream oos = new ObjectOutputStream(
		    new FileOutputStream(aFileName));
	    oos.writeObject(urls);
	    oos.close();

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /** Bootstrap configuration */
    private final Map<String, String> pBootstrapConfiguration = new TreeMap<String, String>();

    /** Other properties */
    private final Map<String, String> pOtherConfiguration = new TreeMap<String, String>();

    /**
     * Extracts the configuration from system properties and program arguments
     * 
     * @param aProgramArguments
     *            The program arguments
     */
    protected void extractConfiguration(final String[] aProgramArguments) {

	// Put bootstrap system properties into the map
	for (Entry<Object, Object> property : System.getProperties().entrySet()) {

	    final String strKey = String.valueOf(property.getKey());
	    if (strKey.startsWith(IBootstrapConstants.PROPERTY_PREFIX)) {

		final Object value = property.getValue();
		if (value != null) {
		    pBootstrapConfiguration.put(strKey,
			    String.valueOf(property.getValue()));
		}
	    } else {
		pOtherConfiguration.put(strKey, (String) property.getValue());
	    }
	}

	// Put bootstrap arguments properties into the map
	for (String arg : aProgramArguments) {

	    final String[] normalizedArg = normalizeArgument(arg);
	    if (normalizedArg[0]
		    .startsWith(IBootstrapConstants.PROPERTY_PREFIX)
		    && normalizedArg[1] != null) {
		// Valid bootstrap argument
		pBootstrapConfiguration.put(normalizedArg[0], normalizedArg[1]);

	    } else {
		pOtherConfiguration.put(normalizedArg[0], normalizedArg[1]);
	    }
	}
    }

    /**
     * Tries to normalize a program argument :
     * 
     * - removes its leading -, --, -D
     * 
     * - Break it into 2 parts if it contains an '='
     * 
     * @param aArg
     *            A program argument
     * @return The normalized argument. Result[0] is never null.
     */
    protected String[] normalizeArgument(final String aArg) {

	String[] result = new String[2];

	if (aArg == null) {
	    // Never return null
	    result[0] = "";
	    result[1] = null;

	} else {
	    // Prepare the argument, maybe its value
	    String argument = aArg;
	    String value = null;

	    // Remove the leading characters
	    for (String leadingSequence : IGNORED_CHARACTERS) {
		if (aArg.startsWith(leadingSequence)) {
		    argument = aArg.substring(leadingSequence.length());
		}
	    }

	    // Extract value, if any
	    int valueIndex = argument.indexOf('=');
	    if (valueIndex != -1) {
		value = argument.substring(valueIndex + 1);
		argument = argument.substring(0, valueIndex);
	    }

	    result[0] = argument;
	    result[1] = value;
	}

	return result;
    }

    /**
     * Prints the help message
     * 
     * @return The help message content
     */
    public StringBuilder printHelp() {

	StringBuilder builder = new StringBuilder();
	builder.append("\nPSEM2M Bootstrap Options :\n");
	builder.append("\t" + IBootstrapConstants.HELP_COMMAND + " : Prints this help\n");
	builder.append("\t" + IBootstrapConstants.READ_LINES_COMMAND
		+ " : Read extra data from the standard input, line by line\n");
	builder.append("\t"
		+ IBootstrapConstants.UNSERIALIZE_COMMAND
		+ " : un-serialize a java.net.URL array from the standard input.\n");

	System.out.println(builder);
	return builder;
    }

    /**
     * Read lines, removing duplications, from the standard input until the
     * first empty line.
     * 
     * @param aInputStream
     *            The stream to read
     * 
     * @return The read lines content
     */
    protected String[] readConfigurationLines(final InputStream aInputStream) {

	// Use a set to avoid duplications
	Set<String> linesSet = new HashSet<String>();

	// Read from standard input
	final BufferedReader reader = new BufferedReader(new InputStreamReader(
		aInputStream));

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
     * Analyzes program arguments and reads the input stream if indicated
     * 
     * @param aProgramArguments
     *            Program arguments
     * @return The bundles URL array, null on unknown action
     */
    protected URL[] readParameters(final String[] aProgramArguments) {

	// Prepare extra configuration reading
	URL[] bundlesConfiguration = new URL[0];

	if (aProgramArguments.length > 0) {

	    // Test the action
	    final String action = aProgramArguments[0].toLowerCase();

	    if (action.equalsIgnoreCase(IBootstrapConstants.READ_LINES_COMMAND)) {
		final String[] extraConfiguration = readConfigurationLines(System.in);
		bundlesConfiguration = stringsToURLs(extraConfiguration);

	    } else if (action.equalsIgnoreCase(IBootstrapConstants.UNSERIALIZE_COMMAND)) {
		bundlesConfiguration = readSerializedConfiguration(System.in);

	    } else if (action.equalsIgnoreCase(IBootstrapConstants.HELP_COMMAND)) {
		// Help
		printHelp();
		return null;

	    } else {
		// Unknown command
		System.err.println("Unknown command : " + action);
		printHelp();
		return null;
	    }

	} else {
	    printHelp();
	    return null;
	}

	return bundlesConfiguration;
    }

    /**
     * Reads the first serialized object from the input stream. Return null if
     * it wasn't an array of URL.
     * 
     * @param aInputStream
     *            The stream to read
     * @return The deserialized URL array, null on error.
     */
    protected URL[] readSerializedConfiguration(final InputStream aInputStream) {

	Object readData = null;

	try {
	    // Try to read some data
	    ObjectInputStream objectStream = new ObjectInputStream(aInputStream);
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
     * Entry point code
     * 
     * @param aProgramArguments
     *            Program arguments
     */
    public void run(final String[] aProgramArguments) {

	// Extract the configuration
	extractConfiguration(aProgramArguments);

	// Act as indicated
	final URL[] bundlesConfiguration = readParameters(aProgramArguments);
	if (bundlesConfiguration == null) {
	    return;
	}

	// Start
	runBootstrap(bundlesConfiguration);
    }

    /**
     * Run the OSGi bootstrap
     */
    protected void runBootstrap(final URL[] aBundlesConfiguration) {

	// Prepare the bootstrap
	OsgiBootstrap bootstrap = new OsgiBootstrap(pBootstrapConfiguration,
		pOtherConfiguration);

	// Initialize the framework
	bootstrap.createFramework();

	// Install indicated bundles
	bootstrap.installBundles(aBundlesConfiguration);

	// Start the framework
	bootstrap.startFramework();

	// Start installed bundles
	bootstrap.startBundles();

	// Activity loop
	try {
	    do {
		System.out.println("Ping");
	    } while (!bootstrap.waitForStop(1000));

	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Stop the framework
	bootstrap.stopFramework();
    }

    /**
     * Converts the given strings array into an URL array. Uses the file
     * protocol for malformed URLs.
     * 
     * @param aStringArray
     *            Strings to be converted
     * @return URL array corresponding to the strings
     */
    protected URL[] stringsToURLs(final String[] aStringArray) {

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
}
