/**
 * File:   InvalidDataException.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.signals;

/**
 * Exception thrown when the POST request body received by
 * {@link ServletReceiver} can't be used to read a valid content
 * 
 * @author Thomas Calmant
 */
public class InvalidDataException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The associated error code */
    private int pErrorCode;

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            An error message
     * @param aErrorCode
     *            An HTTP error code to send back
     */
    public InvalidDataException(final String aMessage, final int aErrorCode) {

        super(aMessage);
        pErrorCode = aErrorCode;
    }

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            An error message
     * @param aErrorCode
     *            An HTTP error code to send back
     * @param aCause
     *            The cause of this exception
     */
    public InvalidDataException(final String aMessage, final int aErrorCode,
            final Throwable aCause) {

        super(aMessage, aCause);
        pErrorCode = aErrorCode;
    }

    /**
     * Retrieves the HTTP error code to sent back to the client
     * 
     * @return the HTTP error code
     */
    public int getErrorCode() {

        return pErrorCode;
    }
}
