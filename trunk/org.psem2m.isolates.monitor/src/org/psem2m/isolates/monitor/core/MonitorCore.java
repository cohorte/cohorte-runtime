/**
 * File:   MonitorCore.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.isolates.monitor.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
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
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.forker.IForker.EStartError;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
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

    /** Last isolate status time stamp for each isolate */
    private final Map<String, Long> pLastIsolatesStatusUID = new HashMap<String, Long>();

    /** Log service */
    @Requires
    private LogService pLogger;

    /** Platform directories service */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Platform state flag */
    private boolean pPlatformRunning = true;

    /** Forker handler presence property */
    @ServiceProperty(name = "forker-handler-present", value = "false")
    private boolean pPropertyForkerHandlerPresent = false;

    /** Forker service presence property */
    @ServiceProperty(name = "forker-service-present", value = "false")
    private boolean pPropertyForkerPresent = false;

    /** Signal sender */
    @Requires
    private ISignalBroadcaster pSignalSender;

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

        // Subscribe to the full-platform stop signal
        aSignalReceiver.registerListener(
                ISignalsConstants.MONITOR_SIGNAL_STOP_PLATFORM, this);

        // Subscribe to 'isolate lost' signal
        aSignalReceiver.registerListener(ISignalsConstants.ISOLATE_LOST_SIGNAL,
                this);

        // Subscribe to isolate status
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

        if (pPlatformRunning && pPropertyForkerHandlerPresent
                && aIsolateStatus.getState() == IsolateStatus.STATE_FAILURE) {

            pForkerHandler.startForker();
        }
    }

    protected void handleIsolateFailure(final String aIsolateId) {

        // Normal isolate handling : when it fails, restart it
        startIsolate(aIsolateId);

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

        if (isStatusObsolete(aIsolateStatus)) {
            // Ignore status if it's too old
            System.out.println("Obsolete status : " + aIsolateStatus);
            return;
        }

        // Source isolate ID
        final String sourceIsolateId = aIsolateStatus.getIsolateId();

        if (IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER
                .equals(sourceIsolateId)) {
            // The forker is a special case
            handleForkerStatus(aIsolateStatus);

        } else {
            if (aIsolateStatus.getState() == IsolateStatus.STATE_FAILURE) {
                // Handle failure
                handleIsolateFailure(sourceIsolateId);

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

        // Get the signal content (can be null)
        final Object signalContent = aSignalData.getSignalContent();

        // Signal status
        if (aSignalName.equals(ISignalsConstants.ISOLATE_STATUS_SIGNAL)) {
            // Test if the signal content is an isolate status
            if (signalContent instanceof IsolateStatus) {
                handleIsolateStatusEvent((IsolateStatus) signalContent);
            }

        } else if (aSignalName.equals(ISignalsConstants.ISOLATE_LOST_SIGNAL)) {
            // Isolate lost -> ID in the signal content
            if (signalContent instanceof CharSequence) {
                handleIsolateFailure(String.valueOf(signalContent));
            }

        } else if (aSignalName
                .equals(ISignalsConstants.MONITOR_SIGNAL_STOP_PLATFORM)) {
            // Stop everything
            stopPlatform();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Clear the time stamps list
        pLastIsolatesStatusUID.clear();

        pLogger.log(LogService.LOG_INFO, "PSEM2M Monitor Core Gone");
    }

    /**
     * Tests if the given status is obsolete,
     * 
     * @param aIsolateStatus
     *            The status to test
     * @return True if the status is obsolete
     */
    protected boolean isStatusObsolete(final IsolateStatus aIsolateStatus) {

        final String sourceIsolateId = aIsolateStatus.getIsolateId();

        // Status time stamp
        final long statusStamp = aIsolateStatus.getStatusUID();

        // Test if the status is too old or duplicated
        final Long lastStamp = pLastIsolatesStatusUID.get(sourceIsolateId);
        if (lastStamp != null) {
            // We already read something from this isolate
            if (lastStamp.longValue() >= statusStamp) {
                /*
                 * We read something after this one, or already read this one,
                 * so ignore it
                 */
                return true;
            }
        }

        // Update the status time stamp
        pLastIsolatesStatusUID.put(sourceIsolateId, statusStamp);
        return false;
    }

    /**
     * Starts the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return True on success, false on error
     */
    protected boolean startIsolate(final String aIsolateId) {

        if (!pPlatformRunning) {
            // Platform is stopped
            return false;
        }

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
     * Stops the platform.
     * 
     * Deactivate the startIsolate() method, sends a stop signal to all
     * isolates, waits for all "suicide" signals. In case of timeout, tells the
     * forker (if any) to kill the remaining isolates.
     */
    protected void stopPlatform() {

        // Deactivate startIsolate()
        pPlatformRunning = false;

        // Kill other monitors (not including ourselves)
        pSignalSender.sendData(ISignalBroadcaster.EEmitterTargets.MONITORS,
                ISignalsConstants.ISOLATE_STOP_SIGNAL, null);

        // Send a stop signal to isolates
        pSignalSender.sendData(ISignalBroadcaster.EEmitterTargets.ISOLATES,
                ISignalsConstants.ISOLATE_STOP_SIGNAL, null);

        // TODO Wait for stop completion

        // TODO Kill remaining isolates

        // Kill the forker
        pForkerHandler.stopForker();

        // Last man standing...
        pSignalSender.sendData(ISignalBroadcaster.EEmitterTargets.LOCAL,
                ISignalsConstants.ISOLATE_STOP_SIGNAL, null);
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
    @Validate
    public void validatePojo() throws BundleException {

        // Clear the time stamps list
        pLastIsolatesStatusUID.clear();

        // Write our access URL to the $BASE/var/monitor.access file
        writeAccessFile();

        pLogger.log(LogService.LOG_INFO, "PSEM2M Monitor Core Ready");
    }

    /**
     * Tries to write this monitor access URL to $BASE/var/monitor.access.
     * 
     * Does nothing on error.
     */
    protected void writeAccessFile() {

        try {
            // Get the monitor access URL
            final String isolateId = pPlatformDirsSvc.getIsolateId();
            final String accessUrl = pConfiguration.getApplication()
                    .getIsolate(isolateId).getAccessUrl();

            // Create the file
            final File accessFile = new File(
                    pPlatformDirsSvc.getPlatformBaseDir(),
                    "/var/monitor.access");
            if (!accessFile.exists()) {
                accessFile.createNewFile();
            }

            System.out.println("Writing to : " + accessFile);

            // Write our access URL
            final OutputStream outputStream = new FileOutputStream(accessFile);
            outputStream.write(accessUrl.getBytes());
            outputStream.close();

        } catch (Exception e) {
            // Error reading the configuration, too bad, but not important
            e.printStackTrace();
        }
    }
}
