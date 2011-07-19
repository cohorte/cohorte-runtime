/**
 * 
 */
package org.psem2m.utilities.bootstrap.streams;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.psem2m.utilities.bootstrap.IMessageSender;

/**
 * Writes log records on the given object output stream.
 * 
 * @author Thomas Calmant
 */
public class MessageSender implements IMessageSender {

    /** Logger name */
    public static final String LOGGER_NAME = "Bootstrap.MessageSender";

    /** Message in human readable mode */
    private boolean pHumanMode;

    /** The object output stream */
    private OutputStream pOutputStream;

    /**
     * Prepares the message sender
     * 
     * @param aOutputStream
     *            The output stream
     * @param aHumanMode
     *            Set human readable output mode
     */
    public MessageSender(final OutputStream aOutputStream) {
	pOutputStream = aOutputStream;
    }

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
    @Override
    public void sendMessage(final Level aLevel,
	    final CharSequence aSourceClass, final CharSequence aSourceMethod,
	    final CharSequence aMessage) {

	sendMessage(aLevel, aSourceClass, aSourceMethod, aMessage, null);
    }

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
    @Override
    public void sendMessage(final Level aLevel,
	    final CharSequence aSourceClass, final CharSequence aSourceMethod,
	    final CharSequence aMessage, final Throwable aThrowable) {

	LogRecord record = new LogRecord(aLevel, aMessage.toString());
	record.setLoggerName("Bootstrap");
	record.setSourceClassName(aSourceClass.toString());
	record.setSourceMethodName(aSourceMethod.toString());
	record.setThrown(aThrowable);

	try {
	    if (pHumanMode) {
		pOutputStream.write((aMessage + "\n").getBytes());

	    } else {
		((ObjectOutputStream) pOutputStream).writeObject(record);
	    }

	} catch (IOException e) {
	    Logger.getLogger(LOGGER_NAME).log(record);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.bootstrap.IMessageSender#setHumanMode(boolean)
     */
    @Override
    public void setHumanMode(final boolean aHumanMode) {
	pHumanMode = aHumanMode;
    }
}
