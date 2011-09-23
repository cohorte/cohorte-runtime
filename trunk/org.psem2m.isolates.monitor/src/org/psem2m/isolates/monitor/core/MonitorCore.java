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
import org.psem2m.isolates.base.isolates.IIsolateHandler;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * Core monitor logic, based on ForkerStarter and IsolateStarter
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

    /** Isolate starter service */
    @Requires(id = "isolate-handler", optional = true)
    private IIsolateHandler pIsolateHandler;

    /** Log service */
    @Requires
    private LogService pLogger;

    /** TODO Forker handler presence property */
    private boolean pPropertyForker = false;

    /** TODO Forker handler presence property */
    private boolean pPropertyIsolate = false;

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

        pPropertyForker = true;
        aForkerHandler.registerIsolateEventListener(this);

        // Immediately try to start the forker
        startForker();
    }

    /**
     * Called by iPOJO when an isolate handler is bound
     * 
     * @param aIsolateHandler
     *            An isolate handler
     */
    @Bind(id = "isolate-handler")
    protected void bindIsolateHandler(final IIsolateHandler aIsolateHandler) {

        pPropertyIsolate = true;
        aIsolateHandler.registerIsolateEventListener(this);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IIsolateStatusEventListener#
     * handleIsolateStatusEvent
     * (org.psem2m.isolates.base.isolates.boot.IsolateStatus)
     */
    @Override
    public void handleIsolateStatusEvent(final IsolateStatus aIsolateStatus) {

        System.out.println("MonitorCore received status : " + aIsolateStatus);
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

        if (aSignalData instanceof IsolateStatus) {
            handleIsolateStatusEvent((IsolateStatus) aSignalData);
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
     * Starts the forker, using the forker handler
     * 
     * @return True on success, false on error
     */
    protected boolean startForker() {

        if (!pPropertyForker) {
            // No forker service
            return false;
        }

        return pForkerHandler.startForker();
    }

    /**
     * Starts the given isolate description
     * 
     * @param aIsolateDescr
     *            An isolate description
     * @return True on success, false on error
     */
    protected boolean startIsolate(final IIsolateDescr aIsolateDescr) {

        if (!pPropertyIsolate) {
            return false;
        }

        return pIsolateHandler.startIsolate(aIsolateDescr, false);
    }

    /**
     * Starts the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return True on success, false on error
     */
    protected boolean startIsolate(final String aIsolateId) {

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

        return startIsolate(isolateDescr);
    }

    /**
     * Called by iPOJO when the forker handler is gone
     * 
     * @param aForkerHandler
     *            A forker handler
     */
    @Unbind(id = "forker-handler")
    protected void unbindForkerHandler(final IForkerHandler aForkerHandler) {

        pPropertyForker = false;
        aForkerHandler.unregisterIsolateEventListener(this);
    }

    /**
     * Called by iPOJO when the isolate handler is gone
     * 
     * @param aIsolateHandler
     *            A isolate handler
     */
    @Unbind(id = "isolate-handler")
    protected void unbindIsolateHandler(final IIsolateHandler aIsolateHandler) {

        pPropertyIsolate = false;
        aIsolateHandler.unregisterIsolateEventListener(this);
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
