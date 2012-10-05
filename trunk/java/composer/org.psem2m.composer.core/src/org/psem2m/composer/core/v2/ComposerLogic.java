/**
 * File:   ComposerLogic.java
 * Author: Thomas Calmant
 * Date:   29 août 2012
 */
package org.psem2m.composer.core.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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
import org.psem2m.composer.EComponentState;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.ICompositionListener;
import org.psem2m.composer.InvalidComponentsSetException;
import org.psem2m.composer.agent.ComposerAgentSignals;
import org.psem2m.composer.config.IComposerConfigHandler;
import org.psem2m.composer.core.InstantiatingComposite;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalSendResult;

/**
 * The composer core logic
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-logic-factory", publicFactory = false)
@Provides(specifications = { IComposer.class, IComposerLogic.class }, properties = @StaticServiceProperty(name = "service.exported.interfaces", value = "org.psem2m.composer.IComposer", type = "String"))
@Instantiate(name = "psem2m-composer-logic")
public class ComposerLogic implements IComposer, IComposerLogic {

    /** Composition listeners requirement ID */
    private static final String LISTENERS_ID = "composition-listeners";

    /** List of composition events listener */
    @Requires(id = LISTENERS_ID, optional = true)
    private ICompositionListener[] pCompositionListeners;

    /** Composer configuration readers */
    @Requires
    private IComposerConfigHandler[] pConfigReaders;

    /** The component instantiation time out (in milliseconds) */
    @Property(name = "component-instantiation-timeout", value = "5000")
    private long pInstantiationTimeout;

    /** Executor to notify composition listeners */
    private ExecutorService pListenersExecutor;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The requests timeouts futures */
    private final Map<String, ScheduledFuture<?>> pRequestsTimeouts = new HashMap<String, ScheduledFuture<?>>();

    /**
     * The delay to wait after receiving a factory message before trying a new
     * resolution (in milliseconds)
     */
    @Property(name = "resolution-delay", value = "500")
    private long pResolutionDelay;

    /** The resolution future */
    private ScheduledFuture<?> pResolutionFuture;

    /** The resolution runner */
    private final Runnable pResolutionRunner = new Runnable() {

        @Override
        public void run() {

            // Call for a new resolution
            resolveComposets();
        }
    };

    /** Scheduler executor for timeouts */
    private ScheduledExecutorService pScheduler;

    /** The signals broadcaster */
    @Requires
    private ISignalBroadcaster pSignalBroadcaster;

    /** The composer status */
    @Requires
    private IComposerStatus pStatus;

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
     * Cancels the scheduled resolution, if any
     * 
     * @return true if a resolution was scheduled
     */
    private synchronized boolean cancelResolution() {

        // Cancel current run
        if (pResolutionFuture != null) {
            pResolutionFuture.cancel(false);
            pResolutionFuture = null;
            return true;
        }

        return false;
    }

    /**
     * Cancels the timeout for the given component, if possible
     * 
     * @param aComponentName
     *            A component name
     */
    private void cancelTimeout(final String aComponentName) {

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
    private synchronized void delayResolution() {

        cancelResolution();

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
    public ComponentsSetBean[] getCompositionSnapshot() {

        return pStatus.getSnapshot();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerLogic#handleComponentEvent(java.
     * lang.String, java.lang.String, org.psem2m.composer.EComponentState)
     */
    @Override
    public synchronized void handleComponentEvent(final String aIsolateId,
            final String aComponentName, final EComponentState aState) {

        if (aComponentName == null || aComponentName.isEmpty()) {
            // Invalid parameters
            return;
        }

        switch (aState) {
        case INSTANTIATING:
        case COMPLETE:
            // Stop the component timeout
            cancelTimeout(aComponentName);
            break;

        case REMOVED:
            handleLostComponent(aComponentName);
            break;

        default:
            // Do nothing
            pLogger.logWarn(this, "handleComponentEvent", "Unhandled state=",
                    aState, "for component=", aComponentName, "on isolate=",
                    aIsolateId);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerLogic#handleFactoriesState(java.
     * lang.String, java.lang.String[],
     * org.psem2m.composer.core.v2.EFactoryState)
     */
    @Override
    public synchronized void handleFactoriesState(final String aIsolateId,
            final String[] aFactories, final EFactoryState aState) {

        if (aIsolateId == null || aIsolateId.isEmpty() || aFactories == null
                || aFactories.length == 0) {
            // Nothing to do
            return;
        }

        // Cancel the current resolution
        boolean needsResolution = cancelResolution();

        switch (aState) {
        case REGISTERED:
            needsResolution |= registerFactories(aIsolateId, aFactories);
            break;

        case UNREGISTERED:
            needsResolution |= unregisterFactories(aIsolateId, aFactories);
            break;

        default:
            pLogger.logWarn(this, "handleFactoryState", "Unhandled state=",
                    aState, "for factories=", aFactories, "on isolate=",
                    aIsolateId);
            break;
        }

        if (needsResolution) {
            // Schedule a new resolution
            delayResolution();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerLogic#handleInstantiationResult(
     * java.lang.String, java.lang.String, java.lang.String[],
     * java.lang.String[])
     */
    @Override
    public synchronized void handleInstantiationResult(final String aIsolateId,
            final String aComposetName, final String[] aInstantiatedComponents,
            final String[] aFailedComponents) {

        // Get the corresponding components set
        final InstantiatingComposite composet = pStatus
                .getComposet(aComposetName);
        if (composet == null) {
            pLogger.logWarn(this, "handleInstantiationResult",
                    "Unknown components set=", aComposetName, "from isolate=",
                    aIsolateId);
            return;
        }

        if (aInstantiatedComponents == null
                || aInstantiatedComponents.length == 0) {
            // No component instantiated
            pLogger.logWarn(this, "handleInstantiationResult",
                    "No component instantiated for set=", aComposetName,
                    "on isolate=", aIsolateId);
            return;
        }

        // Stop the resolution
        boolean needsResolution = cancelResolution();

        // Update the state of the components set
        for (final String componentName : aInstantiatedComponents) {

            // Cancel its timeout
            cancelTimeout(componentName);
            composet.componentStarted(componentName, aIsolateId);
        }

        // Composite status is good
        if (composet.isComplete()) {

            // Update the components set state
            composet.getBean().updateState(EComponentState.COMPLETE);
            pStatus.composetComplete(composet.getName());
        }

        // Call listeners
        notifyUpdate(composet.getBean());

        // Handle Failed instantiations
        if (aFailedComponents != null && aFailedComponents.length != 0) {

            for (final String failedComponentName : aFailedComponents) {
                cancelTimeout(failedComponentName);
            }

            pLogger.logWarn(this, "handleInstantiationResult",
                    "The following components couldn't be started on",
                    aIsolateId, ":", Arrays.toString(aFailedComponents));

            // Ask for a new resolution
            needsResolution = true;

        } else {
            // No component failed
            pLogger.logDebug(this, "handleInstantiationResult",
                    "All components of", aComposetName, "on", aIsolateId,
                    "have been started");
        }

        if (needsResolution) {
            // Schedule a new resolution
            delayResolution();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerLogic#handleIsolateGone(java.lang
     * .String)
     */
    @Override
    public synchronized void handleIsolateGone(final String aIsolateId) {

        // Flag to compute a new composition only if necessary
        // -> Stop the current resolution at first
        boolean needsResolution = cancelResolution();

        // Get the isolate factories
        final String[] factories = pStatus.getIsolateFactories(aIsolateId);

        // Update the composer status
        pStatus.removeIsolateFactories(aIsolateId);

        // Update the components sets
        final InstantiatingComposite[] knownComposets = pStatus.getComposets();
        if (knownComposets != null) {
            for (final InstantiatingComposite composet : knownComposets) {

                final String composetName = composet.getName();

                // Update the components set
                composet.lostComponentTypes(aIsolateId, factories);

                // Test if it is still complete if it was running
                if (pStatus.isComposetActive(composetName)
                        && !composet.isComplete()) {
                    // Components set goes in the Waiting state
                    pStatus.composetWaiting(composetName);

                    // A new resolution is needed
                    needsResolution = true;
                }
            }
        }

        if (needsResolution) {
            // Recompute a resolution if needed
            delayResolution();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerLogic#handleIsolateReady(java.lang
     * .String)
     */
    @Override
    public synchronized void handleIsolateReady(final String aIsolateId) {

        // Ask the isolate for its factories
        final ISignalSendResult result = pSignalBroadcaster.send(
                ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_DUMP, null,
                aIsolateId);
        if (result == null) {
            pLogger.logSevere(this, "handleIsolateReady", "Isolate=",
                    aIsolateId, "didn't answered.");
            return;
        }

        // Extract results
        final Object[] rawResults = result.getResults().get(aIsolateId);
        if (rawResults == null || rawResults.length == 0) {
            // No factories ?
            pLogger.logDebug(this, "handleIsolateReady", "isolate=",
                    aIsolateId, "returned no result");
            return;
        }

        // Get the first array result
        Object[] arrayResult = null;
        for (final Object rawResult : rawResults) {
            if (rawResult instanceof Object[]) {
                arrayResult = (Object[]) rawResult;
            }
        }
        if (arrayResult == null) {
            pLogger.logSevere(this, "handleIsolateReady",
                    "No dump (array) result returned by isolate=", aIsolateId);
            return;
        }

        // Convert object array to string array
        final List<String> stringList = new ArrayList<String>();
        for (final Object rawFactory : arrayResult) {
            if (rawFactory != null) {
                if (!(rawFactory instanceof CharSequence)) {
                    pLogger.logWarn(this, "handleIsolateReady",
                            "Found something that is not a string. Isolate=",
                            aIsolateId, "class=", rawFactory.getClass()
                                    .getName(), "value=", rawFactory);
                }

                // Store the factory name
                stringList.add((String) rawFactory);
            }
        }

        // Register all factories
        if (pStatus.registerFactories(aIsolateId,
                stringList.toArray(new String[stringList.size()]))) {
            // New factories found, ask for a new resolution
            delayResolution();
        }
    }

    /**
     * Handles a lost component
     * 
     * @param aComponentName
     *            The lost component name
     */
    private void handleLostComponent(final String aComponentName) {

        // Retrieve all known components sets
        final InstantiatingComposite[] composets = pStatus.getComposets();
        if (composets != null) {

            // Stop the resolution schedule
            cancelResolution();

            for (final InstantiatingComposite composet : composets) {
                // Update the factories
                composet.componentStopped(aComponentName);

                // Update the completion level if needed
                if (!composet.isComplete()) {
                    pStatus.composetWaiting(composet.getName());
                }
            }

            // Ask for a new resolution
            delayResolution();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.core.v2.IComposerLogic#handleRunningComponents(java
     * .lang.String, org.psem2m.composer.model.ComponentBean[])
     */
    @Override
    public synchronized void handleRunningComponents(final String aIsolateId,
            final ComponentBean[] aRunningComponents) {

        if (aRunningComponents == null) {
            // Nothing to do...
            return;
        }

        // Stop the resolution schedule
        final boolean needsResolution = cancelResolution();

        final Set<ComponentsSetBean> updatedComposets = new HashSet<ComponentsSetBean>();
        for (final ComponentBean component : aRunningComponents) {
            // Get the composition
            final String composetName = component.getRootName();
            final InstantiatingComposite composet = pStatus
                    .getComposet(composetName);

            if (composet == null) {
                // Not for us...
                pLogger.logWarn(this, "handleRunningComponents",
                        "Unknown composet=", composetName, "for component=",
                        component.getName());

            } else {
                // Update the composition (request + success)
                composet.notifyInstantiationRequest(aIsolateId,
                        new ComponentBean[] { component });
                composet.componentStarted(component, aIsolateId);

                // Keep the composition in the update list
                updatedComposets.add(composet.getBean());
            }
        }

        if (needsResolution) {
            // Ask for a new resolution
            delayResolution();
        }

        // Update listeners
        for (final ComponentsSetBean composet : updatedComposets) {
            notifyUpdate(composet);
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
            final ComponentsSetBean aComponentsSetBean)
            throws InvalidComponentsSetException {

        if (aComponentsSetBean == null) {
            // Invalid composite
            pLogger.logSevere(this, "instantiateComponentsSet",
                    "Null components set given");
            throw new InvalidComponentsSetException("Null components set");
        }

        if (!aComponentsSetBean.isRoot()) {
            // Invalid composite
            pLogger.logSevere(this, "instantiateComponentsSet",
                    "Can't instantiate a ComponentsSet which is not root");
            throw new InvalidComponentsSetException("Not a root components set");
        }

        // Get the components
        final ComponentBean[] components = aComponentsSetBean
                .getAllComponents();
        if (components == null || components.length == 0) {
            // Invalid components list
            pLogger.logSevere(this, "instantiateComponentsSet",
                    "Empty components set");
            throw new InvalidComponentsSetException("Empty components set");
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

        // Prepare the components set bean
        final InstantiatingComposite instComposet = new InstantiatingComposite(
                aComponentsSetBean);

        // Store the components set in the waiting list
        pStatus.addWaitingComposet(instComposet);

        // Notify listeners
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
    private synchronized void instantiateComponentsSet(
            final InstantiatingComposite aComposet,
            final Map<String, ComponentBean[]> aComponentsRepartition) {

        for (final Entry<String, ComponentBean[]> isolateEntry : aComponentsRepartition
                .entrySet()) {

            final String isolateId = isolateEntry.getKey();
            final ComponentBean[] isolateComponents = isolateEntry.getValue();

            if (isolateComponents == null || isolateComponents.length == 0) {
                // Ignore empty components arrays
                pLogger.logInfo(this, "instantiateComponentsSet",
                        "Nothing to send to isolate=", isolateId,
                        "for components set=", aComposet.getName());
                continue;
            }

            // Update the components set state
            aComposet.notifyInstantiationRequest(isolateId, isolateComponents);

            // Prepare the timeouts calls
            for (final ComponentBean component : isolateComponents) {

                // Cancel the previous timeout, just in case
                final String componentName = component.getName();
                cancelTimeout(componentName);

                // Prepare the future
                final ScheduledFuture<?> future = pScheduler.schedule(
                        new Runnable() {

                            @Override
                            public void run() {

                                // Do not resolve while handling a timeout
                                cancelResolution();

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

                                // Go back to the waiting state
                                if (!pStatus.isComposetWaiting(componentName)) {
                                    pStatus.composetWaiting(aComposet.getName());
                                }

                                // Ask for a new resolution
                                delayResolution();
                            }
                        }, pInstantiationTimeout, TimeUnit.MILLISECONDS);

                // Store the future
                pRequestsTimeouts.put(componentName, future);
            }

            // Notify listeners
            notifyUpdate(aComposet.getBean());

            // Send the instantiation signal
            pSignalBroadcaster.fire(
                    ComposerAgentSignals.SIGNAL_INSTANTIATE_COMPONENTS,
                    isolateComponents, isolateId);
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Stop the scheduler
        pScheduler.shutdownNow();
        pScheduler = null;

        // Stop the notifier
        pListenersExecutor.shutdownNow();
        pListenersExecutor = null;

        pLogger.logInfo(this, "invalidate", "Composer Logic gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.IComposer#loadCompositionFile(java.lang.String)
     */
    @Override
    public ComponentsSetBean loadCompositionFile(final String aFileName) {

        // Find the corresponding configuration reader
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
     * Notifies listeners of the removal of a composition in a new thread
     * 
     * @param aComponentsSetBean
     *            A components set
     */
    private void notifyRemoval(final ComponentsSetBean aComponentsSetBean) {

        // Execute in another thread
        pListenersExecutor.execute(new Runnable() {

            @Override
            public void run() {

                notifyRemovalThread(aComponentsSetBean);
            }
        });
    }

    /**
     * Notifies listeners of the removal of a composition
     * 
     * @param aComponentsSetBean
     *            A components set
     */
    private void notifyRemovalThread(final ComponentsSetBean aComponentsSetBean) {

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
     * Notifies listeners of a composition update in a new thread
     * 
     * @param aRootComponentsSetBean
     *            Updated components set
     */
    private void notifyUpdate(final ComponentsSetBean aRootComponentsSetBean) {

        // Execute in another thread
        pListenersExecutor.execute(new Runnable() {

            @Override
            public void run() {

                notifyUpdateThread(aRootComponentsSetBean);
            }
        });
    }

    /**
     * Notifies listeners of a composition update
     * 
     * @param aRootComponentsSetBean
     *            Updated components set
     */
    private void notifyUpdateThread(
            final ComponentsSetBean aRootComponentsSetBean) {

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
    private boolean prepareComponentsSet(final ComponentsSetBean aComponentsSet) {

        if (aComponentsSet == null) {
            pLogger.logWarn(this, "prepareComponentsSet", "Null components set");
            return false;
        }

        aComponentsSet.computeName();
        return aComponentsSet.linkWires(null);
    }

    /**
     * Associates the given factories with the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @param aFactories
     *            Component factories supported by the isolate
     * @return True if a new resolution must be scheduled
     */
    private boolean registerFactories(final String aIsolateId,
            final String[] aFactories) {

        // Update the status storage
        return pStatus.registerFactories(aIsolateId, aFactories);
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
            final ComponentsSetBean aComponentsSetBean)
            throws InvalidComponentsSetException {

        if (!aComponentsSetBean.isRoot()) {
            // Invalid components set
            throw new InvalidComponentsSetException(
                    "Can't instantiate a ComponentsSet which is not root");
        }

        // Get the name of the components set
        final String composetName = aComponentsSetBean.getName();

        // Get the InstantiatingComposite object
        final InstantiatingComposite composet = pStatus
                .getComposet(composetName);
        if (composet == null) {
            // Unknown component set
            pLogger.logWarn(this, "removeComponentsSet",
                    "Unknown components set :", aComponentsSetBean);
            return;
        }

        // Get its state
        final EComposetState composetState = pStatus
                .getComposetState(composetName);

        // Remove it from the status
        pStatus.removeComposet(composetName);

        if (composetState != EComposetState.WAITING) {
            // Components might have been instantiated
            sendStopSignals(composet);
        }

        // Notify listeners of the removal
        notifyRemoval(aComponentsSetBean);
    }

    /**
     * Called when some components has been registered for one or more isolates.
     * Tests if a composite can be fully instantiated and does the job if
     * possible.
     */
    private synchronized void resolveComposets() {

        // Get the waiting components sets
        final InstantiatingComposite[] waitingComposets = pStatus
                .getWaitingComposets();
        if (waitingComposets == null) {
            // Nothing to do
            return;
        }

        // Get all known factories for all isolates
        final Map<String, Set<String>> isolatesFactories = pStatus
                .getIsolatesFactories();

        // Try to instantiate sets
        final List<String> resolvedComposets = new ArrayList<String>();
        for (final InstantiatingComposite composet : waitingComposets) {

            final Map<String, ComponentBean[]> resolution = new HashMap<String, ComponentBean[]>();
            if (composet.resolve(isolatesFactories, resolution)) {
                // The given composite can be instantiated
                resolvedComposets.add(composet.getName());

                // Notify listeners
                notifyUpdate(composet.getBean());

                // Do the job
                instantiateComponentsSet(composet, resolution);

            } else {
                pLogger.logInfo(this, "notifyComponentsRegistration",
                        "Incomplete componenents set=", composet.getName(),
                        "remaining=", composet.getRemainingComponents(),
                        "requested=", composet.getRequestedComponents());

                // Notify listeners
                notifyUpdate(composet.getBean());
            }
        }

        // Updates the state of the resolved components sets
        for (final String composetName : resolvedComposets) {
            pStatus.composetInstantiating(composetName);
        }
    }

    /**
     * Sends signals to Composer agents to stop the given components set
     * 
     * @param aComposet
     *            A components set
     */
    private void sendStopSignals(final InstantiatingComposite aComposet) {

        // Send stop signals to agents
        for (final Entry<String, List<String>> entry : aComposet
                .getRunningComponents().entrySet()) {

            final String isolate = entry.getKey();
            final String[] components = entry.getValue().toArray(new String[0]);

            // Tell the agent in the isolate to stop the given component
            pSignalBroadcaster.fire(
                    ComposerAgentSignals.SIGNAL_STOP_COMPONENTS, components,
                    isolate);
        }

        // Extra signals : tell all agents to stop remaining components
        // Those ones may not even be running, so ignore errors...
        final String[] remainingComponents = aComposet.getRemainingComponents()
                .toArray(new String[0]);

        pSignalBroadcaster.fireGroup(
                ComposerAgentSignals.SIGNAL_STOP_COMPONENTS,
                remainingComponents, EBaseGroup.ALL);
    }

    /**
     * Handles the unregistration of factories on an isolate
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aFactories
     *            The unregistered factories
     * @return True if a new resolution must be scheduled
     */
    private synchronized boolean unregisterFactories(final String aIsolateId,
            final String[] aFactories) {

        if (!pStatus.unregisterFactories(aIsolateId, aFactories)) {
            // Factories were unknown
            pLogger.logDebug(this, "unregisterFactories",
                    "Unknown unregistered factories=", aFactories,
                    "on isolate=", aIsolateId);
            return false;
        }

        // Flag indicating if a new composite resolution is needed
        boolean needsNewResolution = false;

        // Retrieve all known components sets
        final InstantiatingComposite[] composets = pStatus.getComposets();
        if (composets == null) {
            // Nothing to do
            return false;
        }

        for (final InstantiatingComposite composet : composets) {
            // Update the factories
            composet.lostComponentTypes(aIsolateId, aFactories);

            // Update the completion level if needed
            if (!composet.isComplete()) {
                pStatus.composetWaiting(composet.getName());

                // A new resolution is needed
                needsNewResolution = true;
            }
        }

        return needsNewResolution;
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Listeners executor
        pListenersExecutor = Executors.newFixedThreadPool(1);

        // Scheduled executor
        pScheduler = Executors.newScheduledThreadPool(1);

        pLogger.logInfo(this, "validate", "Composer Logic ready");

        // Composition auto-run
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
