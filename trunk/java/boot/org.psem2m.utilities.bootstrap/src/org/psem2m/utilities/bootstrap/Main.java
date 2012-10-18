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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
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
import org.psem2m.isolates.constants.boot.IBootstrapConstants;
import org.psem2m.utilities.bootstrap.config.ConfigurationReader;
import org.psem2m.utilities.bootstrap.streams.MessageSender;
import org.psem2m.utilities.bootstrap.streams.RedirectedOutputStream;

/**
 * Bootstrap entry point
 * 
 * @author Thomas Calmant
 */
public final class Main {

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
            Integer.MAX_VALUE);

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

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Special treatment for Mac OS X, if needed.
     * 
     * Loads the AWT library in the main thread.
     * 
     * os.name=[Mac OS X]
     */
    protected void handleMacOsX() {

        final String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("os x")) {
            // Not a Mac OS X host
            return;
        }

        // Load AWT
        final CAWTLoader wAWTLoader = new CAWTLoader();

        // Signal the special behavior
        final StringBuilder message = new StringBuilder();
        message.append("os.name=[").append(osName).append("] ");
        message.append("RgbOfBlackColor=[")
                .append(Integer.toHexString(wAWTLoader.getBlackRgb()))
                .append("]");
        pMessageSender.sendMessage(Level.FINEST, "Main", "run/Start", message);
    }

    /**
     * Analyzes the program arguments
     * 
     * @param aArgs
     *            The program arguments
     */
    protected void initializeFromArgs(final String[] aArgs) {

        final CmdLineParser parser = new CmdLineParser();

        /* Configuration input */
        // Use a human output
        final Option humanOutputOpt = parser
                .addBooleanOption(IBootstrapConstants.HUMAN_OUTPUT_FLAG);

        // Help command
        final Option printHelpOpt = parser
                .addBooleanOption(IBootstrapConstants.HELP_COMMAND);

        // Parse the arguments
        try {
            parser.parse(aArgs);

        } catch (final CmdLineParser.OptionException e) {

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
        final String[] remainingArgs = parser.getRemainingArgs();
        for (final String argument : remainingArgs) {

            final String[] property = normalizeArgument(argument);

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

        final String[] result = new String[2];

        if (aArg == null) {
            // Never return null
            result[0] = "";
            result[1] = null;

        } else {
            // Prepare the argument, maybe its value
            String argument = aArg;
            String value = null;

            // Extract value, if any
            final int valueIndex = argument.indexOf('=');
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

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prepares bootstrap and other properties from the system properties
     */
    protected void prepareProperties() {

        // Put bootstrap system properties into the map
        for (final Entry<Object, Object> property : System.getProperties()
                .entrySet()) {

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

        final StringBuilder builder = new StringBuilder();
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
        final RedirectedOutputStream fakeOutputStream = new RedirectedOutputStream(
                pMessageSender, Level.INFO, "stdout");

        final RedirectedOutputStream fakeErrorStream = new RedirectedOutputStream(
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
     * Based on bndtools.launcher
     * (https://github.com/njbartlett/bndtools.launcher
     * /blob/master/src/bndtools/launcher/Main.java), under EPL License
     * 
     * @param aFramework
     *            The hosted OSGi framework
     * 
     * @return The atomic boolean flag indicating the end of the loop.
     */
    protected AtomicBoolean registerUIThreadService(final Framework aFramework) {

        final String wCurrentThreadName = Thread.currentThread().getName();

        pMessageSender.sendMessage(Level.FINEST, "Main",
                "registerUIThreadService", String.format(
                        "Install the service [%s] CurrentThread=[%s]",
                        Executor.class.getName(), wCurrentThreadName));

        // Get the framework system bundle context
        final BundleContext frameworkContext = aFramework.getBundleContext();

        // Set up the service properties
        final Dictionary<String, Object> mainThreadExecutorProps = new Hashtable<String, Object>();
        mainThreadExecutorProps.put("thread", wCurrentThreadName);
        mainThreadExecutorProps.put(Constants.SERVICE_RANKING,
                Integer.valueOf(-1000));

        // Prepare the service instance
        final Executor mainThreadExecutor = new Executor() {

            @Override
            public void execute(final Runnable command) {

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

                    pMessageSender.sendMessage(Level.INFO,
                            "Main$SynchronousBundleListener", "bundleChanged",
                            "Main bundle is stopping");
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

        // log the beginning of the run method
        pMessageSender
                .sendMessage(Level.FINEST, "Main", "run/Start", String.format(
                        "CurrentThread=[%s]", Thread.currentThread().getName()));

        // tips to make AWT available on MacOsX ???
        handleMacOsX();

        // Read the bundles list and run the bootstrap
        final URL[] bundleConfiguration;
        try {
            bundleConfiguration = readConfiguration();

        } catch (final IOException e) {
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
        runFramework(bundleConfiguration);

        // Close the streams before exit
        closeStreams();

        // Exit in any case (do not wait for non-daemon threads)
        System.exit(0);
    }

    /**
     * Runs and waits for the framework thread. Executes the main thread queue
     * 
     * @param aBundleConfiguration
     *            An array of URLs to bundles to install
     */
    protected void runFramework(final URL[] aBundleConfiguration) {

        FrameworkStarterThread frameworkStarterThread = null;
        try {
            // Prepare the thread
            frameworkStarterThread = new FrameworkStarterThread(pMessageSender,
                    pBootstrapConfiguration, pOtherConfiguration);

            // Prepare the framework (without starting it)
            final Framework framework = frameworkStarterThread
                    .prepareFramework();

            // Register the UI service
            final AtomicBoolean shutdown = registerUIThreadService(framework);
            final Bundle frameworkBundle = framework.getBundleContext()
                    .getBundle();

            // Run the beast
            if (!frameworkStarterThread.runBootstrap(aBundleConfiguration)) {
                throw new Exception("Error in runBootstrap()");
            }

            // Start the framework activity thread
            frameworkStarterThread.start();

            // Enter a loop to poll on the work queue
            while (!shutdown.get()
                    && frameworkBundle.getState() == Bundle.ACTIVE) {
                try {
                    final Runnable work = pUiWorkQueue
                            .poll(3, TimeUnit.SECONDS);
                    if (work != null) {
                        // Get back to work !
                        work.run();
                    }

                } catch (final InterruptedException ex) {

                    pMessageSender.sendMessage(Level.FINEST, "UI-Executor",
                            "Work-loop",
                            "Main thread interrupted. Stop UI worker.");

                    break;

                } catch (final Throwable th) {

                    pMessageSender.sendMessage(Level.WARNING, "UI-Executor",
                            "Work-loop",
                            "An exception was caught in the main thread.", th);
                }
            }

            // Release the framework thread
            frameworkStarterThread.releaseFramework();

            // Wait for it to stop
            frameworkStarterThread.join(5000);

            // Clear the interrupted state if it was uncaught during the above
            // loop
            Thread.interrupted();

        } catch (final Throwable ex) {

            pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME, "run",
                    "Exception while running the main thread", ex);

            if (frameworkStarterThread != null) {
                frameworkStarterThread.releaseFramework();
            }
        }
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

        for (final URL bundleUrl : aBundleUrlArray) {

            if (!fileProtocol.equalsIgnoreCase(bundleUrl.getProtocol())) {
                // Ignore non file: URLs
                continue;
            }

            try {
                final File bundleFile = new File(bundleUrl.toURI());
                if (!bundleFile.isFile()) {
                    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
                            "testBundles", "Bundle not found : '" + bundleUrl
                                    + "'");

                    return false;
                }

            } catch (final URISyntaxException e) {
                // Just print a warning
                pMessageSender.sendMessage(Level.WARNING, CLASS_LOG_NAME,
                        "testBundles", "Bad URI : '" + bundleUrl + "'");
            }

        }

        return true;
    }
}
