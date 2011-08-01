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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import org.psem2m.isolates.base.boot.IsolateStatus;
import org.psem2m.utilities.bootstrap.impl.OsgiBootstrapPure;
import org.psem2m.utilities.bootstrap.streams.ConfigurationReader;
import org.psem2m.utilities.bootstrap.streams.MessageSender;
import org.psem2m.utilities.bootstrap.streams.RedirectedOutputStream;

/**
 * Bootstrap entry point
 * 
 * @author "Thomas Calmant"
 */
public class Main {

	/** Name to use in logs */
	private static final String CLASS_LOG_NAME = "Bootstrap.Main";

	/** @see org.psem2m.isolates.base.IPlatformProperties#PROP_PLATFORM_ISOLATE_ID */
	public final static String PROP_PLATFORM_ISOLATE_ID = "org.psem2m.platform.isolate.id";

	/**
	 * Retrieves the isolate ID system property
	 * 
	 * @return The isolate ID
	 */
	public static String getIsolateId() {
		return System.getProperty(PROP_PLATFORM_ISOLATE_ID, "<unknown>");
	}

	/**
	 * Bootstrap entry point
	 * 
	 * @param aArgs
	 *            Program arguments
	 */
	public static void main(final String[] aArgs) {

		// Read the arguments, open the streams
		Main program = new Main(aArgs);

		// Read the bundles list and run the bootstrap
		URL[] bundleConfiguration = program.readConfiguration();

		// Test bundle files
		if (!program.testBundles(bundleConfiguration)) {
			System.exit(1);
		}

		// Run the framework if everything is OK
		boolean success = program.runBootstrap(bundleConfiguration);

		// Close the streams before exit
		program.closeStreams();

		if (!success) {
			System.exit(1);
		}
	}

	/** Bootstrap configuration */
	private final Map<String, String> pBootstrapConfiguration = new TreeMap<String, String>();

	/** Input configuration reader */
	private final ConfigurationReader pConfigurationReader;

	/** Input configuration stream */
	private InputStream pConfigurationStream = System.in;

	/** Human output format */
	private boolean pHumanOutput = false;

	/** Message sender */
	private final MessageSender pMessageSender;

	/** Other properties */
	private final Map<String, String> pOtherConfiguration = new TreeMap<String, String>();

	/** Object output stream */
	private OutputStream pOutputStream;

	/** Read serialized data flag */
	private boolean pReadSerializedData = false;

	/** Real standard output */
	private final PrintStream pStandardOutput;

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

		pConfigurationReader = new ConfigurationReader(pConfigurationStream,
				pMessageSender);

		redirectOutputs();
	}

	/**
	 * Closes object streams
	 */
	protected void closeStreams() {

		if (pOutputStream != null) {
			try {
				if (!System.out.equals(pOutputStream)) {
					// Avoid closing the stdout, or we'll be blocked here.
					pOutputStream.close();
				}

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

		// Prepare the configuration input stream (default: stdin)
		fileName = (String) parser.getOptionValue(readFromFileOpt);
		if (fileName != null) {
			try {
				pConfigurationStream = new FileInputStream(fileName);

			} catch (FileNotFoundException e) {
				System.err.println("File not found : " + e);
				System.exit(2);
			}
		}

		// Content format
		pReadSerializedData = (Boolean) parser.getOptionValue(
				readSerializedOpt, false);

		/* Use pending arguments */
		String[] remainingArgs = parser.getRemainingArgs();
		for (String argument : remainingArgs) {

			String[] property = normalizeArgument(argument);

			if (property[0]
					.startsWith(IBootstrapConstants.BOOTSTRAP_PROPERTY_PREFIX)) {
				// Put it directly in the bootstrap properties
				pBootstrapConfiguration.put(property[0], property[1]);

			} else {
				// Put it directly in the other properties
				pOtherConfiguration.put(property[0], property[1]);

				// Set immediately system properties (to allow the
				// PSEM2M_HOME/BASE resolution)
				if (property[0]
						.startsWith(IBootstrapConstants.PLATFORM_PROPERTY_PREFIX)) {
					System.setProperty(property[0], property[1]);
				}
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
			if (strKey
					.startsWith(IBootstrapConstants.BOOTSTRAP_PROPERTY_PREFIX)) {

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
	 * 
	 * @return True on success, False on error
	 */
	protected boolean runBootstrap(final URL[] aBundlesConfiguration) {

		// Prepare the bootstrap
		ISvcBootstrap bootstrap = new OsgiBootstrapPure(pMessageSender,
				pBootstrapConfiguration, pOtherConfiguration);

		pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
				"Creating framework...");
		pMessageSender.sendStatus(IsolateStatus.STATE_READ_CONF, 0);

		// Initialize the framework
		if (bootstrap.createFramework() == null) {
			pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
					"runBootstrap", "Can't create framework");
			pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);
			return false;
		}

		pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
				"Installing bundles...");
		pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_LOADED, 1);

		// Install indicated bundles
		if (!bootstrap.installBundles(aBundlesConfiguration)) {
			pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
					"runBootstrap", "Error installing bundles. Abandon");
			pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);
			return false;
		}

		pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
				"Starting framework...");
		pMessageSender.sendStatus(IsolateStatus.STATE_BUNDLES_INSTALLED, 2);

		// Start the framework
		if (!bootstrap.startFramework()) {
			pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
					"runBootstrap", "Error starting framework. Abandon");
			pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);
			return false;
		}

		pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
				"Starting bundles...");
		pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_STARTED, 3);

		// Start installed bundles
		if (!bootstrap.startBundles()) {
			pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
					"runBootstrap", "Error starting bundles. Abandon");
			pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);

			// Stop the framework "gracefully"
			bootstrap.stopFramework();
			return false;
		}

		pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
				"Running...");
		pMessageSender.sendStatus(IsolateStatus.STATE_BUNDLES_STARTED, 4);

		// Activity loop
		try {
			do {
				// Do something..;
			} while (!bootstrap.waitForStop(1000));

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
				"Stopping...");
		pMessageSender.sendStatus(IsolateStatus.STATE_STOPPING, 5);

		// Stop the framework
		if (!bootstrap.stopFramework()) {
			pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
					"runBootstrap", "Error stopping the framework. Ignoring.");

		} else {
			pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME,
					"runBootstrap", "Stopped");
		}

		pMessageSender.sendStatus(IsolateStatus.STATE_STOPPED, 6);
		return true;
	}

	/**
	 * Tests the if all bundles in the given bundle URL array are valid files
	 * 
	 * @param aBundleUrlArray
	 *            A bundle URL array
	 * @return True if all bundles are files, False if one them is not
	 */
	public boolean testBundles(final URL[] aBundleUrlArray) {

		if (aBundleUrlArray == null || aBundleUrlArray.length == 0) {
			pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
					"testBundles", "No bundle in the configuration array");

			return false;
		}

		// The file: protocol prefix
		final String fileProtocol = "file";

		for (URL bundleUrl : aBundleUrlArray) {

			if (!fileProtocol.equalsIgnoreCase(bundleUrl.getProtocol())) {
				// Ignore non file: URLs
				continue;
			}

			try {
				File bundleFile = new File(bundleUrl.toURI());
				if (!bundleFile.isFile()) {
					pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
							"testBundles", "Bundle not found : '" + bundleUrl
									+ "'");

					return false;
				}

			} catch (URISyntaxException e) {
				// Just print a warning
				pMessageSender.sendMessage(Level.WARNING, CLASS_LOG_NAME,
						"testBundles", "Bad URI : '" + bundleUrl + "'");
			}

		}

		return true;
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
