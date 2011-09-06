/**
 * File:   CMasterManager.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IPlatformDirsSvc;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.commons.forker.IsolateConfiguration;
import org.psem2m.isolates.config.IParamId;
import org.psem2m.isolates.config.ISvcConfig;
import org.psem2m.isolates.master.manager.IMasterManagerConfig;
import org.psem2m.utilities.CXTimedoutCall;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author Thomas Calmant
 * 
 */
public class CMasterManager extends CPojoBase {

    /** Default isolate kind */
    private static final IsolateKind DEFAULT_ISOLATE_KIND = IsolateKind.FELIX;

    /** The bundle finder */
    private IBundleFinderSvc pBundleFinderSvc;

    /** Available configuration */
    private ISvcConfig pConfiguration;

    /** Isolates configuration list */
    private final Map<String, IIsolateConfiguration> pIsolatesConfiguration = new LinkedHashMap<String, IIsolateConfiguration>();

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

	List<String> isolateBundles = getConfigurationList(aIsolateId,
		IMasterManagerConfig.ISOLATE_BUNDLES_LIST);
	if (isolateBundles == null || isolateBundles.isEmpty()) {
	    // Ignore empty list
	    return null;
	}

	Set<BundleRef> bundlesRef = new LinkedHashSet<BundleRef>(
		isolateBundles.size());

	for (String bundleName : isolateBundles) {

	    BundleRef ref = pBundleFinderSvc.findBundle(bundleName);
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
     * Reads a list from the given configuration pair
     * 
     * @param aPrefix
     *            Parameter ID prefix (without trailing dot)
     * @param aKey
     *            Parameter ID key
     * @return Null on error, a string list on success
     */
    protected List<String> getConfigurationList(final String aPrefix,
	    final String aKey) {

	List<Object> strRaw = pConfiguration.getParamList(makeParamId(aPrefix
		+ "." + aKey));

	if (strRaw == null) {
	    return null;
	}

	List<String> strList = new ArrayList<String>(strRaw.size());

	for (Object rawObject : strRaw) {
	    if (rawObject != null) {
		strList.add(rawObject.toString().trim());
	    }
	}

	return strList;
    }

    /**
     * Reads a string from the given configuration pair and trims it
     * 
     * @param aPrefix
     *            Parameter ID prefix (without trailing dot)
     * @param aKey
     *            Parameter ID key
     * @return Null on error, a string on success
     */
    protected String getConfigurationStr(final String aPrefix, final String aKey) {

	String result = pConfiguration.getParamStr(makeParamId(aPrefix + "."
		+ aKey));
	if (result == null) {
	    return null;
	}

	return result.trim();
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
     * Prepares a ParamId with the given ID
     * 
     * @param aParamIdStr
     *            Complete parameter ID
     * @return The configuration parameter ID
     */
    protected IParamId makeParamId(final String aParamIdStr) {

	return new IParamId() {
	    @Override
	    public String getId() {
		return aParamIdStr;
	    }
	};
    }

    /**
     * Read isolates descriptions from the configuration. Fails if their is no
     * isolates described.
     * 
     * @throws Exception
     *             The configuration file doesn't contain all needed data
     */
    protected void readIsolatesDescription() throws Exception {

	// Empty current list
	pIsolatesConfiguration.clear();

	// Read the list
	List<String> isolateIdsList = getConfigurationList(
		IMasterManagerConfig.MANAGER_NAMESPACE,
		IMasterManagerConfig.ISOLATE_ID_LIST);
	if (isolateIdsList == null || isolateIdsList.isEmpty()) {
	    throw new Exception("Empty isolate IDs list");
	}

	// Make the configuration array
	for (String isolateId : isolateIdsList) {

	    // Get the framework
	    String isolateFramework = getConfigurationStr(isolateId,
		    IMasterManagerConfig.ISOLATE_FRAMEWORK);

	    IsolateKind kind;

	    if (isolateFramework == null) {
		System.out.println("No kind indicated");
		kind = DEFAULT_ISOLATE_KIND;

	    } else {
		try {
		    kind = IsolateKind.valueOf(isolateFramework);
		} catch (IllegalArgumentException ex) {
		    // Bad framework name value, use default
		    System.out.println("Bad kind. Use default - "
			    + isolateFramework);
		    kind = DEFAULT_ISOLATE_KIND;
		}
	    }

	    // Get the bundles
	    BundleRef[] bundlesRef = getBundlesRef(isolateId);

	    // Get the isolate JVM arguments
	    List<String> isolateArgs = getConfigurationList(isolateId,
		    IMasterManagerConfig.ISOLATE_ARGS);

	    String[] isolateArgsArray = null;
	    if (isolateArgs != null) {
		isolateArgs.toArray(new String[0]);
	    }

	    // Store the configuration
	    IsolateConfiguration isolateConf = new IsolateConfiguration(
		    isolateId, kind, bundlesRef, isolateArgsArray);

	    pIsolatesConfiguration.put(isolateId, isolateConf);
	}
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
	    // DEBUG Removed for tests
	    // System.out.println("Read isolates");
	    // pLoggerSvc.logInfo(this, "validatePojo", "Read conf");
	    // readIsolatesDescription();

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
