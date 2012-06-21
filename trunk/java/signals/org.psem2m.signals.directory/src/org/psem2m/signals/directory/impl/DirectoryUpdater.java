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
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalBroadcaster.ESendMode;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
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

    /** The dumper port system property */
    public static final String PROP_DUMPER_PORT = "psem2m.directory.dumper.port";

    /** Directory dump request */
    private static final String SIGNAL_DUMP = DirectoryUpdater.SIGNAL_PREFIX
            + "/dump";

    /** Prefix of all directory updater signals */
    private static final String SIGNAL_PREFIX = "/psem2m-directory-updater";

    /** Pattern to match all directory updater signals */
    private static final String SIGNAL_PREFIX_MATCH_ALL = DirectoryUpdater.SIGNAL_PREFIX
            + "/*";

    /** Isolate registration notification */
    private static final String SIGNAL_REGISTER = DirectoryUpdater.SIGNAL_PREFIX
            + "/register";

    /** The signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /** The port to access the directory dumper */
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
     * Tries to find the directory dump in the results then stores it
     * 
     * @param aResults
     *            The signal results
     */
    protected void handleDumpedDirectory(final Object[] aResults) {

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

        Map<?, ?> dump = null;
        for (final Object result : aResults) {
            if (result instanceof Map) {
                dump = (Map<?, ?>) result;
                break;
            }
        }

        if (dump != null) {
            // All good, store it
            storeDirectory(dump);

            // Send our registration, one we have the directory...
            pLogger.logDebug(this, "validate", "Sending registration");
            sendRegistration();

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

        if (SIGNAL_DUMP.equals(aSignalName)) {
            // Dump the directory
            return pDirectory.dump();

        } else if (SIGNAL_REGISTER.equals(aSignalName)) {
            // Isolate registration
            registerIsolate(aSignalData);
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
        pReceiver.unregisterListener(SIGNAL_PREFIX_MATCH_ALL, this);
    }

    /**
     * Handles an isolate registration signal
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
            pLogger.logWarn(this, "handleReceivedSignal",
                    "Unreadable isolate groups, class=", rawGroups.getClass()
                            .getName());

            groups = new String[] { "ALL", "ISOLATES" };
        }

        // 1. Update the node address
        pDirectory.setNodeAddress(node, aSignalData.getSenderAddress());

        // 2. Register the isolate
        pDirectory.registerIsolate(isolateId, node, port, groups);

        // 3. Propagate the registration if needed
        if (propagate.booleanValue()) {
            // Stop propagation
            content.put("propagate", Boolean.FALSE);
            pSender.sendGroup(SIGNAL_REGISTER, content, EBaseGroup.OTHERS);
        }
    }

    /**
     * Sends the registration signal to all known isolates
     */
    protected void sendRegistration() {

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
        content.put("node", pDirectory.getLocalNode());
        content.put("groups", groups.toArray(new String[0]));
        content.put("port", pReceiver.getAccessInfo().getPort());
        content.put("propagate", true);

        // Send the signal (don't wait for a result)
        try {
            pSender.sendTo(SIGNAL_REGISTER, content, "localhost", pDumperPort);
        } catch (final Exception e) {
            // Error ?
            pLogger.logWarn(
                    this,
                    "sendRegistration",
                    "Error sending registration signal to the directory dumper.",
                    "Sending to all known isolates");

            // Try with others...
            pSender.sendGroup(SIGNAL_REGISTER, content, EBaseGroup.OTHERS);
        }
    }

    /**
     * Stores the content of the given directory dump
     * 
     * @param aDumpedDirectory
     *            A directory dump
     */
    protected void storeDirectory(final Map<?, ?> aDumpedDirectory) {

        // 1. Setup nodes hosts
        final Map<?, ?> nodesHost = (Map<?, ?>) aDumpedDirectory
                .get("nodes_host");
        for (final Entry<?, ?> entry : nodesHost.entrySet()) {
            pDirectory.setNodeAddress((String) entry.getKey(),
                    (String) entry.getValue());
        }

        // 2. Prepare isolates information
        final Map<String, IsolateInfo> isolates = new HashMap<String, DirectoryUpdater.IsolateInfo>();

        final Map<?, ?> accesses = (Map<?, ?>) aDumpedDirectory.get("accesses");
        for (final Entry<?, ?> entry : accesses.entrySet()) {
            // Cast entry
            final String isolateId = (String) entry.getKey();
            final Map<?, ?> access = (Map<?, ?>) entry.getValue();

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
            pLogger.logDebug(this, "storeDirectory", "Registering id=",
                    info.id, "node=", info.node, "port=", info.port);

            pDirectory.registerIsolate(info.id, info.node, info.port,
                    info.groups.toArray(new String[0]));
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        pLogger.logInfo(this, "validate", "Directory Updater started");

        final String dumpPortStr = System.getProperty(PROP_DUMPER_PORT);
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
            pSender.stack(SIGNAL_DUMP, null, this, ESendMode.SEND,
                    Integer.MAX_VALUE, new HostAccess("localhost", pDumperPort));
        }

        // Register to signals
        pLogger.logDebug(this, "validate", "Registering to the receiver...");
        pReceiver.registerListener(SIGNAL_PREFIX_MATCH_ALL, this);
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

        if (SIGNAL_DUMP.equals(aSignal.getName())) {
            // Found
            pLogger.logDebug(this, "waitingSignalSent",
                    "Dump directory signal sent");

            // Call the handling code
            handleDumpedDirectory(aSignal.getSendToResult());

        } else {
            // Ingored result
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
