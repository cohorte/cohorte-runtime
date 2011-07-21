/**
 * File:   CConfigSvc.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.config.IParamId;
import org.psem2m.isolates.config.IPlatformConfigurationConstants;
import org.psem2m.isolates.config.ISvcConfig;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * Implements the configuration service
 * 
 * @author Thomas Calmant
 */
public class CConfigSvc extends CPojoBase implements ISvcConfig {

    /** The list separator in values */
    public static final String LIST_SEPARATOR = ";";

    /** Read configuration */
    private Properties pConfiguration;

    /** Found configuration files */
    private final Set<File> pConfigurationFiles = new LinkedHashSet<File>();

    /** Log service, handled by iPOJO */
    private IActivityLoggerBase pLoggerSvc;

    /**
     * Default constructor
     */
    public CConfigSvc() {
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
	    configFolders.add(platformDir);
	}

	platformDir = System
		.getProperty(IPlatformConfigurationConstants.SYSTEM_PSEM2M_BASE);
	if (platformDir != null && !platformDir.isEmpty()) {
	    configFolders.add(platformDir);
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
     * @see
     * org.psem2m.isolates.config.ISvcConfig#getParamBool(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public Object getParam(final IParamId aParamId) {
	return pConfiguration.getProperty(aParamId.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.ISvcConfig#getParamDate(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public Boolean getParamBool(final IParamId aParamId) {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.ISvcConfig#getParamNum(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public Date getParamDate(final IParamId aParamId) {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.ISvcConfig#getParamList(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public List<Object> getParamList(final IParamId aParamId) {

	String paramStr = getParamStr(aParamId);
	if (paramStr == null) {
	    return null;
	}

	Object[] paramList = paramStr.split(LIST_SEPARATOR);
	return Arrays.asList(paramList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.ISvcConfig#getParamStr(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public Long getParamNum(final IParamId aParamId) {

	String paramStr = getParamStr(aParamId);
	if (paramStr == null) {
	    // Invalid param ID
	    return null;
	}

	try {
	    return Long.parseLong(paramStr);

	} catch (NumberFormatException ex) {
	    // Invalid number format
	    return null;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.ISvcConfig#isParamExists(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public String getParamStr(final IParamId aParamId) {
	return pConfiguration.getProperty(aParamId.getId());
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
     * @see
     * org.psem2m.isolates.config.ISvcConfig#isParamExists(org.psem2m.isolates
     * .config.IParamId)
     */
    @Override
    public boolean isParamExists(final IParamId aParamId) {
	return pConfiguration.containsKey(aParamId.getId());
    }

    /**
     * Clears the configuration and read all configuration files, as given by
     * {@link #pConfigurationFiles}.
     */
    protected void readConfiguration() {

	// Clear old configuration
	pConfiguration.clear();

	// Read files in the given order
	for (File configFile : pConfigurationFiles) {

	    try {
		// Try to read the XML file
		FileInputStream inputStream = new FileInputStream(configFile);
		pConfiguration.loadFromXML(inputStream);

	    } catch (InvalidPropertiesFormatException e) {

		pLoggerSvc.logWarn(this, "readConfiguration",
			"Invalid properties file format", e);

	    } catch (IOException e) {

		pLoggerSvc.logWarn(this, "readConfiguration",
			"Error reading properties file", e);
	    }
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

	// Find configuration files
	if (!findConfigurationFiles()) {
	    pLoggerSvc.logSevere(this, "validatePojo",
		    "No configuration files have been found.");

	    throw new BundleException("No configuration files have been found.");
	}

	// Parse'em all
	readConfiguration();
    }
}
