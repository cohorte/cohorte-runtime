/**
 * File:   ComponentsSetBean.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.psem2m.composer.EComponentState;

/**
 * Represents a "composet", a set of components or composets.
 * 
 * @author Thomas Calmant
 */
public class ComponentsSetBean extends AbstractModelBean implements
        Serializable, Comparable<ComponentsSetBean> {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** Mapping of contained components (name -&gt; component bean) */
    private final Map<String, ComponentBean> pComponentBeans = new HashMap<String, ComponentBean>();

    /** List of contained components sets */
    private final List<ComponentsSetBean> pComponentSets = new ArrayList<ComponentsSetBean>();

    /** The parent set */
    private ComponentsSetBean pParent = null;

    /**
     * Default constructor
     */
    public ComponentsSetBean() {

        // Does nothing
        super();
    }

    /**
     * Copy constructor
     * 
     * @param aComponentsSetBean
     *            Bean to copy
     */
    public ComponentsSetBean(final ComponentsSetBean aComponentsSetBean,
            final ComponentsSetBean aParent) {

        super(aComponentsSetBean);

        // Make a copy of all components
        for (final Entry<String, ComponentBean> entry : aComponentsSetBean.pComponentBeans
                .entrySet()) {

            final ComponentBean snapshot = new ComponentBean(entry.getValue());
            pComponentBeans.put(entry.getKey(), snapshot);
        }

        // Make a copy of all components set
        for (final ComponentsSetBean bean : aComponentsSetBean.pComponentSets) {

            // Make a snapshot
            pComponentSets.add(new ComponentsSetBean(bean, this));
        }

        // Set the parent
        pParent = aParent;
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
     * Components set comparison.
     * 
     * Components set are greater than null. Components sets are compared by
     * name.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ComponentsSetBean aOther) {

        if (aOther == null) {
            // We're greater than null
            return 1;
        }

        if (equals(aOther)) {
            // Same object
            return 0;
        }

        // Different object, use name ordering
        return safeCompareTo(pName, aOther.pName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#computeName()
     */
    @Override
    public void computeName() {

        if (!isRoot()) {
            // Get the parent name
            final String parentName = pParent.getName();

            // Update the bean name, if needed
            if (parentName != null) {
                pName = parentName + "." + pName;
            }

        } else {
            // We are the root element
            pRootName = pName;
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
                bean.setRootName(pRootName);
                bean.computeName();

                // Now mapped with the new component name
                pComponentBeans.put(bean.getName(), bean);
            }
        }

        // Propagate modification to sub-sets
        for (final ComponentsSetBean bean : pComponentSets) {
            bean.setParentName(pName);
            bean.setRootName(pRootName);
            bean.computeName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.AbstractModelBean#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof ComponentsSetBean) {

            if (!super.equals(aObj)) {
                // Name equality failed
                return false;
            }

            final ComponentsSetBean other = (ComponentsSetBean) aObj;

            // Components
            final Collection<ComponentBean> components = pComponentBeans
                    .values();
            final Collection<ComponentBean> otherComponents = other.pComponentBeans
                    .values();
            if (!(components.containsAll(otherComponents) && otherComponents
                    .containsAll(components))) {
                // Different components
                return false;
            }

            // Components sets
            if (!(pComponentSets.containsAll(other.pComponentSets))
                    && other.pComponentSets.containsAll(pComponentSets)) {
                // Different sub components sets
                return false;
            }

            // All tests passed
            return true;
        }

        return false;
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
     * Retrieves the list of all components (current set and sub-sets)
     * 
     * @return the list of components
     */
    public ComponentBean[] getAllComponents() {

        final List<ComponentBean> allComponents = new ArrayList<ComponentBean>();

        // Recursively populate the result list
        getAllComponents(allComponents);

        return allComponents.toArray(new ComponentBean[allComponents.size()]);
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
     * Retrieves the list of the children components (current set only).
     * 
     * The result array is sorted according to {@link ComponentBean#compareTo()}
     * 
     * @return the list of components
     */
    public ComponentBean[] getComponents() {

        final ComponentBean[] resultArray = pComponentBeans.values().toArray(
                new ComponentBean[0]);

        // Sort the array
        Arrays.sort(resultArray, null);

        return resultArray;
    }

    /**
     * Retrieves the list of components sets
     * 
     * @return the list of components sets
     */
    public ComponentsSetBean[] getComponentSets() {

        final ComponentsSetBean[] resultArray = pComponentSets
                .toArray(new ComponentsSetBean[pComponentSets.size()]);

        // Sort the array
        Arrays.sort(resultArray, null);

        return resultArray;
    }

    /**
     * Retrieves the parent of this component set
     * 
     * @return the parent of this component set
     */
    public ComponentsSetBean getParent() {

        return pParent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#getState()
     */
    @Override
    public EComponentState getState() {

        int stateOrdinal = getStateOrdinal(EComponentState.COMPLETE);

        // Get the minimal state of the components
        for (final ComponentBean bean : pComponentBeans.values()) {

            final int beanStateOrdinal = getStateOrdinal(bean.getState());
            if (beanStateOrdinal >= 0 && beanStateOrdinal < stateOrdinal) {
                // We found a valid state lower than the current one
                stateOrdinal = beanStateOrdinal;
            }

            if (stateOrdinal == 0) {
                // We're at the minimal value
                return getStateEnum(stateOrdinal);
            }
        }

        // Get the minimal state of the components sets
        for (final ComponentsSetBean bean : pComponentSets) {

            final int beanStateOrdinal = getStateOrdinal(bean.getState());
            if (beanStateOrdinal >= 0 && beanStateOrdinal < stateOrdinal) {
                // We found a valid state lower than the current one
                stateOrdinal = beanStateOrdinal;
            }

            if (stateOrdinal == 0) {
                // We're at the minimal value
                return getStateEnum(stateOrdinal);
            }
        }

        return getStateEnum(stateOrdinal);
    }

    /**
     * Converts the given custom ordinal to a state enumeration value
     * 
     * @param aStateOrdinal
     *            An ordinal value
     * @return The corresponding component state, or null
     */
    protected EComponentState getStateEnum(final int aStateOrdinal) {

        switch (aStateOrdinal) {
        case 0:
            return EComponentState.WAITING;

        case 1:
            return EComponentState.RESOLVED;

        case 2:
            return EComponentState.INSTANTIATING;

        case 3:
            return EComponentState.COMPLETE;
        }

        return null;
    }

    /**
     * Converts the given enumeration value to a custom ordinal.
     * 
     * Returned ordinal orders values from WAITING (0) to COMPLETE (3)
     * 
     * @param aState
     *            A component state
     * @return A custom ordinal value, -1 on failure
     */
    protected int getStateOrdinal(final EComponentState aState) {

        if (aState == null) {
            return -1;
        }

        switch (aState) {
        case WAITING:
            return 0;

        case RESOLVED:
            return 1;

        case INSTANTIATING:
            return 2;

        case COMPLETE:
            return 3;
        }

        return -1;
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

    public boolean isRoot() {

        return getParent() == null;
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
    public boolean resolve(
            final Collection<ComponentBean> aComponentsSubSet,
            final Map<String, ? extends Collection<String>> aIsolatesCapabilities,
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

        for (final Entry<String, ? extends Collection<String>> capabilities : aIsolatesCapabilities
                .entrySet()) {

            // Get the entry content
            final String isolateId = capabilities.getKey();
            final Collection<String> isolateTypes = capabilities.getValue();

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

        // TODO : add an output to help the user to make diagnoses
        // System.out.println("==> UNRESOLVED : " + unresolvedComponents);

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
            final Map<String, Collection<String>> aIsolatesCapabilities,
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
     * @param aComponentSets
     *            The components sets under this set
     */
    public void setComponentSets(final ComponentsSetBean[] aComponentSets) {

        pComponentSets.clear();

        if (aComponentSets != null) {
            pComponentSets.addAll(Arrays.asList(aComponentSets));
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

    /**
     * Generates a formatted string with more informations than a simple
     * {@link #toString()}
     * 
     * @return A complete information string
     */
    public String toCompleteString() {

        final StringBuilder builder = new StringBuilder();
        toCompleteString(builder, "");

        return builder.toString();
    }

    /**
     * Generates a formatted string with more informations than a simple
     * {@link #toString()}
     * 
     * @param aBuilder
     *            A string builder to fill
     * @param aPrefix
     *            The current line prefix
     */
    private void toCompleteString(final StringBuilder aBuilder,
            final String aPrefix) {

        final String subPrefix = aPrefix + "  ";

        aBuilder.append("\n").append(aPrefix);
        aBuilder.append("Name : ").append(pName);

        aBuilder.append("\n").append(aPrefix);
        aBuilder.append("Components : [");
        for (final ComponentBean component : pComponentBeans.values()) {
            aBuilder.append("\n").append(subPrefix);
            aBuilder.append("Name: ").append(component.getName());
            aBuilder.append("\n").append(subPrefix);
            aBuilder.append("Type: ").append(component.getType());
            aBuilder.append("\n").append(subPrefix);
            aBuilder.append("Properties: ").append(component.getProperties());
            aBuilder.append("\n").append(subPrefix);
            aBuilder.append("Filters: ").append(component.getFieldsFilters());
            aBuilder.append("\n").append(subPrefix);
            aBuilder.append("Wires: ").append(component.getWires());

        }
        aBuilder.append("\n").append(aPrefix).append("]");

        aBuilder.append("\n").append(aPrefix);
        aBuilder.append("Composets : [");
        for (final ComponentsSetBean component : pComponentSets) {
            component.toCompleteString(aBuilder, subPrefix);
        }
        aBuilder.append("\n").append(aPrefix).append("]\n");
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
