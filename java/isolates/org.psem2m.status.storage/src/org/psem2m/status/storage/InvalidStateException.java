/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
