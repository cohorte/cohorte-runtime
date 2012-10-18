/**
 * File:   IIsolateStatusEventListener.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.isolates.base.isolates;

import org.psem2m.isolates.base.isolates.boot.IsolateStatus;

/**
 * Represents an isolate event listener, notified when an isolate status
 * information is received.
 * 
 * @author Thomas Calmant
 */
public interface IIsolateStatusEventListener {

	/**
	 * Notifies the listener that an isolate has been declared lost
	 * 
	 * @param aIsolateId
	 *            The lost isolate ID
	 */
	void handleIsolateLost(String aIsolateId);

	/**
	 * Notifies the listener that an IsolateStatus object as been received, or
	 * that an isolate has been lost (the isolate status is null)
	 * 
	 * @param aSenderId
	 *            The source isolate ID
	 * @param aIsolateStatus
	 *            The received object, null if the isolate has been lost
	 */
	void handleIsolateStatusEvent(String aSenderId, IsolateStatus aIsolateStatus);
}
