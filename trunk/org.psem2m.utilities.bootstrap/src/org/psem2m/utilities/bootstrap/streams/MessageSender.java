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

import org.psem2m.isolates.base.boot.IsolateStatus;
import org.psem2m.utilities.bootstrap.IMessageSender;
import org.psem2m.utilities.bootstrap.Main;

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
    private final OutputStream pOutputStream;

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
     * Appends the throwable stack trace to given string builder
     * 
     * @param aBuilder
     *            A string builder
     * @param aThrowable
     *            A thrown error
     */
    protected void appendThrowable(final StringBuilder aBuilder,
	    final Throwable aThrowable) {

	if (aBuilder == null || aThrowable == null) {
	    return;
	}

	aBuilder.append(aThrowable);
	aBuilder.append(" :: ");
	aBuilder.append(aThrowable.getMessage());
	aBuilder.append("\n");
	aThrowable.printStackTrace();

	StackTraceElement[] stackTrace = aThrowable.getStackTrace();
	for (StackTraceElement element : stackTrace) {
	    aBuilder.append("\tat ");
	    aBuilder.append(element);
	    aBuilder.append("\n");
	}

	Throwable cause = aThrowable.getCause();
	if (cause != null) {
	    appendThrowable(aBuilder, cause);
	}
    }

    /**
     * Small log record formatter for human output
     * 
     * @param aLogRecord
     *            Log record to format
     * @return A human readable version of the log record
     */
    protected String formatRecord(final LogRecord aLogRecord) {

	StringBuilder builder = new StringBuilder();
	builder.append("[");
	builder.append(aLogRecord.getLoggerName());
	builder.append("][");
	builder.append(aLogRecord.getLevel());
	builder.append("] ");
	builder.append(aLogRecord.getSourceClassName());
	builder.append(".");
	builder.append(aLogRecord.getSourceMethodName());
	builder.append(" :: ");
	builder.append(aLogRecord.getMessage());

	appendThrowable(builder, aLogRecord.getThrown());

	builder.append("\n");
	return builder.toString();
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

	String message = aMessage.toString();
	if (message.trim().isEmpty()) {
	    // Ignore empty messages
	    return;
	}

	LogRecord record = new LogRecord(aLevel, message);
	record.setLoggerName("Bootstrap");
	record.setSourceClassName(aSourceClass.toString());
	record.setSourceMethodName(aSourceMethod.toString());
	record.setThrown(aThrowable);

	try {
	    if (pHumanMode) {
		pOutputStream.write(formatRecord(record).getBytes());

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
     * @see org.psem2m.utilities.bootstrap.IMessageSender#sendStatus(int,
     * double)
     */
    @Override
    public void sendStatus(final int aState, final double aProgress) {

	IsolateStatus status = new IsolateStatus(Main.getIsolateId(), aState,
		aProgress);

	try {
	    if (pHumanMode) {
		sendMessage(Level.INFO, LOGGER_NAME, "sendStatus",
			status.toString());

	    } else {
		((ObjectOutputStream) pOutputStream).writeObject(status);
	    }

	} catch (IOException ex) {
	    Logger.getLogger(LOGGER_NAME).log(Level.SEVERE,
		    "Error sending isolate status", ex);
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
