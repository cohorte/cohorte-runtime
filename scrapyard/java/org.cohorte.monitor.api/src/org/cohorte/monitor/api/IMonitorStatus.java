/**
 * 
 */
package org.cohorte.monitor.api;

import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Defines a monitor status service
 * 
 * @author Thomas Calmant
 */
public interface IMonitorStatus {

    /**
     * Retrieves the description of the given isolate
     * 
     * @param aUID
     *            An isolate UID
     * @return The isolate description, or null
     */
    IsolateConf getIsolateDescription(String aUID);

    /**
     * Retrieves the UIDs of all isolates in any state that are running or will
     * run on the given node.
     * 
     * @param aNode
     *            A node name
     * @return All isolates running or to run on that node
     */
    String[] getIsolatesOnNode(String aNode);

    /**
     * Retrieves the descriptions of all isolates in waiting state to be started
     * on the given node.
     * 
     * Returns null if the node name is invalid. Returns an empty array if no
     * isolate is waiting for that node.
     * 
     * @param aNode
     *            A node name
     * @return All isolates to run on that node
     */
    IsolateConf[] getIsolatesWaitingForNode(String aNode);

    /**
     * Retrieves the IDs of the isolates in a running queue
     * 
     * @return the IDs of the isolates in a running queue
     */
    String[] getRunningIsolatesUIDs();

    /**
     * Retrieves the description of the waiting isolate with the given UID.
     * 
     * Returns null if the isolate is not in the waiting queue.
     * 
     * @param aUID
     *            An isolate UID
     * @return The isolate description, or null
     */
    IsolateConf getWaitingIsolate(String aUID);

    /**
     * Retrieves the IDs of the isolates in the waiting queue
     * 
     * @return the IDs of the isolates in the waiting queue
     */
    String[] getWaitingIsolatesUIDs();

    /**
     * Moves the given isolate UID from the requested queue to loading one
     * 
     * @param aUID
     *            A loading isolate UID
     * @return True if the state has been correctly changed
     */
    boolean isolateLoading(final String aUID);

    /**
     * Moves the given isolate UID from the loading queue to complete one
     * 
     * @param aUID
     *            A loading isolate UID
     * @return True if the state has been correctly changed
     */
    boolean isolateReady(final String aUID);

    /**
     * Sets the given isolate UID from the waiting queue to the requested one
     * 
     * @param aUID
     *            A requested isolate UID
     * @return True if the state has been correctly changed
     */
    boolean isolateRequested(final String aUID);

    /**
     * Sets the given isolate UID from a running queue to the waiting queue
     * 
     * @param aUID
     *            A stopped isolate UID
     * @return True if the state has been correctly changed
     */
    boolean isolateStopped(final String aUID);

    /**
     * Tests if the given isolate UID is in a running queue
     * 
     * @param aUID
     *            An isolate UID
     * @return True if the isolate is in a running queue
     */
    boolean isRunning(String aUID);

    /**
     * Tests if the given isolate UID is in the waiting queue
     * 
     * @param aUID
     *            An isolate UID
     * @return True if the isolate is in the waiting queue
     */
    boolean isWaiting(String aUID);

    /**
     * Stores an isolate in the waiting state
     * 
     * @param aUID
     *            The isolate UID
     * @param aConfiguration
     *            The isolate configuration
     * @return True if the isolate has been stored
     */
    boolean prepareIsolate(String aUID, IsolateConf aConfiguration);
}
