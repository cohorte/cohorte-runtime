/**
 * File:   FrameworkStarterThread.java
 * Author: Thomas Calmant
 * Date:   14 sept. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.osgi.framework.launch.Framework;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;

/**
 * Framework execution thread, allowing to let the main-thread for UI purpose
 * 
 * @author Thomas Calmant
 */
public class FrameworkStarterThread extends Thread {

    /** The class name in logs */
    private static final String CLASS_LOG_NAME = "FrameworkStarterThread";

    /** Thread name */
    public static final String THREAD_NAME = "PSEM2M-Bootstrap-FrameworkStarter";

    /** Bootstrap configuration */
    private final Map<String, String> pBootstrapConfiguration;

    /** Other system properties */
    private final Map<String, String> pFrameworkConfiguration;

    /** The framework start */
    private FrameworkStarter pFrameworkStarter;

    /** The message sender */
    private IMessageSender pMessageSender;

    /** The framework activity lock */
    private Semaphore pSemaphore;

    /**
     * Sets up the framework starter
     * 
     * @param aMessageSender
     *            Log message sender
     * @param aBootstrapConfig
     *            Bootstrap-specific configuration properties
     * @param aFrameworkConfig
     *            Framework system properties
     * 
     * @throws Exception
     *             One of the parameters is null
     */
    public FrameworkStarterThread(final IMessageSender aMessageSender,
            final Map<String, String> aBootstrapConfig,
            final Map<String, String> aFrameworkConfig) throws Exception {

        // Set up the thread
        super(THREAD_NAME);
        setDaemon(true);

        pMessageSender = aMessageSender;
        if (pMessageSender == null) {
            throw new Exception("MessageSender can't be null");
        }

        pBootstrapConfiguration = aBootstrapConfig;
        if (pBootstrapConfiguration == null) {
            throw new Exception("Bootstrap configuration can't be null");
        }

        pFrameworkConfiguration = aFrameworkConfig;
        if (pFrameworkConfiguration == null) {
            throw new Exception("Framework configuration can't be null");
        }

        // Prepare the semaphore (pre-locked)
        pSemaphore = new Semaphore(0);
    }

    /**
     * Prepares the OSGi framework (doesn't starts it nor install bundles)
     * 
     * @return The OSGi framework, null on error
     */
    public Framework prepareFramework() {

        // Prepare the bootstrap
        pFrameworkStarter = new FrameworkStarter(pMessageSender,
                pBootstrapConfiguration, pFrameworkConfiguration);

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Creating framework...");
        pMessageSender.sendStatus(IsolateStatus.STATE_READ_CONF, 0);

        // Initialize the framework
        final Framework framework = pFrameworkStarter.createFramework();
        if (framework == null) {
            pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
                    "runBootstrap", "Can't create framework");
            pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);
        }

        return framework;
    }

    /**
     * Releases the semaphore waiting for the framework
     */
    public void releaseFramework() {

        pSemaphore.release();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        // Framework termination wait thread...
        try {
            pSemaphore.acquire();

        } catch (final InterruptedException e) {
            // Thread interruption, stop the framework (finally block)

        } finally {
            // Send stop informations
            stopFramework();
        }
    }

    /**
     * Run the OSGi bootstrap
     * 
     * @return True on success, False on error
     */
    public boolean runBootstrap(final URL[] aBundlesConfiguration) {

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Installing bundles...");
        pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_LOADED, 1);

        // Install indicated bundles
        if (!pFrameworkStarter.installBundles(aBundlesConfiguration)) {
            pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
                    "runBootstrap", "Error installing bundles. Abandon");
            pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);
            return false;
        }

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Starting framework...");
        pMessageSender.sendStatus(IsolateStatus.STATE_BUNDLES_INSTALLED, 2);

        // Start the framework
        if (!pFrameworkStarter.startFramework()) {
            pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
                    "runBootstrap", "Error starting framework. Abandon");
            pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);
            return false;
        }

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Registering bootstrap services...");
        pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_STARTED, 2.5);

        // Install bootstrap service
        pFrameworkStarter.installBootstrapService();

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Starting bundles...");
        pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_STARTED, 3);

        // Start installed bundles
        if (!pFrameworkStarter.startBundles()) {
            pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
                    "runBootstrap", "Error starting bundles. Abandon");
            pMessageSender.sendStatus(IsolateStatus.STATE_FAILURE, 0);

            // Stop the framework "gracefully"
            pFrameworkStarter.stopFramework();
            return false;
        }

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Running...");
        pMessageSender.sendStatus(IsolateStatus.STATE_BUNDLES_STARTED, 4);

        return true;
    }

    /**
     * Stops the framework
     */
    private void stopFramework() {

        pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME, "runBootstrap",
                "Stopping...");
        pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_STOPPING, 5);

        // Stop the framework
        if (!pFrameworkStarter.stopFramework()) {
            pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
                    "runBootstrap", "Error stopping the framework. Ignoring.");

        } else {
            pMessageSender.sendMessage(Level.INFO, CLASS_LOG_NAME,
                    "runBootstrap", "Stopped");
        }

        pMessageSender.sendStatus(IsolateStatus.STATE_FRAMEWORK_STOPPED, 6);
    }
}
