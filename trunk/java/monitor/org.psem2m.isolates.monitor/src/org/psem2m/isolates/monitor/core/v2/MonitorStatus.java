/**
 * 
 */
package org.psem2m.isolates.monitor.core.v2;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.monitor.IMonitorStatus;
import org.psem2m.isolates.monitor.core.v2.state.InvalidIdException;
import org.psem2m.isolates.monitor.core.v2.state.InvalidStateException;
import org.psem2m.isolates.monitor.core.v2.state.StatusStorage;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * Describes the current status of the monitor
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-monitor-status-factory", publicFactory = false)
@Provides(specifications = IMonitorStatus.class)
@Instantiate(name = "psem2m-monitor-status")
public class MonitorStatus implements IMonitorStatus {

    /** States in which an isolate is considered running */
    private static final EIsolateState RUNNING_STATES[] = { EIsolateState.FULL,
            EIsolateState.LOADING, EIsolateState.REQUESTED };

    /** The configuration service */
    @Requires
    private ISvcConfig pConfig;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The status storage */
    private StatusStorage<EIsolateState, IsolateDescription> pStatus;

    /**
     * Changes the current queue of the given isolate
     * 
     * @param aIsolateId
     *            The changed isolate ID
     * @param aNewState
     *            The new isolate state
     * @param aLogWhat
     *            The current action, used in logs in case of error
     * @return True if the state has been correctly changed
     */
    private synchronized boolean changeQueue(final String aIsolateId,
            final EIsolateState aNewState, final String aLogWhat) {

        pLogger.logDebug(this, aLogWhat, "Isolate ID=", aIsolateId,
                "goes to state=", aNewState);

        try {
            pStatus.changeState(aIsolateId, aNewState);

            // Success !
            return true;

        } catch (final InvalidStateException ex) {
            pLogger.logWarn(this, aLogWhat,
                    "Error changing the state of isolate=", aIsolateId, ":", ex);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, aLogWhat, "Invalid isolate ID=", aIsolateId,
                    ":", ex);
        }

        // An error occurred
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#getIsolateDescription(java
     * .lang.String)
     */
    @Override
    public IsolateDescription getIsolateDescription(final String aIsolateId) {

        try {
            return pStatus.get(aIsolateId);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getIsolateDescription",
                    "Invalid isolate ID=", aIsolateId, ":", ex);
        }

        // Unknown ID
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#getWaitingIsolates(java.lang
     * .String)
     */
    @Override
    public synchronized IsolateDescription[] getIsolatesWaitingForNode(
            final String aNode) {

        if (aNode == null || aNode.isEmpty()) {
            pLogger.logWarn(this, "getWaitingIsolates",
                    "Null node names are not accepted");
            return null;
        }

        final List<IsolateDescription> result = new ArrayList<IsolateDescription>();

        for (final String waitingIsolateId : pStatus
                .getIdsInStates(EIsolateState.WAITING)) {

            try {
                final IsolateDescription isoDescr = pStatus
                        .get(waitingIsolateId);
                if (aNode.equals(isoDescr.getNode())) {
                    // Matching node
                    result.add(isoDescr);
                }

            } catch (final InvalidIdException ex) {
                pLogger.logWarn(this, "getIsolatesWaitingForNode",
                        "Unknown waiting isolate ID=", waitingIsolateId);
            }
        }

        return result.toArray(new IsolateDescription[result.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IMonitorStatus#getRunningIsolatesIDs()
     */
    @Override
    public synchronized String[] getRunningIsolatesIDs() {

        return pStatus.getIdsInStates(RUNNING_STATES);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#getWaitingIsolate(java.lang
     * .String)
     */
    @Override
    public synchronized IsolateDescription getWaitingIsolateDescription(
            final String aIsolateId) {

        if (!isWaiting(aIsolateId)) {
            pLogger.logWarn(this, "getWaitingIsolateDescription",
                    "Isolate ID=", aIsolateId, "is not in the waiting queue.");
            return null;
        }

        try {
            return pStatus.get(aIsolateId);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getWaitingIsolateDescription",
                    "Isolate ID=", aIsolateId, "is unknown.");
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IMonitorStatus#getWaitingIsolatesIDs()
     */
    @Override
    public synchronized String[] getWaitingIsolatesIDs() {

        return pStatus.getIdsInStates(EIsolateState.WAITING);
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Clear the queues
        pStatus.clear();
        pStatus = null;

        pLogger.logInfo(this, "invalidate", "Monitor status gone");
    }

    /**
     * Tests if the given isolate can be accepted by this monitor.
     * 
     * Basically accepts all isolate ID except the current one and the forker
     * IDs.
     * 
     * @param aIsolateDescr
     *            A description to be tested
     * @return True if this monitor can handle the given ID
     */
    protected boolean isAcceptableIsolate(final IsolateDescription aIsolateDescr) {

        // Test the isolate ID
        final String isolateId = aIsolateDescr.getId();
        if (isolateId == null || isolateId.isEmpty()) {
            // Invalid ID
            pLogger.logWarn(this, "isAcceptableIsolate",
                    "Null isolate ID found");
            return false;
        }

        // Test the isolate node
        final String isolateNode = aIsolateDescr.getNode();
        if (isolateNode == null || isolateNode.isEmpty()) {
            // Invalid node
            pLogger.logWarn(this, "isAcceptableIsolate",
                    "Null isolate node found for ID=", isolateId);
            return false;
        }

        // Test forker ID prefix
        if (isolateId
                .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
            // Forker ID found
            return false;
        }

        // Get the current isolate ID
        final String currentId = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
        if (currentId == null || currentId.isEmpty()) {
            pLogger.logWarn(this, "isAcceptableIsolate",
                    "Can't find the current isolate ID.");
            return false;
        }

        // Test ID inequality
        return !currentId.equals(isolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.core.v2.IMonitorStatus#isolateComplete(java
     * .lang.String)
     */
    @Override
    public synchronized boolean isolateComplete(final String aIsolateId) {

        return changeQueue(aIsolateId, EIsolateState.FULL, "isolateComplete");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.core.v2.IMonitorStatus#isolateLoading(java
     * .lang.String)
     */
    @Override
    public synchronized boolean isolateLoading(final String aIsolateId) {

        return changeQueue(aIsolateId, EIsolateState.LOADING, "isolateLoading");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.core.v2.IMonitorStatus#isolateRequested(java
     * .lang.String)
     */
    @Override
    public synchronized boolean isolateRequested(final String aIsolateId) {

        return changeQueue(aIsolateId, EIsolateState.REQUESTED,
                "isolateRequested");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.core.v2.IMonitorStatus#isolateStopped(java
     * .lang.String)
     */
    @Override
    public synchronized boolean isolateStopped(final String aIsolateId) {

        if (!isRunning(aIsolateId)) {
            // Isolate is not running
            pLogger.logWarn(this, "isolateStopped", "Isolate ID=", aIsolateId,
                    "doesn't seem to be running.");
            return false;
        }

        return changeQueue(aIsolateId, EIsolateState.WAITING, "isolateStopped");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#isRunning(java.lang.String)
     */
    @Override
    public synchronized boolean isRunning(final String aIsolateId) {

        try {
            // Get the state of the isolate
            final EIsolateState isolateState = pStatus.getState(aIsolateId);

            // Search for it in the running states
            for (final EIsolateState runningState : RUNNING_STATES) {
                if (runningState == isolateState) {
                    return true;
                }
            }

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "isRunning", "Unknown isolate ID=",
                    aIsolateId, ":", ex);
        }

        // Not found...
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#isWaiting(java.lang.String)
     */
    @Override
    public synchronized boolean isWaiting(final String aIsolateId) {

        try {
            return pStatus.getState(aIsolateId) == EIsolateState.WAITING;

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "isRunning", "Unknown isolate ID=",
                    aIsolateId, ":", ex);
        }

        // Not found...
        return false;
    }

    /**
     * Sets up the waiting isolates map according to the configuration service
     * 
     * Isolates state is not tested, therefore some of them might already have
     * been started.
     */
    protected void loadWaitingIsolates() {

        final ApplicationDescription application = pConfig.getApplication();

        for (final String isolateId : application.getIsolateIds()) {

            // Get the description
            final IsolateDescription isolateDescr = application
                    .getIsolate(isolateId);

            // Avoid to add ourselves or a forker
            if (isAcceptableIsolate(isolateDescr)) {

                try {
                    pStatus.store(isolateId, isolateDescr,
                            EIsolateState.WAITING);

                } catch (final InvalidIdException ex) {
                    pLogger.logWarn(this, "loadWaitingIsolates",
                            "Invalid isolate ID=", isolateId, ":", ex);

                } catch (final InvalidStateException ex) {
                    pLogger.logWarn(this, "loadWaitingIsolates",
                            "Invalid initial state=", ex.getCauseState(),
                            "for isolate ID=", isolateId, ":", ex);
                }
            }
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Sets up the status storage
        pStatus = new StatusStorage<EIsolateState, IsolateDescription>();

        // Load the waiting isolates list
        pLogger.logInfo(this, "validate", "Reading configuration...");
        loadWaitingIsolates();
        pLogger.logInfo(this, "validate", "Isolates count=", pStatus.size());

        // TODO: try to send a signal to each isolate, see if it's available ?

        pLogger.logInfo(this, "validate", "Monitor status ready");
    }
}
