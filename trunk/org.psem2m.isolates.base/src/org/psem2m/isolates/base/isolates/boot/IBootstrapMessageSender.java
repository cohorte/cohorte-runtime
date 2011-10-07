/**
 * File:   IBootstrapMessageSender.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.isolates.boot;

import java.util.logging.LogRecord;

/**
 * Defines the bootstrap message sender interface. Should be used by the isolate
 * agent only.
 * 
 * @author Thomas Calmant
 */
public interface IBootstrapMessageSender {

    /**
     * Sends a log record through the bootstrap output
     * 
     * @param aLogRecord
     *            The record to send
     */
    void sendLog(LogRecord aLogRecord);

    /**
     * Sends an isolate status serialized on the standard output
     * 
     * @param aState
     *            Isolate state
     * @param aProgress
     *            Isolate start progress level
     * 
     * @return The sent isolate status
     */
    IsolateStatus sendStatus(int aState, double aProgress);

    /**
     * Sends an isolate status through the bootstrap output
     * 
     * @param aIsolateStatus
     *            The isolate status to send
     * 
     * @return The sent isolate status
     */
    IsolateStatus sendStatus(IsolateStatus aIsolateStatus);
}
