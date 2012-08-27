/**
 * File:   InvalidStateException.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.isolates.monitor.core.v2.state;

/**
 * Exception thrown by the StatusStorage when facing an invalid or unknown ID
 * 
 * @author Thomas Calmant
 */
public class InvalidIdException extends Exception {

    /** The serialization UID */
    private static final long serialVersionUID = 1L;

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            The exception message
     */
    public InvalidIdException(final String aMessage) {

        super(aMessage);
    }
}
