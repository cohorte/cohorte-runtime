/**
 * File:   InstantiatingComposite.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.psem2m.composer.EComponentState;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;

/**
 * Represents an instantiating components set
 * 
 * @author Thomas Calmant
 */
public class InstantiatingComposite {

    /** The instantiating components set */
    private final ComponentsSetBean pComposet;

    /** Components that still need to be started */
    private final Map<String, InstantiatingComponent> pRemainingComponents = new HashMap<String, InstantiatingComponent>();

    /** Requested (signal sent) components -&gt; host isolate map */
    private final Map<String, InstantiatingComponent> pRequestedComponents = new HashMap<String, InstantiatingComponent>();

    /** Started components list */
    private final Map<String, InstantiatingComponent> pRunningComponents = new HashMap<String, InstantiatingComponent>();

    /**
     * Sets up members
     * 
     * @param aComposet
     *            A components set bean
     */
    public InstantiatingComposite(final ComponentsSetBean aComposet) {

        pComposet = aComposet;

        // Store components names
        for (final ComponentBean component : pComposet.getAllComponents()) {

            pRemainingComponents.put(component.getName(),
                    new InstantiatingComponent(component));
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

        if (aComponentBean == null || aComponentBean.getRootName() == null) {
            return;
        }

        if (!aComponentBean.getRootName().equals(pComposet.getName())) {
            // We're not the root of the given composite
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

        InstantiatingComponent component = null;
        boolean wasTimedOut = false;

        synchronized (pRequestedComponents) {
            // Is this component requested ?
            component = pRequestedComponents.get(aComponentName);

            if (component != null) {
                // Valid component
                pRequestedComponents.remove(aComponentName);

                // Update the state of the component
                component.getComponent().setState(EComponentState.COMPLETE);

            } else if (pRemainingComponents.containsKey(aComponentName)) {
                // Timed-out component...
                wasTimedOut = true;

            } else {
                // Totally unknown
                return;
            }
        }

        if (wasTimedOut) {
            // If the component was timed out, it is now in the remaining map
            synchronized (pRemainingComponents) {

                // Get the timed-out component out of here
                component = pRemainingComponents.get(aComponentName);
                pRemainingComponents.remove(aComponentName);

                if (component != null) {
                    // Update the state of the component
                    component.getComponent().setState(EComponentState.COMPLETE);

                } else {
                    // Something moved - do nothing
                    return;
                }
            }
        }

        synchronized (pRunningComponents) {
            // This update may not be necessary...
            component.setIsolate(aHostIsolate);

            // Store it as running
            pRunningComponents.put(aComponentName, component);
        }
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

        if (!aComponentBean.getRootName().equals(pComposet.getName())) {
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

        final InstantiatingComponent component;

        synchronized (pRunningComponents) {
            // Is the component really running ?
            component = pRunningComponents.get(aComponentName);

            if (component != null) {
                // Component found : remove it from the running ones
                pRunningComponents.remove(aComponentName);

            } else {
                // Unknown component
                return;
            }
        }

        synchronized (pRemainingComponents) {
            // Update components set state
            pRemainingComponents.put(aComponentName, component);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof ComponentsSetBean) {
            // Components set equality
            return aObj.equals(pComposet);

        } else if (aObj instanceof InstantiatingComposite) {
            // Members equality
            return pComposet.equals(((InstantiatingComposite) aObj).pComposet);
        }

        return super.equals(aObj);
    }

    /**
     * Retrieves the instantiating composite
     * 
     * @return the instantiating composite
     */
    public ComponentsSetBean getBean() {

        return pComposet;
    }

    /**
     * Retrieves the composite name
     * 
     * @return the composite name
     */
    public String getName() {

        return pComposet.getName();
    }

    /**
     * Retrieves the list of the names of the components which are still waiting
     * for instantiation
     * 
     * @return the remaining components
     */
    public Set<String> getRemainingComponents() {

        return Collections.unmodifiableSet(pRemainingComponents.keySet());
    }

    /**
     * Retrieves the list of the names of the components which are currently
     * instantiating (request signal sent)
     * 
     * @return the requested components
     */
    public Set<String> getRequestedComponents() {

        return Collections.unmodifiableSet(pRequestedComponents.keySet());
    }

    /**
     * Retrieves a isolate -&gt; components names map
     * 
     * @return the running components map
     */
    public Map<String, List<String>> getRunningComponents() {

        final Map<String, List<String>> resultMap = new HashMap<String, List<String>>();

        synchronized (pRunningComponents) {

            for (final InstantiatingComponent component : pRunningComponents
                    .values()) {

                final String compoName = component.getComponent().getName();
                final String isolate = component.getIsolate();

                // Prepare the components list
                final List<String> isolateCompoList;
                if (resultMap.containsKey(isolate)) {
                    isolateCompoList = resultMap.get(isolate);

                } else {
                    isolateCompoList = new ArrayList<String>();
                    resultMap.put(isolate, isolateCompoList);
                }

                // Store the component
                isolateCompoList.add(compoName);
            }
        }

        return resultMap;
    }

    /**
     * Tests if all components of the components set are instantiated
     * 
     * @return True if the whole components set is instantiated
     */
    public boolean isComplete() {

        return pRemainingComponents.isEmpty() && pRequestedComponents.isEmpty();
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

        synchronized (pRunningComponents) {

            // First loop : detect lost components
            for (final InstantiatingComponent instantiatingComponent : pRunningComponents
                    .values()) {

                if (!aIsolateId.equals(instantiatingComponent.getIsolate())) {
                    // The component is running on a different isolate
                    continue;
                }

                final ComponentBean component = instantiatingComponent
                        .getComponent();

                // Test the component type
                final String componentType = component.getType();
                if (lostTypesSet.contains(componentType)) {
                    // Lost component
                    lostComponents.add(component.getName());
                }
            }
        }

        // Second loop : remove them
        for (final String componentName : lostComponents) {
            componentStopped(componentName);
        }
    }

    /**
     * Updates the state of the requested components information. This method
     * must be called any time one or more components instantiations are
     * requested to an isolate.
     * 
     * @param aIsolateId
     *            Request target
     * @param aRequestedComponents
     *            Requested components
     */
    public void notifyInstantiationRequest(final String aIsolateId,
            final Collection<String> aRequestedComponents) {

        if (aRequestedComponents == null) {
            return;
        }

        // Temporary map
        final Map<String, InstantiatingComponent> requestedComponents = new HashMap<String, InstantiatingComponent>(
                aRequestedComponents.size());

        synchronized (pRemainingComponents) {

            // First loop : find known instantiating components
            for (final String requestedCompoName : aRequestedComponents) {

                final InstantiatingComponent component = pRemainingComponents
                        .get(requestedCompoName);
                if (component != null) {
                    // The component was a remaining one
                    pRemainingComponents.remove(requestedCompoName);

                    // Prepare for next loop
                    requestedComponents.put(requestedCompoName, component);

                    // Update the requested component state
                    component.setIsolate(aIsolateId);
                    component.requestSent();

                    component.getComponent().setState(
                            EComponentState.INSTANTIATING);
                }
            }
        }

        synchronized (pRequestedComponents) {
            // Update the requested components map at once
            pRequestedComponents.putAll(requestedComponents);
        }
    }

    /**
     * Updates the state of the requested components information. This method
     * must be called any time one or more components instantiations are
     * requested to an isolate.
     * 
     * @param aIsolateId
     *            Request target
     * @param aRequestedComponents
     *            Requested components
     */
    public void notifyInstantiationRequest(final String aIsolateId,
            final ComponentBean[] aRequestedComponents) {

        final Set<String> compoNames = new HashSet<String>(
                aRequestedComponents.length);

        // Extract names
        for (final ComponentBean component : aRequestedComponents) {

            compoNames.add(component.getName());
        }

        notifyInstantiationRequest(aIsolateId, compoNames);
    }

    /**
     * Updates the state of the given component if its instantiation timed out.
     * 
     * @param aComponentName
     *            The requested component name
     */
    public void notifyInstantiationTimeout(final String aComponentName) {

        final InstantiatingComponent component;

        synchronized (pRequestedComponents) {

            component = pRequestedComponents.get(aComponentName);

            if (component != null) {
                // Component found
                pRequestedComponents.remove(aComponentName);

            } else {
                // Unknown component
                return;
            }
        }

        synchronized (pRemainingComponents) {
            // Move it in the remaining components map
            pRemainingComponents.put(aComponentName, component);
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
            final Map<String, ? extends Collection<String>> aIsolatesCapabilities,
            final Map<String, ComponentBean[]> aResolution) {

        // Prepare the components beans list
        final List<ComponentBean> remainingComponentsBeans = new ArrayList<ComponentBean>();

        synchronized (pRemainingComponents) {

            for (final InstantiatingComponent component : pRemainingComponents
                    .values()) {

                remainingComponentsBeans.add(component.getComponent());
            }
        }

        // Resolve the remaining components
        return pComposet.resolve(remainingComponentsBeans,
                aIsolatesCapabilities, aResolution);
    }
}
