/**
 * File:   CompositeBean.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.psem2m.composer.model.ComponentBean;

/**
 * PSEM2M Composer composite description bean
 * 
 * @author Thomas Calmant
 */
public class CompositeBean implements Serializable {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** The list of composite components */
    private final Map<String, ComponentBean> pComponents = new HashMap<String, ComponentBean>();

    /** The composite name */
    private String pName;

    /**
     * Default constructor
     */
    public CompositeBean() {

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
        pComponents.put(aComponent.getName(), aComponent);
    }

    /**
     * Retrieves the component of this composite with the given name, null if
     * not found
     * 
     * @param aComponentName
     *            The name of the component to look for
     * @return The component, null if not found
     */
    public ComponentBean getComponent(final String aComponentName) {

        return pComponents.get(aComponentName);
    }

    /**
     * Retrieves the components of the composite
     * 
     * @return the components
     */
    public ComponentBean[] getComponents() {

        final Collection<ComponentBean> components = pComponents.values();
        return components.toArray(new ComponentBean[components.size()]);
    }

    /**
     * Retrieves the composite name
     * 
     * @return the composite name
     */
    public String getName() {

        return pName;
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

        if (!pComponents.values().containsAll(aComponentsSubSet)) {
            // Not all given components are in this composite...
            return false;
        }

        // Clear the resolution map, to be safe
        aResolution.clear();

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

        return resolve(pComponents.values(), aIsolatesCapabilities, aResolution);
    }

    /**
     * Sets the components of the composite
     * 
     * @param aComponents
     *            the components
     */
    public void setComponents(final Collection<ComponentBean> aComponents) {

        pComponents.clear();

        if (aComponents != null) {
            for (final ComponentBean component : aComponents) {
                addComponent(component);
            }
        }
    }

    /**
     * Sets the components of the composite
     * 
     * @param aComponents
     *            the components
     */
    public void setComponents(final ComponentBean[] aComponents) {

        pComponents.clear();

        if (aComponents != null) {
            for (final ComponentBean component : aComponents) {
                addComponent(component);
            }
        }
    }

    /**
     * Sets the composite name
     * 
     * @param aName
     *            the composite name
     */
    public void setName(final String aName) {

        pName = aName;
    }
}
