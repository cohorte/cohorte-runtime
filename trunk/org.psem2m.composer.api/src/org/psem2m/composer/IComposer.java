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
     * @return
     */
    List<CompositionSnapshot> getCompositionSnapshot();

    /**
     * Tries to instantiate the given set of components in the platform
     * 
     * @param aComponentsSet
     *            The set to instantiate
     */
    void instantiateComponentsSet(final ComponentsSetBean aComponentsSetBean);

    /**
     * Add a composition listener
     * 
     * All the stored composition events stored by the composer after this
     * timestamp must be sent to the CompositionListener
     * 
     * @param aCompositionListener
     * @param aTimeStamp
     */
    void registerCompositionListener(
            final ICompositionListener aCompositionListener,
            final long aTimeStamp);

    /**
     * Tries to remove the given set of components in the platform
     * 
     * @param aComponentsSet
     */
    void removeComponentsSet(final ComponentsSetBean aComponentsSetBean)
            throws Exception;

    /**
     * remove a composition listener
     * 
     * @param aCompositionListener
     * @param aComponentSnapshot
     */
    void unregisterCompositionListener(
            final ICompositionListener aCompositionListener);

}
