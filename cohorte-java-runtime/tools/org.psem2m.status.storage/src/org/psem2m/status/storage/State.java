/**
 * File:   State.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.status.storage;

/**
 * Interface for the "extensible enum pattern"
 * 
 * @author Thomas Calmant
 */
public interface State {

    /**
     * Tests if an element in the current state can be changed to the given one
     * 
     * @param aNewState
     *            The new object state
     * @return True if the state change is allowed
     */
    boolean canChangeTo(State aNewState);
}
