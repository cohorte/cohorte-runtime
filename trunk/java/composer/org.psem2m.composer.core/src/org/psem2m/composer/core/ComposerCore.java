/**
 * File:   ComposerCore.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
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
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.ECompositionEvent;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.ICompositionListener;
import org.psem2m.composer.agent.ComposerAgentSignals;
import org.psem2m.composer.config.IComposerConfigHandler;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalSendResult;

/**
 * PSEM2M Composer core agent
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-core-factory", publicFactory = false)
@Instantiate(name = "psem2m-composer-core")
@Provides(specifications = { IComposer.class, IIsolatePresenceListener.class }, properties = @StaticServiceProperty(name = "service.exported.interfaces", value = "org.psem2m.composer.IComposer", type = "String"))
public class ComposerCore extends CPojoBase implements IComposer,
        ISignalListener, IIsolatePresenceListener {

    /** Composition listeners requirement ID */
    private static final String LISTENERS_ID = "composition-listeners";

    /** List of composition events listener */
    @Requires(id = LISTENERS_ID, optional = true)
    private ICompositionListener[] pCompositionListeners;

    /** Composer configuration readers */
    @Requires
    private IComposerConfigHandler[] pConfigReaders;

    /** Composites fully-instantiated */
    private final Map<String, InstantiatingComposite> pFullComposites = new HashMap<String, InstantiatingComposite>();

    /** Composites currently instantiating */
    private final Map<String, InstantiatingComposite> pInstantiatingComposites = new HashMap<String, InstantiatingComposite>();

    /** The component instantiation time out (in milliseconds) */
    @Property(name = "component-instantiation-timeout", value = "1000")
    private long pInstantiationTimeout;

    /** Maps isolates and components */
    private final Map<String, Set<String>> pIsolatesCapabilities = new HashMap<String, Set<String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The requests timeouts futures */
    private final Map<String, ScheduledFuture<?>> pRequestsTimeouts = new HashMap<String, ScheduledFuture<?>>();

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
     * Called by iPOJO when a composition listener is bound
     * 
     * @param aListener
     *            a composition listener
     */
    @Bind(id = LISTENERS_ID, aggregate = true, optional = true)
    protected void bindCompositionListener(final ICompositionListener aListener) {

        // Send the current state to the listener
        try {
            aListener.setCompositionSnapshots(getCompositionSnapshot());

        } catch (final Exception ex) {
            // Just log errors...
            pLogger.logWarn(this, "bindCompositionListener",
                    "Error setting up a composition listener.");

            Throwable e = ex;
            do {
                pLogger.logWarn(this, "...", "Continuation:\n", e);
                e = e.getCause();
            } while (e != null);
        }
    }

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
    public synchronized ComponentsSetBean[] getCompositionSnapshot() {

        final List<ComponentsSetBean> wCompositionSnapshots = new ArrayList<ComponentsSetBean>();
        for (final ComponentsSetBean wComponentsSetBean : pRootsComponentsSetBean) {

            // Use the copy constructor
            wCompositionSnapshots.add(new ComponentsSetBean(wComponentsSetBean,
                    null));
        }

        return wCompositionSnapshots
                .toArray(new ComponentsSetBean[wCompositionSnapshots.size()]);
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
     * @see org.psem2m.isolates.services.monitoring.IIsolatePresenceListener#
     * handleIsolatePresence(java.lang.String, java.lang.String,
     * org.psem2m.isolates
     * .services.monitoring.IIsolatePresenceListener.EPresence)
     */
    @Override
    public void handleIsolatePresence(final String aIsolateId,
            final String aNode, final EPresence aPresence) {

        switch (aPresence) {
        case REGISTERED: {
            // Ask the isolate for its factories
            ISignalSendResult result = null;

            for (int i = 0; i < 5; i++) {
                // Try 5 times max
                result = pSignalBroadcaster.send(
                        ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_DUMP,
                        null, aIsolateId);
                if (result != null
                        && result.getResults().containsKey(aIsolateId)) {
                    // Isolate answered...
                    break;
                }

                try {
                    // Wait a second...
                    Thread.sleep(1000);

                } catch (final InterruptedException e) {
                    // Interrupted: abort
                    return;
                }
            }

            if (result == null) {
                pLogger.logSevere(this, "handleIsolatePresence", "Isolate=",
                        aIsolateId, "node=", aNode, "didn't answered in time.");
                return;
            }

            // Extract results
            final Object[] rawResults = result.getResults().get(aIsolateId);
            if (rawResults == null || rawResults.length == 0) {
                pLogger.logSevere(this, "handleIsolatePresence",
                        "Invalid dump result for isolate=", aIsolateId,
                        "node=", aNode, "results=", rawResults);
                return;
            }

            // Get the first array result
            Object[] arrayResult = null;
            for (final Object rawResult : rawResults) {
                if (result != null && result.getClass().isArray()) {
                    arrayResult = (Object[]) rawResult;
                }
            }

            if (arrayResult == null) {
                pLogger.logSevere(this, "handleIsolatePresence",
                        "No dump (array) result returned by isolate=",
                        aIsolateId, "node=", aNode);
                return;
            }

            // Convert object array to string array
            final List<String> stringList = new ArrayList<String>();
            for (final Object rawFactory : arrayResult) {
                if (rawFactory != null) {
                    if (!(rawFactory instanceof CharSequence)) {
                        pLogger.logWarn(
                                this,
                                "handleIsolatePresence",
                                "Found something that is not a string. Isolate=",
                                aIsolateId, "node=", aNode, "value=",
                                rawFactory);
                    }

                    // Store the factory name
                    stringList.add((String) rawFactory);
                }
            }

            // Register all factories
            registerComponentsForIsolate(aIsolateId,
                    stringList.toArray(new String[0]));

            break;
        }

        case UNREGISTERED:
            // An isolate has been lost
            unregisterIsolate(aIsolateId);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String, org.psem2m.signals.ISignalData)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Extract the signal information
        final String signalSender = aSignalData.getSenderId();
        final Object signalContent = aSignalData.getSignalContent();

        // For Jabsorb results...
        final String[] stringContent = Utilities.getArray(signalContent,
                String.class);
        final ComponentBean[] componentsArray = Utilities.getArray(
                signalContent, ComponentBean.class);

        if (ComposerAgentSignals.SIGNAL_RESPONSE_HANDLES_COMPONENTS
                .equals(aSignalName)) {
            // An isolate can handle some components

            if (componentsArray != null) {
                // We have something that looks like an answer

                if (componentsArray.length == 0) {
                    // We were wrong...
                    return null;
                }

                // Register components capacities
                registerComponentsForIsolate(signalSender, componentsArray);
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
            unregisterIsolate(signalSender);

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY
                .equals(aSignalName)) {
            // An isolate has some new capacities
            if (stringContent != null) {

                registerComponentsForIsolate(signalSender, stringContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_REMOVE_FACTORY
                .equals(aSignalName)) {
            // An isolate lost some capacities
            if (stringContent != null) {

                unregisterComponentsForIsolate(signalSender, stringContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_COMPONENT_CHANGED
                .equals(aSignalName)) {

            // A component state changed
            if (signalContent instanceof Map) {
                handleComponentChangedSignal(signalSender,
                        (Map<String, Object>) signalContent);
            }
        }

        return null;
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
                    "; maybe they are optional...");
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
        notifyUpdate(aComponentsSetBean);

        // Send a signal with all components in an array
        pSignalBroadcaster.fireGroup(
                ComposerAgentSignals.SIGNAL_CAN_HANDLE_COMPONENTS, components,
                EBaseGroup.ALL);
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

            // Update the components set state
            aComposet.notifyInstantiationRequest(isolateId, isolateComponents);

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

                                pLogger.logInfo(
                                        this,
                                        "ComponentRequestTimeout",
                                        componentName,
                                        "Instantiation request timed out on isolate",
                                        isolateId);

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
            pSignalBroadcaster.fire(
                    ComposerAgentSignals.SIGNAL_INSTANTIATE_COMPONENTS,
                    isolateComponents, isolateId);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.IComposer#loadCompositionFile(java.lang.String)
     */
    @Override
    public ComponentsSetBean loadCompositionFile(final String aFileName) {

        IComposerConfigHandler configReader = null;

        for (final IComposerConfigHandler reader : pConfigReaders) {
            if (reader.canHandle(aFileName)) {
                configReader = reader;
                break;
            }
        }

        if (configReader == null) {
            // No reader found
            pLogger.logWarn(this, "loadCompositionFile",
                    "Can't find a configuration reader for", aFileName);
            return null;
        }

        // Load the composition
        return configReader.load(aFileName);
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

                } else {
                    pLogger.logInfo(this, "notifyComponentsRegistration",
                            "remaining=", composite.getRemainingComponents(),
                            "requested=", composite.getRequestedComponents());
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
     * Notifies listeners of the removal of a composition
     * 
     * @param aComponentsSetBean
     *            A components set
     */
    protected void notifyRemoval(final ComponentsSetBean aComponentsSetBean) {

        if (pCompositionListeners.length == 0) {
            // Nothing to do...
            return;
        }

        // Get the components set root name
        final String rootName = aComponentsSetBean.getRootName();

        for (final ICompositionListener listener : pCompositionListeners) {
            // Notify listeners
            try {
                listener.componentsSetRemoved(rootName);

            } catch (final Exception ex) {
                // Just log errors...
                pLogger.logWarn(this, "notifyRemoval",
                        "Error notifying a composition listener:", ex);
            }
        }
    }

    /**
     * Notifies listeners of a composition update
     * 
     * @param aRootComponentsSetBean
     *            Updated components set
     */
    protected void notifyUpdate(final ComponentsSetBean aRootComponentsSetBean) {

        if (pCompositionListeners.length == 0) {
            // Nothing to do...
            return;
        }

        // Make a snapshot of the components set
        final ComponentsSetBean snapshot = new ComponentsSetBean(
                aRootComponentsSetBean, null);

        for (final ICompositionListener listener : pCompositionListeners) {
            // Notify listeners
            try {
                listener.updateCompositionSnapshot(snapshot);

            } catch (final Exception ex) {
                // Just log errors...
                pLogger.logWarn(this, "notifyUpdate",
                        "Error notifying a composition listener:", ex);
            }
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

        final Set<String> isolateComponents;

        synchronized (pIsolatesCapabilities) {
            // Prepare the list of components associated to the isolate

            Set<String> mapComponents = pIsolatesCapabilities.get(aIsolateId);

            if (mapComponents == null) {
                mapComponents = new HashSet<String>();
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

        final Set<String> isolateComponents;

        synchronized (pIsolatesCapabilities) {
            // Prepare the list of components associated to the isolate

            Set<String> mapComponents = pIsolatesCapabilities.get(aIsolateId);

            if (mapComponents == null) {
                mapComponents = new HashSet<String>();
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

        } else {
            // The components set might not be instantiated yet
            for (final InstantiatingComposite waitingComposite : pWaitingComposites) {

                if (waitingComposite.getName().equals(
                        aComponentsSetBean.getName())) {

                    // Found !
                    pWaitingComposites.remove(waitingComposite);

                    // Send the event ECompositionEvent.REMOVE to the
                    // composition listeners
                    notifyRemoval(aComponentsSetBean);
                    return;
                }
            }

            // Unknown component set
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
            pSignalBroadcaster.fire(
                    ComposerAgentSignals.SIGNAL_STOP_COMPONENTS, components,
                    isolate);
        }

        // Extra signals : tell all agents to stop remaining components
        // Those ones may not even be running, so ignore errors...
        final String[] remainingComponents = composite.getRemainingComponents()
                .toArray(new String[0]);

        pSignalBroadcaster.fireGroup(
                ComposerAgentSignals.SIGNAL_STOP_COMPONENTS,
                remainingComponents, EBaseGroup.ALL);

        // Send the event ECompositionEvent.REMOVE to the Composition
        // listeners
        notifyRemoval(aComponentsSetBean);
    }

    /**
     * Removes components types from the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aComponentsTypes
     *            Component types no more supported by the isolate
     */
    protected synchronized void unregisterComponentsForIsolate(
            final String aIsolateId, final String[] aComponentsTypes) {

        final Set<String> isolateComponents = pIsolatesCapabilities
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

            // Update waiting composites states
            for (final InstantiatingComposite composite : pWaitingComposites) {

                // Update the components set types
                composite.lostComponentTypes(aIsolateId, aComponentsTypes);
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

    /**
     * Unregisters all components of the given isolate ID
     * 
     * @param aIsolateId
     *            The unregistered isolate ID
     */
    protected void unregisterIsolate(final String aIsolateId) {

        final Set<String> isolateCaps = pIsolatesCapabilities.get(aIsolateId);
        if (isolateCaps != null) {
            unregisterComponentsForIsolate(aIsolateId,
                    isolateCaps.toArray(new String[0]));
        }
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
            pLogger.logDebug(this, "updateInstantiatingCompositeStatus",
                    "No known composet given, composet=", composetName);
            return;
        }

        // Succeeded component instantiations
        final String[] instantiatedComponents = Utilities.getArray(
                aAgentResult.get(ComposerAgentSignals.RESULT_KEY_INSTANTIATED),
                String.class);

        if (instantiatedComponents == null) {
            // Nothing to do
            pLogger.logDebug(this, "updateInstantiatingCompositeStatus",
                    "No instantiated component");
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
        final String[] failedComponents = Utilities.getArray(
                aAgentResult.get(ComposerAgentSignals.RESULT_KEY_FAILED),
                String.class);

        // Cancel their timeouts
        for (final String failedComponentName : failedComponents) {
            cancelTimeout(failedComponentName);
        }

        if (failedComponents.length != 0) {
            pLogger.logWarn(this, "updateInstantiatingCompositeStatus",
                    "The following components couldn't be started :",
                    Arrays.toString(failedComponents));
        } else {
            pLogger.logDebug(this, "updateInstantiatingCompositeStatus",
                    "All components of", composetName, " have been started");
        }

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
            final ComponentsSetBean composet = loadCompositionFile("autorun_conf.js");
            if (composet != null) {
                instantiateComponentsSet(composet);
            }

        } catch (final Throwable e) {
            pLogger.logSevere(this, "Composer AutoRun",
                    "Error reading the 'autorun' composition", e);
        }
    }
}
