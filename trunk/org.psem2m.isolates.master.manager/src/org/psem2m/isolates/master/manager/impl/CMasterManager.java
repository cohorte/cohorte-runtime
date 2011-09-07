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
import java.util.concurrent.Callable;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IPlatformProperties;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.conf.IApplicationDescr;
import org.psem2m.isolates.base.conf.IBundleDescr;
import org.psem2m.isolates.base.conf.ISvcConfig;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXTimedoutCall;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author Thomas Calmant
 */
public class CMasterManager extends CPojoBase {

    /** Default OSGi framework to use to start the forker (Felix) */
    public static final String DEFAULT_OSGI_FRAMEWORK = "org.apache.felix.main";

    /** The bundle finder */
    private IBundleFinderSvc pBundleFinderSvc;

    /** Available configuration */
    private ISvcConfig pConfiguration;

    /** Log service, handled by iPOJO */
    private IActivityLoggerBase pLoggerSvc;

    /** The platform directory service */
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Default constructor
     */
    public CMasterManager() {
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

	final IApplicationDescr application = pConfiguration.getApplication();
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
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

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
     * @throws Exception
     *             Invalid configuration
     */
    protected void startForker() throws Exception {

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
	final BundleRef osgiFrameworkRef = pBundleFinderSvc
		.findBundle(DEFAULT_OSGI_FRAMEWORK);
	if (osgiFrameworkRef == null || osgiFrameworkRef.getFile() == null) {
	    // Fatal error : can't find the default OSGi framework
	    throw new FileNotFoundException(
		    "Can't find the default OSGi framework - "
			    + DEFAULT_OSGI_FRAMEWORK);
	}

	// Prepare the command line
	final List<String> forkerCommand = new ArrayList<String>();

	// The Java executable
	forkerCommand.add(javaExecutable.getAbsolutePath());

	// Defines properties
	{
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
	try {
	    final Process forkerProcess = builder.start();

	    // Wait some time for the script to return
	    int exitValue = CXTimedoutCall.call(new Callable<Integer>() {

		@Override
		public Integer call() throws Exception {
		    return forkerProcess.waitFor();
		}
	    }, 500);

	    System.out.println("Exit value : " + exitValue);

	    // Test its result
	    if (exitValue != 0) {
		throw new Exception("Error launching the forker isolate");
	    }

	} catch (IOException ex) {
	    throw new Exception("Error launching the forker script", ex);

	} catch (IllegalThreadStateException ex) {
	    // Ignore it : the exit value could not be calculated
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	// logs in the bundle logger
	pLoggerSvc.logInfo(this, "validatePojo", "VALIDATE", toDescription());

	try {
	    System.out.println("Start forker");
	    pLoggerSvc.logInfo(this, "validatePojo", "Start forker");
	    startForker();

	} catch (Exception e) {
	    pLoggerSvc.logSevere(this, "validatePojo",
		    "Error starting Master.manager", e);

	    throw new BundleException("Error starting Master.manager", e);
	}
    }
}
