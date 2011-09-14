/**
 * File:   CMasterManager.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogRecord;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.boot.IsolateStatus;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.IApplicationDescr;
import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author Thomas Calmant
 */
public class CMasterManager extends CPojoBase {

    /** Default OSGi framework to use to start the forker (Felix) */
    public static final String OSGI_FRAMEWORK_FELIX = "org.apache.felix.main";

    private BundleContext pBundleContext;

    /** The bundle finder */
    private IBundleFinderSvc pBundleFinderSvc;

    /** Available configuration */
    private ISvcConfig pConfigurationSvc;

    /** The forker process */
    private Process pForkerProcess;

    /** Forker watcher */
    private ForkerWatchThread pForkerThread;

    /** Log service, handled by iPOJO */
    private IActivityLoggerBase pLoggerSvc;

    /** The platform directory service */
    private IPlatformDirsSvc pPlatformDirsSvc;

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

    /**
     * Handles the given isolate status from the forker or the watcher
     * 
     * @param aIsolateStatus
     *            A forker status
     */
    protected synchronized void handleForkerStatus(
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
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

	// Stop the watcher
	pForkerThread.interrupt();

	// logs in the bundle logger
	pLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
		toDescription());
    }

    /**
     * Logs the given record grabbed from the forker
     * 
     * @param aLogRecord
     *            A forker log record
     */
    protected void logFromForker(final LogRecord aLogRecord) {
	pLoggerSvc.log(aLogRecord);
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
    public void validatePojo() throws BundleException {

	new Thread() {

	    @Override
	    public void run() {
		// Wait for the RSI entry to come up
		waitForRsi();

		try {
		    pLoggerSvc.logInfo(this, "validatePojo", "Start forker");

		    // Start the forker
		    if (startForker() == null) {
			throw new Exception("Can't start the forker");
		    }

		    // Start the forker watcher
		    startWatcher();

		} catch (Exception e) {
		    pLoggerSvc.logSevere(this, "validatePojo",
			    "Error starting Master.manager", e);

		    // throw new
		    // BundleException("Error starting Master.manager", e);
		}
	    }

	}.start();

	// logs in the bundle logger
	pLoggerSvc.logInfo(this, "validatePojo", "VALIDATE", toDescription());

	// // Wait for the RSI entry to come up
	// waitForRsi();
	//
	// try {
	// pLoggerSvc.logInfo(this, "validatePojo", "Start forker");
	//
	// // Start the forker
	// if (startForker() == null) {
	// throw new Exception("Can't start the forker");
	// }
	//
	// // Start the forker watcher
	// startWatcher();
	//
	// } catch (Exception e) {
	// pLoggerSvc.logSevere(this, "validatePojo",
	// "Error starting Master.manager", e);
	//
	// throw new BundleException("Error starting Master.manager", e);
	// }
    }

    /**
     * Sends HTTP GET requests to the remote service importer, in order to start
     * the forker <b>after</b> being able to listen to its notifications.
     */
    private void waitForRsi() {

	// Try to parse the URL and open a connection
	try {
	    URL isolateImporterUrl = new URL(
		    "http://localhost:9000/remote-service-importer");

	    int lastResponse = 0;

	    do {
		try {
		    final URLConnection urlConnection = isolateImporterUrl
			    .openConnection();

		    if (urlConnection instanceof HttpURLConnection) {

			// Only handle HTTP streams

			final HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
			httpConnection.connect();

			lastResponse = httpConnection.getResponseCode();

			httpConnection.disconnect();
		    }
		} catch (IOException ex) {
		    // Ignore
		}

		try {
		    Thread.sleep(500);

		} catch (InterruptedException e) {
		    // Ignore
		}

	    } while (lastResponse == HttpURLConnection.HTTP_NOT_FOUND);

	} catch (MalformedURLException ex) {
	    // WHAT ?
	    ex.printStackTrace();
	}
    }
}
