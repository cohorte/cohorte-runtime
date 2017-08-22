/**
 * File:   InvalidAttributeValueException.java
 * Author: Thomas Calmant
 * Date:   9 janv. 2012
 */
package org.psem2m.sca.converter.core;

/**
 * Thrown while parsing a SCA file, when an XML attribute value is invalid
 * 
 * @author Thomas Calmant
 */
public class InvalidAttributeValueException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /**
     * Sets up the exception message
     * 
     * @param aMessage
     *            Message associated to the exception
     */
    public InvalidAttributeValueException(final String aMessage) {

        super(aMessage);
    }

    /**
     * Sets up the exception, with a cause
     * 
     * @param aMessage
     *            Message associated to the exception
     * @param aCause
     *            The error that caused this exception
     */
    public InvalidAttributeValueException(final String aMessage,
            final Throwable aCause) {

        super(aMessage, aCause);
    }
}
