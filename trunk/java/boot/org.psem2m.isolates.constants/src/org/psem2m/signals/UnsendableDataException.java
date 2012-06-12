/**
 * File:   UnsendableDataException.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.isolates.services.remote.signals;

import java.io.IOException;

/**
 * Exception sent when the given object can't be sent using Signals
 * 
 * @author Thomas Calmant
 */
public class UnsendableDataException extends IOException {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            A description of the error
     */
    public UnsendableDataException(final String aMessage) {

        super(aMessage);
    }

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            A description of the error
     * @param aCause
     *            The cause of this exception
     */
    public UnsendableDataException(final String aMessage, final Throwable aCause) {

        super(aMessage, aCause);
    }
}
