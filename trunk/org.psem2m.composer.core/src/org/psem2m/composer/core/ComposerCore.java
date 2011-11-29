/**
 * File:   ComposerCore.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.ComponentSnapshot;
import org.psem2m.composer.ComponentsSetSnapshot;
import org.psem2m.composer.CompositionEvent;
import org.psem2m.composer.CompositionSnapshot;
import org.psem2m.composer.EComponentState;
import org.psem2m.composer.ECompositionEvent;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.ICompositionListener;
import org.psem2m.composer.agent.ComposerAgentSignals;
import org.psem2m.composer.config.IComposerConfigReader;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.EEmitterTargets;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * PSEM2M Composer core agent
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-core-factory", publicFactory = false)
@Provides(specifications = IComposer.class)
@Instantiate(name = "psem2m-composer-core")
public class ComposerCore extends CPojoBase implements IComposer,
        ISignalListener {

    /** List of composition events listener */
    private final List<ICompositionListener> pCompositionListeners = new ArrayList<ICompositionListener>();

    /** Composer configuration reader */
    @Requires
    private IComposerConfigReader pConfigReader;

    /** The events log, a Timestamp -&gt; Event sorted map */
    private final SortedMap<Long, StoredEvent> pEvents = new TreeMap<Long, StoredEvent>();

    /** Composites fully-instantiated */
    private final Map<String, InstantiatingComposite> pFullComposites = new HashMap<String, InstantiatingComposite>();

    /** Composites currently instantiating */
    private final Map<String, InstantiatingComposite> pInstantiatingComposites = new HashMap<String, InstantiatingComposite>();

    /** The component instantiation time out (in milliseconds) */
    @Property(name = "component-instantiation-timeout", value = "500")
    private long pInstantiationTimeout;

    /** Maps isolates and components */
    private final Map<String, List<String>> pIsolatesCapabilities = new HashMap<String, List<String>>();

    /** Flag to store events in the events map */
    private boolean pLogEvents;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The requests timeouts futures */
    private Map<String, ScheduledFuture<?>> pRequestsTimeouts = new HashMap<String, ScheduledFuture<?>>();

    /**
     * The delay to wait after receiving a factory message before trying a new
     * resolution (in milliseconds)
     */
    @Property(name = "resolution-delay", value = "200")
    private long pResolutionDelay;

    /** The resolution future */
    private ScheduledFuture<?> pResolutionFuture;

    /** The resolution runner */
    private final Runnable pResolutionRunner = new Runnable() {

        @Override
        public void run() {

            // Call for a new resolution
            notifyComponentsRegistration();
        }
    };

    /** The list of roots */
    private final List<ComponentsSetBean> pRootsComponentsSetBean = new ArrayList<ComponentsSetBean>();

    /** Scheduler executor for timeouts */
    private final ScheduledExecutorService pScheduler = Executors
            .newScheduledThreadPool(1);

    /** The signals broadcaster */
    @Requires
    private ISignalBroadcaster pSignalBroadcaster;

    /** Composites waiting for full instantiation */
    private final List<InstantiatingComposite> pWaitingComposites = new ArrayList<InstantiatingComposite>();

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            The bound service
     */
    @Bind(id = "signal-receiver")
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        // Register to all composer agent signals
        aSignalReceiver.registerListener(
                ComposerAgentSignals.FILTER_ALL_RESPONSES, this);

        aSignalReceiver.registerListener(
                ComposerAgentSignals.SIGNAL_FACTORY_PREFIX + "/*", this);

        pLogger.logInfo(this, "bindSignalReceiver",
                "Bound to a signal receiver");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.IComposer#getCompositionSnapshot()
     */
    private ComponentsSetSnapshot buildComponentsSetSnapshot(
            final ComponentsSetBean aComponentsSetBean) {

        final ComponentsSetSnapshot wComponentsSetSnapshot = new ComponentsSetSnapshot(
                aComponentsSetBean);

        // populate the list of children
        for (final ComponentsSetBean wChild : aComponentsSetBean
                .getComponentSets()) {

            wComponentsSetSnapshot.addChild(buildComponentsSetSnapshot(wChild));
        }

        // populate the list of components
        for (final ComponentBean wComponent : aComponentsSetBean
                .getComponents()) {
            wComponentsSetSnapshot.addComponent(new ComponentSnapshot(
                    wComponent));
        }

        return wComponentsSetSnapshot;
    }

    /**
     * Cancels the timeout for the given component, if possible
     * 
     * @param aComponentName
     *            A component name
     */
    protected void cancelTimeout(final String aComponentName) {

        final ScheduledFuture<?> future = pRequestsTimeouts.get(aComponentName);
        if (future != null) {
            // Cancel it if it's not to late
            future.cancel(false);

            // Remove the timeout from the map
            pRequestsTimeouts.remove(aComponentName);
        }
    }

    /**
     * (Re-)Delays the next resolution
     */
    protected synchronized void delayResolution() {

        // Cancel current run
        if (pResolutionFuture != null) {
            pResolutionFuture.cancel(false);
        }

        // Schedule the next call
        pResolutionFuture = pScheduler.schedule(pResolutionRunner,
                pResolutionDelay, TimeUnit.MILLISECONDS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.IComposer#getCompositionSnapshot()
     */
    @Override
    public synchronized List<CompositionSnapshot> getCompositionSnapshot() {

        // Set up the flag to log incoming events
        pLogEvents = true;

        final List<CompositionSnapshot> wCompositionSnapshots = new ArrayList<CompositionSnapshot>();

        for (final ComponentsSetBean wComponentsSetBean : pRootsComponentsSetBean) {

            wCompositionSnapshots.add(new CompositionSnapshot(
                    buildComponentsSetSnapshot(wComponentsSetBean)));
        }

        return wCompositionSnapshots;
    }

    /**
     * Handles a component changed signal
     * 
     * @param aIsolateId
     *            ID of the sender
     * @param aStateMap
     *            The signal content
     */
    protected void handleComponentChangedSignal(final String aIsolateId,
            final Map<String, Object> aStateMap) {

        // Get the component name
        final String componentName = (String) aStateMap
                .get(ComposerAgentSignals.COMPONENT_CHANGED_KEY_NAME);

        if (componentName == null || componentName.isEmpty()) {
            // Invalid data
            return;
        }

        // Get the new state
        final ECompositionEvent event = (ECompositionEvent) aStateMap
                .get(ComposerAgentSignals.COMPONENT_CHANGED_KEY_STATE);
        if (event == null) {
            // Invalid data
            return;
        }

        switch (event) {
        case ADD:
        case START:
            // The component has been added or started : stop the timeout run
            cancelTimeout(componentName);
            break;

        default:
            // Do nothing
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Extract the signal information
        final String signalSender = aSignalData.getIsolateSender();
        final Object signalContent = aSignalData.getSignalContent();

        if (ComposerAgentSignals.SIGNAL_RESPONSE_HANDLES_COMPONENTS
                .equals(aSignalName)) {
            // An isolate can handle some components

            if (signalContent instanceof ComponentBean[]) {
                // We have something that looks like an answer

                final ComponentBean[] components = (ComponentBean[]) signalContent;
                if (components.length == 0) {
                    // We were wrong...
                    return;
                }

                // Register components capacities
                registerComponentsForIsolate(signalSender, components);
            }

        } else if (ComposerAgentSignals.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS
                .equals(aSignalName)) {
            // An isolate started some components
            if (signalContent instanceof Map) {

                updateInstantiatingCompositeStatus(
                        (Map<String, Object>) signalContent, signalSender);
            }

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_GONE
                .equals(aSignalName)) {
            // An isolate agent is gone
            unregisterComponentsForIsolate(signalSender, pIsolatesCapabilities
                    .get(signalSender).toArray(new String[0]));

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY
                .equals(aSignalName)) {
            // An isolate has some new capacities
            if (signalContent instanceof String[]) {

                registerComponentsForIsolate(signalSender,
                        (String[]) signalContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_REMOVE_FACTORY
                .equals(aSignalName)) {
            // An isolate lost some capacities
            if (signalContent instanceof String[]) {

                unregisterComponentsForIsolate(signalSender,
                        (String[]) signalContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_COMPONENT_CHANGED
                .equals(aSignalName)) {

            // A component state changed
            if (signalContent instanceof Map) {
                handleComponentChangedSignal(signalSender,
                        (Map<String, Object>) signalContent);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.IComposer#instantiateComponentsSet(org.psem2m.composer
     * .model.ComponentsSetBean)
     */
    @Override
    public synchronized void instantiateComponentsSet(
            final ComponentsSetBean aComponentsSetBean) {

        if (aComponentsSetBean == null) {
            // Invalid composite
            pLogger.logSevere(this, "instantiateComponentsSet",
                    "Null components set");
            return;
        }

        if (!aComponentsSetBean.isRoot()) {
            // Invalid composite
            pLogger.logSevere(this, "instantiateComponentsSet",
                    "Can't instantiate a ComponentsSet which is not root");
            return;
        }

        // Get the components
        final ComponentBean[] components = aComponentsSetBean
                .getAllComponents();
        if (components == null || components.length == 0) {
            // Invalid components list
            pLogger.logSevere(this, "instantiateComponentsSet",
                    "Empty components set");
            return;
        }

        // Prepare the components set model
        if (!prepareComponentsSet(aComponentsSetBean)) {
            pLogger.logWarn(this, "instantiateComponentsSet",
                    "It seems that some wires couldn't be linked in",
                    aComponentsSetBean.getName(),
                    "; maybe they are optionnal...");
        }

        // Print a complete representation of the components set
        pLogger.logDebug(this, "instantiateComponentsSet", "Model :",
                aComponentsSetBean.toCompleteString());

        // Add the component to the waiting list
        pWaitingComposites.add(new InstantiatingComposite(aComponentsSetBean));

        // register the aComponentsSetBean in the list of roots
        pRootsComponentsSetBean.add(aComponentsSetBean);

        // Send the event ECompositionEvent.ADD to the Composition
        // listeners
        notifyCompositionEvent(ECompositionEvent.ADD, aComponentsSetBean);

        // Send a signal with all components in an array
        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.ISOLATES,
                ComposerAgentSignals.SIGNAL_CAN_HANDLE_COMPONENTS, components);
    }

    /**
     * Sends a signal to selected isolates to instantiate the components of the
     * given components set
     * 
     * @param aComposet
     *            The components set to instantiate
     * @param aComponentsRepartition
     *            The computed components repartition
     */
    protected void instantiateComponentsSet(
            final InstantiatingComposite aComposet,
            final Map<String, ComponentBean[]> aComponentsRepartition) {

        for (final Entry<String, ComponentBean[]> isolateEntry : aComponentsRepartition
                .entrySet()) {

            final String isolateId = isolateEntry.getKey();
            final ComponentBean[] isolateComponents = isolateEntry.getValue();

            if (isolateComponents == null || isolateComponents.length == 0) {
                // Ignore empty components arrays
                pLogger.logInfo(this, "instantiateComponentsSet",
                        "Nothing to send to isolate", isolateId);
                continue;
            }

            // Prepare the timeouts calls
            for (final ComponentBean component : isolateComponents) {

                final String componentName = component.getName();

                // Cancel the previous timeout, just in case
                cancelTimeout(componentName);

                // Prepare the future
                final ScheduledFuture<?> future = pScheduler.schedule(
                        new Runnable() {

                            @Override
                            public void run() {

                                pLogger.logInfo(this,
                                        "ComponentRequestTimeout",
                                        componentName,
                                        "instantiation request timed out.");

                                // Notify the timeout
                                aComposet
                                        .notifyInstantiationTimeout(componentName);

                                // Remove ourself from the map
                                pRequestsTimeouts.remove(componentName);
                            }
                        }, pInstantiationTimeout, TimeUnit.MILLISECONDS);

                // Store the future
                pRequestsTimeouts.put(componentName, future);
            }

            // Send the instantiation signal
            pSignalBroadcaster.sendData(isolateId,
                    ComposerAgentSignals.SIGNAL_INSTANTIATE_COMPONENTS,
                    isolateComponents);

            // Update the components set state
            aComposet.notifyInstantiationRequest(isolateId, isolateComponents);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Composer Core Gone");
    }

    /**
     * Called when some components has been registered for one or more isolates.
     * Tests if a composite can be fully instantiated and does the job if
     * possible.
     */
    protected synchronized void notifyComponentsRegistration() {

        synchronized (pWaitingComposites) {

            final List<InstantiatingComposite> resolvedComposites = new ArrayList<InstantiatingComposite>();

            for (final InstantiatingComposite composite : pWaitingComposites) {

                final Map<String, ComponentBean[]> resolution = new HashMap<String, ComponentBean[]>();
                if (composite.resolve(pIsolatesCapabilities, resolution)) {
                    // The given composite can be instantiated
                    resolvedComposites.add(composite);

                    // Do the job
                    instantiateComponentsSet(composite, resolution);
                }
            }

            // Remove the resolved composites from the waiting list
            for (final InstantiatingComposite composite : resolvedComposites) {

                pWaitingComposites.remove(composite);
                pInstantiatingComposites.put(composite.getName(), composite);
            }
        }
    }

    /**
     * Sends a "composition changed" event to all listeners.
     * 
     * @param aEvent
     *            Event to be sent to all listeners
     */
    protected void notifyComponentStateEvent(final ComponentBean aComponent,
            final EComponentState aState) {

        if (pLogEvents) {
            // Store the event if necessary
            synchronized (pEvents) {
                pEvents.put(System.currentTimeMillis(), new StoredEvent(
                        aComponent, aState));
            }
        }

        // Notify listeners
        for (final ICompositionListener listener : pCompositionListeners) {

            listener.componentStateChanged(aComponent, aState);
        }
    }

    /**
     * Sends a "composition changed" event to all listeners.
     * 
     * @param aEvent
     *            Event to be sent to all listeners
     */
    protected void notifyCompositionEvent(final CompositionEvent aEvent) {

        if (pLogEvents) {
            // Store the event if necessary
            synchronized (pEvents) {
                pEvents.put(System.currentTimeMillis(), new StoredEvent(aEvent));
            }
        }

        // Notify listeners
        for (final ICompositionListener listener : pCompositionListeners) {

            listener.compositionChanged(aEvent);
        }
    }

    /**
     * Sends a "composet changed" event to all listeners.
     * 
     * @param aEvent
     *            Event to be sent to all listeners
     */
    protected void notifyCompositionEvent(final ECompositionEvent aEvent,
            final ComponentsSetBean aComposet) {

        final CompositionEvent event = new CompositionEvent(aEvent, aComposet);

        if (pLogEvents) {
            // Store the event if necessary
            synchronized (pEvents) {
                pEvents.put(System.currentTimeMillis(), new StoredEvent(event));
            }
        }

        // Notify listeners
        for (final ICompositionListener listener : pCompositionListeners) {

            listener.compositionChanged(event);
        }
    }

    /**
     * Resolves the wires defined in the given components set.
     * 
     * For each component, the name is computed and the wires are linked
     * 
     * @param aComponentsSet
     *            A components set
     * 
     * @return True if all wires are linked, false on error
     */
    protected boolean prepareComponentsSet(
            final ComponentsSetBean aComponentsSet) {

        if (aComponentsSet == null) {
            pLogger.logWarn(this, "prepareComponentsSet", "Null components set");
            return false;
        }

        aComponentsSet.computeName();
        return aComponentsSet.linkWires(null);
    }

    /**
     * Associates components types with the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aComponents
     *            Component beans supported by the isolate
     */
    protected void registerComponentsForIsolate(final String aIsolateId,
            final ComponentBean[] aComponents) {

        final List<String> isolateComponents;

        synchronized (pIsolatesCapabilities) {
            // Prepare the list of components associated to the isolate

            List<String> mapComponents = pIsolatesCapabilities.get(aIsolateId);

            if (mapComponents == null) {
                mapComponents = new ArrayList<String>();
                pIsolatesCapabilities.put(aIsolateId, mapComponents);
            }

            isolateComponents = mapComponents;
        }

        synchronized (isolateComponents) {
            // Maps each valid component with the isolate
            for (final ComponentBean component : aComponents) {
                if (component != null && component.getType() != null) {
                    isolateComponents.add(component.getType());
                }
            }
        }

        // Delay a new resolution run
        delayResolution();
    }

    /**
     * Associates components types with the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aComponentsTypes
     *            Component types supported by the isolate
     */
    protected void registerComponentsForIsolate(final String aIsolateId,
            final String[] aComponentsTypes) {

        final List<String> isolateComponents;

        synchronized (pIsolatesCapabilities) {
            // Prepare the list of components associated to the isolate

            List<String> mapComponents = pIsolatesCapabilities.get(aIsolateId);

            if (mapComponents == null) {
                mapComponents = new ArrayList<String>();
                pIsolatesCapabilities.put(aIsolateId, mapComponents);
            }

            isolateComponents = mapComponents;
        }

        synchronized (isolateComponents) {
            // Maps each valid component with the isolate
            for (final String type : aComponentsTypes) {
                if (type != null) {
                    isolateComponents.add(type);
                }
            }
        }

        // Delay a new resolution run
        delayResolution();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.IComposer#addCompositionListener(org.psem2m.composer
     * .ICompositionListener, org.psem2m.composer.CompositionSnapshot)
     */
    @Override
    public void registerCompositionListener(
            final ICompositionListener aCompositionListener,
            final long aTimeStamp) {

        pCompositionListeners.add(aCompositionListener);

        // send all states
        synchronized (pInstantiatingComposites) {
            synchronized (pFullComposites) {
                synchronized (pWaitingComposites) {

                    for (final InstantiatingComposite wIC : pWaitingComposites) {
                        aCompositionListener.componentsSetStateChanged(
                                wIC.getBean(), EComponentState.WAITING);
                    }

                    for (final InstantiatingComposite wIC : pFullComposites
                            .values()) {
                        aCompositionListener.componentsSetStateChanged(
                                wIC.getBean(), EComponentState.COMPLETE);
                    }
                    for (final InstantiatingComposite wIC : pInstantiatingComposites
                            .values()) {
                        aCompositionListener.componentsSetStateChanged(
                                wIC.getBean(), EComponentState.INSTANCIATING);
                    }
                }
            }
        }

        // Get all the events kept after the TimeStamp
        final List<StoredEvent> eventsToSend;

        synchronized (pEvents) {

            // Get the events before the given time stamp
            final SortedMap<Long, StoredEvent> eventsMap = pEvents.subMap(
                    aTimeStamp, System.currentTimeMillis());

            // Get a copy of this events list
            eventsToSend = new LinkedList<StoredEvent>(eventsMap.values());

            // TODO verify that no one else needs those values
            // Stop logging
            pLogEvents = false;

            // Remove treated values
            pEvents.keySet().removeAll(eventsMap.keySet());
        }

        // Send'em all
        for (final StoredEvent event : eventsToSend) {

            switch (event.getType()) {
            case COMPOSITION_EVENT:
                aCompositionListener.compositionChanged(event
                        .getCompositionEvent());
                break;

            case COMPONENT_EVENT:
                aCompositionListener.componentStateChanged(
                        event.getComponent(), event.getState());
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.IComposer#removeComponentsSet(org.psem2m.composer
     * .model.ComponentsSetBean)
     */
    @Override
    public synchronized void removeComponentsSet(
            final ComponentsSetBean aComponentsSetBean) throws Exception {

        if (!aComponentsSetBean.isRoot()) {
            // Invalid composite
            pLogger.logSevere(this, "removeComponentsSet",
                    "Can't instantiate a ComponentsSet which is not root");
            return;
        }

        final String composetName = aComponentsSetBean.getName();

        // Remove the components set from the list of roots
        pRootsComponentsSetBean.remove(aComponentsSetBean);

        // Try to find the corresponding InstantiatingComposite
        final InstantiatingComposite composite;
        if (pFullComposites.containsKey(composetName)) {
            // Fully instantiated components set
            composite = pFullComposites.get(composetName);
            pFullComposites.remove(composetName);

        } else if (pInstantiatingComposites.containsKey(composetName)) {
            // Partially instantiated components set
            composite = pInstantiatingComposites.get(composetName);
            pInstantiatingComposites.remove(composite);

        } else if (pWaitingComposites.contains(aComponentsSetBean)) {
            // Waiting composite : no reason to use it...
            pWaitingComposites.remove(aComponentsSetBean);
            return;

        } else {
            pLogger.logWarn(this, "removeComponentsSet",
                    "Unknown components set :", aComponentsSetBean);
            return;
        }

        // Send signals to agents
        final Map<String, List<String>> runningComponents = composite
                .getRunningComponents();

        for (final Entry<String, List<String>> entry : runningComponents
                .entrySet()) {

            final String isolate = entry.getKey();
            final String[] components = entry.getValue().toArray(new String[0]);

            // Tell the agent in the isolate to stop the given component
            pSignalBroadcaster.sendData(isolate,
                    ComposerAgentSignals.SIGNAL_STOP_COMPONENTS, components);
        }

        // Extra signals : tell all agents to stop remaining components
        // Those ones may not even be running, so ignore errors...
        final String[] remainingComponents = composite.getRemainingComponents()
                .toArray(new String[0]);

        pSignalBroadcaster.sendData(EEmitterTargets.ALL,
                ComposerAgentSignals.SIGNAL_STOP_COMPONENTS,
                remainingComponents);

        // Send the event ECompositionEvent.REMOVE to the Composition
        // listeners
        notifyCompositionEvent(ECompositionEvent.REMOVE, aComponentsSetBean);
    }

    /**
     * Runs a simple test, using the given configuration file
     * 
     * @param aFileName
     *            A configuration file name
     */
    protected void test_conf(final String aFileName) {

        pLogger.logInfo(this, "test_conf", "Running test...");
        final ComponentsSetBean compoSet = pConfigReader.load(aFileName);

        pLogger.logInfo(this, "test_conf", "Result=", compoSet);
        if (compoSet == null) {
            pLogger.logInfo(this, "test_conf", "Can't read the file", aFileName);
            return;
        }

        // Fire at will
        pLogger.logInfo(this, "test_conf", "Fire at will !");
        instantiateComponentsSet(compoSet);

        // pScheduler.schedule(new Runnable() {
        //
        // @Override
        // public void run() {
        //
        // try {
        // removeComponentsSet(compoSet);
        // } catch (final Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // }, 10, TimeUnit.SECONDS);
    }

    /**
     * Removes components types from the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aComponentsTypes
     *            Component types no more supported by the isolate
     */
    protected void unregisterComponentsForIsolate(final String aIsolateId,
            final String[] aComponentsTypes) {

        final List<String> isolateComponents = pIsolatesCapabilities
                .get(aIsolateId);

        if (isolateComponents == null || isolateComponents.isEmpty()) {
            // Nothing to do...
            return;
        }

        // Flag indicating if a new composite resolution is needed
        boolean needsNewResolution = false;

        synchronized (isolateComponents) {
            // Unmaps each component type with the isolate
            for (final String type : aComponentsTypes) {
                isolateComponents.remove(type);
            }

            // Update complete composites states
            for (final InstantiatingComposite composite : pFullComposites
                    .values()) {

                composite.lostComponentTypes(aIsolateId, aComponentsTypes);

                // Update the composite completion level if needed
                if (!composite.isComplete()) {
                    final String compositeName = composite.getName();
                    pFullComposites.remove(compositeName);
                    pWaitingComposites.add(composite);

                    // A new resolution is needed
                    needsNewResolution = true;
                }
            }

            // Update instantiating composites states
            for (final InstantiatingComposite composite : pInstantiatingComposites
                    .values()) {

                composite.lostComponentTypes(aIsolateId, aComponentsTypes);

                // Update the composite completion level if needed
                if (!composite.isComplete()) {
                    final String compositeName = composite.getName();
                    pInstantiatingComposites.remove(compositeName);
                    pWaitingComposites.add(composite);

                    // A new resolution is needed
                    needsNewResolution = true;
                }
            }
        }

        if (needsNewResolution) {
            // Try to recompute a route for degraded composites
            // notifyComponentsRegistration();

            // Delay a new resolution run
            delayResolution();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.IComposer#removeCompositionListener(org.psem2m.composer
     * .ICompositionListener)
     */
    @Override
    public void unregisterCompositionListener(
            final ICompositionListener aCompositionListener) {

        pCompositionListeners.remove(aCompositionListener);
    }

    /**
     * Updates an instantiating composite status according to the given composer
     * agent result
     * 
     * @param aAgentResult
     *            A result map returned by a composer agent
     * @param aHostIsolate
     *            The ID of the isolate sending the new status
     */
    protected void updateInstantiatingCompositeStatus(
            final Map<String, Object> aAgentResult, final String aHostIsolate) {

        // Working composite
        final String composetName = (String) aAgentResult
                .get(ComposerAgentSignals.RESULT_KEY_COMPOSITE);

        // Get the corresponding components set
        final InstantiatingComposite composet = pInstantiatingComposites
                .get(composetName);

        // Unknown composite
        if (composet == null) {
            // Ignore it
            return;
        }

        // Succeeded component instantiations
        final String[] instantiatedComponents = (String[]) aAgentResult
                .get(ComposerAgentSignals.RESULT_KEY_INSTANTIATED);

        if (instantiatedComponents == null) {
            // Nothing to do
            return;
        }

        // Update the composite state
        for (final String componentName : instantiatedComponents) {

            // Cancel the corresponding timeout
            cancelTimeout(componentName);
            composet.componentStarted(componentName, aHostIsolate);
        }

        // Composite status is good
        if (composet.isComplete()) {

            pFullComposites.put(composet.getName(), composet);

            // Nothing more to do
            return;
        }

        // Failed instantiations
        final String[] failedComponents = (String[]) aAgentResult
                .get(ComposerAgentSignals.RESULT_KEY_FAILED);

        // Cancel their timeouts
        for (final String failedComponentName : failedComponents) {
            cancelTimeout(failedComponentName);
        }

        pLogger.logWarn(this, "updateInstantiatingCompositeStatus",
                "The following components couldn't be started :",
                Arrays.toString(failedComponents));

        // Ask for a new resolution
        delayResolution();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Composer Core Ready");

        try {
            test_conf("test_compo.js");
        } catch (final Throwable e) {
            pLogger.logSevere(this, "", "Something went wrong\n", e);
        }
    }
}
