/**
 * File:   Main.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

        if (aArgs.length < 2) {
            printHelp();
            return;
        }

        // Special test command
        if (aArgs[0].equalsIgnoreCase(TEST_COMMAND)) {
            writeSerializationTest("./test.dat");
            return;
        }

        Main bootstrap = new Main();
        bootstrap.run(aArgs);
        bootstrap.closeStreams();
    }

    /**
     * Prints the help message
     * 
     * @return The help message content
     */
    public static StringBuilder printHelp() {

        StringBuilder builder = new StringBuilder();
        builder.append("\nPSEM2M Bootstrap Options :\n");
        builder.append("\t" + IBootstrapConstants.HELP_COMMAND
                + " : Prints this help\n");
        builder.append("\t" + IBootstrapConstants.READ_LINES_COMMAND
                + " : Read extra data from the standard input, line by line\n");
        builder.append("\t"
                + IBootstrapConstants.UNSERIALIZE_COMMAND
                + " : un-serialize a java.net.URL array from the standard input.\n");
        builder.append("\t" + TEST_COMMAND
                + " : Writes a serialized URL array in a file\n");

        System.out.println(builder);
        return builder;
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

    /** Input configuration reader */
    private ConfigurationReader pConfigurationReader;

    /** Message sender */
    private MessageSender pMessageSender;

    /** Object output stream */
    private ObjectOutputStream pObjectOutputStream;

    /** Other properties */
    private final Map<String, String> pOtherConfiguration = new TreeMap<String, String>();

    /** Real standard output */
    private PrintStream pStandardOutput;

    /**
     * Constructor : redirects output immediately
     */
    private Main() {

        // Prepare the input reader
        pConfigurationReader = new ConfigurationReader(System.in);

        // Store old output stream
        pStandardOutput = System.out;

        // Prepare the object output stream
        openStreams();

        // Prepare the message sender
        pMessageSender = new MessageSender(pObjectOutputStream);

        redirectOutputs();
    }

    /**
     * Closes object streams
     */
    protected void closeStreams() {

        if (pObjectOutputStream != null) {
            try {

                pObjectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
     * Opens object output streams
     */
    protected void openStreams() {

        try {
            pObjectOutputStream = new ObjectOutputStream(pStandardOutput);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                bundlesConfiguration = pConfigurationReader.readURLLines();

            } else if (action
                    .equalsIgnoreCase(IBootstrapConstants.UNSERIALIZE_COMMAND)) {
                bundlesConfiguration = pConfigurationReader
                        .readSerializedConfiguration();

            } else if (action
                    .equalsIgnoreCase(IBootstrapConstants.HELP_COMMAND)) {
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
}
