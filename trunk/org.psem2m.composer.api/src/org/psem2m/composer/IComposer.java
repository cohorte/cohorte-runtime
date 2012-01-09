/**
 * File:   IComposer.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer;

import java.util.List;

import org.psem2m.composer.model.ComponentsSetBean;

/**
 * Defines a composer core service
 * 
 * @author Thomas Calmant
 */
public interface IComposer {

    /**
     * Retrieves the snapshots of all currently known compositions
     * 
     * @return Current compositions snapshots
     */
    List<CompositionSnapshot> getCompositionSnapshot();

    /**
     * Tries to instantiate the given set of components in the platform
     * 
     * @param aComponentsSetBean
     *            The set to instantiate
     */
    void instantiateComponentsSet(final ComponentsSetBean aComponentsSetBean);

    /**
     * Add a composition listener
     * 
     * All the stored composition events stored by the composer after this time
     * stamp must be sent to the CompositionListener
     * 
     * @param aCompositionListener
     *            The registering listener
     * @param aTimeStamp
     *            A time stamp
     */
    void registerCompositionListener(
            final ICompositionListener aCompositionListener,
            final long aTimeStamp);

    /**
     * Tries to remove the given set of components in the platform
     * 
     * @param aComponentsSetBean
     *            A running component set
     * @throws Exception
     *             An error occurred (depends on implementation)
     */
    void removeComponentsSet(final ComponentsSetBean aComponentsSetBean)
            throws Exception;

    /**
     * Removes a composition listener
     * 
     * @param aCompositionListener
     *            The listener to remove
     */
    void unregisterCompositionListener(
            final ICompositionListener aCompositionListener);
}
