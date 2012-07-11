/**
 * File:   DirectoryUpdater.java
 * Author: Thomas Calmant
 * Date:   18 juin 2012
 */
package org.psem2m.signals.directory.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener.EPresence;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalBroadcaster.ESendMode;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalDirectoryConstants;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.IWaitingSignal;
import org.psem2m.signals.IWaitingSignalListener;

/**
 * Java implementation of the directory updater
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-directory-updater-factory", publicFactory = false)
@Instantiate(name = "psem2m-signals-directory-updater")
public class DirectoryUpdater implements ISignalListener,
        IWaitingSignalListener {

    /**
     * A small information storage
     * 
     * @author Thomas Calmant
     */
    private class IsolateInfo {
        /** Isolate groups */
        final List<String> groups = new ArrayList<String>();

        /** Isolate ID */
        String id;

        /** Isolate node */
        String node;

        /** Signals access port */
        int port;
    }

    /** The signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /**
     * The port to access the directory dumper. The member shall not be changed
     * after component validation
     */
    private int pDumperPort;

    /** Isolates listeners */
    @Requires(optional = true)
    private IIsolatePresenceListener[] pListeners;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The signal receiver (it must be accessible) */
    @Requires(filter = "(" + ISignalReceiver.PROPERTY_ONLINE + "=true)")
    private ISignalReceiver pReceiver;

    /** The signal sender (it must be usable) */
    @Requires(filter = "(" + ISignalBroadcaster.PROPERTY_ONLINE + "=true)")
    private ISignalBroadcaster pSender;

    /**
     * Retrieves the directory of a remote isolate.
     * 
     * This method is called after a CONTACT signal has been received from a
     * monitor.
     * 
     * @param aSignalData
     *            The received contact signal
     */
    protected void grabRemoteDirectory(final ISignalData aSignalData) {

        // Only monitors can send us this kind of signal
        final String remoteId = aSignalData.getSenderId();
        if (!remoteId
                .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_MONITOR)) {
            // Log & Ignore
            pLogger.logWarn(this, "grabRemoteDirectory",
                    "Contacts must be made by monitors only, sender=", remoteId);
            return;
        }

        // Get information on the sender
        final String remoteAddress = aSignalData.getSenderAddress();
        final String remoteNode = aSignalData.getSenderNode();
        final Object rawContent = aSignalData.getSignalContent();
        if (!(rawContent instanceof Map)) {
            pLogger.logWarn(this, "grabRemoteDirectory", "Unreadable content=",
                    rawContent);
            return;
        }

        // Get the dumper port
        final Map<?, ?> content = (Map<?, ?>) rawContent;
        final Integer remotePort = (Integer) content.get("port");
        if (remotePort == null) {
            pLogger.logWarn(this, "grabRemoteDirectory", "No access port given");
            return;
        }

        // Store the node information
        pDirectory.setNodeAddress(remoteNode, remoteAddress);

        // Grab the directory
        try {
            final Object[] results = pSender.sendTo(
                    ISignalDirectoryConstants.SIGNAL_DUMP, null, remoteAddress,
                    remotePort);

            // Store the dumped directory
            handleDumpedDirectory(results, remoteNode);

            // Send our registration signal
            sendRegistration(remoteAddress, remotePort);

            final String[] isolates = pDirectory.getAllIsolates(null, false);
            if (isolates != null) {
                for (final String isolate : isolates) {
                    notifyPresenceListeners(isolate,
                            pDirectory.getIsolateNode(isolate),
                            EPresence.REGISTERED);
                }
            }

        } catch (final Exception e) {
            pLogger.logSevere(this, "grabRemoteDirectory",
                    "Error grabbing the directory of host=", remoteAddress,
                    "port=", remotePort, "id=", remoteId, ":", e);
        }
    }

    /**
     * Tries to find the directory dump in the results then stores it
     * 
     * If aIgnoredNode is not null, the address corresponding to it in the
     * dumped directory won't be stored.
     * 
     * @param aResults
     *            The signal results
     * @param aIgnoredNode
     *            The address for this node must be ignored
     */
    protected void handleDumpedDirectory(final Object[] aResults,
            final String aIgnoredNode) {

        if (aResults == null || aResults.length == 0) {
            // No result...
            pLogger.logWarn(this, "grabDirectory",
                    "Nothing returned by the directory dumper");
            return;
        }

        // Get the first map result only
        if (aResults.length != 1) {
            pLogger.logWarn(this, "grabDirectory",
                    "More than one result found. Ignoring others.");
        }

        // Type conversion...
        Map<?, ?> dump = null;
        for (final Object result : aResults) {
            if (result instanceof Map) {
                dump = (Map<?, ?>) result;
                break;
            }
        }

        if (dump != null) {
            // All good, store it
            storeDirectory(dump, aIgnoredNode);

            // Send our registration, one we have the directory...
            pLogger.logDebug(this, "validate", "Sending registration");
            sendRegistration("localhost", pDumperPort);

            final String[] isolates = pDirectory.getAllIsolates(null, false);
            if (isolates != null) {
                for (final String isolate : isolates) {
                    notifyPresenceListeners(isolate,
                            pDirectory.getIsolateNode(isolate),
                            EPresence.REGISTERED);
                }
            }

        } else {
            // Nothing found
            pLogger.logWarn(this, "grabDirectory",
                    "No readable result in the dump");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalListener#handleReceivedSignal(java.lang.String,
     * org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (ISignalDirectoryConstants.SIGNAL_DUMP.equals(aSignalName)) {
            // Dump the directory
            return pDirectory.dump();

        } else if (ISignalDirectoryConstants.SIGNAL_REGISTER
                .equals(aSignalName)) {
            // Isolate registration
            registerIsolate(aSignalData);

        } else if (ISignalsConstants.ISOLATE_LOST_SIGNAL.equals(aSignalName)) {
            // Isolate lost
            final String lostIsolate = (String) aSignalData.getSignalContent();
            final String lostIsolateNode = pDirectory
                    .getIsolateNode(lostIsolate);

            pDirectory.unregisterIsolate(lostIsolate);

            // Notify listeners
            notifyPresenceListeners(lostIsolate, lostIsolateNode,
                    EPresence.UNREGISTERED);
        }

        // No result
        return null;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Unregister signals
        pReceiver.unregisterListener(
                ISignalDirectoryConstants.SIGNAL_PREFIX_MATCH_ALL, this);

        pReceiver.unregisterListener(ISignalsConstants.ISOLATE_LOST_SIGNAL,
                this);

        pLogger.logInfo(this, "invalidate", "Directory Updater Gone");
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
    protected void notifyPresenceListeners(final String aIsolateId,
            final String aNode, final EPresence aPresence) {

        pLogger.logDebug(this, "notifyPresenceListeners",
                "Notify presence of=", aIsolateId, "to=", pListeners);

        for (final IIsolatePresenceListener listener : pListeners) {
            // Notify all listeners
            try {
                listener.handleIsolatePresence(aIsolateId, aNode, aPresence);

            } catch (final Exception ex) {
                // Just log...
                pLogger.logWarn(this, "notifyPresenceListeners", "Listener=",
                        listener, "failed to handle event");
            }
        }
    }

    /**
     * Handles an isolate registration signal: registers the isolate in the
     * local directory and propagates the registration if necessary
     * 
     * @param aSignalData
     *            The signal data
     */
    protected void registerIsolate(final ISignalData aSignalData) {

        // Extract information
        @SuppressWarnings("unchecked")
        final Map<String, Object> content = (Map<String, Object>) aSignalData
                .getSignalContent();

        final String isolateId = (String) content.get("id");
        if (pDirectory.getIsolateId().equals(isolateId)) {
            // Ignore self-registration
            return;
        }

        final String node = (String) content.get("node");
        final Integer port = (Integer) content.get("port");
        final Boolean propagate = (Boolean) content.get("propagate");

        // Get the group
        final Object rawGroups = content.get("groups");
        String[] groups;
        if (rawGroups instanceof Collection) {
            // Collection...
            groups = ((Collection<?>) rawGroups).toArray(new String[0]);

        } else if (rawGroups instanceof String[]) {
            // Best case
            groups = (String[]) rawGroups;

        } else if (rawGroups instanceof Object[]) {
            // Simple array...
            groups = Arrays.asList((Object[]) rawGroups).toArray(new String[0]);

        } else {
            // Default
            pLogger.logWarn(this, "registerIsolate",
                    "Unreadable isolate groups, class=", rawGroups.getClass()
                            .getName());

            groups = new String[] { "ALL", "ISOLATES" };
        }

        String address;
        if (aSignalData.getSenderNode().equals(node)) {
            /*
             * If both the registered and the registrar are on the same node,
             * use the sender address to update the node access
             */
            address = aSignalData.getSenderAddress();

        } else {
            /*
             * Else use the address indicated in the signal, or use the sender
             * address
             */
            address = (String) content.get("address");
            if (address == null || address.isEmpty()) {
                address = aSignalData.getSenderAddress();
            }
        }

        // 1. Update the node address
        pDirectory.setNodeAddress(node, address);

        // 2. Register the isolate
        pDirectory.registerIsolate(isolateId, node, port, groups);

        // Notify listeners
        notifyPresenceListeners(isolateId, node, EPresence.REGISTERED);

        // 3. Propagate the registration if needed
        if (propagate.booleanValue()) {
            // Stop propagation
            content.put("propagate", Boolean.FALSE);

            // Store the address we used
            content.put("address", address);

            pSender.fireGroup(ISignalDirectoryConstants.SIGNAL_REGISTER,
                    content, EBaseGroup.OTHERS, isolateId);
        }
    }

    /**
     * Sends the registration signal the given isolate (that will propagate the
     * signal once)
     * 
     * @param aRemoteAddress
     *            The target address
     * @param aRemotePort
     *            The target port
     */
    protected void sendRegistration(final String aRemoteAddress,
            final int aRemotePort) {

        final String isolateId = pDirectory.getIsolateId();

        // Compute groups
        final List<String> groups = new ArrayList<String>();
        groups.add("ALL");
        if (isolateId.startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)) {
            // Forker can only be forkers...
            groups.add("FORKERS");

        } else {
            // Normal isolate
            groups.add("ISOLATES");
        }

        if (isolateId
                .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_MONITOR)) {
            groups.add("MONITORS");
        }

        // Prepare content
        final Map<String, Object> content = new HashMap<String, Object>();
        content.put("id", isolateId);
        // Let the receiver compute our address
        content.put("address", null);
        content.put("node", pDirectory.getLocalNode());
        content.put("groups", groups.toArray(new String[0]));
        content.put("port", pReceiver.getAccessInfo().getPort());
        content.put("propagate", true);

        // Send the signal (don't wait for a result)
        try {
            pSender.sendTo(ISignalDirectoryConstants.SIGNAL_REGISTER, content,
                    aRemoteAddress, aRemotePort);

        } catch (final Exception e) {
            // Error ?
            pLogger.logWarn(this, "sendRegistration",
                    "Error sending registration signal to host=",
                    aRemoteAddress, "port=", aRemotePort,
                    "Sending to all known isolates");

            // Try with others...
            pSender.sendGroup(ISignalDirectoryConstants.SIGNAL_REGISTER,
                    content, EBaseGroup.OTHERS);
        }
    }

    /**
     * Stores the content of the given directory dump
     * 
     * If aIgnoredNode is not null, the address corresponding to it in the
     * dumped directory won't be stored.
     * 
     * @param aDumpedDirectory
     *            A directory dump
     * @param aIgnoredNode
     *            The address for this node must be ignored
     */
    protected void storeDirectory(final Map<?, ?> aDumpedDirectory,
            final String aIgnoredNode) {

        // Local information
        final String localId = pDirectory.getIsolateId();
        final String localNode = pDirectory.getLocalNode();

        // 1. Setup nodes hosts
        final Map<?, ?> nodesHost = (Map<?, ?>) aDumpedDirectory
                .get("nodes_host");
        for (final Entry<?, ?> entry : nodesHost.entrySet()) {
            final String node = (String) entry.getKey();
            if (!node.equals(localNode) && !node.equals(aIgnoredNode)) {
                // Node passed the filter
                pDirectory.setNodeAddress(node, (String) entry.getValue());
            }
        }

        // 2. Prepare isolates information
        final Map<String, IsolateInfo> isolates = new HashMap<String, DirectoryUpdater.IsolateInfo>();

        final Map<?, ?> accesses = (Map<?, ?>) aDumpedDirectory.get("accesses");
        for (final Entry<?, ?> entry : accesses.entrySet()) {
            // Cast entry
            final String isolateId = (String) entry.getKey();
            final Map<?, ?> access = (Map<?, ?>) entry.getValue();

            if (localId.equals(isolateId)) {
                // Ignore current isolate
                continue;
            }

            // Create the information bean
            final IsolateInfo info = new IsolateInfo();
            info.id = isolateId;
            info.node = (String) access.get("node");
            info.port = (Integer) access.get("port");

            isolates.put(isolateId, info);
        }

        final Map<?, ?> groups = (Map<?, ?>) aDumpedDirectory.get("groups");
        for (final Entry<?, ?> entry : groups.entrySet()) {

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
                        info.groups.add(group);
                    }
                }

            } else {
                pLogger.logWarn(this, "grabDirectory", "Unreadable groups=",
                        rawGroupIsolates.getClass().getName());
            }
        }

        // 3. Register all new isolates
        for (final IsolateInfo info : isolates.values()) {
            pDirectory.registerIsolate(info.id, info.node, info.port,
                    info.groups.toArray(new String[0]));
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        pLogger.logDebug(this, "validate", "Directory Updater starting...");

        // Compute the dump port
        final String dumpPortStr = System
                .getProperty(ISignalDirectoryConstants.PROP_DUMPER_PORT);
        pDumperPort = -1;
        try {
            pDumperPort = Integer.parseInt(dumpPortStr);

        } catch (final NumberFormatException e) {
            // Ignore
        }

        if (pDumperPort <= 0) {
            // Bad port
            pLogger.logWarn(this, "validate", "Unreadable dumper port=",
                    dumpPortStr);

        } else {
            // Retrieve the directory
            pLogger.logDebug(this, "validate", "Grabbing directory from port=",
                    pDumperPort);

            // Stack the signal...
            pSender.stack(ISignalDirectoryConstants.SIGNAL_DUMP, null, this,
                    ESendMode.SEND, Integer.MAX_VALUE, new HostAccess(
                            "localhost", pDumperPort));
        }

        // Register to signals
        pLogger.logDebug(this, "validate", "Registering to signals...");
        pReceiver.registerListener(
                ISignalDirectoryConstants.SIGNAL_PREFIX_MATCH_ALL, this);
        pReceiver.registerListener(ISignalsConstants.ISOLATE_LOST_SIGNAL, this);

        pLogger.logInfo(this, "validate", "Directory Updater Ready");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.IWaitingSignalListener#waitingSignalSent(org.psem2m
     * .signals.IWaitingSignal)
     */
    @Override
    public void waitingSignalSent(final IWaitingSignal aSignal) {

        if (ISignalDirectoryConstants.SIGNAL_DUMP.equals(aSignal.getName())) {
            // Found
            pLogger.logDebug(this, "waitingSignalSent",
                    "Dump directory signal response received");

            // Call the handling code
            handleDumpedDirectory(aSignal.getSendToResult(), null);

        } else {
            // Ignored result
            pLogger.logDebug(this, "waitingSignalSent", "Signal name=",
                    aSignal.getName(), "sent.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.IWaitingSignalListener#waitingSignalTimeout(org.psem2m
     * .signals.IWaitingSignal)
     */
    @Override
    public void waitingSignalTimeout(final IWaitingSignal aSignal) {

        pLogger.logWarn(this, "waitingSignalTimeout", "Signal name=",
                aSignal.getName(), "timed out");
    }
}
