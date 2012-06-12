/**
 * File:   SignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.signals.directory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalDirectory;

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

    /** Isolate ID -&gt; Access */
    private final Map<String, HostAccess> pAccesses = new HashMap<String, HostAccess>();

    /** Group name -&gt; Isolate IDs */
    private final Map<String, List<String>> pGroups = new HashMap<String, List<String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Node name -&gt; Isolate IDs */
    private final Map<String, List<String>> pNodes = new HashMap<String, List<String>>();

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
            final HostAccess access = pAccesses.get(isolate);
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
     * @see
     * org.psem2m.signals.ISignalDirectory#getIsolateAccess(java.lang.String)
     */
    @Override
    public synchronized HostAccess getIsolateAccess(final String aIsolateId) {

        return pAccesses.get(aIsolateId);
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
     * @see org.psem2m.signals.ISignalDirectory#getIsolateNode()
     */
    @Override
    public String getIsolateNode() {

        return System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_NODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#getNodeIsolates(java.lang.String)
     */
    @Override
    public synchronized List<String> getNodeIsolates(final String aNodeName) {

        final List<String> isolates = pNodes.get(aNodeName);
        if (isolates != null) {
            // Return a copy of the list
            return new ArrayList<String>(isolates);
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

        pAccesses.clear();
        pGroups.clear();
        pNodes.clear();

        pLogger.logInfo(this, "invalidatePojo", "Signals directory gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#registerIsolate(java.lang.String,
     * java.lang.String, java.lang.String, int, java.lang.String[])
     */
    @Override
    public synchronized void registerIsolate(final String aIsolateId,
            final String aNode, final String aHostAddress, final int aPort,
            final String... aGroups) throws IllegalArgumentException {

        if (aIsolateId == null || aIsolateId.isEmpty()) {
            throw new IllegalArgumentException("Empty isolate ID : '"
                    + aIsolateId + "'");
        }

        if (aNode == null || aNode.isEmpty()) {
            throw new IllegalArgumentException("Empty node name for isolate '"
                    + aIsolateId + "'");
        }

        if (aHostAddress == null || aHostAddress.isEmpty()) {
            throw new IllegalArgumentException("Empty host for isolate '"
                    + aIsolateId + "'");
        }

        if (aIsolateId.equals(getIsolateId())) {
            // Ignore our own registration
            return;
        }

        // Store the access
        pAccesses.put(aIsolateId, new HostAccess(aHostAddress, aPort));

        // Store the node
        List<String> isolates = pNodes.get(aNode);
        if (isolates == null) {
            // Create the node entry
            isolates = new ArrayList<String>();
            pNodes.put(aNode, isolates);
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

        } else {
            pLogger.logWarn(this, "registerIsolate", "Isolate ID=", aIsolateId,
                    "has no group");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalDirectory#unregisterIsolate(java.lang.String)
     */
    @Override
    public synchronized boolean unregisterIsolate(final String aIsolateId) {

        if (aIsolateId == null || aIsolateId.isEmpty()) {
            // Nothing to do
            return false;
        }

        boolean result = false;

        if (pAccesses.remove(aIsolateId) != null) {
            // Isolate access removed
            result = true;
        }

        // Remove references in nodes
        for (final List<String> isolates : pNodes.values()) {
            if (isolates.contains(aIsolateId)) {
                isolates.remove(aIsolateId);
                result = true;
            }
        }

        // Remove references in groups
        for (final List<String> isolates : pGroups.values()) {
            if (isolates.contains(aIsolateId)) {
                isolates.remove(aIsolateId);
                result = true;
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Validate
    @Override
    public void validatePojo() throws BundleException {

        // Register the local isolate
        pAccesses.put(getIsolateId(), new HostAccess(null, 0));

        pLogger.logInfo(this, "validatePojo", "Signals directory ready");
    }
}
