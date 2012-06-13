/**
 * File:   ISignalDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.signals;

import java.util.List;
import java.util.Map;


/**
 * Defines a directory for the PSEM2M Signals services. Provides access strings
 * to each isolates or to a group of isolates.
 * 
 * @author Thomas Calmant
 */
public interface ISignalDirectory {

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
     * Retrieves the name of the current node
     * 
     * @return the name of the current node
     */
    String getIsolateNode();

    /**
     * Retrieves the IDs of the isolates on the given node
     * 
     * @param aNodeName
     *            A node name
     * @return The list of isolates on the given node, null if there is no
     *         isolate
     */
    List<String> getNodeIsolates(String aNodeName);

    /**
     * Registers an isolate in the directory.
     * 
     * @param aIsolateId
     *            The ID of the isolate to register
     * @param aNode
     *            The node hosting the isolate
     * @param aHostAddress
     *            The host of the isolate
     * @param aPort
     *            The port to access the isolate
     * @param aGroups
     *            All groups of the isolate
     * @throws IllegalArgumentException
     *             An argument is invalid
     */
    void registerIsolate(String aIsolateId, String aNode, String aHostAddress,
            int aPort, String... aGroups) throws IllegalArgumentException;

    /**
     * Unregisters the given isolate of the directory
     * 
     * @param aIsolateId
     *            The ID of the isolate to unregister
     * @return True if the isolate has been unregistered
     */
    boolean unregisterIsolate(String aIsolateId);
}
