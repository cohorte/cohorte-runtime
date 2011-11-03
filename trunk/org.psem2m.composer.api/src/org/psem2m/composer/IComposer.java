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
     * Tries to instantiate the given set of components in the platform
     * 
     * @param aComponentsSet
     *            The set to instantiate
     */
    void instantiateComponentsSet(final ComponentsSetBean aComponentsSet);
}
