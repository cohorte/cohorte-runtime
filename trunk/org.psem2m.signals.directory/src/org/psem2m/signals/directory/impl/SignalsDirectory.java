/**
 * File:   SignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.signals.directory.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.EEmitterTargets;
import org.psem2m.isolates.services.remote.signals.ISignalsDirectory;

/**
 * Simple implementation of the PSEM2M Signals isolates directory, based on the
 * PSEM2M Configuration service
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-directory-factory", publicFactory = false)
@Provides(specifications = ISignalsDirectory.class)
@Instantiate(name = "psem2m-signals-directory")
public class SignalsDirectory extends CPojoBase implements ISignalsDirectory {

    /** The configuration service */
    @Requires
    private ISvcConfig pConfiguration;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Retrieves the list of all non-internal isolates, testing whether the ID
     * starts with {@link IPlatformProperties#SPECIAL_ISOLATE_ID_FORKER} or not.
     * 
     * @return All non-internal isolates ID (never null)
     */
    protected String[] getAllIsolates() {

        final List<String> isolatesList = new ArrayList<String>();

        for (final String isolateId : pConfiguration.getApplication()
                .getIsolateIds()) {

            if (isValidIsolate(isolateId)
                    && !isolateId
                            .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
                // The isolate ID doesn't have the internal prefix
                isolatesList.add(isolateId);
            }
        }

        return isolatesList.toArray(new String[isolatesList.size()]);
    }

    /**
     * Retrieves the list of all internal isolates, except the forker, testing
     * whether the ID starts with
     * {@link IPlatformProperties#SPECIAL_ISOLATE_ID_FORKER} or not.
     * 
     * @return All monitors isolates ID (never null)
     */
    protected String[] getAllMonitors() {

        final List<String> monitorsList = new ArrayList<String>();

        for (final String isolateId : pConfiguration.getApplication()
                .getIsolateIds()) {

            if (isValidIsolate(isolateId)
                    && isolateId
                            .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {

                monitorsList.add(isolateId);
            }
        }

        return monitorsList.toArray(new String[monitorsList.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolate
     * (java.lang.String)
     */
    @Override
    public String getIsolate(final String aIsolateId) {

        final IIsolateDescr isolate = pConfiguration.getApplication()
                .getIsolate(aIsolateId);

        if (isolate == null) {
            pLogger.logInfo(this, "// getIsolate //", "Unknown isolate=",
                    aIsolateId);
            return null;
        }

        return isolate.getAccessUrl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (java.util.Collection)
     */
    @Override
    public String[] getIsolates(final Collection<String> aIsolatesIds) {

        if (aIsolatesIds == null) {
            pLogger.logInfo(this, "// getIsolates //", "Null collection");
            return null;
        }

        return getIsolates(aIsolatesIds
                .toArray(new String[aIsolatesIds.size()]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.
     * EEmitterTargets)
     */
    @Override
    public String[] getIsolates(final EEmitterTargets aTargets) {

        switch (aTargets) {

        case FORKER: {
            // Special case : the forker
            final String accessStr = getIsolate(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER);
            if (accessStr == null) {
                pLogger.logInfo(this, "// getIsolates //", "No URL to forker");
                return null;
            }

            return new String[] { accessStr };
        }

        case ISOLATES:
            // Non-internal isolates
            return getIsolates(getAllIsolates());

        case MONITORS:
            // All monitors
            return getIsolates(getAllMonitors());

        case ALL: {
            // Monitors and isolates
            final Set<String> allIsolates = pConfiguration.getApplication()
                    .getIsolateIds();
            final Set<String> accessStrings = new HashSet<String>(
                    allIsolates.size());

            for (final String isolateId : allIsolates) {

                final IIsolateDescr isolate = pConfiguration.getApplication()
                        .getIsolate(isolateId);

                if (isolate != null && isValidIsolate(isolateId)) {
                    accessStrings.add(isolate.getAccessUrl());
                }
            }

            return accessStrings.toArray(new String[accessStrings.size()]);
        }
        }

        // Unknown target
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (java.lang.String[])
     */
    @Override
    public String[] getIsolates(final String[] aIsolatesIds) {

        if (aIsolatesIds == null) {
            pLogger.logInfo(this, "// getIsolates //", "Null array");
            return null;
        }

        final List<String> isolates = new ArrayList<String>(aIsolatesIds.length);

        for (final String isolateId : aIsolatesIds) {
            final IIsolateDescr isolate = pConfiguration.getApplication()
                    .getIsolate(isolateId);
            if (isolate != null) {
                // Store the access URL, directly
                isolates.add(isolate.getAccessUrl());

            } else {
                pLogger.logInfo(this, "// getIsolates //", "Unknown isolate=",
                        isolateId);
            }
        }

        if (isolates.isEmpty()) {
            return null;
        }

        return isolates.toArray(new String[isolates.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Invalidate
    @Override
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Signals directory gone");
    }

    /**
     * Tests if the given isolate ID can be used in a "getAllXXX" method.
     * Returns false if the isolate ID is the current one or the forker one.
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return True if the isolate ID can be used
     */
    protected boolean isValidIsolate(final String aIsolateId) {

        if (aIsolateId == null || aIsolateId.isEmpty()) {
            pLogger.logInfo(this, "// isValidIsolate //", "Invalid isolate=",
                    aIsolateId);
            return false;
        }

        // Isolate ID can change on slave agent order
        final String currentIsolateId = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);

        return !aIsolateId
                .equals(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)
                && !aIsolateId.equals(currentIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#reloadDirectory
     * ()
     */
    @Override
    public boolean reloadDirectory() {

        final boolean firstLoad = (pConfiguration.getApplication() == null);

        // Store previous isolates access strings
        final String[] oldAccesses;

        if (!firstLoad) {
            oldAccesses = getIsolates(ISignalBroadcaster.EEmitterTargets.ALL);
            Arrays.sort(oldAccesses);

        } else {
            // First load : can't compare with the previous content...
            oldAccesses = null;
        }

        // Reload the configuration
        pConfiguration.refresh();

        if (firstLoad) {
            // Stop here on first load
            return pConfiguration.getApplication() != null;
        }

        // Get new access strings
        final String[] newAccesses = getIsolates(ISignalBroadcaster.EEmitterTargets.ALL);
        Arrays.sort(newAccesses);

        // Compare *sorted* arrays
        return Arrays.equals(newAccesses, oldAccesses);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Validate
    @Override
    public void validatePojo() throws BundleException {

        reloadDirectory();
        pLogger.logInfo(this, "validatePojo", "Signals directory ready");
    }
}
