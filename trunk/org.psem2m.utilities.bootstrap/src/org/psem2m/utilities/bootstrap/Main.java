/**
 * File:   Main.java
 * Author: "Thomas Calmant"
 * Date:   19 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import org.psem2m.utilities.bootstrap.impl.OsgiBootstrap;
import org.psem2m.utilities.bootstrap.streams.ConfigurationReader;
import org.psem2m.utilities.bootstrap.streams.MessageSender;
import org.psem2m.utilities.bootstrap.streams.RedirectedOutputStream;

/**
 * @author "Thomas Calmant"
 * 
 */
public class Main {

    /**
     * Bootstrap entry point
     * 
     * @param aArgs
     *            Program arguments
     */
    public static void main(final String[] aArgs) {

	Main program = new Main(aArgs);

	// Read the bundles list and run the bootstrap
	URL[] bundleConfiguration = program.readConfiguration();
	program.runBootstrap(bundleConfiguration);

	program.closeStreams();
    }

    /** Bootstrap configuration */
    private final Map<String, String> pBootstrapConfiguration = new TreeMap<String, String>();

    /** Input configuration reader */
    private ConfigurationReader pConfigurationReader;

    /** Human output format */
    private boolean pHumanOutput = false;

    /** Message sender */
    private MessageSender pMessageSender;

    /** Other properties */
    private final Map<String, String> pOtherConfiguration = new TreeMap<String, String>();

    /** Object output stream */
    private OutputStream pOutputStream;

    /** Read serialized data flag */
    private boolean pReadSerializedData = false;

    /** Real standard output */
    private PrintStream pStandardOutput;

    /**
     * Constructor : redirects output immediately
     */
    private Main(final String[] aArgs) {

	prepareProperties();

	// Read arguments
	initializeFromArgs(aArgs);

	// Store old output stream
	pStandardOutput = System.out;

	// Prepare the output stream
	openStreams();

	// Prepare the message sender
	pMessageSender = new MessageSender(pOutputStream);
	pMessageSender.setHumanMode(pHumanOutput);

	redirectOutputs();
    }

    /**
     * Closes object streams
     */
    protected void closeStreams() {

	if (pOutputStream != null) {
	    try {
		pOutputStream.close();

	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Analyzes the program arguments
     * 
     * @param aArgs
     *            The program arguments
     */
    protected void initializeFromArgs(final String[] aArgs) {

	CmdLineParser parser = new CmdLineParser();

	/* Configuration input */
	// Dummy option
	parser.addBooleanOption(IBootstrapConstants.READ_LINES_COMMAND);

	Option readFromFileOpt = parser
		.addStringOption(IBootstrapConstants.READ_FROM_FILE);

	Option readSerializedOpt = parser
		.addBooleanOption(IBootstrapConstants.UNSERIALIZE_COMMAND);

	// Use a human output
	Option humanOutputOpt = parser
		.addBooleanOption(IBootstrapConstants.HUMAN_OUTPUT_FLAG);

	// Help command
	Option printHelpOpt = parser
		.addBooleanOption(IBootstrapConstants.HELP_COMMAND);

	// Write some test data
	Option writeTestFileOpt = parser
		.addStringOption(IBootstrapConstants.TEST_COMMAND);

	// Parse the arguments
	try {
	    parser.parse(aArgs);

	} catch (CmdLineParser.OptionException e) {

	    // Fatal error : unrecognized option
	    System.err.println(e.getMessage());
	    printHelp();
	    System.exit(1);
	}

	/* Handle options */
	// Print help
	if ((Boolean) parser.getOptionValue(printHelpOpt, false)) {
	    printHelp();
	    return;
	}

	// Write sample file
	String fileName = (String) parser.getOptionValue(writeTestFileOpt);
	if (fileName != null) {
	    writeSerializationTest(fileName);
	    return;
	}

	// Set the human output flag
	pHumanOutput = (Boolean) parser.getOptionValue(humanOutputOpt, false);

	// Prepare the read input stream
	InputStream configStream = System.in;

	fileName = (String) parser.getOptionValue(readFromFileOpt);
	if (fileName != null) {
	    try {
		configStream = new FileInputStream(fileName);

	    } catch (FileNotFoundException e) {
		System.err.println("File not found : " + e);
		System.exit(2);
	    }
	}

	pConfigurationReader = new ConfigurationReader(configStream);

	// Content format
	pReadSerializedData = (Boolean) parser.getOptionValue(
		readSerializedOpt, false);

	/* Use pending arguments */
	String[] remainingArgs = parser.getRemainingArgs();
	for (String argument : remainingArgs) {

	    String[] property = normalizeArgument(argument);

	    if (property[0].startsWith(IBootstrapConstants.PROPERTY_PREFIX)) {
		// Put it directly in the bootstrap properties
		pBootstrapConfiguration.put(property[0], property[1]);

	    } else {
		// Put it directly in the other properties
		pOtherConfiguration.put(property[0], property[1]);
	    }
	}
    }

    /**
     * Tries to normalize a program argument :
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
     * Opens object output streams
     */
    protected void openStreams() {

	if (pHumanOutput) {
	    pOutputStream = System.out;

	} else {
	    try {
		pOutputStream = new ObjectOutputStream(pStandardOutput);

	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Prepares bootstrap and other properties from the system properties
     */
    protected void prepareProperties() {
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
    }

    /**
     * Prints the help message
     * 
     * @return The help message content
     */
    public StringBuilder printHelp() {

	StringBuilder builder = new StringBuilder();
	builder.append("\nPSEM2M Bootstrap Options :\n");

	builder.append("\t --" + IBootstrapConstants.HELP_COMMAND
		+ " : Prints this help\n");

	builder.append("\t --"
		+ IBootstrapConstants.HUMAN_OUTPUT_FLAG
		+ " : Output in a human readable mode (default: serialized mode)\n");

	builder.append("\t --"
		+ IBootstrapConstants.READ_LINES_COMMAND
		+ " : Reads the list of bundle from the input, one bundle file name by line (default)\n");

	builder.append("\t --" + IBootstrapConstants.UNSERIALIZE_COMMAND
		+ " : un-serializes a java.net.URL array from the input.\n");

	builder.append("\t --" + IBootstrapConstants.READ_FROM_FILE
		+ "=<file> "
		+ ": Use the given file as input (default: standard input)\n");

	builder.append("\t --" + IBootstrapConstants.TEST_COMMAND
		+ " : Writes a serialized URL array in a file\n");

	System.out.println(builder);
	return builder;
    }

    /**
     * Reads the configuration from the bootstrap input
     * 
     * @return The bundles URL array, null on error
     */
    protected URL[] readConfiguration() {

	if (pReadSerializedData) {
	    // Serialized data
	    return pConfigurationReader.readSerializedConfiguration();
	}

	// Line by line data
	return pConfigurationReader.readURLLines();
    }

    /**
     * Replaces current standard output streams them with a memory output.
     */
    protected void redirectOutputs() {

	// New output streams
	RedirectedOutputStream fakeOutputStream = new RedirectedOutputStream(
		pMessageSender, Level.INFO, "stdout");

	RedirectedOutputStream fakeErrorStream = new RedirectedOutputStream(
		pMessageSender, Level.WARNING, "stderr");

	// Replace standard output
	PrintStream fakeOutput = new PrintStream(fakeOutputStream, true);
	System.setOut(fakeOutput);

	// Replace standard error output
	fakeOutput = new PrintStream(fakeErrorStream, true);
	System.setErr(fakeOutput);
    }

    /**
     * Run the OSGi bootstrap
     */
    protected void runBootstrap(final URL[] aBundlesConfiguration) {

	// Prepare the bootstrap
	OsgiBootstrap bootstrap = new OsgiBootstrap(pMessageSender,
		pBootstrapConfiguration, pOtherConfiguration);

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Creating framework...");

	// Initialize the framework
	if (bootstrap.createFramework() == null) {
	    pMessageSender.sendMessage(Level.SEVERE, "Main", "runBootstrap",
		    "Can't create framework");
	    return;
	}

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Installing bundles...");

	// Install indicated bundles
	bootstrap.installBundles(aBundlesConfiguration);

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Starting framework...");

	// Start the framework
	bootstrap.startFramework();

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Starting bundles...");

	// Start installed bundles
	bootstrap.startBundles();

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Running...");

	// Activity loop
	try {
	    do {
		// Do something..;
	    } while (!bootstrap.waitForStop(1000));

	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Stopping...");

	// Stop the framework
	bootstrap.stopFramework();

	pMessageSender.sendMessage(Level.INFO, "Main", "runBootstrap",
		"Stopped");
    }

    /**
     * Writes a serialized sample file
     * 
     * @param aFileName
     *            Output file name
     */
    protected void writeSerializationTest(final String aFileName) {

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
}
