/**
 * File:   InvalidStateException.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.status.storage;

/**
 * Exception thrown by the StatusStorage when facing an invalid state
 * 
 * @author Thomas Calmant
 */
public class InvalidStateException extends Exception {

    /** The serialization UID */
    private static final long serialVersionUID = 1L;

    /** The state that caused the exception */
    private final State pCauseState;

    /** The previous state, if any */
    private final State pPreviousState;

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            The exception message
     */
    public InvalidStateException(final String aMessage) {

        super(aMessage);
        pPreviousState = null;
        pCauseState = null;
    }

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            The exception message
     * @param aState
     *            The state that caused the exception
     */
    public InvalidStateException(final String aMessage, final State aState) {

        super(aMessage);
        pPreviousState = null;
        pCauseState = aState;
    }

    /**
     * Sets up the exception, if a state change is forbidden
     * 
     * @param aMessage
     *            The exception message
     * @param aPreviousState
     *            The previous state for the given ID
     * @param aNewState
     *            The new (refused) state for the given ID
     */
    public InvalidStateException(final String aMessage,
            final State aPreviousState, final State aNewState) {

        super(aMessage);
        pPreviousState = aPreviousState;
        pCauseState = aNewState;
    }

    /**
     * Retrieves the (requested) state that caused the exception
     * 
     * @return the state that caused the exception
     */
    public State getCauseState() {

        return pCauseState;
    }

    /**
     * Retrieves the previous (current) state, if any
     * 
     * @return the previous state
     */
    public State getPreviousState() {

        return pPreviousState;
    }
}
