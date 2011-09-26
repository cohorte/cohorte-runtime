/**
 * File:   MonitorCore.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.isolates.monitor.core;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.isolates.IForkerHandler;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.forker.IForker.EStartError;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * Core monitor logic, based on IForkerStarter and IForker
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-monitor-core-factory", publicFactory = false)
@Instantiate(name = "psem2m-monitor-core")
public class MonitorCore extends CPojoBase implements
        IIsolateStatusEventListener, ISignalListener {

    /** Configuration service */
    @Requires
    private ISvcConfig pConfiguration;

    /** Forker starter service */
    @Requires(id = "forker-handler", optional = true)
    private IForkerHandler pForkerHandler;

    /** The forker service */
    @Requires(id = "forker-service", optional = true)
    private IForker pForkerSvc;

    /** Log service */
    @Requires
    private LogService pLogger;

    /** TODO Forker handler presence property */
    private boolean pPropertyForkerHandlerPresent = false;

    /** TODO Forker service presence property */
    private boolean pPropertyForkerPresent = false;

    /**
     * Default constructor
     */
    public MonitorCore() {

        super();
    }

    /**
     * Called by iPOJO when a forker handler is bound
     * 
     * @param aForkerHandler
     *            The bound forker handler
     */
    @Bind(id = "forker-handler")
    protected void bindForkerHandler(final IForkerHandler aForkerHandler) {

        pPropertyForkerHandlerPresent = true;
        aForkerHandler.registerIsolateEventListener(this);

        // Immediately try to start the forker
        aForkerHandler.startForker();
    }

    /**
     * Called by iPOJO when a forker service is bound.
     * 
     * @param aForker
     *            A forker service
     */
    @Bind(id = "forker-service")
    protected void bindForkerService(final IForker aForker) {

        // Update the service state
        pPropertyForkerPresent = true;

        // Get the current isolate ID, to avoid running ourselves
        final String currentIsolateId = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);

        for (String isolateId : pConfiguration.getApplication().getIsolateIds()) {

            if (isolateId.equals(currentIsolateId)) {
                // Do not start ourselves
                continue;

            } else if (isolateId
                    .equals(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)) {
                // Do not start the forker that way
                continue;

            } else {
                // Start the damn thing
                startIsolate(isolateId);
            }
        }
    }

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            A signal receiver
     */
    @Bind(id = "signal-receiver")
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        aSignalReceiver.registerListener(
                ISignalsConstants.ISOLATE_STATUS_SIGNAL, this);
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
     * Handles forker status messages
     * 
     * @param aIsolateStatus
     *            A forker isolate status
     */
    protected void handleForkerStatus(final IsolateStatus aIsolateStatus) {

        if (pPropertyForkerHandlerPresent
                && aIsolateStatus.getState() == IsolateStatus.STATE_FAILURE) {

            pForkerHandler.startForker();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IIsolateStatusEventListener#
     * handleIsolateStatusEvent
     * (org.psem2m.isolates.base.isolates.boot.IsolateStatus)
     */
    @Override
    public void handleIsolateStatusEvent(final IsolateStatus aIsolateStatus) {

        final String sourceIsolateId = aIsolateStatus.getIsolateId();

        if (IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER
                .equals(sourceIsolateId)) {

            // The forker is a special case
            handleForkerStatus(aIsolateStatus);

        } else {

            if (aIsolateStatus.getState() == IsolateStatus.STATE_FAILURE) {
                // Normal isolate handling : when it fails, restart it
                startIsolate(sourceIsolateId);

            } else {
                // Simply log
                System.out.println("MonitorCore received status : "
                        + aIsolateStatus);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Test if the signal content is an isolate status
        final Object signalContent = aSignalData.getSignalContent();
        if (signalContent instanceof IsolateStatus) {
            handleIsolateStatusEvent((IsolateStatus) signalContent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "PSEM2M Monitor Core Gone");
    }

    /**
     * Starts the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return True on success, false on error
     */
    protected boolean startIsolate(final String aIsolateId) {

        if (!pPropertyForkerPresent) {
            // No forker service
            return false;
        }

        if (aIsolateId == null) {
            // Invalid ID
            return false;
        }

        final IIsolateDescr isolateDescr = pConfiguration.getApplication()
                .getIsolate(aIsolateId);
        if (isolateDescr == null) {
            // Unknown isolate
            return false;
        }

        final EStartError result = pForkerSvc.startIsolate(isolateDescr);

        // Success if the isolate is running (even if we done nothing)
        return result == EStartError.SUCCESS
                || result == EStartError.ALREADY_RUNNING;
    }

    /**
     * Asks the forker service to stop the given isolate.
     * 
     * @param aIsolateId
     *            The ID of the isolate to stop
     * @return True if the forker was called.
     */
    protected boolean stopIsolate(final String aIsolateId) {

        if (!pPropertyForkerPresent) {
            return false;
        }

        pForkerSvc.stopIsolate(aIsolateId);
        return true;
    }

    /**
     * Called by iPOJO when the forker handler is gone
     * 
     * @param aForkerHandler
     *            A forker handler
     */
    @Unbind(id = "forker-handler")
    protected void unbindForkerHandler(final IForkerHandler aForkerHandler) {

        pPropertyForkerHandlerPresent = false;
        aForkerHandler.unregisterIsolateEventListener(this);
    }

    /**
     * Called by iPOJO when a forker service is gone.
     * 
     * @param aForker
     *            A forker service
     */
    @Unbind(id = "forker-service")
    protected void unbindForkerService(final IForker aForker) {

        // Update the service state
        pPropertyForkerPresent = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "PSEM2M Monitor Core Ready");
    }
}
