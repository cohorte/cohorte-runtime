/**
 * File:   CJsonConfigSvc.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config.impl;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.config.IPlatformConfigurationConstants;
import org.psem2m.isolates.config.json.JsonConfigReader;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.BundleDescription;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.utilities.CXListUtils;

/**
 * Implements the configuration service
 * 
 * @author Thomas Calmant
 */
public class CJsonConfigSvc extends CPojoBase implements ISvcConfig {

    /** Minimum age of the configuration before a reload (in milliseconds) */
    public static final long MINIMUM_AGE = 1000;

    /** File finder service, injected by iPOJO */
    private IFileFinderSvc pFileFinder;

    /** Log service, injected by iPOJO */
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /** Time stamp of the last configuration load */
    private long pLastLoad;

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
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

        // logs in the bundle logger
        pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
                toDescription());
    }

    /**
     * log a dump of the config
     */
    private void logDumpConfig() {

        pIsolateLoggerSvc.logInfo(this, "logDumpConfig", "Application=",
                getApplication().getApplicationId());
        for (final String wIsolateId : getApplication().getIsolateIds()) {

            pIsolateLoggerSvc.logInfo(this, "logDumpConfig", " - IsolateId=",
                    wIsolateId);

            for (final BundleDescription wIBundleDescr : getApplication()
                    .getIsolate(wIsolateId).getBundles()) {

                pIsolateLoggerSvc.logInfo(this, "logDumpConfig",
                        "   - Bundle=", wIBundleDescr.getSymbolicName(),
                        "Optional=", wIBundleDescr.getOptional(), "Version=",
                        wIBundleDescr.getVersion());

                if (wIBundleDescr.hasProperties()) {
                    pIsolateLoggerSvc.logInfo(this, "logDumpConfig",
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
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        // logs in the bundle logger
        pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
                toDescription());

        // Read the configuration
        refresh();

        // Log a dump of the config
        logDumpConfig();
    }
}
