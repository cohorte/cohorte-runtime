/**
 * 
 */
package org.psem2m.isolates.monitor;

import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * Defines a monitor status service
 * 
 * @author Thomas Calmant
 */
public interface IMonitorStatus {

	/**
	 * Retrieves the description of the given isolate
	 * 
	 * @param aIsolateId
	 *            An isolate ID
	 * @return The isolate description, or null
	 */
	IsolateDescription getIsolateDescription(String aIsolateId);

	/**
	 * Retrieves the IDs of the isolates in a running queue
	 * 
	 * @return the IDs of the isolates in a running queue
	 */
	String[] getRunningIsolatesIDs();

	/**
	 * Retrieves the description of the waiting isolate with the given ID.
	 * 
	 * Returns null if the isolate is not in the waiting queue.
	 * 
	 * @param aIsolateId
	 *            An isolate ID
	 * @return The isolate description, or null
	 */
	IsolateDescription getWaitingIsolateDescription(String aIsolateId);

	/**
	 * Retrieves the IDs of the isolates in the waiting queue
	 * 
	 * @return the IDs of the isolates in the waiting queue
	 */
	String[] getWaitingIsolatesIDs();

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
	IsolateDescription[] getIsolatesWaitingForNode(String aNode);

	/**
	 * Moves the given isolate ID from the loading queue to complete one
	 * 
	 * @param aIsolateId
	 *            A loading isolate ID
	 * @return True if the state has been correctly changed
	 */
	boolean isolateComplete(final String aIsolateId);

	/**
	 * Moves the given isolate ID from the requested queue to loading one
	 * 
	 * @param aIsolateId
	 *            A loading isolate ID
	 * @return True if the state has been correctly changed
	 */
	boolean isolateLoading(final String aIsolateId);

	/**
	 * Sets the given isolate ID from the waiting queue to the requested one
	 * 
	 * @param aIsolateId
	 *            A requested isolate ID
	 * @return True if the state has been correctly changed
	 */
	boolean isolateRequested(final String aIsolateId);

	/**
	 * Sets the given isolate ID from a running queue to the waiting queue
	 * 
	 * @param aIsolateId
	 *            A stopped isolate ID
	 * @return True if the state has been correctly changed
	 */
	boolean isolateStopped(final String aIsolateId);

	/**
	 * Tests if the given isolate ID is in a running queue
	 * 
	 * @param aIsolateId
	 *            An isolate ID
	 * @return True if the isolate is in a running queue
	 */
	boolean isRunning(String aIsolateId);

	/**
	 * Tests if the given isolate ID is in the waiting queue
	 * 
	 * @param aIsolateId
	 *            An isolate ID
	 * @return True if the isolate is in the waiting queue
	 */
	boolean isWaiting(String aIsolateId);
}