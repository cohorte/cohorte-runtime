/**
 * File:   InvalidComponentsSetException.java
 * Author: Thomas Calmant
 * Date:   29 août 2012
 */
package org.psem2m.composer;

/**
 * Error thrown when facing an invalid components set
 * 
 * @author Thomas Calmant
 */
public class InvalidComponentsSetException extends Exception {

    /** Serialization UID */
    private static final long serialVersionUID = 1L;

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            Message associated to the exception
     */
    public InvalidComponentsSetException(final String aMessage) {

        super(aMessage);
    }
}
