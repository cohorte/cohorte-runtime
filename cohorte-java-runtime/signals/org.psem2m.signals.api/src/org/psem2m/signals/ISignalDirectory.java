/**
 * File:   ISignalDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.signals;

import java.util.Collection;
import java.util.Map;

/**
 * Defines a directory for the PSEM2M Signals services. Provides access strings
 * to each isolates or to a group of isolates.
 * 
 * @author Thomas Calmant
 */
public interface ISignalDirectory {

    /**
     * Constants to handle base groups, computed by the directory itself
     * 
     * @author Thomas Calmant
     */
    public enum EBaseGroup {
        /** All isolates, including the current one */
        ALL,

        /** Current isolate */
        CURRENT,

        /** All forkers, including the current isolate if it is a forker */
        FORKERS,

        /**
         * All isolates, including monitors and the current one, excluding
         * forkers. If the current isolate is a forker, it is excluded.
         */
        ISOLATES,

        /** All monitors, including the current isolate if it is a monitor */
        MONITORS,

        /** All isolates on the current node, excluding the current one */
        NEIGHBOURS,

        /** All isolates, with monitors and forkers, but this one */
        OTHERS,

        /**
         * All isolates, with monitors and forkers, but this one, even if they
         * are not validated
         */
        STORED,
    }

    /**
     * Represents the access to the local isolate
     */
    HostAccess LOCAL_ACCESS = new HostAccess(null, -1);

    /**
     * Returns a snapshot of the directory.
     * 
     * The result is a map with 4 entries :
     * <ul>
     * <li>'accesses': Isolate UID -&gt; {'node' -&gt; Node Name, 'port' -&gt;
     * Port}</li>
     * <li>'names': Isolate Name -&gt; [Isolates UIDs]</li>
     * <li>'nodes_host': Node Name -&gt; Host Address</li>
     * </ul>
     * 
     * @return A snapshot of the directory
     */
    Map<String, Object> dump();

    /**
     * Retrieves all known isolates which name begins with the given prefix. If
     * the prefix is null or empty, returns all known isolates.
     * 
     * Returns null if no isolate matched the prefix.
     * 
     * @param aPrefix
     *            An optional prefix filter on isolate names
     * @param aIncludeCurrent
     *            If true, include the current isolate in the result
     * @param aOnlyValidated
     *            If true, only return isolates that have been validated
     * @return All known isolates beginning with prefix, or null
     * 
     * @see #validateIsolatePresence(String)
     */
    String[] getAllIsolates(String aPrefix, boolean aIncludeCurrent,
            boolean aOnlyValidated);

    /**
     * Retrieves all known nodes UID. Returns null if no node is known.
     * 
     * @return All known nodes, or null
     */
    String[] getAllNodes();

    /**
     * Retrieves an Isolate Id -&gt; (host, port) map, containing all known
     * isolates that belong to given group.
     * 
     * @param aGroup
     *            A base group
     * @return An ID -&gt; (host, port) map, null if the group is unknown
     */
    Map<String, HostAccess> getGroupAccesses(EBaseGroup aGroup);

    /**
     * Retrieves the host name to access the given node
     * 
     * @param aNodeUID
     *            A node UID
     * @return A host name or address
     */
    String getHostForNode(String aNodeUID);

    /**
     * Retrieves the (host, port) tuple to access the given isolate, or null
     * 
     * @param aIsolateUID
     *            The ID of an isolate
     * @return A (host, port) object, or null if the isolate is unknown
     */
    HostAccess getIsolateAccess(String aIsolateUID);

    /**
     * Retrieves the name associated to the given UID
     * 
     * @param aIsolateUID
     *            An isolate UID
     * @return The isolate name
     */
    String getIsolateName(String aIsolateUID);

    /**
     * Retrieves the UID of the node of the given isolate
     * 
     * @param aIsolateUID
     *            An isolate UID
     * @return The UID of node hosting the given isolate, or null
     */
    String getIsolateNode(String aIsolateUID);

    /**
     * Retrieves the UIDs of the isolates on the given node
     * 
     * @param aNodeUID
     *            A node UID
     * @return The list of isolates on the given node, null if there is no
     *         isolate
     */
    String[] getIsolatesOnNode(String aNodeUID);

    /**
     * Retrieves the UIDs of the isolate having the given name
     * 
     * @param aIsolateName
     *            An isolate name
     * @return All UIDs associated to the given name
     */
    String[] getNameUIDs(String aIsolateName);

    /**
     * Returns the name associated to the given node UID
     * 
     * @param aNodeUID
     *            The UID of a node
     * @return The node name, or null
     */
    String getNodeName(String aNodeUID);

    /**
     * Returns the node UIDs associated to the given node name
     * 
     * @param aNodeName
     *            The name of a node
     * @return An array of UIDs, or null
     */
    String[] getNodeUIDs(String aNodeName);

    /**
     * Tests if the given isolate ID is registered in the directory
     * 
     * @param aIsolateUID
     *            An isolate ID
     * @return True if the ID is known, else false
     */
    boolean isRegistered(String aIsolateUID);

    /**
     * Tests if the given isolate ID is registered and has been validated
     * 
     * @param aIsolateUID
     *            An isolate ID
     * @return True if the ID is known and validated, else false
     */
    boolean isValidated(String aIsolateUID);

    /**
     * Notifies the isolate presence listeners of the validated presence of the
     * given isolate. This method must be called after
     * {@link #validateIsolatePresence(String)}.
     * 
     * @param aIsolateUID
     *            The validated isolate ID.
     * @return true on success, false if the isolate wasn't in the validated
     *         state.
     */
    boolean notifyIsolatePresence(String aIsolateUID);

    /**
     * Registers an isolate in the directory if it is not yet known
     * 
     * @param aIsolateUID
     *            The UID of the isolate to register
     * @param aIsolateName
     *            The name of the isolate
     * @param aNode
     *            The node hosting the isolate
     * @param aPort
     *            The port to access the isolate
     * @param aGroups
     *            All groups of the isolate
     * @return True if the isolate has been registered, False on error or if the
     *         isolate was already known for this access.
     * @throws IllegalArgumentException
     *             An argument is invalid
     */
    boolean registerIsolate(String aIsolateUID, String aIsolateName,
            String aNode, int aPort) throws IllegalArgumentException;

    /**
     * Registers an isolate in the directory if it is not yet known, without
     * doing the access validation process.
     * 
     * @param aIsolateUID
     *            The UID of the isolate
     * @param aIsolateName
     *            The name of the isolate
     * @param aNode
     *            The node hosting the isolate
     * @param aPort
     *            The port to access the isolate
     * @param aGroups
     *            All groups of the isolate
     * @return True if the isolate has been registered, False on error or if the
     *         isolate was already known for this access.
     * @throws IllegalArgumentException
     *             An argument is invalid
     */
    boolean registerValidated(String aIsolateUID, String aIsolateName,
            String aNode, int aPort) throws IllegalArgumentException;

    /**
     * Sets up the address to access the given node. Overrides the previous
     * address and returns it.
     * 
     * If the given address is null or empty, only returns the current node
     * address.
     * 
     * @param aNodeName
     *            The node name
     * @param aHostAddress
     *            The address to the node host
     * @return The previous address
     */
    String setNodeAddress(String aNodeName, String aHostAddress);

    /**
     * Associates the given node UID to a node name.
     * 
     * @param aNodeUID
     *            A node UID
     * @param aNodeName
     *            A node name
     */
    void setNodeName(String aNodeUID, String aNodeName);

    /**
     * Stores the content of the given directory dump
     * 
     * If aIgnoredNode is not null, the address corresponding to it in the
     * dumped directory won't be stored.
     * 
     * @param aDumpedDirectory
     *            A directory dump
     * @param aIgnoredNodes
     *            The name of the nodes to ignore
     * @param aIgnoredUIDs
     *            The isolate UIDs to ignore
     * @return The list of the newly registered isolates
     */
    String[] storeDump(Map<?, ?> aDumpedDirectory,
            Collection<String> aIgnoredNodes, Collection<String> aIgnoredUIDs);

    /**
     * Notifies the directory we are synchronizing with the given isolate. This
     * method must be called when sending or receiving a SYN-ACK signal.
     * 
     * @param aIsolateUID
     *            An isolate ID
     * @return True if the state change succeeded
     */
    boolean synchronizingIsolatePresence(String aIsolateUID);

    /**
     * Unregisters the given isolate of the directory
     * 
     * @param aIsolateUID
     *            The ID of the isolate to unregister
     * @return True if the isolate has been unregistered
     */
    boolean unregisterIsolate(String aIsolateUID);

    /**
     * Notifies the directory that an isolate has acknowledged the registration
     * of the current isolate. This method must be called when an ACK signal has
     * been received.
     * 
     * @param aIsolateUID
     *            An isolate ID
     * @return True if the state change succeeded
     */
    boolean validateIsolatePresence(String aIsolateUID);
}
