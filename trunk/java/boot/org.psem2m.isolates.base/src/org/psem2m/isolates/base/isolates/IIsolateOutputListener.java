/**
 * File:   IIsolateOutputListener.java
 * Author: Thomas Calmant
 * Date:   14 sept. 2011
 */
package org.psem2m.isolates.base.isolates;

import java.util.logging.LogRecord;

import org.psem2m.isolates.base.isolates.boot.IsolateStatus;

/**
 * Interface to be implemented by classes reading isolate process standard
 * output
 * 
 * @author Thomas Calmant
 */
public interface IIsolateOutputListener {

    /**
     * Handles the reception of a log record object. The source isolate ID can
     * be different of the real object sender in case of message routing.
     * 
     * This method should be synchronized.
     * 
     * @param aSourceIsolateId
     *            The isolate ID from which the record has been received
     * @param aLogRecord
     *            The log record
     */
    void handleIsolateLogRecord(String aSourceIsolateId, LogRecord aLogRecord);

    /**
     * Handles the reception of an isolate status object. The source isolate ID
     * can be different of the one stored in the object in case of message
     * routing.
     * 
     * The isolate status is null if the contact with the isolate has been lost.
     * 
     * This method should be synchronized.
     * 
     * @param aSourceIsolateId
     *            The isolate ID from which the status has been received
     * @param aIsolateStatus
     *            The isolate status object, null if the contact with the
     *            isolate has been lost.
     */
    void handleIsolateStatus(String aSourceIsolateId,
            IsolateStatus aIsolateStatus);
}
