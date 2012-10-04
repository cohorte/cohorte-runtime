/**
 * File:   IComposerLogic.java
 * Author: Thomas Calmant
 * Date:   30 août 2012
 */
package org.psem2m.composer.core.v2;

import org.psem2m.composer.EComponentState;

/**
 * Defines the composer logic service
 * 
 * @author Thomas Calmant
 */
public interface IComposerLogic {

    /**
     * Forces the composer logic to prepare a new resolution for waiting
     * components sets. The resolution may be delayed if the composer is
     * working.
     */
    void forceResolution();

    /**
     * Handles the change of a component state
     * 
     * @param aIsolateId
     *            ID of the isolate hosting the component
     * @param aComponentName
     *            Component name
     * @param aState
     *            New component state
     */
    void handleComponentEvent(String aIsolateId, String aComponentName,
            EComponentState aState);

    /**
     * Handles a factory state change
     * 
     * @param aIsolateId
     *            The source isolate ID
     * @param aFactoryName
     *            The name of the changed factory
     * @param aState
     *            The new state
     */
    void handleFactoriesState(String aIsolateId, String[] aFactories,
            EFactoryState aState);

    /**
     * Updates an instantiating components set status according to the given
     * Composer agent result
     * 
     * @param aIsolateId
     *            The source isolate ID
     * @param aComposetName
     *            Name of the components set
     * @param aInstantiatedComponents
     *            List of correctly instantiated components
     * @param aFailedComponents
     *            List of the components that failed to be instantiated
     */
    void handleInstantiationResult(String aIsolateId, String aComposetName,
            String[] aInstantiatedComponents, String[] aFailedComponents);

    /**
     * Handles the loss of an isolate
     * 
     * @param aIsolateId
     *            The isolate ID
     */
    void handleIsolateGone(String aIsolateId);

    /**
     * Handles the arrival of an isolate
     * 
     * @param aIsolateId
     *            The isolate ID
     */
    void handleIsolateReady(String aIsolateId);
}
