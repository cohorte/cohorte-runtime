/**
 * File:   IComposerStatus.java
 * Author: Thomas Calmant
 * Date:   29 août 2012
 */
package org.psem2m.composer.core.v2;

import java.util.Map;
import java.util.Set;

import org.psem2m.composer.core.InstantiatingComposite;
import org.psem2m.composer.model.ComponentsSetBean;

/**
 * Defines the service that stores the state of a composer
 * 
 * @author Thomas Calmant
 */
public interface IComposerStatus {

    /**
     * Adds the given components set in the waiting status
     * 
     * @param aComponentsSet
     *            The components set description
     * @return True if the components set wasn't already known
     */
    boolean addWaitingComposet(InstantiatingComposite aComponentsSet);

    /**
     * Changes the state of a components set to "complete"
     * 
     * Use {@link #addWaitingComposet(InstantiatingComposite)} to store a new
     * components set.
     * 
     * @param aName
     *            Name of the components set
     * @return True if the components state changed
     */
    boolean composetComplete(String aName);

    /**
     * Changes the state of a components set to "instantiating"
     * 
     * Use {@link #addWaitingComposet(InstantiatingComposite)} to store a new
     * components set.
     * 
     * @param aName
     *            Name of the components set
     * @return True if the components state changed
     */
    boolean composetInstantiating(String aName);

    /**
     * Changes the state of a components set to "waiting".
     * 
     * Use {@link #addWaitingComposet(InstantiatingComposite)} to store a new
     * components set.
     * 
     * @param aName
     *            Name of the components set
     * @return True if the components state changed
     */
    boolean composetWaiting(String aName);

    /**
     * Retrieves the components set with the given name
     * 
     * @param aComposetName
     *            The name of a components set
     * @return The found components set, or null
     */
    InstantiatingComposite getComposet(String aComposetName);

    /**
     * Retrieves all of the components sets
     * 
     * @return All components sets
     */
    InstantiatingComposite[] getComposets();

    /**
     * Retrieves the state of a components set state
     * 
     * @param aComposetName
     *            The name of a components set
     * @return The state of the components set, null if unknown
     */
    EComposetState getComposetState(String aComposetName);

    /**
     * Retrieves the factories registered on the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return All factories registered on the given isolate
     */
    Set<String> getIsolateFactories(String aIsolateId);

    /**
     * Retrieves the isolate ID -&gt; factories map
     * 
     * @return the isolate ID -&gt; factories map
     */
    Map<String, Set<String>> getIsolatesFactories();

    /**
     * Makes a snapshot of the current components sets status
     * 
     * @return a snapshot of the components sets
     */
    ComponentsSetBean[] getSnapshot();

    /**
     * Retrieves the components sets in the waiting state
     * 
     * @return The waiting components sets
     */
    InstantiatingComposite[] getWaitingComposets();

    /**
     * Registers factories in the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aFactoryId
     *            Component factories IDs
     * @return True if one of the factories wasn't already known
     */
    boolean registerFactories(String aIsolateId, String[] aFactories);

    /**
     * Removes the given components set from the storage
     * 
     * @param aComponentsSetName
     *            The name of the components set
     * @return True if the components set was known
     */
    boolean removeComposet(String aComponentsSetName);

    /**
     * Remove all entries corresponding to the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     */
    void removeIsolate(String aIsolateId);

    /**
     * Unregisters factories from the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aFactories
     *            Component factories IDs
     * @return True if one of the factories were known
     */
    boolean unregisterFactories(String aIsolateId, String[] aFactories);
}
