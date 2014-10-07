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

import java.util.Collection;

/**
 * Defines a status storage service.
 *
 * @author Thomas Calmant
 *
 * @param <S>
 *            Enumeration defining states
 * @param <T>
 *            Type of the values associated to each entry
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
     * Tests if the given ID is in the storage
     *
     * @param aId
     *            A value ID
     * @return True if the ID is known
     */
    boolean contains(String aId);

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
     * Retrieves the value associated to the given ID, or the default one if the
     * ID is unknown
     *
     * @param aId
     *            A value ID
     * @param aDefault
     *            The value to use if the ID is unknown
     * @return The value associated to the ID, or the default one
     */
    T getdefault(String aId, T aDefault);

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
