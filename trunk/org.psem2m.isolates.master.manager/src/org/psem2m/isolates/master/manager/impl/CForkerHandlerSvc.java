/**
 * File:   CForkerHandlerSvc.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogRecord;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.isolates.IForkerHandler;
import org.psem2m.isolates.base.isolates.IIsolateOutputListener;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.conf.IApplicationDescr;
import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;

/**
 * PSEM2M Master Manager, starting and monitoring the Forker process.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-master-manager-factory", publicFactory = false)
@Provides(specifications = IForkerHandler.class)
@Instantiate(name = "psem2m-master-manager")
public class CForkerHandlerSvc extends CPojoBase implements IForkerHandler,
        IIsolateOutputListener {

    /** Default OSGi framework to use to start the forker (Felix) */
    public static final String OSGI_FRAMEWORK_FELIX = "org.apache.felix.main";

    /** The bundle finder */
    @Requires
    private IBundleFinderSvc pBundleFinderSvc;

    /** Available configuration */
    @Requires
    private ISvcConfig pConfigurationSvc;

    /** The forker process */
    private Process pForkerProcess;

    /** Forker watcher */
    private ForkerWatchThread pForkerThread;

    /** IsolateStatus listeners */
    private final Set<IIsolateStatusEventListener> pIsolateListeners = new HashSet<IIsolateStatusEventListener>();

    /** Log service, handled by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLoggerSvc;

    /** The platform directory service */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Signal sender */
    @Requires
    private ISignalBroadcaster pSignalSender;

    /**
     * Default constructor
     */
    public CForkerHandlerSvc() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // ...
    }

    /**
     * Reads the bundles list for the given isolate and returns it as an array
     * of bundle references
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return An array of bundle references, null on error
     */
    protected BundleRef[] getBundlesRef(final String aIsolateId) {

        final IApplicationDescr application = pConfigurationSvc
                .getApplication();
        final Set<IBundleDescr> isolateBundles = application.getIsolate(
                aIsolateId).getBundles();

        if (isolateBundles == null || isolateBundles.isEmpty()) {
            // Ignore empty list
            return null;
        }

        final Set<BundleRef> bundlesRef = new LinkedHashSet<BundleRef>(
                isolateBundles.size());

        for (final IBundleDescr bundleDescr : isolateBundles) {

            final BundleRef ref = pBundleFinderSvc.findBundle(bundleDescr
                    .getSymbolicName());

            if (ref != null) {
                bundlesRef.add(ref);
            } else {
                // Return null on error
                return null;
            }
        }

        return bundlesRef.toArray(new BundleRef[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.isolates.boot.IIsolateOutputListener#handleLogRecord
     * ( java.lang.String, java.util.logging.LogRecord)
     */
    @Override
    public void handleIsolateLogRecord(final String aSourceIsolateId,
            final LogRecord aLogRecord) {

        pLoggerSvc.log(aLogRecord);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.boot.IIsolateOutputListener#
     * handleIsolateStatus (java.lang.String,
     * org.psem2m.isolates.base.isolates.boot.IsolateStatus)
     */
    @Override
    public synchronized void handleIsolateStatus(final String aIsolateId,
            final IsolateStatus aIsolateStatus) {

        // Notify listeners (let them take a decision)
        for (final IIsolateStatusEventListener listener : pIsolateListeners) {
            listener.handleIsolateStatusEvent(aIsolateId, aIsolateStatus);
        }
    }

    /**
     * Tries to start the forker bundle. Code is based on JavaRunner from the
     * bundle "forker".
     * 
     * @return The forker process
     * 
     * @throws Exception
     *             Invalid configuration
     */
    protected Process internalStartForker() throws Exception {

        // Get the forker configuration
        final IIsolateDescr forkerDescr = pConfigurationSvc.getApplication()
                .getIsolate(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER);
        if (forkerDescr == null) {
            throw new Exception("No configuration found to start the forker.");
        }

        // Find the Java executable
        final File javaExecutable = pPlatformDirsSvc.getJavaExecutable();
        if (javaExecutable == null || !javaExecutable.exists()) {
            // Fatal error : don't know where is Java
            throw new FileNotFoundException("Can't find the Java executable");
        }

        // Find the bootstrap
        final File bootstrapJar = pBundleFinderSvc.getBootstrap();
        if (bootstrapJar == null) {
            // Fatal error if the JAR file is not found
            throw new FileNotFoundException("Can't find the bootstrap");
        }

        // Find the OSGi Framework
        // FIXME: use the "kind" argument
        final BundleRef osgiFrameworkRef = pBundleFinderSvc
                .findBundle(OSGI_FRAMEWORK_FELIX);
        if (osgiFrameworkRef == null || osgiFrameworkRef.getFile() == null) {
            // Fatal error : can't find the default OSGi framework
            throw new FileNotFoundException(
                    "Can't find the default OSGi framework - "
                            + OSGI_FRAMEWORK_FELIX);
        }

        // Prepare the command line
        final List<String> forkerCommand = new ArrayList<String>();

        // The Java executable
        forkerCommand.add(javaExecutable.getAbsolutePath());

        // Add the debug parameters, if needed
        forkerCommand.addAll(setupDebugMode());

        // Defines properties
        {
            // Isolate VM arguments
            forkerCommand.addAll(forkerDescr.getVMArgs());

            // Isolate ID
            forkerCommand.add(makeJavaProperty(
                    IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
                    IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER));

            // PSEM2M Home
            forkerCommand.add(makeJavaProperty(
                    IPlatformProperties.PROP_PLATFORM_HOME, pPlatformDirsSvc
                            .getPlatformHomeDir().getAbsolutePath()));

            // PSEM2M Base
            forkerCommand.add(makeJavaProperty(
                    IPlatformProperties.PROP_PLATFORM_BASE, pPlatformDirsSvc
                            .getPlatformBaseDir().getAbsolutePath()));

            // Same host name than this monitor
            forkerCommand.add(makeJavaProperty(
                    IPlatformProperties.PROP_PLATFORM_HOST_NAME,
                    Utilities.getHostName()));
        }

        // The class path
        {
            forkerCommand.add("-cp");

            final StringBuilder cpBuilder = new StringBuilder();

            // Bootstrap
            cpBuilder.append(bootstrapJar.getAbsolutePath());
            cpBuilder.append(File.pathSeparator);

            // OSGi Framework
            cpBuilder.append(osgiFrameworkRef.getFile().getAbsolutePath());
            cpBuilder.append(File.pathSeparator);

            // Working directory
            cpBuilder.append(".");

            forkerCommand.add(cpBuilder.toString());
        }

        // The bootstrap main class
        forkerCommand.add(IBundleFinderSvc.BOOTSTRAP_MAIN_CLASS);

        // Prepare the process builder
        final ProcessBuilder builder = new ProcessBuilder(forkerCommand);

        // Compute the working directory
        final File workingDir = pPlatformDirsSvc
                .getIsolateWorkingDir(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER);
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }

        builder.directory(workingDir);

        // Run !
        pForkerProcess = builder.start();
        return pForkerProcess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Stop the watcher, let the forker live
        stopWatcher();

        // logs in the bundle logger
        pLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
                toDescription());
    }

    /**
     * Prepares a Java interpreter argument to define a JVM property
     * 
     * @param aKey
     *            Property name
     * @param aValue
     *            Property value
     * @return The property definition argument
     */
    protected String makeJavaProperty(final String aKey, final String aValue) {

        final StringBuilder propertyDef = new StringBuilder(aKey.length()
                + aValue.length() + 3);

        propertyDef.append("-D");
        propertyDef.append(aKey);
        propertyDef.append("=");
        propertyDef.append(aValue);

        return propertyDef.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.isolates.IForkerHandler#registerIsolateEventListener
     * (org.psem2m.isolates.base.isolates.IIsolateStatusEventListener)
     */
    @Override
    public void registerIsolateEventListener(
            final IIsolateStatusEventListener aListener) {

        if (aListener != null) {
            synchronized (pIsolateListeners) {
                pIsolateListeners.add(aListener);
            }
        }
    }

    /**
     * Prepares the debug mode parameters, if needed / possible
     * 
     * @return The debug parameters (to be set before -jar), never null
     */
    protected List<String> setupDebugMode() {

        // JVM debug port
        final int monitorDebugPort;

        // Result parameters list
        final List<String> resultList = new ArrayList<String>();

        // Test if the debug port is indicated
        final String debugPortStr = System
                .getProperty(IPlatformProperties.PROP_BASE_DEBUG_PORT);
        if (debugPortStr == null) {
            // Nothing to do
            return resultList;
        }

        // Prepare the base port to be used
        try {
            monitorDebugPort = Integer.parseInt(debugPortStr);

            if (monitorDebugPort <= 0 || monitorDebugPort > 65535) {
                throw new NumberFormatException("Invalid port number");
            }

        } catch (final NumberFormatException ex) {
            pLoggerSvc.logWarn(this, "setupDebugMode",
                    "Can't activate Debug Mode, invalide port number : ",
                    debugPortStr);

            // Can't do anything useful
            return resultList;
        }

        // Forker debug port is the monitor one +1
        final int forkerDebugPort = monitorDebugPort + 1;

        // JVM debug mode
        resultList.add("-Xdebug");

        // Connection parameter
        resultList.add(String.format(
                "-Xrunjdwp:transport=dt_socket,address=127.0.0.1:%d,suspend=y",
                forkerDebugPort));

        // Forker base debug port
        resultList.add(makeJavaProperty(
                IPlatformProperties.PROP_BASE_DEBUG_PORT,
                Integer.toString(forkerDebugPort)));

        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IForkerHandler#startForker()
     */
    @Override
    public boolean startForker() {

        // Start the forker process
        try {
            if (internalStartForker() == null) {
                return false;
            }

        } catch (final Exception e) {
            pLoggerSvc.logSevere(this, "startForker",
                    "Error starting forker process : ", e);
            return false;
        }

        // Start the forker watcher
        try {
            startWatcher();

        } catch (final IOException e) {
            pLoggerSvc.logWarn(this, "startForker",
                    "Forker has been started, but its watcher failed.");
        }

        return true;
    }

    /**
     * Starts the forker watcher thread
     * 
     * @throws IOException
     *             Invalid forker output format
     */
    protected void startWatcher() throws IOException {

        if (pForkerThread != null) {
            pForkerThread.interrupt();
        }

        pForkerThread = new ForkerWatchThread(this, pForkerProcess);
        pForkerThread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IForkerHandler#stopForker()
     */
    @Override
    public boolean stopForker() {

        // Send a signal
        pSignalSender.sendData(ISignalBroadcaster.EEmitterTargets.FORKER,
                ISignalsConstants.ISOLATE_STOP_SIGNAL, null);

        if (pForkerProcess != null) {
            // Use the brutal force
            // TODO use a kinder way
            pForkerProcess.destroy();
            pForkerProcess = null;
            return true;
        }

        return false;
    }

    /**
     * Interrupts the watching thread
     */
    protected void stopWatcher() {

        if (pForkerThread != null) {
            pForkerThread.interrupt();
            pForkerThread = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IForkerHandler#
     * unregisterIsolateEventListener
     * (org.psem2m.isolates.base.isolates.IIsolateStatusEventListener)
     */
    @Override
    public void unregisterIsolateEventListener(
            final IIsolateStatusEventListener aListener) {

        if (aListener != null) {
            synchronized (pIsolateListeners) {
                pIsolateListeners.remove(aListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // logs in the bundle logger
        pLoggerSvc.logInfo(this, "validatePojo", "VALIDATE", toDescription());
    }
}
