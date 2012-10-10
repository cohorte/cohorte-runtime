/**
 * 
 */
package org.psem2m.isolates.monitor.core.v2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.forker.IForker;
import org.psem2m.forker.IForkerEventListener;
import org.psem2m.forker.IForkerStatus;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.monitor.IMonitorStatus;
import org.psem2m.isolates.monitor.IPlatformMonitor;
import org.psem2m.isolates.monitor.core.IsolateFailureHandler;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;

/**
 * The isolate monitor core logic
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-monitor-logic-factory", publicFactory = false)
@Provides(specifications = { IPlatformMonitor.class,
        IForkerEventListener.class, IIsolateStatusEventListener.class })
@Instantiate(name = "psem2m-monitor-logic")
public class MonitorLogic implements IPlatformMonitor, IForkerEventListener,
        IIsolateStatusEventListener {

    /** The isolate failure handler */
    private IsolateFailureHandler pFailureHandler;

    /** The forker service */
    @Requires
    private IForker pForker;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Maximum tries in a streak */
    @ServiceProperty(name = "tries.streak.tries", value = "3", mandatory = true)
    private int pMaxTriesStreak;

    /** Platform running flag */
    private boolean pPlatformRunning;

    /** The signal sender */
    @Requires
    private ISignalBroadcaster pSignalSender;

    /** The monitor status service */
    @Requires
    private IMonitorStatus pStatus;

    /** Time to wait between two tries streaks */
    @ServiceProperty(name = "tries.streak.gap", value = "5", mandatory = true)
    private int pWaitingTimeBetweenStreaks;

    /** Time to wait between two tries within a streak */
    @ServiceProperty(name = "tries.streak.waitTime", value = "1", mandatory = true)
    private int pWaitTimeInStreak;

    /**
     * Tells the forker to kill the given isolate ID. The isolate might not have
     * been started.
     * 
     * @param aIsolateId
     *            The ID of the isolate to stop
     */
    private void failsafeStop(final String aIsolateId) {

        pForker.stopIsolate(aIsolateId);
    }

    /**
     * Converts a forker result to a message string
     * 
     * @param aResult
     *            A forker status code
     * @return The corresponding string message
     */
    public String forkerResultToString(final int aResult) {

        switch (aResult) {
        case IForkerStatus.ALREADY_RUNNING:
            return "Isolate is already running";

        case IForkerStatus.NO_PROCESS_REF:
            return "No process reference returned by the runner";

        case IForkerStatus.NO_WATCHER:
            return "The watcher thread couldn't be started";

        case IForkerStatus.REQUEST_ERROR:
            return "An error occurred while sending a request to the forker";

        case IForkerStatus.REQUEST_NO_RESULT:
            return "The forker returned no result";

        case IForkerStatus.REQUEST_TIMEOUT:
            return "The request to the forker timed out";

        case IForkerStatus.RUNNER_EXCEPTION:
            return "The forker thrown an exception";

        case IForkerStatus.SUCCESS:
            return "The isolate process has been successfully started";

        case IForkerStatus.UNKNOWN_KIND:
            return "The forker doesn't handle this kind of isolate";

        default:
            return MessageFormat.format("Unknown status ''{0}''", aResult);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forker.IForkerEventListener#handleForkerEvent(org.psem2m.forker
     * .IForkerEventListener.EForkerEventType, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void handleForkerEvent(final EForkerEventType aEventType,
            final String aForkerId, final String aNode, final String aHost) {

        pLogger.logInfo(this, "handleForkerEvent",
                "Handling forker event type=", aEventType, "forker=",
                aForkerId, "node=", aNode, "host=", aHost);

        switch (aEventType) {
        case REGISTERED:
            // New forker detected
            startIsolatesForNode(aNode);
            break;

        case UNREGISTERED:
            // TODO: Forker lost: try to communicate with its isolates
            break;

        default:
            // Unknown type (version mismatch ?)
            pLogger.logWarn(this, "handleForkerEvent",
                    "Unknown forker event type=", aEventType);
            break;
        }
    }

    /**
     * Handles the result of a call to
     * {@link IForker#startIsolate(java.util.Map)}
     * 
     * @param aIsolateDescr
     *            The started isolate description
     * @param aResult
     *            The forker result
     * @return True if the isolate has been successfully started
     */
    protected boolean handleForkerResult(
            final IsolateDescription aIsolateDescr, final int aResult) {

        // Log method name
        final String logMethodName = "handleForkerResult";

        // Log the result
        final String isolateId = aIsolateDescr.getId();
        pLogger.logInfo(this, logMethodName, "Isolate ID=", isolateId,
                "start result=", aResult);

        switch (aResult) {
        case IForkerStatus.SUCCESS:
            // The isolate process has been started
            pLogger.logInfo(this, logMethodName, "Isolate ID=", isolateId,
                    "process started");
            pStatus.isolateLoading(isolateId);
            return true;

        case IForkerStatus.ALREADY_RUNNING:
            // The isolate was already there
            pLogger.logInfo(this, logMethodName, "Isolate ID=", isolateId,
                    "was already running");
            pStatus.isolateLoading(isolateId);
            return true;

        case IForkerStatus.NO_PROCESS_REF:
        case IForkerStatus.NO_WATCHER:
        case IForkerStatus.REQUEST_TIMEOUT:
        case IForkerStatus.REQUEST_NO_RESULT:
        case IForkerStatus.REQUEST_ERROR:
            // An error occurred, the isolate process might be still here
            failsafeStop(isolateId);
            pStatus.isolateStopped(isolateId);

            // Log the error
            pLogger.logInfo(this, logMethodName, "Isolate ID=", isolateId,
                    "failed to start on node=", aIsolateDescr.getNode(),
                    "reason=", forkerResultToString(aResult));

            // TODO: Try again (modify the configuration according to the
            // error ?)
            break;

        case IForkerStatus.NO_MATCHING_FORKER:
            // Forker didn't start
            pStatus.isolateStopped(isolateId);

            // Log the error
            pLogger.logInfo(this, logMethodName,
                    "No forker found to start isolate ID=", isolateId,
                    "on node=", aIsolateDescr.getNode());

            // TODO: Try again later
            break;

        default:
            // Log the error
            pLogger.logInfo(this, logMethodName, "Unknown forker status code=",
                    aResult, "to start isolate ID=", isolateId, "on node=",
                    aIsolateDescr.getNode());
            break;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IIsolateStatusEventListener#
     * handleIsolateLost(java.lang.String)
     */
    @Override
    public void handleIsolateLost(final String aIsolateId) {

        // Isolate is considered stopped
        if (pStatus.isolateStopped(aIsolateId) && pFailureHandler != null) {
            // State change OK and failure handler present
            pFailureHandler.isolateFailed(aIsolateId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IIsolateStatusEventListener#
     * handleIsolateStatusEvent(java.lang.String,
     * org.psem2m.isolates.base.isolates.boot.IsolateStatus)
     */
    @Override
    public void handleIsolateStatusEvent(final String aSenderId,
            final IsolateStatus aIsolateStatus) {

        if (aIsolateStatus == null) {
            pLogger.logWarn(this, "",
                    "Invalid isolate status received from ID=", aSenderId);
            return;
        }

        // The ID of the isolate corresponding to the status
        final String statusId = aIsolateStatus.getIsolateId();

        pLogger.logInfo(this, "handleIsolateStatusEvent",
                "Received isolate status=", aIsolateStatus, "for ID=",
                statusId, "from isolate ID=", aSenderId);

        switch (aIsolateStatus.getState()) {
        case IsolateStatus.STATE_AGENT_DONE:
            // Isolate complete
            pLogger.logInfo(this, "handleIsolateStatusEvent", "Isolate ID=",
                    statusId, "has been fully loaded");
            pStatus.isolateComplete(statusId);
            break;

        case IsolateStatus.STATE_FRAMEWORK_STOPPED:
        case IsolateStatus.STATE_FRAMEWORK_STOPPING:
            // Isolate stopped
            pLogger.logInfo(this, "handleIsolateStatusEvent", "Isolate ID=",
                    statusId, "has stopped");
            pStatus.isolateStopped(statusId);

            // TODO: try again if not intended
            if (pPlatformRunning) {
                pFailureHandler.isolateFailed(statusId);
            }
            break;

        case IsolateStatus.STATE_FAILURE:
            // Isolate failure
            pLogger.logInfo(this, "handleIsolateStatusEvent", "Isolate ID=",
                    statusId, "has stopped");
            pStatus.isolateStopped(statusId);

            // Try again
            pFailureHandler.isolateFailed(statusId);
            break;

        default:
            // Unknown event
            break;
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Disable startIsolate()
        pPlatformRunning = false;

        // Stop the failure handler
        pFailureHandler.stop();
        pFailureHandler = null;

        pLogger.logInfo(this, "invalidate", "Monitor Logic gone");
    }

    /**
     * Asks the forker to start the given isolate description
     * 
     * @param aIsolateDescr
     *            An isolate description
     * @return True if the isolate has been started, else false
     */
    protected synchronized boolean startIsolate(
            final IsolateDescription aIsolateDescr) {

        if (!pPlatformRunning) {
            pLogger.logInfo(this, "startIsolate",
                    "Platform is not running. Ignored startIsolate() for ID=",
                    aIsolateDescr.getId());

            return false;
        }

        pLogger.logInfo(this, "startIsolate", "Starting isolate ID=",
                aIsolateDescr.getId(), "on node=", aIsolateDescr.getNode());

        // Call the forker
        pStatus.isolateRequested(aIsolateDescr.getId());
        final int result = pForker.startIsolate(aIsolateDescr.toMap());

        // Handle the forker result
        return handleForkerResult(aIsolateDescr, result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IPlatformMonitor#startIsolate(java.lang.String
     * )
     */
    @Override
    public synchronized boolean startIsolate(final String aIsolateId) {

        if (pStatus.isRunning(aIsolateId)) {
            // Already started isolate
            pLogger.logInfo(this, "startIsolate", "Isolate ID=", aIsolateId,
                    "already running");
            return true;
        }

        // Try with the waiting list
        final IsolateDescription descr = pStatus
                .getWaitingIsolateDescription(aIsolateId);
        if (descr == null) {
            pLogger.logWarn(this, "startIsolate", "Unknown isolate ID=",
                    aIsolateId);
            return false;
        }

        return startIsolate(descr);
    }

    /**
     * Starts all isolates waiting on the given node
     * 
     * @param aNode
     *            A node where isolates can be started
     * @return The list of descriptions that failed
     */
    protected List<IsolateDescription> startIsolatesForNode(final String aNode) {

        // Get the isolates list
        final IsolateDescription[] nodeIsolates = pStatus
                .getIsolatesWaitingForNode(aNode);
        if (nodeIsolates == null || nodeIsolates.length == 0) {
            // Nothing to do
            pLogger.logInfo(this, "startIsolatesForNode",
                    "No isolate configured for node=", aNode);
        }

        pLogger.logDebug(this, "startIsolatesForNode", "Node=", aNode,
                "isolate=", nodeIsolates);

        // The result list
        final List<IsolateDescription> failedIsolates = new ArrayList<IsolateDescription>();

        // Loop on the copy
        for (final IsolateDescription isolateDescr : nodeIsolates) {

            if (!startIsolate(isolateDescr)) {
                failedIsolates.add(isolateDescr);
            }
        }

        return failedIsolates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IPlatformMonitor#stopIsolate(java.lang.String
     * )
     */
    @Override
    public boolean stopIsolate(final String aIsolateId) {

        pForker.stopIsolate(aIsolateId);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IPlatformMonitor#stopPlatform()
     */
    @Override
    public synchronized void stopPlatform() {

        pLogger.logInfo(this, "stopPlatform", "Stopping platform...");

        // Deactivate startIsolate()
        pPlatformRunning = false;

        // Stop the failure handler
        if (pFailureHandler != null) {
            pFailureHandler.stop();
        }

        // Tell the forkers we don't need them to start isolates anymore
        pForker.setPlatformStopping();

        // Tell the forker service to stop the running isolates
        for (final String isolateId : pStatus.getRunningIsolatesIDs()) {
            pForker.stopIsolate(isolateId);
        }

        // FIXME: wait for stopping isolates ? Like before ?
        try {
            Thread.sleep(1000);

        } catch (final InterruptedException ex) {
            pLogger.logWarn(this, "",
                    "Interrupted while waiting for isolates to stop.", ex);
        }

        // Kill the forkers
        pSignalSender.fireGroup(ISignalsConstants.ISOLATE_STOP_SIGNAL, null,
                EBaseGroup.FORKERS);

        // Last man standing...
        pSignalSender.fireGroup(ISignalsConstants.ISOLATE_STOP_SIGNAL, null,
                EBaseGroup.CURRENT);
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Set platform running
        pPlatformRunning = true;

        // Set up the failure handler
        pFailureHandler = new IsolateFailureHandler(this, pMaxTriesStreak,
                pWaitTimeInStreak, pWaitingTimeBetweenStreaks);

        pLogger.logInfo(this, "validate", "Monitor Logic ready");
    }
}
