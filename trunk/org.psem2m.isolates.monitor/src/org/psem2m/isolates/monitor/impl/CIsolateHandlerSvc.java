/**
 * File:   IsolateManager.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.monitor.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.isolates.IIsolateHandler;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.monitor.IBundleMonitorLoggerService;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.forker.IForker;

/**
 * Isolate manager : starts/restarts/stops isolates according to the
 * configuration
 */
@Component(name = "psem2m-isolate-handler-factory", publicFactory = false)
@Provides(specifications = IIsolateHandler.class)
@Instantiate(name = "psem2m-isolate-handler")
public class CIsolateHandlerSvc extends CPojoBase implements IIsolateHandler {

    /** Logger, injected by iPOJO **/
    private IBundleMonitorLoggerService pBundleMonitorLoggerSvc;

    /** Forker service, injected by iPOJO */
    @Requires
    private IForker pForkerSvc;

    /** IsolateStatus listeners */
    private final Set<IIsolateStatusEventListener> pIsolateListeners = new HashSet<IIsolateStatusEventListener>();

    /**
     * Initiates the manager
     */
    public CIsolateHandlerSvc() {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

        // logs in the bundle logger
        pBundleMonitorLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
                toDescription());
    }

    /**
     * Tests if the given isolate ID is an internal one, therefore if it should
     * be ignored while starting the platform.
     * 
     * @param aIsolateId
     *            ID to be tested
     * @return True if the ID should be ignored
     */
    protected boolean isInternalIsolate(final String aIsolateId) {

        if (aIsolateId == null) {
            // Invalid ID, don't use it
            return true;
        }

        if (aIsolateId
                .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
            // The ID begins with the internal prefix
            return true;
        }

        final String currentId = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
        if (aIsolateId.equals(currentId)) {
            // Current isolate ID is an internal one
            return true;
        }

        // Play with it
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.core.IIsolateHandler#registerIsolateEventListener
     * (org.psem2m.isolates.monitor.core.IIsolateStatusEventListener)
     */
    @Override
    public void registerIsolateEventListener(
            final IIsolateStatusEventListener aListener) {

        if (aListener != null) {
            synchronized (pIsolateListeners) {
                pIsolateListeners.add(aListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.isolates.IIsolateHandler#startIsolate(org.psem2m
     * .isolates.services.conf.IIsolateDescr, boolean)
     */
    @Override
    public boolean startIsolate(final IIsolateDescr aIsolateDescr,
            final boolean aForceRestart) {

        // Get the isolate configuration
        if (aIsolateDescr == null) {
            pBundleMonitorLoggerSvc.logWarn(this, "startIsolate",
                    "Invalid isolate description");
            return false;
        }

        try {
            // Start the process
            final IForker.EStartError result = pForkerSvc
                    .startIsolate(aIsolateDescr);

            pBundleMonitorLoggerSvc.logInfo(this, "startIsolate",
                    "Start isolate ", aIsolateDescr.getId(), " = ", result);

        } catch (Exception e) {
            // Should not happen
            pBundleMonitorLoggerSvc.logSevere(this, "startIsolate",
                    "Error preparing or starting isolate : ", e);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.isolates.IIsolateHandler#stopIsolate(java.lang
     * .String)
     */
    @Override
    public boolean stopIsolate(final String aIsolateId) {

        pForkerSvc.stopIsolate(aIsolateId);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.core.IIsolateHandler#
     * unregisterIsolateEventListener
     * (org.psem2m.isolates.monitor.core.IIsolateStatusEventListener)
     */
    @Override
    public void unregisterIsolateEventListener(
            final IIsolateStatusEventListener aListener) {

        if (aListener != null) {
            synchronized (pIsolateListeners) {
                pIsolateListeners.remove(aListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() {

        // logs in the bundle logger
        pBundleMonitorLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
                toDescription());
    }
}
