/**
 * File:   CMasterManager.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.commons.IBundleRef;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.commons.IPlatformConfiguration;
import org.psem2m.isolates.commons.Utilities;
import org.psem2m.isolates.commons.forker.IsolateConfiguration;
import org.psem2m.isolates.commons.impl.PlatformConfiguration;
import org.psem2m.isolates.config.IParamId;
import org.psem2m.isolates.config.ISvcConfig;
import org.psem2m.isolates.master.manager.IMasterManagerConfig;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author Thomas Calmant
 * 
 */
public class CMasterManager extends CPojoBase {

    /** Default isolate kind */
    private static final IsolateKind DEFAULT_ISOLATE_KIND = IsolateKind.FELIX;

    /** Available configuration */
    private ISvcConfig pConfiguration;

    /** Isolates configuration list */
    private final List<IIsolateConfiguration> pIsolatesConfiguration = new ArrayList<IIsolateConfiguration>();

    /** Log service, handled by iPOJO */
    private IActivityLoggerBase pLoggerSvc;

    /** The platform configuration */
    private IPlatformConfiguration pPlatformConfiguration = new PlatformConfiguration();

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
    protected IBundleRef[] getBundlesRef(final String aIsolateId) {

	List<String> isolateBundles = getConfigurationList(aIsolateId,
		IMasterManagerConfig.ISOLATE_BUNDLES_LIST);
	if (isolateBundles == null || isolateBundles.isEmpty()) {
	    // Ignore empty list
	    return null;
	}

	Set<IBundleRef> bundlesRef = new LinkedHashSet<IBundleRef>(
		isolateBundles.size());

	for (String bundleName : isolateBundles) {

	    IBundleRef ref = Utilities.findBundle(pPlatformConfiguration,
		    bundleName);
	    if (ref != null) {
		bundlesRef.add(ref);
	    } else {
		// Return null on error
		return null;
	    }
	}

	return bundlesRef.toArray(new IBundleRef[0]);
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
		strList.add(rawObject.toString());
	    }
	}

	return strList;
    }

    /**
     * Reads a string from the given configuration pair
     * 
     * @param aPrefix
     *            Parameter ID prefix (without trailing dot)
     * @param aKey
     *            Parameter ID key
     * @return Null on error, a string on success
     */
    protected String getConfigurationStr(final String aPrefix, final String aKey) {
	return pConfiguration.getParamStr(makeParamId(aPrefix + "." + aKey));
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
     * Prepares a ParamId with the Master Manager prefix
     * 
     * @param aKey
     *            Master manager configuration key
     * @return The parameter ID
     */
    protected IParamId makeManagerParamId(final String aKey) {
	return makeParamId(IMasterManagerConfig.MANAGER_NAMESPACE + "." + aKey);
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
     * @return True on success, False on failure
     */
    protected boolean readIsolatesDescription() {

	// Empty current list
	pIsolatesConfiguration.clear();

	// Read the list
	List<String> isolateIdsList = getConfigurationList(
		IMasterManagerConfig.MANAGER_NAMESPACE,
		IMasterManagerConfig.ISOLATE_ID_LIST);

	if (isolateIdsList == null || isolateIdsList.isEmpty()) {
	    return false;
	}

	for (String isolateId : isolateIdsList) {

	    // Get the framework
	    String isolateFramework = getConfigurationStr(isolateId,
		    IMasterManagerConfig.ISOLATE_FRAMEWORK);

	    IsolateKind kind;
	    try {
		kind = IsolateKind.valueOf(isolateFramework);
	    } catch (IllegalArgumentException ex) {
		// Bad framework name value, use default
		kind = DEFAULT_ISOLATE_KIND;
	    }

	    // Get the bundles
	    IBundleRef[] bundlesRef = getBundlesRef(isolateId);

	    // Store the configuration
	    IsolateConfiguration isolateConf = new IsolateConfiguration(
		    isolateId, kind, bundlesRef);
	    pIsolatesConfiguration.add(isolateConf);
	}

	return true;
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

    }
}
