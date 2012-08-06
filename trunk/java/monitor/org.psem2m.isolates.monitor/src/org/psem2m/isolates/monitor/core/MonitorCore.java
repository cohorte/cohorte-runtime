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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.forker.IForker;
import org.psem2m.forker.IForkerEventListener;
import org.psem2m.forker.IForkerStatus;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.isolates.IForkerHandler;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.monitor.IPlatformMonitor;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Core monitor logic, based on IForkerStarter and IForker
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-monitor-core-factory", publicFactory = false)
@Provides(specifications = IPlatformMonitor.class)
@Instantiate(name = "psem2m-monitor-core")
public class MonitorCore extends CPojoBase implements
        IIsolateStatusEventListener, ISignalListener, IPlatformMonitor,
        IForkerEventListener {

    /** The forker handler dependency ID */
    private static final String FORKER_HANDLER_ID = "forker-handler";

    /** The forker service dependency ID */
    private static final String FORKER_ID = "forker-service";

    /** Maximum launch tries in a streak */
    public static final String PROPERTY_MAX_TRIES_STREAK = "org.psem2m.monitor.isolates.triesMaxStreak";

    /** Time to wait before killing isolates in a hard way */
    public static final String PROPERTY_STOP_PLATFORM_TIMEOUT = "org.psem2m.monitor.stop.timeout";

    /** Time to wait before next streak */
    public static final String PROPERTY_WAIT_TIME_BEFORE_STREAK = "org.psem2m.monitor.isolates.triesWaitBeforeStreak";

    /** Time to wait before next try when in a streak */
    public static final String PROPERTY_WAIT_TIME_IN_STREAK = "org.psem2m.monitor.isolates.triesWaitInStreak";

    /** Configuration service */
    @Requires
    private ISvcConfig pConfiguration;

    /** The isolate failure handler */
    private IsolateFailureHandler pFailureHandler;

    /** Forker starter service */
    @Requires(id = FORKER_HANDLER_ID, optional = true)
    private IForkerHandler pForkerHandler;

    /** The forker service */
    @Requires(id = FORKER_ID)
    private IForker pForkerSvc;

    /** The current isolate host name */
    private String pHostName;

    /** Stopped isolates (when platform is not running) */
    private final Set<String> pIsolatesToStop = new HashSet<String>();

    /** The thread launched during a StopPlatform event */
    private Thread pKillerThread;

    /** Last isolate status time stamp for each isolate */
    private final Map<String, Long> pLastIsolatesStatusUID = new HashMap<String, Long>();

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Platform directories service */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Platform state flag */
    private boolean pPlatformRunning = true;

    /** Forker handler presence property */
    @ServiceProperty(name = "forker-handler-present", value = "false")
    private boolean pPropertyForkerHandlerPresent = false;

    /** Signal sender */
    @Requires
    private ISignalBroadcaster pSignalSender;

    /** Time to wait for isolates to shut down (in milliseconds) */
    private int pStopPlatformTimeout;

    /** Semaphore used while the platform is stopping */
    private Semaphore pStopSemaphore;

    /**
     * Called by iPOJO when a forker handler is bound
     * 
     * @param aForkerHandler
     *            The bound forker handler
     */
    @Bind(id = FORKER_HANDLER_ID)
    protected void bindForkerHandler(final IForkerHandler aForkerHandler) {

        pPropertyForkerHandlerPresent = true;
        aForkerHandler.registerIsolateEventListener(this);
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

    /**
     * Retrieves the description of the given isolate ID if and only if it can
     * be started by the current monitor.
     * 
     * @return The isolate description, or null
     */
    public IsolateDescription getIsolateDescription(final String aIsolateId) {

        if (aIsolateId == null) {
            // Invalid ID
            return null;
        }

        final IsolateDescription isolateDescr = pConfiguration.getApplication()
                .getIsolate(aIsolateId);
        if (isolateDescr == null) {
            // Unknown isolate
            return null;
        }

        final String isolateNode = isolateDescr.getNode();
        if (isolateNode == null) {
            // No node name : refuse it
            return null;
        }

        return isolateDescr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.forker.IForkerEventListener#handleForkerEvent (
     * org.psem2m.forker.IForkerEventListener.EForkerEventType ,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void handleForkerEvent(final EForkerEventType aEventType,
            final String aForkerId, final String aHost) {

        // Get the current isolate ID
        final String currentIsolateId = pPlatformDirsSvc.getIsolateId();

        // Get the description of the application
        final ApplicationDescription appDescr = pConfiguration.getApplication();

        new Thread(new Runnable() {

            @Override
            public void run() {

                // Forker registered for an host, start all corresponding
                // isolates
                for (final String isolateId : appDescr.getIsolateIds()) {

                    final IsolateDescription isolateDescr = appDescr
                            .getIsolate(isolateId);

                    if (pForkerSvc.isOnNode(aForkerId, isolateDescr.getNode())) {

                        // Same host as the new forker
                        switch (aEventType) {
                        case REGISTERED:
                            if (isolateId.equals(currentIsolateId)) {
                                // Do not start ourselves
                                continue;

                            } else if (isolateId
                                    .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)) {
                                // Do not start a forker that way
                                continue;

                            } else {
                                // Start the isolate...
                                startIsolate(isolateId);
                            }
                            break;

                        case UNREGISTERED:
                            // Consider lost all isolates of this host
                            pSignalSender.fireGroup(
                                    ISignalsConstants.ISOLATE_LOST_SIGNAL,
                                    isolateId, EBaseGroup.ALL);
                            break;
                        }
                    }
                }
            }
        }).start();
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

    /**
     * Handles an isolate failure (lost or killed)
     * 
     * @param aIsolateId
     *            The isolate that failed
     */
    protected void handleIsolateFailure(final String aIsolateId) {

        if (!pPlatformRunning) {
            // Platform is stopping
            isolateStopped(aIsolateId);
            return;
        }

        // Tell the failure handler
        pFailureHandler.isolateFailed(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IIsolateStatusEventListener#
     * handleIsolateStatusEvent
     * (org.psem2m.isolates.base.isolates.boot.IsolateStatus)
     */
    @Override
    public void handleIsolateStatusEvent(final String aSourceIsolateId,
            final IsolateStatus aIsolateStatus) {

        if (aIsolateStatus == null) {
            // Contact lost with the isolate
            handleIsolateFailure(aSourceIsolateId);
            return;
        }

        if (isStatusObsolete(aIsolateStatus)) {
            // Ignore status if it's too old
            pLogger.logInfo(this, "handleIsolateStatusEvent",
                    "Obsolete status=", aIsolateStatus);
            return;
        }

        if (IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER
                .equals(aSourceIsolateId)) {
            // The forker is a special case
            handleForkerStatus(aIsolateStatus);

        } else {

            switch (aIsolateStatus.getState()) {
            case IsolateStatus.STATE_FAILURE:
                // Handle failure
                handleIsolateFailure(aSourceIsolateId);
                break;

            case IsolateStatus.STATE_FRAMEWORK_STOPPING:
            case IsolateStatus.STATE_FRAMEWORK_STOPPED:
                // Same treatment for both states : the isolate is gone
                isolateStopped(aSourceIsolateId);

                // Continue to default treatment

            default:
                // Simply log
                pLogger.logDebug(this, "", "MonitorCore ignored a status=",
                        aIsolateStatus);
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String, org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Get the signal content (can be null)
        final Object signalContent = aSignalData.getSignalContent();

        // Signal status
        if (aSignalName.equals(ISignalsConstants.ISOLATE_STATUS_SIGNAL)) {
            // Test if the signal content is an isolate status
            if (signalContent instanceof IsolateStatus) {
                final IsolateStatus status = (IsolateStatus) signalContent;
                handleIsolateStatusEvent(status.getIsolateId(), status);
            }

        } else if (aSignalName.equals(ISignalsConstants.ISOLATE_LOST_SIGNAL)) {
            // Isolate lost -> ID in the signal content
            if (signalContent instanceof CharSequence) {

                // Forward message to isolates
                pSignalSender.fire(ISignalsConstants.ISOLATE_LOST_SIGNAL,
                        signalContent, "ALL");

                handleIsolateFailure(String.valueOf(signalContent));
            }

        } else if (aSignalName
                .equals(ISignalsConstants.MONITOR_SIGNAL_STOP_PLATFORM)) {
            // Stop everything
            if (pPlatformRunning && pStopSemaphore == null) {
                stopPlatform();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Unregister...
        pForkerSvc.unregisterListener(this);

        // Clear the time stamps list
        pLastIsolatesStatusUID.clear();

        // Stop the failure handler
        pFailureHandler.stop();
        pFailureHandler = null;

        if (pKillerThread != null) {
            try {
                // Wait for the killer
                pKillerThread.join();

            } catch (final InterruptedException e) {
                // Ignore...
            }

            pKillerThread = null;
        }

        // Clean up host name
        pHostName = null;

        pLogger.logInfo(this, "invalidatePojo", "PSEM2M Monitor Core Gone");
    }

    /**
     * Handles the end of an isolate when the platform is stopping
     * 
     * @param aIsolateId
     *            The isolate that stopped
     */
    protected void isolateStopped(final String aIsolateId) {

        if (!pPlatformRunning) {

            synchronized (pIsolatesToStop) {
                pIsolatesToStop.remove(aIsolateId);

                // No more isolates to stop ?
                if (pIsolatesToStop.isEmpty()) {
                    // Unlock the idled stopPlatform() job
                    pStopSemaphore.release();
                }
            }
        }
    }

    /**
     * Tests if the given status is obsolete
     * 
     * @param aIsolateStatus
     *            The status to test
     * 
     * @return True if the status is obsolete
     */
    protected boolean isStatusObsolete(final IsolateStatus aIsolateStatus) {

        final String sourceIsolateId = aIsolateStatus.getIsolateId();

        // Status time stamp
        final long statusStamp = aIsolateStatus.getStatusUID();

        // Test if the status is too old or duplicated
        final Long lastStamp = pLastIsolatesStatusUID.get(sourceIsolateId);
        if (lastStamp != null && lastStamp.longValue() >= statusStamp) {
            /*
             * We already read something from this isolate and we read something
             * after this one, or already read this one, so ignore it
             */
            return true;
        }

        // Update the status time stamp
        pLastIsolatesStatusUID.put(sourceIsolateId, statusStamp);
        return false;
    }

    /**
     * Finishes the job of {@link #stopPlatform()} : kills the remaining
     * isolates with forker, then kills the forker and the current monitor.
     */
    protected void killRemainingIsolates() {

        // Compute remaining isolates
        synchronized (pIsolatesToStop) {
            pLogger.logInfo(this, "killRemainingIsolates",
                    "Remaining isolates=", pIsolatesToStop.size());

            // Kill remaining isolates
            for (final String isolateId : pIsolatesToStop) {
                pLogger.logInfo(this, "killRemainingIsolates",
                        "Killing isolate=", isolateId, "using the forker");
                pForkerSvc.stopIsolate(isolateId);
            }
        }

        // Kill the forker
        if (pPropertyForkerHandlerPresent) {
            // Try to use the handler to stop the forker
            pForkerHandler.stopForker();

        } else {
            // Else, use the stop signal
            pSignalSender.fireGroup(ISignalsConstants.ISOLATE_STOP_SIGNAL,
                    null, EBaseGroup.FORKERS);
        }

        // Last man standing...
        pSignalSender.fireGroup(ISignalsConstants.ISOLATE_STOP_SIGNAL, null,
                EBaseGroup.CURRENT);
    }

    /**
     * Retrieves the given system property integer. Returns the default value on
     * error.
     * 
     * @param aProperty
     *            A system property name
     * @param aDefaultValue
     *            A default value
     * @return The system property value or the default one on error
     */
    protected int readIntSystemProperty(final String aProperty,
            final int aDefaultValue) {

        try {
            return Integer.parseInt(System.getProperty(aProperty));
        } catch (final NumberFormatException e) {
            return aDefaultValue;
        }
    }

    /**
     * Starts the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return True on success, false on error
     */
    @Override
    public boolean startIsolate(final String aIsolateId) {

        if (!pPlatformRunning) {
            // Platform is stopped
            pLogger.logDebug(this, "startIsolate",
                    "Platform stopping. Ignoring order");
            return false;
        }

        final IsolateDescription isolateDescr = getIsolateDescription(aIsolateId);
        if (isolateDescr == null) {
            // The isolate can't be started by this monitor
            return false;
        }

        final int result = pForkerSvc.startIsolate(isolateDescr.toMap());

        // Log the result
        pLogger.logInfo(this, "startIsolate",
                "Result calling forker to start isolate=", aIsolateId,
                ", result=", result);

        // Success if the isolate is running (even if we done nothing) return
        return result == IForkerStatus.SUCCESS
                || result == IForkerStatus.ALREADY_RUNNING;
    }

    /**
     * Asks the forker service to stop the given isolate.
     * 
     * @param aIsolateId
     *            The ID of the isolate to stop
     * @return True if the forker was called.
     */
    @Override
    public boolean stopIsolate(final String aIsolateId) {

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
    @Override
    public void stopPlatform() {

        pLogger.logInfo(this, "stopPlatform", "Stopping platform...");

        // Deactivate startIsolate()
        pPlatformRunning = false;

        // Tell the forkers we don't need them to start isolates anymore
        pForkerSvc.setPlatformStopping();

        // Stop the failure handler
        if (pFailureHandler != null) {
            pFailureHandler.stop();
        }

        // Prepare the list of configured isolates.
        pIsolatesToStop.clear();

        // Only try to stop isolates that can be started by this monitor
        final ApplicationDescription application = pConfiguration
                .getApplication();
        if (application != null && application.getIsolateIds() != null) {

            for (final String isolateId : application.getIsolateIds()) {

                if (getIsolateDescription(isolateId) != null) {
                    pIsolatesToStop.add(isolateId);
                }
            }

        } else if (application == null) {
            pLogger.logSevere(this, "stopPlatform",
                    "Configuration returned a null application !");
        }

        // Prepare the end semaphore
        pStopSemaphore = new Semaphore(0);

        // Remove the forker and the current monitor
        pIsolatesToStop.remove(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER);
        pIsolatesToStop.remove(System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID));

        // Kill other monitors (not including ourselves)
        pSignalSender.fireGroup(ISignalsConstants.MONITOR_SIGNAL_STOP_PLATFORM,
                null, EBaseGroup.MONITORS);

        // Send a stop signal to isolates
        pSignalSender.fire(ISignalsConstants.ISOLATE_STOP_SIGNAL, null,
                pIsolatesToStop.toArray(new String[0]));

        // Finish the job in a new thread
        pKillerThread = new Thread(new Runnable() {

            @Override
            public void run() {

                // Wait for the isolates to stop (1 second max)
                try {
                    pStopSemaphore.tryAcquire(pStopPlatformTimeout,
                            TimeUnit.MILLISECONDS);

                } catch (final InterruptedException e) {
                    // Pass through the semaphore on interruption
                }

                // Finish the job
                killRemainingIsolates();
            }
        });

        pKillerThread.start();
    }

    /**
     * Called by iPOJO when the forker handler is gone
     * 
     * @param aForkerHandler
     *            A forker handler
     */
    @Unbind(id = FORKER_HANDLER_ID)
    protected void unbindForkerHandler(final IForkerHandler aForkerHandler) {

        pPropertyForkerHandlerPresent = false;
        aForkerHandler.unregisterIsolateEventListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Compute the host name
        if (pHostName == null) {
            pHostName = Utilities.getHostName();
        }

        // Clear the time stamps list
        pLastIsolatesStatusUID.clear();

        // Write our access URL to the $BASE/var/monitor.access file
        writeAccessFile();

        // Prepare the stopPlatform() time out
        pStopPlatformTimeout = readIntSystemProperty(
                PROPERTY_STOP_PLATFORM_TIMEOUT, 1000);

        // Prepare the failure handler
        final int maxTriesStreak = readIntSystemProperty(
                PROPERTY_MAX_TRIES_STREAK, 3);
        final int timeInStreak = readIntSystemProperty(
                PROPERTY_WAIT_TIME_IN_STREAK, 1);
        final int timeBeforeStreak = readIntSystemProperty(
                PROPERTY_WAIT_TIME_BEFORE_STREAK, 10);

        pFailureHandler = new IsolateFailureHandler(this, maxTriesStreak,
                timeInStreak, timeBeforeStreak);

        // Register to the forker events
        pForkerSvc.registerListener(this);

        pLogger.logInfo(this, "validatePojo", "PSEM2M Monitor Core Ready");
    }

    /**
     * Tries to write this monitor access URL to $BASE/var/monitor.access.
     * 
     * Does nothing on error.
     */
    protected void writeAccessFile() {

        try {
            // Get the monitor access URL
            final IsolateDescription isolateDescr = pConfiguration
                    .getCurrentIsolate();
            final String accessUrl = "http://localhost:"
                    + isolateDescr.getPort();

            // Create the file
            final File accessFile = new File(
                    pPlatformDirsSvc.getPlatformBaseDir(),
                    "/var/monitor.access");
            if (!accessFile.exists()) {
                accessFile.createNewFile();
            }

            // Write our access URL
            final OutputStream outputStream = new FileOutputStream(accessFile);
            outputStream.write(accessUrl.getBytes());
            outputStream.close();

        } catch (final Exception e) {
            // Error reading the configuration, too bad, but not important
            e.printStackTrace();
        }
    }
}
