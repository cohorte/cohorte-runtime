/**
 * File:   Main.java
 * Author: Thomas Calmant
 * Date:   19 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.utilities.bootstrap.config.ConfigurationReader;
import org.psem2m.utilities.bootstrap.streams.MessageSender;
import org.psem2m.utilities.bootstrap.streams.RedirectedOutputStream;

/**
 * Bootstrap entry point
 * 
 * @author Thomas Calmant
 */
public class Main {

    /** Name to use in logs */
    private static final String CLASS_LOG_NAME = "Bootstrap.Main";

    /**
     * Retrieves the isolate ID system property
     * 
     * @return The isolate ID
     */
    public static String getIsolateId() {
	return System.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
		"<unknown>");
    }

    /**
     * Bootstrap entry point
     * 
     * @param aArgs
     *            Program arguments
     */
    public static void main(final String[] aArgs) {

	// Read the arguments, open the streams
	new Main(aArgs).run();
    }

    /** Bootstrap configuration */
    private final Map<String, String> pBootstrapConfiguration = new TreeMap<String, String>();

    /** Configuration file reader */
    private final ConfigurationReader pConfigurationReader;

    /** Real error output */
    private final PrintStream pErrorOutput;

    /** Framework starter thread */
    private FrameworkStarterThread pFrameworkStarterThread;

    /** Human output format */
    private boolean pHumanOutput = false;

    /** Message sender */
    private final MessageSender pMessageSender;

    /** Other properties */
    private final Map<String, String> pOtherConfiguration = new TreeMap<String, String>();

    /** Object output stream */
    private OutputStream pOutputStream;

    /** Real standard output */
    private final PrintStream pStandardOutput;

    /** The UI Thread work queue */
    private final BlockingQueue<Runnable> pUiWorkQueue = new LinkedBlockingQueue<Runnable>(
	    1);

    /**
     * Constructor : redirects output immediately
     */
    private Main(final String[] aArgs) {

	prepareProperties();

	// Read arguments
	initializeFromArgs(aArgs);

	// Store old output stream
	pStandardOutput = System.out;
	pErrorOutput = System.err;

	// Prepare the output stream
	openStreams();

	// Prepare the message sender
	pMessageSender = new MessageSender(pOutputStream);
	pMessageSender.setHumanMode(pHumanOutput);

	pConfigurationReader = new ConfigurationReader(pMessageSender);

	redirectOutputs();
    }

    /**
     * Closes object streams
     */
    protected void closeStreams() {

	// Reset system error (in any case)
	System.setErr(pErrorOutput);

	if (pOutputStream != null) {
	    try {
		if (!pStandardOutput.equals(pOutputStream)) {

		    // Reset System.out (or we will be stuck on close())
		    System.setOut(pStandardOutput);

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
	// Use a human output
	Option humanOutputOpt = parser
		.addBooleanOption(IBootstrapConstants.HUMAN_OUTPUT_FLAG);

	// Help command
	Option printHelpOpt = parser
		.addBooleanOption(IBootstrapConstants.HELP_COMMAND);

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

	// Set the human output flag
	pHumanOutput = (Boolean) parser.getOptionValue(humanOutputOpt, false);

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

		/*
		 * Set immediately system properties (to allow the
		 * PSEM2M_HOME/BASE resolution)
		 */
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

	System.out.println(builder);
	return builder;
    }

    /**
     * Reads the configuration from the bootstrap input
     * 
     * @return The bundles URL array, null on error
     * @throws IOException
     *             An error occurred reading configuration files
     */
    protected URL[] readConfiguration() throws IOException {

	// Line by line data
	return pConfigurationReader.readConfiguration();
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
     * Prepares the "runInThreadUI" service. The result flag is turned to on
     * when the framework main bundle is stopped.
     * 
     * Based on bndtools.launcher (EPL License)
     * 
     * @param aFramework
     *            The hosted OSGi framework
     * 
     * @return The atomic boolean flag indicating the end of the loop.
     */
    protected AtomicBoolean registerUIThreadService(final Framework aFramework) {

	// Get the framework system bundle context
	final BundleContext frameworkContext = aFramework.getBundleContext();

	// Set up the service properties
	final Properties mainThreadExecutorProps = new Properties();
	mainThreadExecutorProps.put("thread", "main");
	mainThreadExecutorProps.put(Constants.SERVICE_RANKING,
		Integer.valueOf(-1000));

	// Prepare the service instance
	final Executor mainThreadExecutor = new Executor() {
	    @Override
	    public void execute(final Runnable command) {
		pMessageSender.sendMessage(Level.FINEST, "UI-Executor",
			"execute", "Executor enqueuing a new task");

		// add() will throw an exception if the queue is full, which is
		// what we want
		pUiWorkQueue.add(command);
	    }
	};

	// Register the executor
	frameworkContext.registerService(Executor.class.getName(),
		mainThreadExecutor, mainThreadExecutorProps);

	// Shutdown flag
	final AtomicBoolean shutdown = new AtomicBoolean(false);
	final Thread mainThread = Thread.currentThread();

	/*
	 * Create a bundle listener that will pull us out of the queue polling
	 * loop when the system bundle starts to shutdown
	 */
	frameworkContext.addBundleListener(new SynchronousBundleListener() {
	    @Override
	    public void bundleChanged(final BundleEvent event) {
		if (event.getBundle().getBundleId() == 0
			&& event.getType() == BundleEvent.STOPPING) {

		    pMessageSender.sendMessage(Level.INFO, "UI-Executor",
			    "bundleChanged", "Main bundle is stopping");
		    shutdown.set(true);
		    mainThread.interrupt();
		}
	    }
	});

	return shutdown;
    }

    /**
     * Bootstrap main program
     */
    public void run() {

	// Read the bundles list and run the bootstrap
	URL[] bundleConfiguration;
	try {
	    bundleConfiguration = readConfiguration();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);

	    // Avoids compilation warning
	    return;
	}

	// Test bundle files
	if (!testBundles(bundleConfiguration)) {
	    System.exit(1);

	    // Avoids compilation warning
	    return;
	}

	// Run the framework...
	try {
	    // Prepare the thread
	    pFrameworkStarterThread = new FrameworkStarterThread(
		    pMessageSender, pBootstrapConfiguration,
		    pOtherConfiguration);

	    // Prepare the framework (without starting it)
	    final Framework framework = pFrameworkStarterThread
		    .prepareFramework();

	    // Register the UI service
	    final AtomicBoolean shutdown = registerUIThreadService(framework);
	    final Bundle frameworkBundle = framework.getBundleContext()
		    .getBundle();

	    // Run the beast
	    pFrameworkStarterThread.runBootstrap(bundleConfiguration);

	    // Start the framework activity thread
	    pFrameworkStarterThread.start();

	    // Enter a loop to poll on the work queue
	    while (!shutdown.get()
		    && frameworkBundle.getState() == Bundle.ACTIVE) {
		try {
		    Runnable work = pUiWorkQueue.poll(3, TimeUnit.SECONDS);
		    if (work != null) {
			pMessageSender.sendMessage(Level.FINEST, "UI-Executor",
				"Work-loop",
				"Main thread received a work task, executing.");

			work.run();
		    }

		} catch (InterruptedException e) {
		    pMessageSender.sendMessage(Level.FINEST, "UI-Executor",
			    "Work-loop",
			    "Main thread interrupted. End of the world.");
		}
	    }

	    // Interrupt the framework starter
	    pFrameworkStarterThread.interrupt();

	    // Clear the interrupted state if it was uncaught during the above
	    // loop
	    Thread.interrupted();

	} catch (Exception ex) {

	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "runBootstrap", "Can't start the framework thread", ex);

	    if (pFrameworkStarterThread != null) {
		pFrameworkStarterThread.stopFramework();
	    }
	}

	// Close the streams before exit
	closeStreams();

	// Exit in any case (do not wait for non-daemon threads)
	System.exit(0);
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
}
