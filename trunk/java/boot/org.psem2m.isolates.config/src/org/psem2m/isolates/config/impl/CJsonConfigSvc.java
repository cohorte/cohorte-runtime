/**
 * File:   CJsonConfigSvc.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config.impl;

import java.io.FileNotFoundException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.config.IPlatformConfigurationConstants;
import org.psem2m.isolates.config.json.JsonConfigReader;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.BundleDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.utilities.CXListUtils;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * Implements the configuration service
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-config-json-factory", publicFactory = false)
@Provides(specifications = ISvcConfig.class)
@Instantiate(name = "psem2m-config-json")
public class CJsonConfigSvc extends CPojoBase implements ISvcConfig {

    /** Minimum age of the configuration before a reload (in milliseconds) */
    private static final long MINIMUM_AGE = 1000;

    /** The current isolate description */
    private IsolateDescription pCurrentIsolate;

    /** File finder service, injected by iPOJO */
    @Requires
    private IFileFinderSvc pFileFinder;

    /** Time stamp of the last configuration load */
    private long pLastLoad;

    /** Log service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** JSON Configuration reader */
    private JsonConfigReader pReader;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.ISvcConfig#getApplication()
     */
    @Override
    public ApplicationDescription getApplication() {

        final String[] appIds = pReader.getApplicationIds();
        if (appIds == null || appIds.length < 1) {
            return null;
        }

        return pReader.getApplication(appIds[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.conf.ISvcConfig#getCurrentIsolate()
     */
    @Override
    public synchronized IsolateDescription getCurrentIsolate() {

        if (pCurrentIsolate != null) {
            // Return the forced current isolate value
            return pCurrentIsolate;
        }

        // Return the read configuration
        final String isolateId = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
        return getApplication().getIsolate(isolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() {

        // Clean up
        pLastLoad = 0;
        pCurrentIsolate = null;
        pReader = null;

        // logs in the bundle logger
        pLogger.logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());
    }

    /**
     * log a dump of the config
     */
    private void logDumpConfig() {

        pLogger.logInfo(this, "logDumpConfig", "Application=", getApplication()
                .getApplicationId());
        for (final String wIsolateId : getApplication().getIsolateIds()) {

            pLogger.logInfo(this, "logDumpConfig", " - IsolateId=", wIsolateId);

            for (final BundleDescription wIBundleDescr : getApplication()
                    .getIsolate(wIsolateId).getBundles()) {

                pLogger.logInfo(this, "logDumpConfig", "   - Bundle=",
                        wIBundleDescr.getSymbolicName(), "Optional=",
                        wIBundleDescr.getOptional(), "Version=",
                        wIBundleDescr.getVersion());

                if (wIBundleDescr.hasProperties()) {
                    pLogger.logInfo(this, "logDumpConfig",
                            "     - Properties=", CXListUtils
                                    .PropertiesToString(wIBundleDescr
                                            .getProperties()));
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.conf.ISvcConfig#parseIsolate(java.lang.String
     * )
     */
    @Override
    public IsolateDescription parseIsolate(final String aConfigurationString) {

        try {
            // Parse the string
            final JSONObject isolateObj = new JSONObject(aConfigurationString);

            // Parse the JSON object
            return pReader.parseIsolate(isolateObj, null);

        } catch (final JSONException ex) {
            // Parse error
            pLogger.logWarn(this, "parseIsolate",
                    "Error parsing configuration=\n", aConfigurationString,
                    "\nException=", ex);

        } catch (final FileNotFoundException ex) {
            // Should never happen
            pLogger.logWarn(this, "parseIsolate", "Error looking for file=",
                    ex.getMessage(), "referenced in configuration=\n",
                    aConfigurationString);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.ISvcConfig#refresh()
     */
    @Override
    public boolean refresh() {

        if (System.currentTimeMillis() - pLastLoad < MINIMUM_AGE) {
            // Too soon
            return false;
        }

        pLastLoad = System.currentTimeMillis();
        return pReader.load(IPlatformConfigurationConstants.FILE_MAIN_CONF,
                pFileFinder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.conf.ISvcConfig#setCurrentIsolate(org.psem2m
     * .isolates.services.conf.beans.IsolateDescription)
     */
    @Override
    public synchronized void setCurrentIsolate(
            final IsolateDescription aIsolateDescription) {

        pCurrentIsolate = aIsolateDescription;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Prepare the JSON reader
        pReader = new JsonConfigReader();

        // logs in the bundle logger
        pLogger.logInfo(this, "validatePojo", "VALIDATE", toDescription());

        // Read the configuration
        refresh();

        // Log a dump of the config
        logDumpConfig();
    }
}
