/**
 * File:   EIsolateState.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.isolates.monitor.core.v2;

import org.psem2m.isolates.monitor.core.v2.state.State;

/**
 * Defines the states of an isolate
 * 
 * @author Thomas Calmant
 */
public enum EIsolateState implements State {

    /** Isolate is completely loaded */
    FULL {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>WAITING: isolate has been killed</li>
         * <li>LOADING: isolate configuration changed</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.isolates.monitor.core.v2.state.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate lost (-> waiting) or updating (-> loading)
            return aNewState == WAITING || aNewState == LOADING;
        }
    },

    /** Isolate is loading */
    LOADING {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>WAITING: isolate has been killed</li>
         * <li>FULL: isolate has been successfully loaded</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.isolates.monitor.core.v2.state.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate lost (-> waiting) or loaded (-> full)
            return aNewState == WAITING || aNewState == FULL;
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
         * <li>FULL: isolate is immediately loaded (no agent)</li>
         * </ul>
         * 
         * @see org.psem2m.isolates.monitor.core.v2.EIsolateState#canChangeTo
         *      (org.psem2m.isolates.monitor.core.v2.state.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate lost (-> waiting) or started (-> loading) or loaded (->
            // full)
            return aNewState == WAITING || aNewState == LOADING
                    || aNewState == FULL;
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
         *      (org.psem2m.isolates.monitor.core.v2.state.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Isolate requested
            return aNewState == REQUESTED;
        }
    },
}
