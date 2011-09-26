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
     * Notifies the listener that an IsolateStatus object as been received.
     * 
     * @param aIsolateStatus
     *            THe received object
     */
    void handleIsolateStatusEvent(IsolateStatus aIsolateStatus);
}
