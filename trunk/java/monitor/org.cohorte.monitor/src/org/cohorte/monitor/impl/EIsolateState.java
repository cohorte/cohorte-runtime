/**
 * File:   EIsolateState.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.cohorte.monitor.impl;

import org.psem2m.status.storage.State;

/**
 * Defines the states of an isolate
 * 
 * @author Thomas Calmant
 */
public enum EIsolateState implements State {

    /** Isolate is loading */
    LOADING {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>WAITING: isolate has been killed</li>
         * <li>READY: isolate has been successfully loaded</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.status.storage.impl.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate lost (-> waiting) or loaded (-> full)
            return aNewState == WAITING || aNewState == READY;
        }
    },

    /** Isolate is completely loaded */
    READY {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>WAITING: isolate has been killed</li>
         * <li>LOADING: isolate configuration changed</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.status.storage.impl.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate lost (-> waiting) or updating (-> loading)
            return aNewState == WAITING || aNewState == LOADING;
        }
    },

    /** Isolate start request sent */
    REQUESTED {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>WAITING: isolate has been killed</li>
         * <li>LOADING: isolate is loading</li>
         * <li>READY: isolate is immediately loaded (no agent)</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.status.storage.impl.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate lost (-> waiting) or started (-> loading) or loaded (->
            // full)
            return aNewState == WAITING || aNewState == LOADING
                    || aNewState == READY;
        }
    },

    /** Isolate has not yet been requested */
    WAITING {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>REQUESTED: isolate has been requested</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.status.storage.impl.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate requested
            return aNewState == REQUESTED;
        }
    },
}
