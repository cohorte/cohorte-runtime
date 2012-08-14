/**
 * 
 */
package org.psem2m.isolates.monitor.core.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.monitor.IMonitorStatus;
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

    /** The configuration service */
    @Requires
    private ISvcConfig pConfig;

    /** Fully instantiated isolates set */
    private final Set<String> pFullIsolates = new HashSet<String>();

    /** The isolate ID -&gt; Description mapping */
    private final Map<String, IsolateDescription> pIsolates = new HashMap<String, IsolateDescription>();

    /** Loading isolates map : Isolate ID -&gt; Isolate description */
    private final Set<String> pLoadingIsolates = new HashSet<String>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Requested isolates map : Isolate ID -&gt; Isolate description */
    private final Set<String> pRequestedIsolates = new HashSet<String>();

    /** Contains the queues where isolates are considered as running */
    private final List<Set<String>> pRunningQueues = new ArrayList<Set<String>>();

    /** Waiting isolates map : Isolate ID -&gt; Isolate description */
    private final Set<String> pWaitingIsolates = new HashSet<String>();

    /**
     * Changes the current queue of the given isolate
     * 
     * @param aIsolateId
     *            The changed isolate ID
     * @param aSourceQueue
     *            The current isolate state
     * @param aTargetQueue
     *            The target isolate state
     * @param aLogWhat
     *            The current action, used in logs in case of error
     * @return True if the state has been correctly changed
     */
    private synchronized boolean changeQueue(final String aIsolateId,
            final Set<String> aSourceQueue, final Set<String> aTargetQueue,
            final String aLogWhat) {

        // Try to remove the isolate
        if (!aSourceQueue.remove(aIsolateId)) {
            // The isolate wasn't there
            pLogger.logWarn(this, aLogWhat, "Isolate ID=", aIsolateId,
                    "is not is the source queue");
            return false;
        }
        ;

        // Add it to the requested queue
        return aTargetQueue.add(aIsolateId);
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

        return pIsolates.get(aIsolateId);
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

        for (final String waitingIsolateId : pWaitingIsolates) {

            final IsolateDescription isoDescr = pIsolates.get(waitingIsolateId);
            if (aNode.equals(isoDescr.getNode())) {
                // Matching node
                result.add(isoDescr);
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

        final Set<String> result = new HashSet<String>();

        // Aggregate queues
        for (final Set<String> runningQueue : pRunningQueues) {
            result.addAll(runningQueue);
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the running queue containing the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The running queue containing the given isolate, or null
     */
    private synchronized Set<String> getRunningQueue(final String aIsolateId) {

        for (final Set<String> runningQueue : pRunningQueues) {

            if (runningQueue.contains(aIsolateId)) {
                // Found !
                return runningQueue;
            }
        }

        return null;
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
            pLogger.logWarn(this, "", "Isolate ID=", aIsolateId,
                    "is not in the waiting queue.");
            return null;
        }

        return pIsolates.get(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IMonitorStatus#getWaitingIsolatesIDs()
     */
    @Override
    public synchronized String[] getWaitingIsolatesIDs() {

        return pWaitingIsolates.toArray(new String[pWaitingIsolates.size()]);
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Clear the queues
        pRunningQueues.clear();
        pIsolates.clear();
        pFullIsolates.clear();
        pLoadingIsolates.clear();
        pRequestedIsolates.clear();
        pWaitingIsolates.clear();

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

        return changeQueue(aIsolateId, pLoadingIsolates, pFullIsolates,
                "isolateComplete");
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

        return changeQueue(aIsolateId, pRequestedIsolates, pLoadingIsolates,
                "isolateLoading");
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

        return changeQueue(aIsolateId, pWaitingIsolates, pRequestedIsolates,
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

        final Set<String> runningQueue = getRunningQueue(aIsolateId);
        if (runningQueue == null) {
            // Isolate not found
            pLogger.logWarn(this, "isolateStopped", "Isolate ID=", aIsolateId,
                    "doesn't seem to be running.");
            return false;
        }

        // Found the isolate: remove it from the queue
        runningQueue.remove(aIsolateId);

        // Put it in the waiting queue
        return pWaitingIsolates.add(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#isRunning(java.lang.String)
     */
    @Override
    public synchronized boolean isRunning(final String aIsolateId) {

        return getRunningQueue(aIsolateId) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IMonitorStatus#isWaiting(java.lang.String)
     */
    @Override
    public synchronized boolean isWaiting(final String aIsolateId) {

        return pWaitingIsolates.contains(aIsolateId);
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

                pIsolates.put(isolateId, isolateDescr);
                pWaitingIsolates.add(isolateId);
            }
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Fill the running queues list
        pRunningQueues.add(pFullIsolates);
        pRunningQueues.add(pLoadingIsolates);
        pRunningQueues.add(pRequestedIsolates);

        // Load the waiting isolates list
        pLogger.logInfo(this, "validate", "Reading configuration...");
        loadWaitingIsolates();
        pLogger.logInfo(this, "validate", "Isolates count=", pIsolates.size());

        // TODO: try to send a signal to each isolate, see if it's available ?

        pLogger.logInfo(this, "validate", "Monitor status ready");
    }
}
