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
import org.psem2m.composer.ComposerAgentSignals;
import org.psem2m.composer.CompositeBean;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.model.ComponentBean;
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

    /** Composites fully-instantiated */
    private final Map<String, InstantiatingComposite> pFullComposites = new HashMap<String, InstantiatingComposite>();

    /** Composites currently instantiating */
    private final Map<String, InstantiatingComposite> pInstantiatingComposites = new HashMap<String, InstantiatingComposite>();

    /** Maps isolates and components */
    private final Map<String, List<String>> pIsolatesCapabilities = new HashMap<String, List<String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

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
     * org.psem2m.composer.IComposer#instantiateComposite(org.psem2m.composer
     * .Composite)
     */
    @Override
    public void instantiateComposite(final CompositeBean aComposite) {

        if (aComposite == null) {
            // Invalid composite
            return;
        }

        // Get the components
        final ComponentBean[] components = aComposite.getComponents();
        if (components == null || components.length == 0) {
            // Invalid components list
            return;
        }

        // Add the component to the waiting list
        pWaitingComposites.add(new InstantiatingComposite(aComposite));

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
    protected void instantiateComposite(final CompositeBean aComposite,
            final Map<String, ComponentBean[]> aComponentsRepartition) {

        for (final Entry<String, ComponentBean[]> isolateEntry : aComponentsRepartition
                .entrySet()) {

            final String isolateId = isolateEntry.getKey();
            final ComponentBean[] isolateComponents = isolateEntry.getValue();

            if (isolateComponents == null || isolateComponents.length == 0) {
                // Ignore empty components arrays
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
                    instantiateComposite(composite.getBean(), resolution);
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

    private void test() {

        final CompositeBean compositeTest = new CompositeBean();
        compositeTest.setName("Composite-Test");

        final ComponentBean compoProvider = new ComponentBean();
        compoProvider.setName("hello-1");
        compoProvider.setType("hello-test");
        compoProvider.setIsolate("isolate-1");
        compoProvider.setFieldFilter("logger", null);
        compositeTest.addComponent(compoProvider);

        final ComponentBean compoConsumer = new ComponentBean();
        compoConsumer.setName("hello-consumer");
        compoConsumer.setType("hello-consumer-test");
        compoConsumer.setIsolate("isolate-2");
        compoConsumer.setFieldFilter("logger", null);
        compositeTest.addComponent(compoConsumer);

        // Fire at will
        instantiateComposite(compositeTest);
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

        test();
    }
}
