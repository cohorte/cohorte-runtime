/**
 * File:   SignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.signals.directory.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Bind;
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
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener.EPresence;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.status.storage.IStatusStorage;
import org.psem2m.status.storage.IStatusStorageCreator;
import org.psem2m.status.storage.InvalidIdException;
import org.psem2m.status.storage.InvalidStateException;

/**
 * Simple implementation of the PSEM2M Signals isolates directory, based on the
 * PSEM2M Configuration service
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-directory-factory", publicFactory = false)
@Provides(specifications = ISignalDirectory.class)
@Instantiate(name = "psem2m-signals-directory")
public class SignalsDirectory extends CPojoBase implements ISignalDirectory {

    /** Isolate presence listeners dependency ID */
    private static final String ID_LISTENERS = "isolate-presence-listeners";

    /** Isolate ID -&gt; (Node, Port) */
    private final Map<String, HostAccess> pAccesses = new HashMap<String, HostAccess>();

    /** Special information: local isolate port */
    private int pCurrentIsolatePort;

    /** Group name -&gt; Isolate IDs */
    private final Map<String, List<String>> pGroups = new HashMap<String, List<String>>();

    /** Isolates listeners */
    @Requires(id = ID_LISTENERS, optional = true)
    private IIsolatePresenceListener[] pListeners;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Node name -&gt; Host address */
    private final Map<String, String> pNodesHost = new HashMap<String, String>();

    /** Node name -&gt; Isolate IDs */
    private final Map<String, List<String>> pNodesIsolates = new HashMap<String, List<String>>();

    /** Status of the components sets */
    private IStatusStorage<EIsolateRegistrationState, Object> pRegistrationStatus;

    /** The status storage creator */
    @Requires
    private IStatusStorageCreator pStatusCreator;

    /**
     * Called by iPOJO when an new isolate presence listener is bound
     * 
     * @param aListener
     *            A new isolate presence listener
     */
    @Bind(id = ID_LISTENERS)
    protected synchronized void bindPresenceListener(
            final IIsolatePresenceListener aListener) {

        final String[] isolates = getAllIsolates(null, true, true);
        if (isolates != null) {
            // Notify the new listener of all known isolates
            for (final String isolate : isolates) {
                final String node = getIsolateNode(isolate);
                aListener.handleIsolatePresence(isolate, node,
                        EPresence.REGISTERED);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#dump()
     */
    @Override
    public synchronized Map<String, Object> dump() {

        // Compute accesses...
        final Map<String, Object> accesses = new HashMap<String, Object>();
        for (final Entry<String, HostAccess> entry : pAccesses.entrySet()) {
            // Prepare the access map
            final HostAccess access = entry.getValue();
            final Map<String, Object> isolateAccessMap = new HashMap<String, Object>();

            if (ISignalDirectory.LOCAL_ACCESS.equals(access)) {
                // Special treatment for the local isolate
                isolateAccessMap.put("node", getLocalNode());
                isolateAccessMap.put("port", pCurrentIsolatePort);

            } else {
                isolateAccessMap.put("node", access.getAddress());
                isolateAccessMap.put("port", access.getPort());
            }

            // Store the access map
            accesses.put(entry.getKey(), isolateAccessMap);
        }

        final Map<String, Object> result = new HashMap<String, Object>();
        result.put("accesses", accesses);

        // Groups...
        result.put("groups", new HashMap<String, Object>(pGroups));

        // Hosts...
        result.put("nodes_host", new HashMap<String, Object>(pNodesHost));

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#getAllIsolates(java.lang.String,
     * boolean)
     */
    @Override
    public synchronized String[] getAllIsolates(final String aPrefix,
            final boolean aIncludeCurrent, final boolean aOnlyValidated) {

        if (pAccesses.isEmpty()) {
            // Nothing to return
            return null;
        }

        // Get the current isolate ID
        final String currentId = getIsolateId();

        final Set<String> resultSet;
        if (aPrefix == null || aPrefix.isEmpty()) {
            // No filter, return all IDs
            resultSet = new HashSet<String>(pAccesses.keySet());

        } else {
            // Filter IDs
            resultSet = new HashSet<String>();
            for (final String isolate : pAccesses.keySet()) {
                if (isolate.startsWith(aPrefix)) {
                    resultSet.add(isolate);
                }
            }
        }

        if (!aIncludeCurrent) {
            // Remove current isolate if needed
            resultSet.remove(currentId);
        }

        if (aOnlyValidated) {
            // Get all validated isolates IDs
            final String[] validatedIds = pRegistrationStatus.getIdsInStates(
                    EIsolateRegistrationState.VALIDATED,
                    EIsolateRegistrationState.NOTIFIED);
            if (validatedIds == null || validatedIds.length == 0) {
                // Nothing returned
                return null;
            }

            // Filter the result set
            resultSet.retainAll(Arrays.asList(validatedIds));
        }

        if (resultSet.isEmpty()) {
            // Nothing to return
            return null;
        }

        return resultSet.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#getAllNodes()
     */
    @Override
    public String[] getAllNodes() {

        if (pNodesIsolates.isEmpty()) {
            return null;
        }

        return pNodesIsolates.keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#getGroupAccesses(org.psem2m.signals
     * .ISignalDirectory.EBaseGroup)
     */
    @Override
    public Map<String, HostAccess> getGroupAccesses(final EBaseGroup aGroup) {

        // Current ID
        final String currentId = getIsolateId();

        // Filtered IDs
        String[] matchingIsolates = null;

        switch (aGroup) {
        case ALL:
            // Return all isolates, including the current one
            matchingIsolates = getAllIsolates(null, true, true);
            break;

        case OTHERS:
            // Return all isolates, excluding the current one
            matchingIsolates = getAllIsolates(null, false, true);
            break;

        case STORED:
            matchingIsolates = getAllIsolates(null, false, false);
            break;

        case CURRENT:
            // Only the current isolate
            matchingIsolates = new String[] { currentId };
            break;

        case FORKERS:
            // Return only forkers, including the current one
            matchingIsolates = getAllIsolates(
                    IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER, true, true);
            break;

        case MONITORS:
            // Return only monitors, including the current one
            matchingIsolates = getAllIsolates(
                    IPlatformProperties.SPECIAL_ISOLATE_ID_MONITOR, true, true);
            break;

        case ISOLATES:
            // Return all isolates but the forkers
            matchingIsolates = getAllIsolates(null, true, true);
            if (matchingIsolates != null) {
                // Use a temporary set
                final Set<String> set = new HashSet<String>(
                        Arrays.asList(matchingIsolates));

                final Iterator<String> iter = set.iterator();
                while (iter.hasNext()) {
                    // Filter IDs
                    final String isolate = iter.next();
                    if (isolate
                            .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)) {
                        // Remove forkers
                        iter.remove();
                    }
                }

                matchingIsolates = set.toArray(new String[0]);
            }
            break;

        case NEIGHBOURS:
            // All isolates from the current node, excluding the current one
            matchingIsolates = getIsolatesOnNode(getLocalNode());
            if (matchingIsolates != null) {
                // Use a temporary set
                final Set<String> set = new HashSet<String>(
                        Arrays.asList(matchingIsolates));

                // Remove the current ID
                set.remove(currentId);
                matchingIsolates = set.toArray(new String[0]);
            }
            break;

        default:
            matchingIsolates = null;
            break;
        }

        if (matchingIsolates == null || matchingIsolates.length == 0) {
            // No IDs found
            return null;
        }

        final Map<String, HostAccess> accesses = new HashMap<String, HostAccess>();
        for (final String isolate : matchingIsolates) {
            // Find all accesses for this group
            final HostAccess access = getIsolateAccess(isolate);
            if (access != null) {
                accesses.put(isolate, access);
            }
        }

        return accesses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#getGroupAccesses(java.lang.String)
     */
    @Override
    public synchronized Map<String, HostAccess> getGroupAccesses(
            final String aGroupName) {

        if (aGroupName == null) {
            // Empty name
            return null;
        }

        final List<String> isolates = pGroups.get(aGroupName.toLowerCase());
        if (isolates == null) {
            // Unknown group
            return null;
        }

        final Map<String, HostAccess> accesses = new HashMap<String, HostAccess>();
        for (final String isolate : isolates) {
            // Find all accesses for this group
            final HostAccess access = getIsolateAccess(isolate);
            if (access != null) {
                accesses.put(isolate, access);
            }
        }

        if (accesses.isEmpty()) {
            // Nothing found, consider the group unknown
            return null;
        }

        return accesses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#getHostForNode(java.lang.String)
     */
    @Override
    public synchronized String getHostForNode(final String aNodeName) {

        return pNodesHost.get(aNodeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#getIsolateAccess(java.lang.String)
     */
    @Override
    public synchronized HostAccess getIsolateAccess(final String aIsolateId) {

        final HostAccess nodeAccess = pAccesses.get(aIsolateId);
        if (nodeAccess == null) {
            // Unknown isolate
            return null;
        }

        if (getIsolateId().equals(aIsolateId)) {
            // Special case
            return ISignalDirectory.LOCAL_ACCESS;
        }

        final String nodeHost = pNodesHost.get(nodeAccess.getAddress());
        if (nodeHost == null) {
            // Unknown host
            pLogger.logWarn(this, "getIsolateAccess", "Unknown node=",
                    nodeHost, "for isolate=", aIsolateId);
            return null;
        }

        return new HostAccess(nodeHost, nodeAccess.getPort());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory# getCurrentIsolateId()
     */
    @Override
    public String getIsolateId() {

        // Isolate ID can change on slave agent order
        return System.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#getLocalNode()
     */
    @Override
    public synchronized String getIsolateNode(final String aIsolateId) {

        if (aIsolateId == null || aIsolateId.isEmpty()) {
            // No need to loop
            return null;
        }

        for (final Entry<String, List<String>> entry : pNodesIsolates
                .entrySet()) {
            final List<String> isolates = entry.getValue();
            if (isolates.contains(aIsolateId)) {
                // Found !
                return entry.getKey();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#getIsolatesOnNode(java.lang.String)
     */
    @Override
    public synchronized String[] getIsolatesOnNode(final String aNodeName) {

        final List<String> isolates = pNodesIsolates.get(aNodeName);
        if (isolates != null && !isolates.isEmpty()) {
            // Return a copy of the list
            return isolates.toArray(new String[0]);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#getLocalNode()
     */
    @Override
    public String getLocalNode() {

        return System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_NODE);
    }

    /**
     * Returns a collection that contains the base one and the additional
     * content. Uses a new collection if needed.
     * 
     * @param aBaseCollection
     *            A base collection (can be null)
     * @param aMinimumContent
     *            An object that must be in the returned collection
     * @return A new collection if the base one didn't contain the minimum
     *         content
     */
    private Collection<String> getMinimalCollection(
            final Collection<String> aBaseCollection,
            final String aMinimumContent) {

        final Collection<String> ignoredNodes;
        if (aBaseCollection == null) {
            // Use a new set with only the local node
            ignoredNodes = new HashSet<String>();
            ignoredNodes.add(aMinimumContent);

        } else if (!aBaseCollection.contains(aMinimumContent)) {
            // Use a new list, with the content
            ignoredNodes = new HashSet<String>(aBaseCollection);
            ignoredNodes.add(aMinimumContent);

        } else {
            // Nothing to do
            ignoredNodes = aBaseCollection;
        }

        return ignoredNodes;
    }

    /**
     * Retrieves the current state of the registration of the given isolate.
     * Returns null if the isolate is unknown.
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The registration state, or null.
     */
    private EIsolateRegistrationState getRegistrationState(
            final String aIsolateId) {

        try {
            return pRegistrationStatus.getState(aIsolateId);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getRegistrationState",
                    "Unknown isolate ID=", aIsolateId);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Invalidate
    @Override
    public void invalidatePojo() throws BundleException {

        // Clear the status storage
        pStatusCreator.deleteStorage(pRegistrationStatus);
        pRegistrationStatus = null;

        pAccesses.clear();
        pGroups.clear();
        pNodesHost.clear();
        pNodesIsolates.clear();

        pLogger.logInfo(this, "invalidatePojo", "Signals directory gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#isRegistered(java.lang.String)
     */
    @Override
    public boolean isRegistered(final String aIsolateId) {

        return pAccesses.containsKey(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#isValidated(java.lang.String)
     */
    @Override
    public synchronized boolean isValidated(final String aIsolateId) {

        final EIsolateRegistrationState state = getRegistrationState(aIsolateId);
        return state == EIsolateRegistrationState.VALIDATED
                || state == EIsolateRegistrationState.NOTIFIED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#notifyIsolatePresence(java.lang.String
     * )
     */
    @Override
    public synchronized boolean notifyIsolatePresence(final String aIsolateId) {

        // Update the registration
        if (registrationStateChange(aIsolateId,
                EIsolateRegistrationState.NOTIFIED)) {

            // We can notify listeners
            notifyPresenceListeners(aIsolateId, getIsolateNode(aIsolateId),
                    EPresence.REGISTERED);

            return true;

        } else {
            // Can't change state
            pLogger.logWarn(this, "notifyIsolatePresence",
                    "Can't notify the presence of isolate=", aIsolateId,
                    "due to current state=", getRegistrationState(aIsolateId));
        }

        return false;
    }

    /**
     * Notifies isolate presence listeners of an event
     * 
     * @param aIsolateId
     *            Isolate ID
     * @param aNode
     *            Node of the isolate
     * @param aPresence
     *            Presence event type
     */
    private synchronized void notifyPresenceListeners(final String aIsolateId,
            final String aNode, final EPresence aPresence) {

        for (final IIsolatePresenceListener listener : pListeners) {
            // Notify all listeners
            try {
                listener.handleIsolatePresence(aIsolateId, aNode, aPresence);

            } catch (final Exception ex) {
                // Just log...
                pLogger.logWarn(this, "notifyPresenceListeners", "Listener=",
                        listener, "failed to handle event", ex);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#registerIsolate(java.lang.String,
     * java.lang.String, java.lang.String, int, java.lang.String[])
     */
    @Override
    public synchronized boolean registerIsolate(final String aIsolateId,
            final String aNode, final int aPort, final String... aGroups) {

        if (aIsolateId == null || aIsolateId.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Empty isolate ID: ''{0}''", aIsolateId));
        }

        if (aNode == null || aNode.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Empty node name for isolate ''{0}''", aIsolateId));
        }

        if (aIsolateId.equals(getIsolateId())) {
            // Ignore our own registration
            return false;
        }

        // Prepare the new access object
        final HostAccess oldAccess = pAccesses.get(aIsolateId);
        final HostAccess isolateAccess = new HostAccess(aNode, aPort);

        if (oldAccess != null) {
            // Already known isolate
            if (isolateAccess.equals(oldAccess)) {
                // Same access, no update, stop here
                return false;

            } else {
                // Log the update
                pLogger.logDebug(this, "registerIsolate",
                        "Already known isolate=", aIsolateId,
                        "- Access updated from=", oldAccess, "to=",
                        isolateAccess);
            }

            final String oldNode = oldAccess.getAddress();
            if (!aNode.equals(oldNode)) {
                // Isolate moved to another node -> remove the old entry
                pLogger.logInfo(this, "registerIsolate", "Isolate ID=",
                        aIsolateId, "moved from=", oldNode, "to=", aNode);

                final List<String> isolates = pNodesIsolates.get(oldNode);
                if (isolates != null) {
                    isolates.remove(aIsolateId);
                }

                // Consider an unregistration
                unregisterIsolate(aIsolateId);
            }
        }

        // Store the access
        pAccesses.put(aIsolateId, isolateAccess);

        // Store the node
        List<String> isolates = pNodesIsolates.get(aNode);
        if (isolates == null) {
            // Create the node entry
            isolates = new ArrayList<String>();
            pNodesIsolates.put(aNode, isolates);
        }

        if (!isolates.contains(aIsolateId)) {
            isolates.add(aIsolateId);
        }

        // Store the groups
        if (aGroups != null) {
            for (final String group : aGroups) {
                // Lower case for case insensitivity
                final String lowerGroup = group.toLowerCase();

                isolates = pGroups.get(lowerGroup);
                if (isolates == null) {
                    // Create the group
                    isolates = new ArrayList<String>();
                    pGroups.put(lowerGroup, isolates);
                }

                if (!isolates.contains(aIsolateId)) {
                    isolates.add(aIsolateId);
                }
            }
        }

        // Store the registration state
        registrationStateCreate(aIsolateId,
                EIsolateRegistrationState.REGISTERED);

        pLogger.logDebug(this, "registerIsolate", "Registered isolate ID=",
                aIsolateId, "Access=", isolateAccess);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#registerLocal(int,
     * java.lang.String[])
     */
    @Override
    public synchronized void registerLocal(final int aPort,
            final String... aGroups) {

        final String isolateId = getIsolateId();
        final String node = getLocalNode();

        // Store the access port...
        pCurrentIsolatePort = aPort;

        // Store the node
        List<String> isolates = pNodesIsolates.get(node);
        if (isolates == null) {
            // Create the node entry
            isolates = new ArrayList<String>();
            pNodesIsolates.put(node, isolates);
        }

        if (!isolates.contains(isolateId)) {
            isolates.add(isolateId);
        }

        // Store the groups
        if (aGroups != null) {
            for (final String group : aGroups) {
                // Lower case for case insensitivity
                final String lowerGroup = group.toLowerCase();

                isolates = pGroups.get(lowerGroup);
                if (isolates == null) {
                    // Create the group
                    isolates = new ArrayList<String>();
                    pGroups.put(lowerGroup, isolates);
                }

                if (!isolates.contains(isolateId)) {
                    isolates.add(isolateId);
                }
            }
        }

        // We are immediately validated
        if (getRegistrationState(isolateId) == null) {
            // First registration
            registrationStateCreate(isolateId,
                    EIsolateRegistrationState.VALIDATED);

            // Notify listeners
            notifyIsolatePresence(isolateId);

        } else {
            pLogger.logDebug(this, "registerLocal",
                    "Re-definition of current isolate");
        }
    }

    /**
     * Changes the state of the registration of an isolate
     * 
     * @param aIsolateId
     *            ID of the isolate
     * @param aNewState
     *            The new registration state
     * @return True on success, else false
     */
    private boolean registrationStateChange(final String aIsolateId,
            final EIsolateRegistrationState aNewState) {

        try {
            pRegistrationStatus.changeState(aIsolateId, aNewState);
            return true;

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "isolateStateChange", "Invalid isolate ID=",
                    aIsolateId, ex);

        } catch (final InvalidStateException ex) {
            pLogger.logWarn(this, "isolateStateChange",
                    "Invalid state change=", aNewState, "for isolate=",
                    aIsolateId, ex);
        }

        return false;
    }

    /**
     * Stores a new isolate in the status storage
     * 
     * @param aIsolateId
     *            ID of the stored isolate
     * @param aInitialState
     *            The initial registration state
     * @return True on success, else false
     */
    private boolean registrationStateCreate(final String aIsolateId,
            final EIsolateRegistrationState aInitialState) {

        try {
            return pRegistrationStatus.store(aIsolateId, null, aInitialState);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "isolateStateCreate", "Invalid isolate ID=",
                    aIsolateId, ex);

        } catch (final InvalidStateException ex) {
            pLogger.logWarn(this, "isolateStateCreate",
                    "Invalid initial state=", aInitialState, "for isolate=",
                    aIsolateId, ex);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#setNodeAddress(java.lang.String,
     * java.lang.String)
     */
    @Override
    public synchronized String setNodeAddress(final String aNodeName,
            final String aHostAddress) {

        // Get the previous address
        final String oldAddress = pNodesHost.get(aNodeName);

        if (aHostAddress == null || aHostAddress.isEmpty()
                || aHostAddress.equals(oldAddress)) {
            // No modification
            return oldAddress;
        }

        pLogger.logInfo(this, "setNodeAddress", "Address of node=", aNodeName,
                "updated: previous=", oldAddress, "new=", aHostAddress);

        return pNodesHost.put(aNodeName, aHostAddress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#unregisterIsolate(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalDirectory#storeDump(java.util.Map,
     * java.util.Collection, java.util.Collection)
     */
    @Override
    public synchronized String[] storeDump(final Map<?, ?> aDumpedDirectory,
            final Collection<String> aIgnoredNodes,
            final Collection<String> aIgnoredIds) {

        // 0. Always ignore the current isolate and node
        final String localId = getIsolateId();
        final String localNode = getLocalNode();

        final Collection<String> ignoredNodes = getMinimalCollection(
                aIgnoredNodes, localNode);
        final Collection<String> ignoredIds = getMinimalCollection(aIgnoredIds,
                localId);

        // 1. Setup nodes hosts
        final Map<?, ?> nodesHost = (Map<?, ?>) aDumpedDirectory
                .get("nodes_host");
        for (final Entry<?, ?> entry : nodesHost.entrySet()) {
            final String node = (String) entry.getKey();
            if (!ignoredNodes.contains(node)) {
                // Node passed the filter
                setNodeAddress(node, (String) entry.getValue());
            }
        }

        // 2. Prepare isolates information
        final Map<String, IsolateInfo> isolates = new HashMap<String, IsolateInfo>();

        final Map<?, ?> accesses = (Map<?, ?>) aDumpedDirectory.get("accesses");
        for (final Entry<?, ?> entry : accesses.entrySet()) {
            // Cast entry
            final String isolateId = (String) entry.getKey();
            final Map<?, ?> access = (Map<?, ?>) entry.getValue();

            if (!ignoredIds.contains(isolateId)) {
                // Create the information bean
                isolates.put(isolateId,
                        new IsolateInfo(isolateId, (String) access.get("node"),
                                (Integer) access.get("port")));
            }
        }

        final Map<?, ?> groups = (Map<?, ?>) aDumpedDirectory.get("groups");
        for (final Entry<?, ?> entry : groups.entrySet()) {
            // Reconstruct groups
            final String group = (String) entry.getKey();
            final Object rawGroupIsolates = entry.getValue();

            Collection<?> groupIsolates = null;
            if (rawGroupIsolates instanceof Collection) {
                // Collection...
                groupIsolates = (Collection<?>) rawGroupIsolates;

            } else if (rawGroupIsolates instanceof Object[]) {
                // Array...
                groupIsolates = Arrays.asList((Object[]) rawGroupIsolates);
            }

            if (groupIsolates != null) {
                for (final Object isolate : groupIsolates) {
                    final IsolateInfo info = isolates.get(isolate);
                    if (info != null) {
                        info.getGroups().add(group);
                    }
                }

            } else {
                pLogger.logWarn(this, "grabDirectory", "Unreadable groups=",
                        rawGroupIsolates.getClass().getName());
            }
        }

        // 3. Register all new isolates
        final List<String> newIsolates = new ArrayList<String>();
        for (final IsolateInfo info : isolates.values()) {
            if (registerIsolate(info.getId(), info.getNode(), info.getPort(),
                    info.getGroups().toArray(new String[0]))) {

                newIsolates.add(info.getId());
            }
        }

        if (newIsolates.isEmpty()) {
            // Return null instead of an empty list
            return null;
        }

        return newIsolates.toArray(new String[newIsolates.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#synchronizingIsolatePresence(java
     * .lang.String)
     */
    @Override
    public synchronized boolean synchronizingIsolatePresence(
            final String aIsolateId) {

        final EIsolateRegistrationState currentState = getRegistrationState(aIsolateId);
        switch (currentState) {
        case VALIDATED:
        case NOTIFIED:
            // Do nothing, we already have validated this isolate
            return false;

        default:
            // Default: update the component state
            return registrationStateChange(aIsolateId,
                    EIsolateRegistrationState.SYNCHRONIZING);
        }
    }

    @Override
    public synchronized boolean unregisterIsolate(final String aIsolateId) {

        if (aIsolateId == null || aIsolateId.isEmpty()
                || !pAccesses.containsKey(aIsolateId)) {
            // Nothing to do
            return false;
        }

        // Remove the access information
        pAccesses.remove(aIsolateId);

        // Remove isolate reference in its node
        String isolateNode = null;
        for (final Entry<String, List<String>> entry : pNodesIsolates
                .entrySet()) {
            final String node = entry.getKey();
            final List<String> isolates = entry.getValue();

            if (isolates.contains(aIsolateId)) {
                // Remove the reference
                isolates.remove(aIsolateId);
                isolateNode = node;
                break;
            }
        }

        // Remove references in groups
        for (final List<String> isolates : pGroups.values()) {
            if (isolates.contains(aIsolateId)) {
                isolates.remove(aIsolateId);
            }
        }

        // Keep the current registration state
        final EIsolateRegistrationState registrationState = getRegistrationState(aIsolateId);

        try {
            // Remove the registration state
            pRegistrationStatus.remove(aIsolateId);

        } catch (final InvalidIdException ex) {
            pLogger.logDebug(this, "unregisterIsolate",
                    "Error removing registration state for isolate=",
                    aIsolateId, ex);
        }

        if (registrationState == EIsolateRegistrationState.NOTIFIED) {
            // Notify listeners if they knew about the isolate
            notifyPresenceListeners(aIsolateId, isolateNode,
                    EPresence.UNREGISTERED);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#validateIsolatePresence(java.lang
     * .String)
     */
    @Override
    public synchronized boolean validateIsolatePresence(final String aIsolateId) {

        if (getRegistrationState(aIsolateId) == EIsolateRegistrationState.NOTIFIED) {
            // We already have notified the presence of this isolate
            return false;
        }

        // Change the state the registration
        if (registrationStateChange(aIsolateId,
                EIsolateRegistrationState.VALIDATED)) {
            pLogger.logDebug(this, "validateIsolatePresence", "Isolate ID=",
                    aIsolateId, "validated");
            return true;

        } else {
            // Can't change state
            pLogger.logWarn(this, "validateIsolatePresence",
                    "Failed to change registration state of isolate=",
                    aIsolateId, "due to state=",
                    getRegistrationState(aIsolateId));
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Validate
    @Override
    public void validatePojo() throws BundleException {

        // Create the status storage
        pRegistrationStatus = pStatusCreator.createStorage();

        // Register the local isolate, without access port nor group
        pAccesses.put(getIsolateId(), ISignalDirectory.LOCAL_ACCESS);
        pNodesHost.put(getLocalNode(), "localhost");
        registerLocal(-1, (String[]) null);

        pLogger.logInfo(this, "validatePojo", "Signals directory ready");
    }
}
