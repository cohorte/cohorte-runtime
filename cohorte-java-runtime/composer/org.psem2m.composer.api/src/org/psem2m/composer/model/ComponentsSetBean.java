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

    /** Indentation for pretty printing */
    private static final String PRETTY_PRINT_INDENT = "   ";

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
        aComponent.setParentName(getName());

        // Store it
        pComponentBeans.put(aComponent.getName(), aComponent);
    }

    /**
     * Appends a new line with the given prefix in the given string builder
     * 
     * @param aBuilder
     *            A string builder
     * @param aPrefix
     *            An optional line prefix (can be null)
     * @return The given string builder
     */
    private StringBuilder appendNewLine(final StringBuilder aBuilder,
            final CharSequence aPrefix) {

        if (aBuilder == null) {
            // Small protection
            return null;
        }

        // New line
        aBuilder.append("\n");

        if (aPrefix != null) {
            // Add the prefix, if any
            aBuilder.append(aPrefix);
        }

        // Return the builder
        return aBuilder;
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
        return safeCompareTo(getName(), aOther.getName());
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
                final StringBuilder builder = new StringBuilder();
                builder.append(parentName);
                builder.append(".");
                builder.append(getName());

                setName(builder.toString());
            }

        } else {
            // We are the root element
            setRootName(getName());
        }

        synchronized (pComponentBeans) {

            // Copy components list
            final List<ComponentBean> components = new ArrayList<ComponentBean>(
                    pComponentBeans.values());

            // Reset the components map
            pComponentBeans.clear();

            // Update their name and re-populate the components map
            for (final ComponentBean bean : components) {
                bean.setParentName(getName());
                bean.setRootName(getRootName());
                bean.computeName();

                // Now mapped with the new component name
                pComponentBeans.put(bean.getName(), bean);
            }
        }

        // Propagate modification to sub-sets
        for (final ComponentsSetBean bean : pComponentSets) {
            bean.setParentName(getName());
            bean.setRootName(getRootName());
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
    private ComponentBean findComponent(final String aComponentName,
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
    private void getAllComponents(final Collection<ComponentBean> aComponents) {

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
     * Returns the lowest component state of the current and given state. If the
     * given state is null, returns the current state.
     * 
     * @param aOtherState
     *            A component state
     * @return The lowest state of the current and the given one
     */
    private EComponentState getLowest(final EComponentState aState,
            final EComponentState aOtherState) {

        if (aOtherState == null || aOtherState.equals(aState)) {
            // Nothing to do
            return aState;
        }

        if (aState == null) {
            // Nothing to do
            return aOtherState;
        }

        switch (aState) {
        case REMOVED:
            // Ignore this state
            return aOtherState;

        case COMPLETE:
            // Nothing can be upon complete
            return aOtherState;

        case WAITING:
            // Nothing can be above waiting
            return aState;

        case INSTANTIATING:
            // Only Complete is a greater state
            switch (aOtherState) {
            case COMPLETE:
                return aState;

            default:
                return aOtherState;
            }

        case RESOLVED:
            // Resolved state is before Instantiating and Complete
            switch (aOtherState) {
            case INSTANTIATING:
            case COMPLETE:
                return aState;

            default:
                return aOtherState;
            }

        default:
            // Unhandled case
            return aState;
        }
    }

    /**
     * Retrieves the parent of this component set
     * 
     * @return the parent of this component set
     */
    public ComponentsSetBean getParent() {

        return pParent;
    }

    /**
     * Returns the lowest state found in the components or sets contained in
     * this set.
     * 
     * @return The computed state of this components set
     * @see org.psem2m.composer.model.IModelBean#getState()
     */
    @Override
    public EComponentState getState() {

        // Start with the highest state
        EComponentState lowestState = EComponentState.COMPLETE;

        // Get the minimal state of our components
        for (final ComponentBean component : pComponentBeans.values()) {

            // Update the lowest state with the current bean value
            lowestState = getLowest(lowestState, component.getState());

            if (lowestState == EComponentState.WAITING) {
                // We reached the lowest possible state
                return lowestState;
            }
        }

        // Get the minimal state of the components sets
        for (final ComponentsSetBean composet : pComponentSets) {

            // Update the lowest state with the current bean value
            lowestState = getLowest(lowestState, composet.getState());

            if (lowestState == EComponentState.WAITING) {
                // We reached the lowest possible state
                return lowestState;
            }
        }

        return lowestState;
    }

    /**
     * Returns an indented prefix for pretty print in {@link #toString()}.
     * 
     * @param aCurrentIndent
     *            The current indentation (null is considered as an empty
     *            string)
     * @return The new indentation (current + 3 spaces)
     */
    private String indent(final CharSequence aCurrentIndent) {

        final StringBuilder builder = new StringBuilder();
        if (aCurrentIndent != null) {
            builder.append(aCurrentIndent);
        }

        builder.append(PRETTY_PRINT_INDENT);
        return builder.toString();
    }

    /**
     * Tests if this set has no components nor components sets.
     * 
     * @return True if the set is empty
     */
    public boolean isEmpty() {

        return pComponentBeans.isEmpty() && pComponentSets.isEmpty();
    }

    /**
     * Tests if this components set is the root of a composition, i.e. has no
     * parent set.
     * 
     * @return True if this components set is a root
     */
    public boolean isRoot() {

        return pParent == null;
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
     * Merges two ComponentBean arrays into a single one, avoiding doubles.
     * 
     * @param aExistingArray
     *            An original array (can be null)
     * @param aAddedItems
     *            Items to add to the array (can be null)
     * @return A merged array, or null if both arguments were null.
     */
    private ComponentBean[] mergeArrays(final ComponentBean[] aExistingArray,
            final ComponentBean[] aAddedItems) {

        if (aAddedItems == null) {
            // Nothing to add
            return aExistingArray;
        }

        if (aExistingArray == null) {
            // Nothing to add either
            return aAddedItems;
        }

        // We use an array list to keep the original items order
        final List<ComponentBean> tempSet = new ArrayList<ComponentBean>(
                Arrays.asList(aExistingArray));

        for (final ComponentBean item : aAddedItems) {
            if (!tempSet.contains(item)) {
                // Add if not yet in the set
                tempSet.add(item);
            }
        }

        return tempSet.toArray(new ComponentBean[tempSet.size()]);
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

            // Try a resolution
            final ComponentBean[] resolvedComponents = resolveOnIsolate(
                    isolateId, capabilities.getValue(), unresolvedComponents);

            // Update the resolution map (merge with previous resolution)
            final ComponentBean[] newResolution = mergeArrays(
                    aResolution.get(isolateId), resolvedComponents);
            if (newResolution != null) {
                aResolution.put(isolateId, newResolution);
            }

            // Clean the awaiting components list
            for (final ComponentBean component : resolvedComponents) {
                unresolvedComponents.remove(component);
            }

            // Test if the resolution is complete
            if (unresolvedComponents.isEmpty()) {
                return true;
            }
        }

        // TODO : add an output to help the user to make diagnoses
        // System.out.println("==> UNRESOLVED : " + unresolvedComponents);

        return false;
    }

    /**
     * Tries to resolve the given components with the given isolate
     * informations. Updates the state of the resolved components to
     * {@link EComponentState#RESOLVED}.
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aIsolateTypes
     *            The component types available on the given isolate
     * @param aToResolve
     *            Components to resolve
     * @return The list of resolved components (subset of aToResolve)
     */
    private ComponentBean[] resolveOnIsolate(final String aIsolateId,
            final Collection<String> aIsolateTypes,
            final Collection<ComponentBean> aToResolve) {

        // Prepare the resolution content
        final List<ComponentBean> resolvedComponents = new ArrayList<ComponentBean>();

        // Test all awaiting components
        for (final ComponentBean component : aToResolve) {

            final String componentHost = component.getIsolate();
            if (componentHost != null && !componentHost.isEmpty()
                    && !componentHost.equals(aIsolateId)) {
                // No the host we're waiting for
                continue;
            }

            if (aIsolateTypes.contains(component.getType())) {
                // We've found a match
                resolvedComponents.add(component);

                // Update the component state
                component.setState(EComponentState.RESOLVED);
            }
        }

        return resolvedComponents.toArray(new ComponentBean[resolvedComponents
                .size()]);
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
                // Update the component information
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
            for (final ComponentsSetBean composet : aComponentSets) {
                // Update the components set information
                composet.setParent(this);
                pComponentSets.add(composet);
            }
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
            setParentName(pParent.getName());

        } else {
            setParentName(null);
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
        toCompleteString(builder, null);

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
            final CharSequence aPrefix) {

        // Indent a bit more
        final String subPrefix = indent(aPrefix);

        // Components set name
        appendNewLine(aBuilder, subPrefix);
        aBuilder.append("Name: ").append(getName());

        // Components information
        appendNewLine(aBuilder, subPrefix);
        aBuilder.append("Components : [");
        for (final ComponentBean component : pComponentBeans.values()) {
            // Component name
            appendNewLine(aBuilder, subPrefix);
            aBuilder.append("Name: ").append(component.getName());

            // Component type
            appendNewLine(aBuilder, subPrefix);
            aBuilder.append("Type: ").append(component.getType());

            // Component properties
            appendNewLine(aBuilder, subPrefix);
            aBuilder.append("Properties: ").append(component.getProperties());
            appendNewLine(aBuilder, subPrefix);

            // Component filters
            aBuilder.append("Filters: ").append(component.getFieldsFilters());

            // Component wires
            appendNewLine(aBuilder, subPrefix);
            aBuilder.append("Wires: ").append(component.getWires());

        }
        appendNewLine(aBuilder, subPrefix).append("]");

        // Sub components sets information
        appendNewLine(aBuilder, subPrefix);
        aBuilder.append("Composets : [");
        for (final ComponentsSetBean component : pComponentSets) {
            component.toCompleteString(aBuilder, subPrefix);
        }
        appendNewLine(aBuilder, subPrefix).append("]\n");
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
        builder.append("Name=").append(getName());
        builder.append(", Parent=").append(pParent);
        builder.append(")");

        return builder.toString();
    }

    /**
     * Updates the state of all sub-components
     * 
     * @param aNewState
     *            New components states
     */
    public synchronized void updateState(final EComponentState aNewState) {

        for (final ComponentBean bean : getAllComponents()) {
            bean.setState(aNewState);
        }
    }
}
