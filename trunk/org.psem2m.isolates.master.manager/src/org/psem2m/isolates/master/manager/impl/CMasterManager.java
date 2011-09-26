/**
 * File:   CMasterManager.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogRecord;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.isolates.IIsolateOutputListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.IApplicationDescr;
import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * PSEM2M Master Manager, starting and monitoring the Forker process.
 * 
 * @author Thomas Calmant
 */
@Component(name = "isolates-master-manager-factory", publicFactory = false)
@Instantiate(name = "isolates-master-manager")
public class CMasterManager extends CPojoBase implements
        IIsolateOutputListener, ISignalListener {

    /** Signal indicating that the isolate state is OK to start a forker */
    public static final String MASTER_MANAGER_READY_SIGNAL = "/master-manager/READY";

    /** Default OSGi framework to use to start the forker (Felix) */
    public static final String OSGI_FRAMEWORK_FELIX = "org.apache.felix.main";

    /** The Bundle Context */
    private BundleContext pBundleContext;

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

    /** Log service, handled by iPOJO */
    @Requires(from = "isolates-master-manager-logger")
    private IActivityLoggerBase pLoggerSvc;

    /** The platform directory service */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Signal sender */
    @Requires
    private ISignalBroadcaster pSignalEmitter;

    /** Signal receiver */
    @Requires(filter = "(" + ISignalReceiver.PROPERTY_ONLINE + "=true)")
    private ISignalReceiver pSignalReceiver;

    /**
     * Default constructor
     */
    public CMasterManager(final BundleContext context) {

        super();
        pBundleContext = context;
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
     * Sends the READY message to the current isolate
     */
    protected void emitSignal() {

        // Send the message...
        pSignalEmitter.sendData(pPlatformDirsSvc.getIsolateId(),
                MASTER_MANAGER_READY_SIGNAL, null);
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

        for (IBundleDescr bundleDescr : isolateBundles) {

            BundleRef ref = pBundleFinderSvc.findBundle(bundleDescr
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

        System.out.println("Forker said : " + aIsolateStatus);

        if (aIsolateStatus.getState() == IsolateStatus.STATE_FAILURE) {
            System.err.println("Forker as failed.");
            pLoggerSvc.logSevere(this, "handleForkerStatus",
                    "Forker as failed starting.");

            try {
                // Try to restart it
                startForker();

                // Don't forget to restart the thread
                startWatcher();

            } catch (Exception ex) {
                // Log the restart error
                pLoggerSvc.logSevere(this, "handleForkerStatus",
                        "Error restarting the forker :", ex);

                try {
                    pBundleContext.getBundle(0).stop();

                } catch (Exception e) {
                    // At this point, it's difficult to do something nice...
                    pLoggerSvc.logSevere(this, "handleForkerStatus",
                            "CAN'T SUICIDE", e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (!aSignalName.equals(MASTER_MANAGER_READY_SIGNAL)
                || !pPlatformDirsSvc.getIsolateId().equals(
                        aSignalData.getIsolateSender())) {
            // Not of out business...
        }

        // That was the signal we were waiting for
        try {
            pLoggerSvc.logInfo(this, "handleReceivedSignal", "Start forker");

            if (startForker() == null) {
                pLoggerSvc.logSevere(this, "handleReceivedSignal",
                        "Can't start the forker.");

            } else {
                pLoggerSvc.logInfo(this, "handleReceivedSignal",
                        "Forker started.");

                // Start the forker watcher
                startWatcher();
            }

        } catch (Exception e) {
            pLoggerSvc.logSevere(this, "handleReceivedSignal",
                    "Error starting the forker", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Stop the watcher
        if (pForkerThread != null) {
            pForkerThread.interrupt();
            pForkerThread = null;
        }

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

    /**
     * Tries to start the forker bundle. Code is based on JavaRunner from the
     * bundle "forker".
     * 
     * @return The forker process
     * 
     * @throws Exception
     *             Invalid configuration
     */
    protected Process startForker() throws Exception {

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
        }

        // The class path
        {
            forkerCommand.add("-cp");

            StringBuilder cpBuilder = new StringBuilder();

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
        ProcessBuilder builder = new ProcessBuilder(forkerCommand);

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
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Register to the internal signal
        pSignalReceiver.registerListener(MASTER_MANAGER_READY_SIGNAL, this);

        // Emit the start forker signal
        pSignalEmitter.sendData(pPlatformDirsSvc.getIsolateId(),
                MASTER_MANAGER_READY_SIGNAL, null);

        // logs in the bundle logger
        pLoggerSvc.logInfo(this, "validatePojo", "VALIDATE", toDescription());
    }
}
