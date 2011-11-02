/**
 * File:   InstantiatingComposite.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

    /** Started components -&gt; host isolate map */
    private final Map<String, String> pRunningComponents = new HashMap<String, String>();

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
     * @param aHostIsolate
     *            The ID of isolate hosting the component
     */
    public void componentStarted(final ComponentBean aComponentBean,
            final String aHostIsolate) {

        if (aComponentBean == null) {
            return;
        }

        if (!pComposite.getName().equals(aComponentBean.getCompositeName())) {
            // We're not the parent of the given composite
            return;
        }

        // Update composite state
        componentStarted(aComponentBean.getName(), aHostIsolate);
    }

    /**
     * Updates the composite state, telling that the given component has been
     * started
     * 
     * @param aComponentName
     *            A started component name
     * @param aHostIsolate
     *            The ID of isolate hosting the component
     */
    public void componentStarted(final String aComponentName,
            final String aHostIsolate) {

        pRemainingComponents.remove(aComponentName);
        pRunningComponents.put(aComponentName, aHostIsolate);
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

        // Update composite state
        componentStopped(aComponentBean.getName());
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

        // Update composite state
        pRunningComponents.remove(aComponentName);
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

    /**
     * Called when some component types has been lost for a given isolate
     * 
     * @param aIsolateId
     *            ID of the isolate that lost the given types
     * 
     * @param aComponentsTypes
     *            Lost component types
     */
    public void lostComponentTypes(final String aIsolateId,
            final String[] aComponentsTypes) {

        if (aIsolateId == null || aComponentsTypes == null
                || aComponentsTypes.length == 0) {
            // Nothing to do...
            return;
        }

        // Transform the array into a set, to increase performance
        final Set<String> lostTypesSet = new HashSet<String>(
                Arrays.asList(aComponentsTypes));

        // List of lost elements
        final Set<String> lostComponents = new HashSet<String>();

        // First loop : detect lost components
        for (final Entry<String, String> entry : pRunningComponents.entrySet()) {

            if (!aIsolateId.equals(entry.getValue())) {
                // The component is running on a different isolate
                continue;
            }

            final ComponentBean component = pComposite.getComponent(entry
                    .getKey());
            if (component == null) {
                // Component not found, ignore it
                continue;
            }

            // Test the component type
            final String componentType = component.getType();
            if (lostTypesSet.contains(componentType)) {
                // Lost component
                lostComponents.add(component.getName());
            }
        }

        // Second loop : remove them
        for (final String componentName : lostComponents) {
            componentStopped(componentName);
        }
    }

    /**
     * Resolves the components not yet instantiated of this composite
     * 
     * @param aIsolatesCapabilities
     *            Isolates capabilities
     * @param aResolution
     *            The resulting resolution
     * @return True if the whole composite has been resolved
     */
    public boolean resolve(
            final Map<String, List<String>> aIsolatesCapabilities,
            final Map<String, ComponentBean[]> aResolution) {

        // Prepare the components beans list
        final List<ComponentBean> remainingComponentsBeans = new ArrayList<ComponentBean>();
        for (final String componentName : pRemainingComponents) {

            final ComponentBean bean = pComposite.getComponent(componentName);
            if (bean != null) {
                remainingComponentsBeans.add(bean);
            }
        }

        // Resolve the remaining components
        return pComposite.resolve(remainingComponentsBeans,
                aIsolatesCapabilities, aResolution);
    }
}
