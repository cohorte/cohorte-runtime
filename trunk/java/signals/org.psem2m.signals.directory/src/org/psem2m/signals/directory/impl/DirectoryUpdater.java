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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalBroadcaster.ESendMode;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalDirectoryConstants;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalSendResult;
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

    /** The signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /**
     * The port to access the directory dumper. The member shall not be changed
     * after component validation
     */
    private int pDumperPort;

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
    private void grabRemoteDirectory(final ISignalData aSignalData) {

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

        } catch (final Exception e) {
            pLogger.logSevere(this, "grabRemoteDirectory",
                    "Error grabbing the directory of host=", remoteAddress,
                    "port=", remotePort, "id=", remoteId, ":", e);
        }
    }

    /**
     * Handles an ACK signal
     * 
     * @param aSenderId
     *            ID of the signal broadcaster
     */
    private void handleAck(final String aSenderId) {

        pLogger.logDebug(this, "handleSynAck", "ACK from", aSenderId);

        // Our acknowledgment has been received
        if (pDirectory.validateIsolatePresence(aSenderId)) {
            pDirectory.notifyIsolatePresence(aSenderId);
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
    private synchronized void handleDumpedDirectory(final Object[] aResults,
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
            // All good, store the dump in the directory
            pDirectory.storeDump(dump, Arrays.asList(aIgnoredNode), null);

            // Send our registration signal to all isolates we known,
            // without propagation
            sendRegistrationToAll(true);

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
    public synchronized Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // The sender ID
        final String senderId = aSignalData.getSenderId();

        if (ISignalDirectoryConstants.SIGNAL_DUMP.equals(aSignalName)) {
            pLogger.logDebug(this, "handleReceivedSignal", "DUMP from",
                    senderId);

            // Register the incoming isolate
            registerIsolate(aSignalData);

            // Dump the directory
            return pDirectory.dump();

        } else if (ISignalDirectoryConstants.SIGNAL_REGISTER
                .equals(aSignalName)) {
            // Register the isolate
            registerIsolate(aSignalData);

        } else if (ISignalDirectoryConstants.SIGNAL_REGISTER_SYNACK
                .equals(aSignalName)) {
            // We received a registration acknowledgment
            handleSynAck(senderId);

        } else if (ISignalDirectoryConstants.SIGNAL_REGISTER_ACK
                .equals(aSignalName)) {
            // Final acknowledgment
            handleAck(senderId);

        } else if (ISignalsConstants.ISOLATE_LOST_SIGNAL.equals(aSignalName)) {
            // Isolate lost
            final String lostIsolate = (String) aSignalData.getSignalContent();
            pDirectory.unregisterIsolate(lostIsolate);

        } else if (ISignalDirectoryConstants.SIGNAL_CONTACT.equals(aSignalName)) {
            // Monitor contact. Should only happen for forkers
            grabRemoteDirectory(aSignalData);
        }

        // No result
        return null;
    }

    /**
     * Handles a SYN-ACK signal
     * 
     * @param aSenderId
     *            ID of the signal broadcaster
     */
    private void handleSynAck(final String aSenderId) {

        pLogger.logDebug(this, "handleSynAck", "SYN-ACK from", aSenderId);

        if (!pDirectory.isRegistered(aSenderId)) {
            // Unknown isolate, ask for a REGISTER signal
            pLogger.logWarn(this, "handleSynAck", "Unknown isolate ID:",
                    aSenderId);

        } else if (pDirectory.synchronizingIsolatePresence(aSenderId)) {

            // Validate the registration (the isolate talked to us)
            pDirectory.validateIsolatePresence(aSenderId);

            // Our registration has been accepted, final acknowledgment
            // (blocking call)
            final ISignalSendResult result = pSender.send(
                    ISignalDirectoryConstants.SIGNAL_REGISTER_ACK, null,
                    aSenderId);

            if (result != null) {
                // Notify our listeners (final step)
                pDirectory.notifyIsolatePresence(aSenderId);

            } else {
                // Failed to talk to the isolate ?
                pLogger.logWarn(this, "handleSynAck", "Isolate ID=", aSenderId,
                        "did not respond to the ACK signal.");
            }

        } else {
            pLogger.logWarn(this, "handleSynAck",
                    "SYN-ACK ignored due to failed state change for isolate=",
                    aSenderId);
        }
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
     * Prepares the registration signal content.
     * 
     * @param aPropagate
     *            If true, the receivers of this signal will re-emit it
     * @return The content for a registration signal
     */
    private Map<String, Object> prepareRegistrationContent(
            final boolean aPropagate) {

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
        content.put("propagate", aPropagate);

        return content;
    }

    /**
     * Handles an isolate registration signal: registers the isolate in the
     * local directory and propagates the registration if necessary
     * 
     * @param aSignalData
     *            The signal data
     */
    private synchronized void registerIsolate(final ISignalData aSignalData) {

        // Get the ID of the sender
        final String senderId = aSignalData.getSenderId();

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
        final boolean newlyRegistered = pDirectory.registerIsolate(isolateId,
                node, port, groups);

        if (pDirectory.isRegistered(isolateId)) {
            // 2b. Acknowledge the registration

            if (isolateId.equals(senderId)) {
                /*
                 * Case 1: we got the registration from the isolate itself ->
                 * Send a SYN-ACK
                 */
                if (newlyRegistered) {
                    // Update registration state
                    pDirectory.synchronizingIsolatePresence(isolateId);
                }

                pSender.fire(ISignalDirectoryConstants.SIGNAL_REGISTER_SYNACK,
                        null, isolateId);

            } else if (newlyRegistered) {
                /*
                 * Case 2: we got the registration by propagation -> Send a
                 * REGISTER to the registered isolate
                 */
                pLogger.logDebug(this, "registerIsolate",
                        "Sending a registration signal to", isolateId,
                        "due to a propagation");
                pSender.fire(ISignalDirectoryConstants.SIGNAL_REGISTER,
                        prepareRegistrationContent(false), isolateId);
            }
        }

        // 3. Propagate the registration if needed
        if (propagate.booleanValue()) {
            // Stop propagation
            content.put("propagate", Boolean.FALSE);

            // Store the address we used
            content.put("address", address);

            pSender.fireGroup(ISignalDirectoryConstants.SIGNAL_REGISTER,
                    content, EBaseGroup.STORED, isolateId);
        }
    }

    /**
     * Sends the registration signal to all isolates stored in the directory
     * 
     * @param aPropagate
     *            If true, the receivers of this signal will re-emit it
     */
    private void sendRegistrationToAll(final boolean aPropagate) {

        // Get the content
        final Map<String, Object> content = prepareRegistrationContent(aPropagate);

        // Send the signal (don't wait for a result)
        final ISignalSendResult res = pSender.sendGroup(
                ISignalDirectoryConstants.SIGNAL_REGISTER, content,
                EBaseGroup.STORED);

        if (res == null) {
            // No answer, registration may have failed
            pLogger.logWarn(this, "sendRegistrationToAll",
                    "Registration signal not sent/received");

        } else {
            // Log the receivers
            pLogger.logDebug(this, "sendRegistrationToAll",
                    "Registration sent to=", res.getResults().keySet());
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        pLogger.logDebug(this, "validate", "Directory Updater starting...");

        // Register to signals
        pLogger.logDebug(this, "validate", "Registering to signals...");
        pReceiver.registerListener(
                ISignalDirectoryConstants.SIGNAL_PREFIX_MATCH_ALL, this);
        pReceiver.registerListener(ISignalsConstants.ISOLATE_LOST_SIGNAL, this);

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

            // Prepare signal content, without propagating the signal
            final Map<String, Object> content = prepareRegistrationContent(false);

            // Stack the signal
            pSender.stack(ISignalDirectoryConstants.SIGNAL_DUMP, content, this,
                    ESendMode.SEND, Integer.MAX_VALUE, new HostAccess(
                            "localhost", pDumperPort));
        }

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
