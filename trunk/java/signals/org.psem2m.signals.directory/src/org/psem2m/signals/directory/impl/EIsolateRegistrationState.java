/**
 * File:   EIsolateRegistrationState.java
 * Author: Thomas Calmant
 * Date:   15 oct. 2012
 */
package org.psem2m.signals.directory.impl;

import org.psem2m.status.storage.State;

/**
 * Definition of the states of the registration of an isolate
 * 
 * @author Thomas Calmant
 */
public enum EIsolateRegistrationState implements State {

    NOTIFIED {

        /**
         * Final state (can't be changed)
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            return false;
        }
    },

    /** Isolate stored in the registry */
    REGISTERED {

        /**
         * Allowed next state:
         * 
         * <ul>
         * <li>SYNCHRONIZING: we sent or received a SYN-ACK</li>
         * </ul>
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            return aNewState == SYNCHRONIZING || aNewState == VALIDATED;
        }
    },

    /** A SYN-ACK signal has been sent to this isolate */
    SYNCHRONIZING {

        /**
         * Allowed next state:
         * 
         * <ul>
         * <li>VALIDATED: we received the ACK signal</li>
         * <li>SYNCHRONIZING: the registering isolate is also synchronizing us</li>
         * </ul>
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            return aNewState == VALIDATED || aNewState == SYNCHRONIZING;
        }
    },

    /** The isolate has sent an ACK signal */
    VALIDATED {

        /**
         * Allowed next state:
         * 
         * <ul>
         * <li>NOTIFIED: listeners has been notified of the presence of this
         * isolate</li>
         * </ul>
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            return aNewState == NOTIFIED;
        }
    },
}
