/**
 * File:   IStatusStorage.java
 * Author: "Thomas Calmant"
 * Date:   27 août 2012
 */
package org.psem2m.status.storage;

import java.util.Collection;

/**
 * @author "Thomas Calmant"
 * 
 * @param <S>
 * @param <T>
 */
public interface IStatusStorage<S extends State, T> {

    /**
     * Changes the state of the given ID
     * 
     * @param aId
     *            A value ID
     * @param aNewState
     *            The new state of the ID
     * @throws InvalidStateException
     *             The requested state change is forbidden
     * @throws InvalidIdException
     *             The given ID wasn't found
     */
    void changeState(final String aId, final S aNewState)
            throws InvalidStateException, InvalidIdException;

    /**
     * Clears the whole storage
     */
    void clear();

    /**
     * Retrieves the value associated to the given ID
     * 
     * @param aId
     *            An ID
     * @return The value associated to the ID
     * @throws InvalidIdException
     *             The given ID wasn't found
     */
    T get(final String aId) throws InvalidIdException;

    /**
     * Retrieves all the IDs currently in one of the given states
     * 
     * @param aStates
     *            An array of states
     * @return The set of all IDs in the given states
     */
    String[] getIdsInStates(final State... aStates);

    /**
     * Retrieves the state of the given ID
     * 
     * @param aId
     *            An ID
     * @throws InvalidIdException
     *             The given ID wasn't found
     */
    S getState(final String aId) throws InvalidIdException;

    /**
     * Retrieves the stored values
     * 
     * @return the stored values
     */
    Collection<T> getValues();

    /**
     * Retrieves all the values currently in one of the given states
     * 
     * @param aStates
     *            An array of states
     * @return The set of all values in the given states
     */
    Collection<T> getValuesInStates(final State... aStates);

    /**
     * Removes the given ID from the storage
     * 
     * @param aId
     *            An ID
     * @throws InvalidIdException
     *             The given ID wasn't found
     */
    void remove(final String aId) throws InvalidIdException;

    /**
     * Retrieves the size of the values map
     * 
     * @return the size of the values map
     */
    int size();

    /**
     * Stores the given ID in the given state
     * 
     * @param aId
     *            An ID
     * @param aValue
     *            The value associated to the ID
     * @param aInitialState
     *            The initial state
     * @return True on success
     * @throws InvalidIdException
     *             Invalid ID (empty or already known)
     * @throws InvalidStateException
     *             Invalid initial state (null...)
     */
    boolean store(final String aId, final T aValue, final S aInitialState)
            throws InvalidIdException, InvalidStateException;

}
