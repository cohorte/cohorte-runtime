/**
 * 
 */
package org.cohorte.monitor.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.monitor.api.IMonitorStatus;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.conf.beans.IsolateConf;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.status.storage.IStatusStorage;
import org.psem2m.status.storage.IStatusStorageCreator;
import org.psem2m.status.storage.InvalidIdException;
import org.psem2m.status.storage.InvalidStateException;

/**
 * Describes the current status of the monitor
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-monitor-status-factory")
@Provides(specifications = IMonitorStatus.class)
public class MonitorStatus implements IMonitorStatus {

    /** States in which an isolate is considered running */
    private static final EIsolateState RUNNING_STATES[] = {
            EIsolateState.READY, EIsolateState.LOADING, EIsolateState.REQUESTED };

    /** The configuration service */
    @Requires
    private ISvcConfig pConfig;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Platform information service */
    @Requires
    private IPlatformDirsSvc pPlatform;

    /** The status storage */
    private IStatusStorage<EIsolateState, IsolateConf> pStatus;

    /** The status storage creator */
    @Requires
    private IStatusStorageCreator pStatusCreator;

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
     * org.cohorte.monitor.api.IMonitorStatus#getIsolateDescription(java.lang
     * .String)
     */
    @Override
    public IsolateConf getIsolateDescription(final String aIsolateId) {

        try {
            return pStatus.get(aIsolateId);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getIsolateConf", "Invalid isolate ID=",
                    aIsolateId, ":", ex);
        }

        // Unknown ID
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.api.IMonitorStatus#getIsolatesOnNode(java.lang.String
     * )
     */
    @Override
    public String[] getIsolatesOnNode(final String aNode) {

        if (aNode == null || aNode.isEmpty()) {
            pLogger.logWarn(this, "getIsolatesOnNode",
                    "Null node names are not accepted");
            return null;
        }

        final List<String> uids = new LinkedList<String>();

        for (final IsolateConf isolate : pStatus.getValues()) {
            if (aNode.equals(isolate.getNode())) {
                // Matching node
                uids.add(isolate.getUID());
            }
        }

        return uids.toArray(new String[uids.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#getWaitingIsolates(java.lang
     * .String)
     */
    @Override
    public synchronized IsolateConf[] getIsolatesWaitingForNode(
            final String aNode) {

        if (aNode == null || aNode.isEmpty()) {
            pLogger.logWarn(this, "getWaitingIsolates",
                    "Null node names are not accepted");
            return null;
        }

        final List<IsolateConf> result = new LinkedList<IsolateConf>();

        for (final IsolateConf isolate : pStatus
                .getValuesInStates(EIsolateState.WAITING)) {
            if (aNode.equals(isolate.getNode())) {
                // Matching node
                result.add(isolate);
            }
        }

        return result.toArray(new IsolateConf[result.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.monitor.api.IMonitorStatus#getRunningIsolatesUIDs()
     */
    @Override
    public synchronized String[] getRunningIsolatesUIDs() {

        return pStatus.getIdsInStates(RUNNING_STATES);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.api.IMonitorStatus#getWaitingIsolate(java.lang.String
     * )
     */
    @Override
    public synchronized IsolateConf getWaitingIsolate(final String aIsolateId) {

        if (!isWaiting(aIsolateId)) {
            pLogger.logWarn(this, "getWaitingIsolate", "Isolate ID=",
                    aIsolateId, "is not in the waiting queue.");
            return null;
        }

        try {
            return pStatus.get(aIsolateId);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getWaitingIsolate", "Isolate ID=",
                    aIsolateId, "is unknown.");
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.monitor.api.IMonitorStatus#getWaitingIsolatesUIDs()
     */
    @Override
    public synchronized String[] getWaitingIsolatesUIDs() {

        return pStatus.getIdsInStates(EIsolateState.WAITING);
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Clear the queues
        pStatusCreator.deleteStorage(pStatus);
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
    protected boolean isAcceptableIsolate(final IsolateConf aIsolateDescr) {

        // Test the isolate ID
        final String isolateUID = aIsolateDescr.getUID();
        if (isolateUID == null || isolateUID.isEmpty()) {
            // Invalid ID
            pLogger.logWarn(this, "isAcceptableIsolate",
                    "Null isolate UID found");
            return false;
        }

        // Test the isolate node
        final String isolateNode = aIsolateDescr.getNode();
        if (isolateNode == null || isolateNode.isEmpty()) {
            // Invalid node
            pLogger.logWarn(this, "isAcceptableIsolate",
                    "Null isolate node found for ID=", isolateUID);
            return false;
        }

        // Get the current isolate ID
        final String currentUID = pPlatform.getIsolateUID();
        if (currentUID == null || currentUID.isEmpty()) {
            pLogger.logWarn(this, "isAcceptableIsolate",
                    "Can't find the current isolate ID.");
            return false;
        }

        // Test forker special name
        final String isolateName = aIsolateDescr.getName();
        if (isolateName
                .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
            // Special name found
            return false;
        }

        // Test ID inequality
        return !currentUID.equals(isolateUID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.api.IMonitorStatus#isolateLoading(java.lang.String)
     */
    @Override
    public synchronized boolean isolateLoading(final String aIsolateId) {

        return changeQueue(aIsolateId, EIsolateState.LOADING, "isolateLoading");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.api.IMonitorStatus#isolateReady(java.lang.String)
     */
    @Override
    public synchronized boolean isolateReady(final String aIsolateId) {

        return changeQueue(aIsolateId, EIsolateState.READY, "isolateComplete");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.api.IMonitorStatus#isolateRequested(java.lang.String)
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
     * org.cohorte.monitor.api.IMonitorStatus#isolateStopped(java.lang.String)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.monitor.api.IMonitorStatus#newIsolate(java.lang.String,
     * org.psem2m.isolates.services.conf.beans.IsolateConf)
     */
    @Override
    public synchronized boolean prepareIsolate(final String aUID,
            final IsolateConf aConfiguration) {

        if (pStatus.contains(aUID)) {
            // Already known UID
            try {
                if (pStatus.getState(aUID) != EIsolateState.WAITING) {
                    // The isolate may have been requested
                    return false;

                } else {
                    // Remove the current entry, as the configuration might have
                    // changed
                    pStatus.remove(aUID);
                }
            } catch (final InvalidIdException ex) {
                // Can't happen
                pLogger.logSevere(this, "prepareIsolate",
                        "UID known but invalid ID exception thrown=", ex);
            }
        }

        try {
            // Store the configuration
            return pStatus.store(aUID, aConfiguration, EIsolateState.WAITING);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "newIsolate", "Invalid isolate ID=", aUID,
                    ":", ex);

        } catch (final InvalidStateException ex) {
            pLogger.logWarn(this, "newIsolate", "Invalid initial state=",
                    ex.getCauseState(), "for isolate ID=", aUID, ":", ex);
        }

        return false;
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Sets up the status storage
        pStatus = pStatusCreator.createStorage();

        // Make this isolate as ready
        try {
            pStatus.store(pPlatform.getIsolateUID(), null, EIsolateState.READY);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "validate",
                    "Can't store the current isolate state:", ex);

        } catch (final InvalidStateException ex) {
            pLogger.logWarn(this, "validate",
                    "Can't store the current isolate state:", ex);
        }

        pLogger.logInfo(this, "validate", "Monitor status ready");
    }
}
