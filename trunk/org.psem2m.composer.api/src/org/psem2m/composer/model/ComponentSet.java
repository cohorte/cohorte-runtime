/**
 * File:   ComponentSet.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Represents a "composet", a set of components or composets.
 * 
 * @author Thomas Calmant
 */
public class ComponentSet {

    /** List of contained components */
    private final List<ComponentBean> pComponentBeans = new ArrayList<ComponentBean>();

    /** List of contained component sets */
    private final List<ComponentSet> pComponentSets = new ArrayList<ComponentSet>();

    /** The name */
    private String pName;

    /** Flag to indicate that the set contains only components */
    private boolean pOnlyComponents;

    /** The name of the parent component set */
    private String pParentName;

    /**
     * Default constructor
     */
    public ComponentSet() {

        // Does nothing
    }

    /**
     * Retrieves the list of components
     * 
     * @return the list of components
     */
    public List<ComponentBean> getComponentBeans() {

        return pComponentBeans;
    }

    /**
     * Retrieves the list of component sets
     * 
     * @return the list of component sets
     */
    public List<ComponentSet> getComponentSets() {

        return pComponentSets;
    }

    /**
     * Retrieves the component set name
     * 
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * Retrieves the name of the parent set
     * 
     * @return the name of the parent set
     */
    public String getParentName() {

        return pParentName;
    }

    /**
     * Tests if the set is empty
     * 
     * @return True if the set is empty
     */
    public boolean isEmpty() {

        if (pComponentBeans.isEmpty()) {
            // No components
            return pOnlyComponents || pComponentSets.isEmpty();
        }

        // No subsets
        return pComponentSets.isEmpty();
    }

    /**
     * Tests if the set contains only components
     * 
     * @return True if the set contains only components
     */
    public boolean isOnlyComponents() {

        return pOnlyComponents;
    }

    /**
     * Sets the components contained in this set
     * 
     * @param aComponents
     *            The components in this set
     */
    public void setComponentBeans(final Collection<ComponentBean> aComponents) {

        pComponentBeans.clear();

        if (aComponents != null) {
            pComponentBeans.addAll(aComponents);
        }
    }

    /**
     * Sets the components sets contained in this set
     * 
     * @param aComponents
     *            The components sets under this set
     */
    public void setComponentSets(final Collection<ComponentSet> aComponentSets) {

        pComponentSets.clear();

        if (aComponentSets != null) {
            pComponentSets.addAll(aComponentSets);
        }
    }

    /**
     * Sets the component set name
     * 
     * @param aName
     *            the name
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * Indicates that the set contains only components
     * 
     * @param aOnlyComponents
     *            the set contains only components
     */
    public void setOnlyComponents(final boolean aOnlyComponents) {

        pOnlyComponents = aOnlyComponents;
    }

    /**
     * Sets the name of the parent set
     * 
     * @param aParentName
     *            the name of the parent set
     */
    public void setParentName(final String aParentName) {

        pParentName = aParentName;
    }
}
