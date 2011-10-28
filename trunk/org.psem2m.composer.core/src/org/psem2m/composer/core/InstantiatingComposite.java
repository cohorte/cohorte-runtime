/**
 * File:   InstantiatingComposite.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.core;

import java.util.ArrayList;
import java.util.List;

import org.psem2m.composer.ComponentBean;
import org.psem2m.composer.CompositeBean;

/**
 * Represents an instantiating composite
 * 
 * @author Thomas Calmant
 */
public class InstantiatingComposite {

    /** The instantiating composite */
    private final CompositeBean pComposite;

    /** Components that still need to be started */
    private final List<String> pRemainingComponents = new ArrayList<String>();

    /**
     * Sets up members
     * 
     * @param aComposite
     *            A composite bean
     */
    public InstantiatingComposite(final CompositeBean aComposite) {

        pComposite = aComposite;

        // Store components names
        for (final ComponentBean component : pComposite.getComponents()) {

            pRemainingComponents.add(component.getName());
        }
    }

    /**
     * Updates the composite state, telling that the given component has been
     * started
     * 
     * @param aComponentBean
     *            A started component
     */
    public void componentStarted(final ComponentBean aComponentBean) {

        if (aComponentBean == null) {
            return;
        }

        if (!pComposite.getName().equals(aComponentBean.getCompositeName())) {
            // We're not the parent of the given composite
            return;
        }

        pRemainingComponents.remove(aComponentBean.getName());
    }

    /**
     * Updates the composite state, telling that the given component has been
     * started
     * 
     * @param aComponentName
     *            A started component name
     */
    public void componentStarted(final String aComponentName) {

        pRemainingComponents.remove(aComponentName);
    }

    /**
     * Updates the composite state, telling that the given component has been
     * stopped
     * 
     * @param aComponentBean
     *            A stopped component
     */
    public void componentStopped(final ComponentBean aComponentBean) {

        if (aComponentBean == null) {
            return;
        }

        if (!pComposite.getName().equals(aComponentBean.getCompositeName())) {
            // We're not the parent of the given composite
            return;
        }

        pRemainingComponents.add(aComponentBean.getName());
    }

    /**
     * Updates the composite state, telling that the given component has been
     * stopped
     * 
     * @param aComponentName
     *            A stopped component name
     */
    public void componentStopped(final String aComponentName) {

        if (aComponentName == null) {
            return;
        }

        pRemainingComponents.add(aComponentName);
    }

    /**
     * Retrieves the instantiating composite
     * 
     * @return the instantiating composite
     */
    public CompositeBean getBean() {

        return pComposite;
    }

    /**
     * Retrieves the composite name
     * 
     * @return the composite name
     */
    public String getName() {

        return pComposite.getName();
    }

    /**
     * Tests if all components of the composite are instantiated
     * 
     * @return True if the whole composite is instantiated
     */
    public boolean isComplete() {

        return pRemainingComponents.isEmpty();
    }
}
