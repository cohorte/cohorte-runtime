/**
 * File:   EComposetState.java
 * Author: Thomas Calmant
 * Date:   29 août 2012
 */
package org.psem2m.composer.core.v2;

import org.psem2m.status.storage.State;

/**
 * Defines the states of a components set
 * 
 * @author Thomas Calmant
 */
public enum EComposetState implements State {

    /** Fully instantiated components set */
    FULL {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>INSTANTIATING: a component disappeared</li>
         * <li>WAITING: the components set has been stopped</li>
         * </ul>
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Component lost (-> instantiating) or set stopped (-> waiting)
            return aNewState == INSTANTIATING || aNewState == WAITING;
        }
    },

    /** Components are instantiating */
    INSTANTIATING {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>FULL: all components have been instantiated</li>
         * <li>WAITING: the components set has been stopped</li>
         * </ul>
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Components loaded (-> full) or set stopped (-> waiting)
            return aNewState == FULL || aNewState == WAITING;
        }
    },

    /** Components set not yet requested */
    WAITING {

        /**
         * Allowed next states:
         * 
         * <ul>
         * <li>WAITING: components set can stay in the waiting state</li>
         * <li>INSTANTIATING: components set instantiation requested</li>
         * </ul>
         * 
         * @see org.psem2m.status.storage.State#canChangeTo(org.psem2m.status.storage.State)
         */
        @Override
        public boolean canChangeTo(final State aNewState) {

            // Set instantiating (-> instantiating) or staying (-> waiting)
            return aNewState == INSTANTIATING || aNewState == WAITING;
        }
    },
}
