/**
 * File:   IComposer.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer;

/**
 * Defines a composer core service
 * 
 * @author Thomas Calmant
 */
public interface IComposer {

    /**
     * Tries to instantiate the given composite in the platform
     * 
     * @param aComposite
     *            The composite to instantiate
     */
    void instantiateComposite(final CompositeBean aComposite);
}
