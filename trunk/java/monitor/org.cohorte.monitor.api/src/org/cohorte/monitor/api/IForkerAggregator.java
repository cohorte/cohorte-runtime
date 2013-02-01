/**
 * File:   IForkerDirectory.java
 * Author: Thomas Calmant
 * Date:   28 janv. 2013
 */
package org.cohorte.monitor.api;

import java.util.Map;

/**
 * Keeps a directory of forkers and sends orders
 * 
 * @author Thomas Calmant
 */
public interface IForkerAggregator {

    /**
     * Tests if the given isolate is alive
     * 
     * @param aUID
     *            An isolate UID
     * @return True if the isolate is known and alive
     */
    boolean isAlive(String aUID);

    /**
     * Sets the forkers in platform stopping mode : they must not start new
     * isolates.
     */
    void setPlatformStopping();

    /**
     * Selects a forker and calls it to start a new isolate
     * 
     * @param aUID
     *            Isolate UID
     * @param aNode
     *            Isolate node
     * @param aKind
     *            Isolate kind
     * @param aConfiguration
     *            A configuration dictionary
     * @return The forker result code
     */
    int startIsolate(String aUID, String aNode, String aKind,
            Map<String, Object> aConfiguration);

    /**
     * Stops the isolate with the given UID
     * 
     * @param aUID
     *            An isolate UID
     * @return True if the isolate has been stopped
     */
    boolean stopIsolate(String aUID);
}
