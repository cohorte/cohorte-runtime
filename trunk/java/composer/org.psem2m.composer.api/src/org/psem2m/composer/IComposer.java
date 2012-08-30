/**
 * File:   IComposer.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer;

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
    ComponentsSetBean[] getCompositionSnapshot();

    /**
     * Tries to instantiate the given set of components in the platform
     * 
     * @param aComponentsSetBean
     *            The set to instantiate
     * @throws InvalidComponentsSetException
     *             Invalid components set
     */
    void instantiateComponentsSet(ComponentsSetBean aComponentsSetBean)
            throws InvalidComponentsSetException;

    /**
     * Tries to load the given composition file
     * 
     * @param aFileName
     *            A path to a composition file
     * @return The loaded component set bean, null on error
     */
    ComponentsSetBean loadCompositionFile(String aFileName);

    /**
     * Tries to remove the given set of components in the platform
     * 
     * @param aComponentsSetBean
     *            A running component set
     * @throws Exception
     *             An error occurred (depends on implementation)
     */
    void removeComponentsSet(ComponentsSetBean aComponentsSetBean)
            throws Exception;
}
