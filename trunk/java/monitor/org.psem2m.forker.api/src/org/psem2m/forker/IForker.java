/**
 * File:   IForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.forker;

import java.util.Map;

/**
 * Description of the Forker service
 * 
 * @author Thomas Calmant
 */
public interface IForker {

    /**
     * Retrieves the name of the host machine of this forker
     * 
     * @return The name of the forker host
     */
    String getNodeName();

    /**
     * Tests if the given forker is on the given node.
     * 
     * @param aForkerId
     *            A forker ID
     * @param aNodeName
     *            The node name
     * @return True if the given forker is on the given node
     */
    boolean isOnNode(String aForkerId, String aNodeName);

    /**
     * Tests the given isolate state
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return The isolate process state
     */
    int ping(String aIsolateId);

    /**
     * Sets the forkers in platform stopping mode : they must not start new
     * isolates.
     */
    void setPlatformStopping();

    /**
     * Starts a process according to the given configuration
     * 
     * @param aIsolateConfiguration
     *            The configuration of the isolate to start
     * 
     * @return The starter error code
     */
    int startIsolate(Map<String, Object> aIsolateConfiguration);

    /**
     * Kills the process with the given isolate ID
     * 
     * @param aIsolateId
     *            The ID of the isolate to kill
     */
    void stopIsolate(String aIsolateId);
}
