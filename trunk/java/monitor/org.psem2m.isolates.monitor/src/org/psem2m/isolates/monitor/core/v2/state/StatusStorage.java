/**
 * File:   StatusStorage.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.isolates.monitor.core.v2.state;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Describes the current status of the monitor
 * 
 * @author Thomas Calmant
 */
public class StatusStorage<T> {

    /** IDs -> State */
    private final Map<String, State> pIdStates = new HashMap<String, State>();

    /** States -> IDs */
    private final Map<State, Set<String>> pStates = new HashMap<State, Set<String>>();

    /** IDs -> Value */
    private final Map<String, T> pValues = new HashMap<String, T>();

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
    public synchronized void changeState(final String aId, final State aNewState)
            throws InvalidStateException, InvalidIdException {

        // Validate the new state
        if (aNewState == null) {
            throw new InvalidStateException("Null state given");
        }

        // Get the previous state
        final State oldState = pIdStates.get(aId);
        if (oldState == null) {
            // Unknown ID
            throw new InvalidIdException(MessageFormat.format(
                    "Unknown ID: ''{0}''", aId));
        }

        // Test if new state is allowed
        if (!oldState.canChangeTo(aNewState)) {
            // Invalid state change
            throw new InvalidStateException(
                    MessageFormat.format(
                            "Can''t change state of ID ''{0}'' from ''{1}'' to ''{2}''",
                            aId, oldState, aNewState), oldState, aNewState);
        }

        // Get the previous set
        final Set<String> previousSet = pStates.get(oldState);
        if (previousSet == null || !previousSet.contains(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "Previous state ''{0}'' doesn''t contain ID ''{1}''",
                    oldState, aId));
        }

        // Create the new set if needed
        Set<String> newSet = pStates.get(aNewState);
        if (newSet == null) {
            newSet = new HashSet<String>();
            pStates.put(aNewState, newSet);
        }

        // Change map
        pIdStates.put(aId, aNewState);

        // Change queues
        previousSet.remove(aId);
        newSet.add(aId);

        // TODO: store change in history
    }

    /**
     * Clears all collections
     */
    public synchronized void clear() {

        pIdStates.clear();
        pValues.clear();
        pStates.clear();
    }

    /**
     * Retrieves the value associated to the given ID
     * 
     * @param aId
     *            An ID
     * @return The value associated to the ID
     * @throws InvalidIdException
     *             The given ID wasn't found
     */
    public T get(final String aId) throws InvalidIdException {

        if (!pValues.containsKey(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "Unknown ID: ''{0}''", aId));
        }

        return pValues.get(aId);
    }

    /**
     * Retrieves all the IDs currently in one of the given states
     * 
     * @param aStates
     *            An array of states
     * @return The set of all IDs in the given states
     */
    public synchronized String[] getIdsInStates(final State... aStates) {

        // Do nothing if the aStates is null
        if (aStates == null) {
            return null;
        }

        // Find the corresponding values
        final Set<String> keys = new HashSet<String>();
        for (final State state : aStates) {

            final Set<String> stateIds = pStates.get(state);
            if (stateIds != null) {
                keys.addAll(stateIds);
            }
        }

        // Return the values
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Retrieves the state of the given ID
     * 
     * @param aId
     *            An ID
     * @throws InvalidIdException
     *             The given ID wasn't found
     */
    public synchronized State getState(final String aId)
            throws InvalidIdException {

        if (!pIdStates.containsKey(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "Unknown ID: ''{0}''", aId));
        }

        return pIdStates.get(aId);
    }

    /**
     * Retrieves all the values currently in one of the given states
     * 
     * @param aStates
     *            An array of states
     * @return The set of all values in the given states
     */
    public synchronized Collection<T> getValuesInStates(final State... aStates) {

        // Do nothing if the aStates is null
        if (aStates == null) {
            return null;
        }

        // Get the IDs in the given states
        final String[] keys = getIdsInStates(aStates);

        // Get the values
        final Set<T> values = new HashSet<T>();
        for (final String key : keys) {

            final T value = pValues.get(key);
            if (value != null) {
                // Be sure to have valid values...
                values.add(value);
            }
        }

        // Return the values
        return values;
    }

    /**
     * Retrieves the size of the values map
     * 
     * @return the size of the values map
     */
    public int size() {

        return pValues.size();
    }

    public synchronized boolean store(final String aId, final T aValue,
            final State aInitialState) throws InvalidIdException,
            InvalidStateException {

        // Check parameters
        if (aId == null || aId.isEmpty()) {
            throw new InvalidIdException("Null or empty IDs are forbidden.");
        }

        if (aInitialState == null) {
            throw new InvalidStateException("Null initial state is forbidden");
        }

        // Store the value
        pValues.put(aId, aValue);

        // Associate the initial state to the ID
        pIdStates.put(aId, aInitialState);

        // Associate the ID to the initial state
        return storeIdInState(aId, aInitialState);
    }

    /**
     * Stores the given ID in the given state set. Creates the set if needed
     * 
     * @param aId
     *            An ID
     * @param aState
     *            The state to store the ID
     * @return The result of {@link Set#add(Object)}
     */
    private boolean storeIdInState(final String aId, final State aState) {

        Set<String> stateSet = pStates.get(aState);
        if (stateSet == null) {
            stateSet = new HashSet<String>();
            pStates.put(aState, stateSet);
        }

        return stateSet.add(aId);
    }
}
