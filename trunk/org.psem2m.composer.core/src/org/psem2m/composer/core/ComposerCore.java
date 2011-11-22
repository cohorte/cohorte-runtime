/**
 * File:   ComposerCore.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.ComponentSnapshot;
import org.psem2m.composer.ComponentsSetSnapshot;
import org.psem2m.composer.CompositionSnapshot;
import org.psem2m.composer.EComponentState;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.ICompositionListener;
import org.psem2m.composer.agent.ComposerAgentSignals;
import org.psem2m.composer.config.IComposerConfigReader;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
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

    private final List<ICompositionListener> pCompositionListeners = new ArrayList<ICompositionListener>();

    /** Composer configuration reader */
    @Requires
    private IComposerConfigReader pConfigReader;

    /** Composites fully-instantiated */
    private final Map<String, InstantiatingComposite> pFullComposites = new HashMap<String, InstantiatingComposite>();

    /** Composites currently instantiating */
    private final Map<String, InstantiatingComposite> pInstantiatingComposites = new HashMap<String, InstantiatingComposite>();

    /** Maps isolates and components */
    private final Map<String, List<String>> pIsolatesCapabilities = new HashMap<String, List<String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** the list of roots */
    private final List<ComponentsSetBean> pRootsComponentsSetBean = new ArrayList<ComponentsSetBean>();

    /** The signals broadcaster */
    @Requires
    private ISignalBroadcaster pSignalBroadcaster;

    /** Composites waiting for full instantiation */
    private final List<InstantiatingComposite> pWaitingComposites = new ArrayList<InstantiatingComposite>();

    /**
     * Default constructor
     */
    public ComposerCore() {

        super();
    }

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            The bound service
     */
    @Bind
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

        ComponentsSetSnapshot wComponentsSetSnapshot = new ComponentsSetSnapshot(
                aComponentsSetBean);

        // populate the list of children
        for (ComponentsSetBean wChild : aComponentsSetBean.getComponentSets()) {

            wComponentsSetSnapshot.addChild(buildComponentsSetSnapshot(wChild));
        }
        // populate the list of components
        for (ComponentBean wComponent : aComponentsSetBean.getComponents()) {
            wComponentsSetSnapshot.addComponent(new ComponentSnapshot(
                    wComponent));
        }
        return wComponentsSetSnapshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.IComposer#getCompositionSnapshot()
     */
    @Override
    public synchronized List<CompositionSnapshot> getCompositionSnapshot() {

        // TODO : set a list to keep all the events since the snapshots
        // was created

        List<CompositionSnapshot> wCompositionSnapshots = new ArrayList<CompositionSnapshot>();

        for (ComponentsSetBean wComponentsSetBean : pRootsComponentsSetBean) {

            wCompositionSnapshots.add(new CompositionSnapshot(
                    buildComponentsSetSnapshot(wComponentsSetBean)));
        }
        return wCompositionSnapshots;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.IComposer#instantiateComponentsSet(org.psem2m.composer
     * .model.ComponentsSetBean)
     */
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
            if (signalContent instanceof Map<?, ?>) {

                updateInstantiatingCompositeStatus(
                        (Map<String, Object>) signalContent, signalSender);
            }

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_GONE
                .equals(aSignalName)) {
            // An isolate agent is gone
            pIsolatesCapabilities.remove(aSignalData.getIsolateSender());

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
    public void instantiateComponentsSet(
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
        final ComponentBean[] components = aComponentsSetBean.getComponents();
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

        // TODO : send the event ECompositionEvent.ADD to the Composition
        // listeners

        // Send a signal with all components in an array
        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.ISOLATES,
                ComposerAgentSignals.SIGNAL_CAN_HANDLE_COMPONENTS, components);
    }

    /**
     * Sends a signal to selected isolates to instantiate the components of the
     * given composite
     * 
     * @param aComposite
     *            The composite to instantiate
     * @param aComponentsRepartition
     *            The computed components repartition
     */
    protected void instantiateComponentsSet(final ComponentsSetBean aComposite,
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

            pSignalBroadcaster.sendData(isolateId,
                    ComposerAgentSignals.SIGNAL_INSTANTIATE_COMPONENTS,
                    isolateComponents);
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
    protected void notifyComponentsRegistration() {

        synchronized (pWaitingComposites) {

            final List<InstantiatingComposite> resolvedComposites = new ArrayList<InstantiatingComposite>();

            for (final InstantiatingComposite composite : pWaitingComposites) {

                final Map<String, ComponentBean[]> resolution = new HashMap<String, ComponentBean[]>();
                if (composite.resolve(pIsolatesCapabilities, resolution)) {
                    // The given composite can be instantiated
                    resolvedComposites.add(composite);

                    // Do the job
                    instantiateComponentsSet(composite.getBean(), resolution);
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

        // Time to try a new instantiation
        notifyComponentsRegistration();
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

        // Time to try a new instantiation
        notifyComponentsRegistration();
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

                    for (InstantiatingComposite wIC : pWaitingComposites) {
                        aCompositionListener.conponentsSetStateChanged(
                                wIC.getBean(), EComponentState.WAITING);
                    }

                    for (InstantiatingComposite wIC : pFullComposites.values()) {
                        aCompositionListener.conponentsSetStateChanged(
                                wIC.getBean(), EComponentState.COMPLETE);
                    }
                    for (InstantiatingComposite wIC : pInstantiatingComposites
                            .values()) {
                        aCompositionListener.conponentsSetStateChanged(
                                wIC.getBean(), EComponentState.INSTANCIATING);
                    }
                }
            }
        }

        // TODO : send all the events kept after the TimeStamp
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.IComposer#removeComponentsSet(org.psem2m.composer
     * .model.ComponentsSetBean)
     */
    @Override
    public void removeComponentsSet(final ComponentsSetBean aComponentsSetBean)
            throws Exception {

        if (!aComponentsSetBean.isRoot()) {
            // Invalid composite
            pLogger.logSevere(this, "removeComponentsSet",
                    "Can't instantiate a ComponentsSet which is not root");
            return;
        }

        // register the aComponentsSetBean in the list of roots
        pRootsComponentsSetBean.remove(aComponentsSetBean);

        // TODO : clear the part of the composition correspondint to that root
        // ComponentsSet ....

        // TODO : send the event ECompositionEvent.REMOVE to the Composition
        // listeners

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

        if (isolateComponents == null) {
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
        }

        if (needsNewResolution) {
            // Try to recompute a route for degraded composites
            notifyComponentsRegistration();
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
        final String compositeName = (String) aAgentResult
                .get(ComposerAgentSignals.RESULT_KEY_COMPOSITE);

        // Get the corresponding composite
        final InstantiatingComposite composite = pInstantiatingComposites
                .get(compositeName);

        // Unknown composite
        if (composite == null) {
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

            composite.componentStarted(componentName, aHostIsolate);
        }

        // Composite status is good
        if (composite.isComplete()) {

            pFullComposites.put(composite.getName(), composite);

            // Nothing more to do
            return;
        }

        // Failed instantiations
        final String[] failedComponents = (String[]) aAgentResult
                .get(ComposerAgentSignals.RESULT_KEY_FAILED);

        pLogger.logWarn(this, "updateInstantiatingCompositeStatus",
                "The following components couldn't be started :",
                Arrays.toString(failedComponents));
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
