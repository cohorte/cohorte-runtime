/**
 * File:   CJsonConfigSvc.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config.impl;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.config.IPlatformConfigurationConstants;
import org.psem2m.isolates.config.json.JsonConfigReader;
import org.psem2m.isolates.services.conf.IApplicationDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * Implements the configuration service
 * 
 * @author Thomas Calmant
 */
public class CJsonConfigSvc extends CPojoBase implements ISvcConfig {

    /** Found configuration files */
    private final Set<File> pConfigurationFiles = new LinkedHashSet<File>();

    /** File finder service, injected by iPOJO */
    private IFileFinderSvc pFileFinder;

    /** Log service, injected by iPOJO */
    private IActivityLoggerBase pLoggerSvc;

    /** JSON Configuration reader */
    private final JsonConfigReader pReader = new JsonConfigReader();

    /**
     * Default constructor
     */
    public CJsonConfigSvc() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.ISvcConfig#getParam(org.psem2m.isolates.config
     * .IParamId)
     */
    @Override
    public void destroy() {
	// ...
    }

    /**
     * Tries to find all possible configuration files, in priority-descending
     * order
     * 
     * @return True if at least one configuration file has been found
     */
    protected boolean findConfigurationFiles() {

	// Clear old values
	pConfigurationFiles.clear();

	final Set<String> configFolders = new LinkedHashSet<String>();

	// Add extra "standard" configuration folders
	configFolders.addAll(Arrays
		.asList(IPlatformConfigurationConstants.EXTRA_CONF_FOLDERS));

	// Get the home then base folders, if they exist
	String platformDir = System
		.getProperty(IPlatformConfigurationConstants.SYSTEM_PSEM2M_HOME);
	if (platformDir != null && !platformDir.isEmpty()) {
	    configFolders.add(platformDir + File.separator
		    + IPlatformConfigurationConstants.SUBDIR_CONF);
	}

	platformDir = System
		.getProperty(IPlatformConfigurationConstants.SYSTEM_PSEM2M_BASE);
	if (platformDir != null && !platformDir.isEmpty()) {
	    configFolders.add(platformDir + File.separator
		    + IPlatformConfigurationConstants.SUBDIR_CONF);
	}

	// Test configuration files existence
	for (String folder : configFolders) {

	    File configFile = new File(folder,
		    IPlatformConfigurationConstants.FILE_MAIN_CONF);

	    if (configFile.isFile()) {
		pConfigurationFiles.add(configFile);
	    }
	}

	return !pConfigurationFiles.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.ISvcConfig#getApplication()
     */
    @Override
    public IApplicationDescr getApplication() {

	final String[] appIds = pReader.getApplicationIds();
	if (appIds == null || appIds.length < 1) {
	    return null;
	}

	return pReader.getApplication(appIds[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

	// logs in the bundle logger
	pLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
		toDescription());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.ISvcConfig#refresh()
     */
    @Override
    public boolean refresh() {
	return pReader.load(IPlatformConfigurationConstants.FILE_MAIN_CONF,
		pFileFinder);
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

	// Read the configuration
	refresh();
    }
}
