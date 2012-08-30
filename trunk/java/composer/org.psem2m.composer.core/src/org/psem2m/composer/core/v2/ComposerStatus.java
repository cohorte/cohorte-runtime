/**
 * File:   ComposerStatus.java
 * Author: Thomas Calmant
 * Date:   29 août 2012
 */
package org.psem2m.composer.core.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.composer.core.InstantiatingComposite;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.status.storage.IStatusStorage;
import org.psem2m.status.storage.IStatusStorageCreator;
import org.psem2m.status.storage.InvalidIdException;
import org.psem2m.status.storage.InvalidStateException;

/**
 * Describes the current status of the composer
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-status-factory", publicFactory = false)
@Provides(specifications = IComposerStatus.class)
@Instantiate(name = "psem2m-composer-status")
public class ComposerStatus implements IComposerStatus {

    /** Status of the components sets */
    private IStatusStorage<EComposetState, InstantiatingComposite> pComposetStatus;

    /** Factories -&gt; Isolates */
    private final Map<String, Set<String>> pFactoryIsolates = new HashMap<String, Set<String>>();

    /** Isolate -&gt; Factories */
    private final Map<String, Set<String>> pIsolateFactories = new HashMap<String, Set<String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The status storage creator */
    @Requires
    private IStatusStorageCreator pStatusCreator;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#addWaitingComposet(org.psem2m
     * .composer.core.InstantiatingComposite)
     */
    @Override
    public boolean addWaitingComposet(
            final InstantiatingComposite aComponentsSet) {

        try {
            return pComposetStatus.store(aComponentsSet.getName(),
                    aComponentsSet, EComposetState.WAITING);

        } catch (final InvalidIdException ex) {
            pLogger.logSevere(this, "addWaitingComposet",
                    "Invalid components set ID", ex);

        } catch (final InvalidStateException ex) {
            pLogger.logSevere(this, "addWaitingComposet",
                    "Invalid components set initial state", ex);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#composetComplete(java.lang
     * .String)
     */
    @Override
    public boolean composetComplete(final String aName) {

        return composetStateChange(aName, EComposetState.FULL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#composetInstantiating(java
     * .lang.String)
     */
    @Override
    public boolean composetInstantiating(final String aName) {

        return composetStateChange(aName, EComposetState.INSTANTIATING);
    }

    /**
     * Changes the components set state in the status storage
     * 
     * @param aName
     *            The name of the components set
     * @param aNewState
     *            The new state
     * @return True if the state has been modified
     */
    private boolean composetStateChange(final String aName,
            final EComposetState aNewState) {

        try {
            pComposetStatus.changeState(aName, aNewState);
            return true;

        } catch (final InvalidStateException ex) {
            pLogger.logWarn(this, "composetComplete",
                    "Invalid new components set state=", aNewState, ex);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "composetComplete",
                    "Invalid components set id=", aName, ex);
        }

        // An error occurred
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#composetWaiting(java.lang
     * .String)
     */
    @Override
    public boolean composetWaiting(final String aName) {

        return composetStateChange(aName, EComposetState.WAITING);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#getComposet(java.lang.String)
     */
    @Override
    public InstantiatingComposite getComposet(final String aComposetName) {

        try {
            return pComposetStatus.get(aComposetName);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getComposet",
                    "Unknown components set name=", aComposetName, ex);
        }

        // Not found
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.core.v2.IComposerStatus#getComposets()
     */
    @Override
    public InstantiatingComposite[] getComposets() {

        final Collection<InstantiatingComposite> composets = pComposetStatus
                .getValues();
        if (composets == null || composets.isEmpty()) {
            return null;
        }

        return composets.toArray(new InstantiatingComposite[composets.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#getComposetState(java.lang
     * .String)
     */
    @Override
    public synchronized EComposetState getComposetState(
            final String aComposetName) {

        try {
            return pComposetStatus.getState(aComposetName);

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "getComposetState",
                    "Invalid components set=", aComposetName, ex);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#getIsolateFactories(java.
     * lang.String)
     */
    @Override
    public synchronized Set<String> getIsolateFactories(final String aIsolateId) {

        // Return a copy of the set
        return new HashSet<String>(getSet(pIsolateFactories, aIsolateId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.core.v2.IComposerStatus#getIsolatesFactories()
     */
    @Override
    public synchronized Map<String, Set<String>> getIsolatesFactories() {

        // Make a copy of the map
        final Map<String, Set<String>> copy = new HashMap<String, Set<String>>(
                pIsolateFactories.size());
        for (final Entry<String, Set<String>> entry : pIsolateFactories
                .entrySet()) {
            copy.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }

        return copy;
    }

    /**
     * Retrieves the set stored at the given key in the given map. Creates it if
     * needed.
     * 
     * @param aMap
     *            A map containing sets
     * @param aKey
     *            A key in the map
     * @return The set associated to the given key
     */
    private <K, V> Set<V> getSet(final Map<K, Set<V>> aMap, final K aKey) {

        if (aMap == null) {
            // Nothing to do
            return null;
        }

        // Get the current set
        Set<V> set = aMap.get(aKey);

        if (set == null) {
            // Create a new one if needed
            set = new HashSet<V>();
            aMap.put(aKey, set);
        }

        return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.core.v2.IComposerStatus#getSnapshot()
     */
    @Override
    public synchronized ComponentsSetBean[] getSnapshot() {

        final List<ComponentsSetBean> snapshot = new ArrayList<ComponentsSetBean>(
                pComposetStatus.size());

        // Make a copy of all beans
        for (final InstantiatingComposite composetBean : pComposetStatus
                .getValues()) {

            snapshot.add(new ComponentsSetBean(composetBean.getBean(), null));
        }

        return snapshot.toArray(new ComponentsSetBean[snapshot.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.core.v2.IComposerStatus#getWaitingComposets()
     */
    @Override
    public InstantiatingComposite[] getWaitingComposets() {

        final Collection<InstantiatingComposite> composets = pComposetStatus
                .getValuesInStates(EComposetState.WAITING);

        if (composets == null || composets.isEmpty()) {
            // No composets in this state
            return null;
        }

        return composets.toArray(new InstantiatingComposite[composets.size()]);
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Clear the status
        pStatusCreator.deleteStorage(pComposetStatus);

        // Clear the maps
        pFactoryIsolates.clear();
        pIsolateFactories.clear();

        pLogger.logInfo(this, "invalidate", "Composer Status gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#registerFactories(java.lang
     * .String, java.lang.String[])
     */
    @Override
    public synchronized boolean registerFactories(final String aIsolateId,
            final String[] aFactories) {

        boolean result = false;

        // Register the isolate of the factory
        for (final String factory : aFactories) {
            final Set<String> factorySet = getSet(pFactoryIsolates, factory);
            result |= factorySet.add(aIsolateId);
        }

        // Register the factory in the isolate
        final Set<String> isolateSet = getSet(pIsolateFactories, aIsolateId);
        result |= isolateSet.addAll(Arrays.asList(aFactories));

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#removeComposet(org.psem2m
     * .composer.model.ComponentsSetBean)
     */
    @Override
    public boolean removeComposet(final String aComponentsSetName) {

        try {
            pComposetStatus.remove(aComponentsSetName);
            return true;

        } catch (final InvalidIdException ex) {
            pLogger.logWarn(this, "removeComposet", "Unknown components set=",
                    aComponentsSetName, ex);
        }

        // An error occurred
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#removeIsolate(java.lang.String
     * )
     */
    @Override
    public synchronized void removeIsolate(final String aIsolateId) {

        final Set<String> isolateFactories = pIsolateFactories.get(aIsolateId);
        if (isolateFactories != null) {
            // Remove factories
            for (final String factory : isolateFactories) {
                final Set<String> factoryIsolates = pFactoryIsolates
                        .get(factory);
                if (factoryIsolates != null) {
                    factoryIsolates.remove(aIsolateId);
                }
            }
        }

        // Remove isolate
        pIsolateFactories.remove(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerStatus#unregisterFactories(java.
     * lang.String, java.lang.String[])
     */
    @Override
    public synchronized boolean unregisterFactories(final String aIsolateId,
            final String[] aFactories) {

        boolean result = false;

        if (aFactories == null || aFactories.length == 0) {
            // Nothing to do
            return false;
        }

        // Unregister the isolate from the factory
        for (final String factory : aFactories) {
            final Set<String> factorySet = pFactoryIsolates.get(factory);
            if (factorySet != null) {
                result |= factorySet.remove(aIsolateId);
            }
        }

        // Unregister the factory from the isolate
        final Set<String> isolateSet = pIsolateFactories.get(aIsolateId);
        if (isolateSet != null) {
            result |= isolateSet.removeAll(Arrays.asList(aFactories));
        }

        return result;
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Create the status
        pComposetStatus = pStatusCreator.createStorage();

        pLogger.logInfo(this, "validate", "Composer Status ready");
    }
}
