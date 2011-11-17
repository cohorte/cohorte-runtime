/**
 * File:   ComponentsSetBean.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

import java.io.Serializable;
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
public class ComponentsSetBean extends AbstractModelBean implements
        Serializable {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** List of contained components */
    private final Map<String, ComponentBean> pComponentBeans = new HashMap<String, ComponentBean>();

    /** List of contained components sets */
    private final List<ComponentsSetBean> pComponentSets = new ArrayList<ComponentsSetBean>();

    /** The parent set */
    private ComponentsSetBean pParent;

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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#computeName()
     */
    @Override
    public void computeName() {

        if (pParent != null) {
            // Get the parent name
            final String parentName = pParent.getName();

            // Update the bean name, if needed
            if (parentName != null) {
                pName = parentName + "." + pName;
            }
        }

        synchronized (pComponentBeans) {

            // Copy components list
            final List<ComponentBean> components = new ArrayList<ComponentBean>(
                    pComponentBeans.values());

            // Reset the components map
            pComponentBeans.clear();

            // Update their name and re-populate the components map
            for (final ComponentBean bean : components) {
                bean.setParentName(pName);
                bean.computeName();

                // Now mapped with the new component name
                pComponentBeans.put(bean.getName(), bean);
            }
        }

        // Propagate modification to sub-sets
        for (final ComponentsSetBean bean : pComponentSets) {
            bean.setParentName(pName);
            bean.computeName();
        }
    }

    /**
     * Recursively looks for a component endings with the given name, for leaves
     * to the root component set.
     * 
     * @param aComponentName
     *            A component name
     * @return The first component found, or null
     */
    public ComponentBean findComponent(final String aComponentName) {

        return findComponent(aComponentName, null, true);
    }

    /**
     * Recursively looks for a component endings with the given name, for leaves
     * to the root component set
     * 
     * @param aComponentName
     *            A component name
     * @param aCallingChild
     *            Child that called this method, to avoid looking into it twice.
     * @param aTryParent
     *            Tells if the component must ask its parent to continue the
     *            research or not
     * 
     * @return The first component found, or null
     */
    protected ComponentBean findComponent(final String aComponentName,
            final ComponentsSetBean aCallingChild, final boolean aTryParent) {

        // Look into components
        for (final String compoName : pComponentBeans.keySet()) {

            if (compoName.endsWith(aComponentName)) {
                return pComponentBeans.get(compoName);
            }
        }

        // Look into sub-sets
        for (final ComponentsSetBean subset : pComponentSets) {

            if (subset.equals(aCallingChild)) {
                // Ignore calling child
                continue;
            }

            final ComponentBean component = subset.findComponent(
                    aComponentName, null, false);
            if (component != null) {
                return component;
            }
        }

        if (pParent == null || !aTryParent) {
            // No more way to try
            return null;
        }

        // Ask the parent to continue the research
        return pParent.findComponent(aComponentName, this, true);
    }

    /**
     * Recursively populates the list with defined components
     * 
     * @param aComponents
     *            A list that will be populated with components (can't be null)
     */
    protected void getAllComponents(final Collection<ComponentBean> aComponents) {

        aComponents.addAll(pComponentBeans.values());

        for (final ComponentsSetBean subset : pComponentSets) {
            subset.getAllComponents(aComponents);
        }
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
        return getComponent(aComponentName, true);
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
     * Retrieves the list of all components (current set and sub-sets)
     * 
     * @return the list of components
     */
    public ComponentBean[] getComponents() {

        final List<ComponentBean> allComponents = new ArrayList<ComponentBean>();

        // Recursively populate the result list
        getAllComponents(allComponents);

        return allComponents.toArray(new ComponentBean[allComponents.size()]);
    }

    /**
     * Retrieves the list of components sets
     * 
     * @return the list of components sets
     */
    public List<ComponentsSetBean> getComponentSets() {

        return pComponentSets;
        // return pComponentSets.toArray(new
        // ComponentsSetBean[pComponentSets.size()]);
    }

    /**
     * Tests if the set is empty
     * 
     * @return True if the set is empty
     */
    public boolean isEmpty() {

        // We have beans...
        return pComponentBeans.isEmpty() && pComponentSets.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.model.IModelBean#linkWires(org.psem2m.composer.model
     * .ComponentsSetBean)
     */
    @Override
    public boolean linkWires(final ComponentsSetBean aCallingParent) {

        boolean success = true;

        // Components
        for (final ComponentBean bean : pComponentBeans.values()) {
            success &= bean.linkWires(this);
        }

        // Sub-sets
        for (final ComponentsSetBean subset : pComponentSets) {
            success &= subset.linkWires(this);
        }

        return success;
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
                if (componentHost != null && !componentHost.isEmpty()
                        && !componentHost.equals(isolateId)) {
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
     * Sets the parent of this components set
     * 
     * @param aParent
     *            the parent of this set
     */
    public void setParent(final ComponentsSetBean aParent) {

        pParent = aParent;

        // Also update the parent name
        if (pParent != null) {
            pParentName = pParent.getName();

        } else {
            pParentName = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("ComponentsSet(");
        builder.append("Name=").append(pName);
        builder.append(", Parent=").append(pParent);
        builder.append(")");

        return builder.toString();
    }
}
