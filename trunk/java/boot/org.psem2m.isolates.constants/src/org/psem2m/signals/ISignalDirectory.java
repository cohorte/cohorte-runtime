/**
 * File:   ISignalDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.signals;

import java.util.Map;

/**
 * Defines a directory for the PSEM2M Signals services. Provides access strings
 * to each isolates or to a group of isolates.
 * 
 * @author Thomas Calmant
 */
public interface ISignalDirectory {

    /**
     * Retrieves all known isolates which ID begins with the given prefix. If
     * the prefix is null or empty, returns all known isolates.
     * 
     * Returns null if no isolate matched the prefix.
     * 
     * @param aPrefix
     *            An optional prefix filter
     * @return All known isolates beginning with prefix, or null
     */
    String[] getAllIsolates(String aPrefix);

    /**
     * Retrieves an Isolate Id -&gt; (host, port) map, containing all known
     * isolates that belong to given group.
     * 
     * @param aGroupName
     *            A group name
     * @return An ID -&gt; (host, port) map, null if the group is unknown
     */
    Map<String, HostAccess> getGroupAccesses(String aGroupName);

    /**
     * Retrieves the host name to access the node
     * 
     * @param aNodeName
     *            A node name
     * @return A host name or address
     */
    String getHostForNode(String aNodeName);

    /**
     * Retrieves the (host, port) tuple to access the given isolate, or null
     * 
     * @param aIsolateId
     *            The ID of an isolate
     * @return A (host, port) object, or null if the isolate is unknown
     */
    HostAccess getIsolateAccess(String aIsolateId);

    /**
     * Retrieves the current isolate ID
     * 
     * @return The current isolate ID
     */
    String getIsolateId();

    /**
     * Retrieves the node of the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The node hosting the given isolate, or null
     */
    String getIsolateNode(String aIsolateId);

    /**
     * Retrieves the IDs of the isolates on the given node
     * 
     * @param aNodeName
     *            A node name
     * @return The list of isolates on the given node, null if there is no
     *         isolate
     */
    String[] getIsolatesOnNode(String aNodeName);

    /**
     * Retrieves the name of the current node
     * 
     * @return the name of the current node
     */
    String getLocalNode();

    /**
     * Tests if the given isolate ID is registered in the directory
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return True if the ID is known, else false
     */
    boolean isRegistered(String aIsolateId);

    /**
     * Registers an isolate in the directory if it is not yet known
     * 
     * @param aIsolateId
     *            The ID of the isolate to register
     * @param aNode
     *            The node hosting the isolate
     * @param aPort
     *            The port to access the isolate
     * @param aGroups
     *            All groups of the isolate
     * @return True if the isolate has been register
     * @throws IllegalArgumentException
     *             An argument is invalid
     */
    boolean registerIsolate(String aIsolateId, String aNode, int aPort,
            String... aGroups) throws IllegalArgumentException;

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
     * Unregisters the given isolate of the directory
     * 
     * @param aIsolateId
     *            The ID of the isolate to unregister
     * @return True if the isolate has been unregistered
     */
    boolean unregisterIsolate(String aIsolateId);
}
