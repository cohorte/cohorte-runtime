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
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Java implementation of the directory updater
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-directory-updater-factory", publicFactory = false)
@Instantiate(name = "psem2m-signals-directory-updater")
public class DirectoryUpdater implements ISignalListener {

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

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The signal receiver */
    @Requires
    private ISignalReceiver pReceiver;

    /** The signal sender */
    @Requires
    private ISignalBroadcaster pSender;

    /**
     * Sends a directory dump signal to the given port on local host.
     * 
     * @param aDumpPort
     *            The signal receiver port
     */
    protected void grabDirectory(final int aDumpPort) {

        final Object[] results;
        try {
            results = pSender.sendTo(SIGNAL_DUMP, null, "localhost", aDumpPort);

        } catch (final Exception e) {
            pLogger.logWarn(this, "grabDirectory",
                    "Error requested for a directory dump:", e);
            return;
        }

        if (results == null || results.length == 0) {
            // No result...
            pLogger.logWarn(this, "grabDirectory",
                    "Nothing returned by the directory dumper");
            return;
        }

        // Get the first map result only
        if (results.length != 1) {
            pLogger.logWarn(this, "grabDirectory",
                    "More than one result found. Ignoring others.");
        }

        Map<?, ?> dump = null;
        for (final Object result : results) {
            if (result instanceof Map) {
                dump = (Map<?, ?>) result;
                break;
            }
        }

        if (dump == null) {
            // Nothing found
            pLogger.logWarn(this, "grabDirectory",
                    "No readable result in the dump");
            return;
        }

        // 1. Setup nodes hosts
        final Map<?, ?> nodesHost = (Map<?, ?>) dump.get("nodes_host");
        for (final Entry<?, ?> entry : nodesHost.entrySet()) {
            pDirectory.setNodeAddress((String) entry.getKey(),
                    (String) entry.getValue());
        }

        // 2. Prepare isolates information
        final Map<String, IsolateInfo> isolates = new HashMap<String, DirectoryUpdater.IsolateInfo>();

        final Map<?, ?> accesses = (Map<?, ?>) dump.get("accesses");
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

        final Map<?, ?> groups = (Map<?, ?>) dump.get("groups");
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
            final Map<?, ?> content = (Map<?, ?>) aSignalData
                    .getSignalContent();

            // Extract information
            final String isolateId = (String) content.get("id");
            final String node = (String) content.get("node");
            final Integer port = (Integer) content.get("port");

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
                groups = Arrays.asList((Object[]) rawGroups).toArray(
                        new String[0]);

            } else {
                // Default
                pLogger.logWarn(this, "handleReceivedSignal",
                        "Unreadable isolate groups, class=", rawGroups
                                .getClass().getName());

                groups = new String[] { "ALL", "ISOLATES" };
            }

            // Update the node address
            pDirectory.setNodeAddress(node, aSignalData.getSenderAddress());

            // Register the isolate
            pDirectory.registerIsolate(isolateId, node, port, groups);
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

        // Wait in a thread for a valid access
        new Thread(new Runnable() {

            @Override
            public void run() {

                final HostAccess access = pReceiver.getAccessInfo();
                if (access == null) {
                    pLogger.logSevere(this, "sendRegistration",
                            "NULL access information");

                    // Wait a little
                    try {
                        Thread.sleep(100);

                    } catch (final InterruptedException e) {
                        // We must stop there
                        return;
                    }
                }

                pLogger.logSevere(this, "sendRegistration",
                        "Access information found !");

                content.put("port", access.getPort());

                // Send the signal (don't wait for a result)
                pSender.fireGroup(SIGNAL_REGISTER, content, "ALL");
            }
        }).start();
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        final String dumpPortStr = System.getProperty(PROP_DUMPER_PORT);
        int dumpPort = -1;
        try {
            dumpPort = Integer.parseInt(dumpPortStr);

        } catch (final NumberFormatException e) {
            // Ignore
        }

        if (dumpPort <= 0) {
            // Bad port
            pLogger.logWarn(this, "validate", "Unreadable dumper port=",
                    dumpPortStr);

        } else {
            // Retrieve the directory
            grabDirectory(dumpPort);
        }

        // Send our registration
        sendRegistration();

        // Register to signals
        pReceiver.registerListener(SIGNAL_PREFIX_MATCH_ALL, this);
    }
}
