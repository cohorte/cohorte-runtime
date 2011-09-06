/**
 * File:   CMasterManager.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.conf.IApplicationDescr;
import org.psem2m.isolates.base.conf.IBundleDescr;
import org.psem2m.isolates.base.conf.ISvcConfig;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.utilities.CXTimedoutCall;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author Thomas Calmant
 */
public class CMasterManager extends CPojoBase {

    /** Default isolate kind */
    private static final IsolateKind DEFAULT_ISOLATE_KIND = IsolateKind.FELIX;

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
     * Tries to start the forker bundle
     * 
     * @throws Exception
     *             Invalid configuration
     */
    protected void startForker() throws Exception {

	// Find the script
	List<String> forkerCommand = pPlatformDirsSvc.getForkerStartCommand();
	if (forkerCommand == null) {
	    throw new Exception("Can't determine how to start the forker");
	}

	// Prepare the process builder
	ProcessBuilder builder = new ProcessBuilder(forkerCommand);

	// TODO compute the working directory in a better way...
	CXFileDir workingDir = pPlatformDirsSvc
		.getIsolateWorkingDir("psem2m.forker");
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
