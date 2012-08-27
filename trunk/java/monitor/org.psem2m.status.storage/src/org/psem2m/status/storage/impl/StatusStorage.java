/**
 * File:   StatusStorage.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.status.storage.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.psem2m.status.storage.IStatusStorage;
import org.psem2m.status.storage.InvalidIdException;
import org.psem2m.status.storage.InvalidStateException;
import org.psem2m.status.storage.State;

/**
 * Describes the current status of the monitor
 * 
 * @author Thomas Calmant
 */
public class StatusStorage<S extends State, T> implements IStatusStorage<S, T> {

    /** IDs -> State */
    private final Map<String, S> pIdStates = new HashMap<String, S>();

    /** States -> IDs */
    private final Map<S, Set<String>> pStates = new HashMap<S, Set<String>>();

    /** IDs -> Value */
    private final Map<String, T> pValues = new HashMap<String, T>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.status.storage.IStatusStorage#changeState(java.lang.String,
     * org.psem2m.status.storage.State)
     */
    @Override
    public synchronized void changeState(final String aId, final S aNewState)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorage#clear()
     */
    @Override
    public synchronized void clear() {

        pIdStates.clear();
        pValues.clear();
        pStates.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorage#get(java.lang.String)
     */
    @Override
    public synchronized T get(final String aId) throws InvalidIdException {

        if (!pValues.containsKey(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "Unknown ID: ''{0}''", aId));
        }

        return pValues.get(aId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.status.storage.IStatusStorage#getIdsInStates(org.psem2m.status
     * .storage.State[])
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorage#getState(java.lang.String)
     */
    @Override
    public synchronized S getState(final String aId) throws InvalidIdException {

        if (!pIdStates.containsKey(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "Unknown ID: ''{0}''", aId));
        }

        return pIdStates.get(aId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.status.storage.IStatusStorage#getValuesInStates(org.psem2m
     * .status.storage.State[])
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorage#remove(java.lang.String)
     */
    @Override
    public synchronized void remove(final String aId) throws InvalidIdException {

        // Be sure we know the ID
        if (!pValues.containsKey(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "Unknown ID: ''{0}''", aId));
        }

        // Remove the map entries
        pValues.remove(aId);
        pIdStates.remove(aId);

        // Remove from the state sets
        for (final Set<String> stateSet : pStates.values()) {
            if (stateSet != null) {
                stateSet.remove(aId);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorage#size()
     */
    @Override
    public int size() {

        return pValues.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorage#store(java.lang.String,
     * java.lang.Object, org.psem2m.status.storage.State)
     */
    @Override
    public synchronized boolean store(final String aId, final T aValue,
            final S aInitialState) throws InvalidIdException,
            InvalidStateException {

        // Check parameters
        if (aId == null || aId.isEmpty()) {
            throw new InvalidIdException("Null or empty IDs are forbidden.");
        }

        if (pValues.containsKey(aId)) {
            throw new InvalidIdException(MessageFormat.format(
                    "ID ''{0}'' is already in use.", aId));
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
    private boolean storeIdInState(final String aId, final S aState) {

        Set<String> stateSet = pStates.get(aState);
        if (stateSet == null) {
            stateSet = new HashSet<String>();
            pStates.put(aState, stateSet);
        }

        return stateSet.add(aId);
    }
}
