/**
 * 
 */
package org.psem2m.utilities.bootstrap.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import org.psem2m.utilities.bootstrap.IMessageSender;

/**
 * Flushes buffered data to the message sender
 * 
 * @author Thomas Calmant
 */
public class RedirectedOutputStream extends OutputStream {

    /** The internal buffer */
    private StringBuffer pBuffer;

    /** The output stream log level */
    private Level pLevel;

    /** The output stream logger name */
    private String pLoggerName;

    /** The message sender */
    private IMessageSender pSender;

    /**
     * Prepares the output stream
     */
    public RedirectedOutputStream(final IMessageSender aMessageSender,
            final Level aLevel, final String aName) {
        super();

        pBuffer = new StringBuffer();
        pLevel = aLevel;
        pLoggerName = aName;
        pSender = aMessageSender;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        pBuffer.delete(0, pBuffer.length());
        super.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {

        pSender.sendMessage(pLevel, "RedirectedOutputStream", pLoggerName,
                pBuffer);
        pBuffer.delete(0, pBuffer.length());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(final int aByte) throws IOException {

        // We receive a byte, not a code point
        pBuffer.appendCodePoint(aByte);
    }
}
