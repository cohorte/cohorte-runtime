/**
 * 
 */
package org.psem2m.utilities.bootstrap;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.isolates.base.isolates.boot.IsolateStatus;

/**
 * Represents a message sender
 * 
 * @author Thomas Calmant
 */
public interface IMessageSender {

    /**
     * Sends the given message on the standard output
     * 
     * @param aLogRecord
     *            Log to be written
     */
    void sendLog(LogRecord aLogRecord);

    /**
     * Sends the given message via a LogRecord object serialized on the standard
     * output
     * 
     * @param aLevel
     *            Message level
     * @param aSourceClass
     *            Source class name
     * @param aSourceMethod
     *            Source method name
     * @param aMessage
     *            Log message
     */
    void sendMessage(final Level aLevel, final CharSequence aSourceClass,
            final CharSequence aSourceMethod, final CharSequence aMessage);

    /**
     * Sends the given message via a LogRecord object serialized on the standard
     * output
     * 
     * @param aLevel
     *            Message level
     * @param aSourceClass
     *            Source class name
     * @param aSourceMethod
     *            Source method name
     * @param aMessage
     *            Log message
     * @param aThrowable
     *            An exception
     */
    void sendMessage(final Level aLevel, final CharSequence aSourceClass,
            final CharSequence aSourceMethod, final CharSequence aMessage,
            final Throwable aThrowable);

    /**
     * Sends an isolate status serialized on the standard output
     * 
     * @param aState
     *            Isolate state
     * @param aProgress
     *            Isolate start progress level
     * @return The sent isolate status
     */
    IsolateStatus sendStatus(int aState, double aProgress);

    /**
     * Sends an isolate status serialized on the standard output
     * 
     * @param aIsolateStatus
     *            The isolate status
     * @return The sent isolate status
     */
    IsolateStatus sendStatus(IsolateStatus aIsolateStatus);

    /**
     * Activates or deactivates the human readable output mode
     * 
     * @param aHumanMode
     *            Human mode flag
     */
    void setHumanMode(boolean aHumanMode);
}
