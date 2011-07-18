/**
 * 
 */
package org.psem2m.utilities.bootstrap;

import java.util.logging.Level;

/**
 * Represents a message sender
 * 
 * @author Thomas Calmant
 */
public interface IMessageSender {

    /**
     * Sends the given message via a LogRecord object serialized on the standard
     * input
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
     * input
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
}
