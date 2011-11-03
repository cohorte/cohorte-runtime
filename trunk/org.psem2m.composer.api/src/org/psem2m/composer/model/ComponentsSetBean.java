/**
 * File:   ComponentsSetBean.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a "composet", a set of components or composets.
 * 
 * @author Thomas Calmant
 */
public class ComponentsSetBean {

    /** List of contained components */
    private final Map<String, ComponentBean> pComponentBeans = new HashMap<String, ComponentBean>();

    /** List of contained components sets */
    private final List<ComponentsSetBean> pComponentSets = new ArrayList<ComponentsSetBean>();

    /** The name */
    private String pName;

    /** Flag to indicate that the set contains only components */
    private boolean pOnlyComponents;

    /** The name of the parent components set */
    private String pParentName;

    /**
     * Default constructor
     */
    public ComponentsSetBean() {

        // Does nothing
    }

    /**
     * Adds the component to the composite
     * 
     * @param aComponent
     *            A component
     */
    public void addComponent(final ComponentBean aComponent) {

        if (aComponent == null) {
            // Nothing to do
            return;
        }

        // Associate the component to the composite...
        aComponent.setParentName(pName);

        // Store it
        pComponentBeans.put(aComponent.getName(), aComponent);
    }

    /**
     * Retrieves the component of this set with the given name
     * 
     * @param aComponentName
     *            Name of the component to retrieve
     * @return The component with the given name, null if not found
     */
    public ComponentBean getComponent(final String aComponentName) {

        // None-recursive search
        return getComponent(aComponentName, false);
    }

    /**
     * Retrieves the component of the set with the given name
     * 
     * @param aComponentName
     *            Name of the component to retrieve
     * @param aRecursive
     *            If true, looks into sub-sets
     * @return The component with the given name, null if not found
     */
    public ComponentBean getComponent(final String aComponentName,
            final boolean aRecursive) {

        ComponentBean component = pComponentBeans.get(aComponentName);

        if (component == null && aRecursive) {
            // Component not found, look in sub-sets
            for (final ComponentsSetBean subset : pComponentSets) {

                component = subset.getComponent(aComponentName, aRecursive);
                if (component != null) {
                    // Found !
                    return component;
                }
            }
        }

        return component;
    }

    /**
     * Retrieves the list of components
     * 
     * @return the list of components
     */
    public ComponentBean[] getComponents() {

        final Collection<ComponentBean> components = pComponentBeans.values();
        return components.toArray(new ComponentBean[components.size()]);
    }

    /**
     * Retrieves the list of components sets
     * 
     * @return the list of components sets
     */
    public ComponentsSetBean[] getComponentSets() {

        return pComponentSets.toArray(new ComponentsSetBean[pComponentSets
                .size()]);
    }

    /**
     * Retrieves the components set name
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

        // We have beans...
        return false;
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
     * Tries to resolve a component repartition in isolates according to their
     * capabilities and to components properties
     * 
     * @param aComponentsSubSet
     *            A set of components to be resolved, subset of composite
     *            components
     * @param aIsolatesCapabilities
     *            Isolates capabilities
     * @param aResolution
     *            The resulting resolution
     * 
     * @return True if the whole composite has been resolved
     */
    public boolean resolve(final Collection<ComponentBean> aComponentsSubSet,
            final Map<String, List<String>> aIsolatesCapabilities,
            final Map<String, ComponentBean[]> aResolution) {

        if (aComponentsSubSet == null || aIsolatesCapabilities == null
                || aResolution == null) {
            // Can't resolve
            return false;
        }

        // Try with sub-sets
        for (final ComponentsSetBean subset : pComponentSets) {

            if (!subset.resolve(aComponentsSubSet, aIsolatesCapabilities,
                    aResolution)) {
                // A sub-set failed
                return false;
            }
        }

        // Make a copy of required components
        final List<ComponentBean> unresolvedComponents = new ArrayList<ComponentBean>(
                aComponentsSubSet);

        for (final Entry<String, List<String>> capabilities : aIsolatesCapabilities
                .entrySet()) {

            // Get the entry content
            final String isolateId = capabilities.getKey();
            final List<String> isolateTypes = capabilities.getValue();

            // Prepare the resolution content
            final List<ComponentBean> resolvedComponents = new ArrayList<ComponentBean>();

            // Test all awaiting components
            for (final ComponentBean component : unresolvedComponents) {

                final String componentHost = component.getIsolate();
                if (componentHost != null && !componentHost.equals(isolateId)) {
                    // No the host we're waiting for
                    continue;
                }

                if (isolateTypes.contains(component.getType())) {
                    // We've found a match
                    resolvedComponents.add(component);
                }
            }

            // Update the resolution map
            aResolution.put(isolateId, resolvedComponents
                    .toArray(new ComponentBean[resolvedComponents.size()]));

            // Clean the awaiting components list
            for (final ComponentBean component : resolvedComponents) {
                unresolvedComponents.remove(component);
            }

            // Test if the resolution is total
            if (unresolvedComponents.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to resolve a component repartition in isolates according to their
     * capabilities and to components properties
     * 
     * @param aIsolatesCapabilities
     *            Isolates capabilities
     * @param aResolution
     *            The resulting resolution
     * 
     * @return True if the whole composite has been resolved
     */
    public boolean resolve(
            final Map<String, List<String>> aIsolatesCapabilities,
            final Map<String, ComponentBean[]> aResolution) {

        // Clear the resolution map, to be safe
        aResolution.clear();

        return resolve(pComponentBeans.values(), aIsolatesCapabilities,
                aResolution);
    }

    /**
     * Sets the components contained in this set
     * 
     * @param aComponents
     *            The components in this set
     */
    public void setComponents(final Collection<ComponentBean> aComponents) {

        pComponentBeans.clear();

        if (aComponents != null) {
            for (final ComponentBean component : aComponents) {
                addComponent(component);
            }
        }
    }

    /**
     * Sets the components contained in this set
     * 
     * @param aComponents
     *            The components in this set
     */
    public void setComponents(final ComponentBean[] aComponents) {

        pComponentBeans.clear();

        if (aComponents != null) {
            for (final ComponentBean component : aComponents) {
                addComponent(component);
            }
        }
    }

    /**
     * Sets the components sets contained in this set
     * 
     * @param aComponents
     *            The components sets under this set
     */
    public void setComponentSets(
            final Collection<ComponentsSetBean> aComponentSets) {

        pComponentSets.clear();

        if (aComponentSets != null) {
            pComponentSets.addAll(aComponentSets);
        }
    }

    /**
     * Sets the components set name
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
