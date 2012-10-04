/**
 * File:   ComponentMonitor.java
 * Author: Thomas Calmant
 * Date:   29 août 2012
 */
package org.psem2m.composer.core.v2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.composer.EComponentState;
import org.psem2m.composer.agent.ComposerAgentSignals;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Component monitor
 * 
 * Notifies the composer of components signals
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-monitor-factory", publicFactory = false)
@Provides(specifications = IIsolatePresenceListener.class)
@Instantiate(name = "psem2m-composer-monitor")
public class ComponentMonitor implements ISignalListener,
        IIsolatePresenceListener {

    /** The composer logic */
    @Requires
    private IComposerLogic pComposer;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

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

        aSignalReceiver.registerListener(
                ComposerAgentSignals.SIGNAL_COMPONENT_CHANGED, this);

        pLogger.logInfo(this, "bindSignalReceiver",
                "Bound to a signal receiver");
    }

    /**
     * Extracts the different factories corresponding the given components
     * 
     * @param aComponentsArray
     *            An array of components descriptions
     * @return All factories found in the descriptions, or null
     */
    private String[] getFactories(final ComponentBean[] aComponentsArray) {

        if (aComponentsArray == null || aComponentsArray.length == 0) {
            // Nothing to do
            return null;
        }

        // Get factories
        final Set<String> factories = new HashSet<String>(
                aComponentsArray.length);
        for (final ComponentBean component : aComponentsArray) {
            factories.add(component.getType());
        }

        // Return an array
        return factories.toArray(new String[factories.size()]);
    }

    /**
     * Handles a component changed signal
     * 
     * @param aIsolateId
     *            ID of the sender
     * @param aStateMap
     *            The signal content
     */
    private void handleComponentChangedSignal(final String aIsolateId,
            final Map<String, Object> aStateMap) {

        // Get the component name
        final String componentName = (String) aStateMap
                .get(ComposerAgentSignals.COMPONENT_CHANGED_KEY_NAME);
        if (componentName == null || componentName.isEmpty()) {
            // Invalid data
            pLogger.logWarn(this, "handleComponentChangedSignal",
                    "No component name found");
            return;
        }

        // Get the new state
        final EComponentState state = (EComponentState) aStateMap
                .get(ComposerAgentSignals.COMPONENT_CHANGED_KEY_STATE);
        if (state == null) {
            // Invalid data
            pLogger.logWarn(this, "handleComponentChangedSignal",
                    "No component state found");
            return;
        }

        // Call the composer logic
        pComposer.handleComponentEvent(aIsolateId, componentName, state);
    }

    private void handleComponentHandlingSignal(final String aSenderId,
            final Map<String, Object> aSignalContent) {

        // Extract arrays
        final ComponentBean[] handledComponents = Utilities.arrayObjectToArray(
                aSignalContent.get("handled"), ComponentBean.class);
        final ComponentBean[] runningComponents = Utilities.arrayObjectToArray(
                aSignalContent.get("running"), ComponentBean.class);

        // Update the components state
        if (runningComponents != null) {
            String composetName = null;
            for (final ComponentBean bean : runningComponents) {
                if (composetName == null) {
                    // Find the root components set name
                    composetName = bean.getRootName();
                }
            }

            if (composetName != null) {
                /*
                 * TODO: notify the composer logic of running components
                 * Problem: the composition might not even be in 'instantiating'
                 * when this method is executed
                 */
            }
        }

        // Update the factories state
        pComposer.handleFactoriesState(aSenderId,
                getFactories(handledComponents), EFactoryState.REGISTERED);
    }

    /**
     * Handles the result of an instantiation, received from an agent
     * 
     * @param aIsolateID
     *            ID of the sender
     * @param aAgentResult
     *            The agent result
     */
    private void handleInstantiationResult(final String aIsolateID,
            final Map<String, Object> aAgentResult) {

        // Working components set
        final String composetName = (String) aAgentResult
                .get(ComposerAgentSignals.RESULT_KEY_COMPOSITE);
        if (composetName == null || composetName.isEmpty()) {
            // No components set name: no possible action
            pLogger.logWarn(this, "handleInstantiationResult",
                    "Invalid components set name=", composetName);
            return;
        }

        // Succeeded component instantiations
        final String[] instantiatedComponents = Utilities.getArray(
                aAgentResult.get(ComposerAgentSignals.RESULT_KEY_INSTANTIATED),
                String.class);

        // Failed instantiations
        final String[] failedComponents = Utilities.getArray(
                aAgentResult.get(ComposerAgentSignals.RESULT_KEY_FAILED),
                String.class);

        // Call the composer logic
        pComposer.handleInstantiationResult(aIsolateID, composetName,
                instantiatedComponents, failedComponents);
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
        case REGISTERED:
            pComposer.handleIsolateReady(aIsolateId);
            break;

        case UNREGISTERED:
            pComposer.handleIsolateGone(aIsolateId);
            break;

        default:
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalListener#handleReceivedSignal(java.lang.String,
     * org.psem2m.signals.ISignalData)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Extract the signal information
        final String senderId = aSignalData.getSenderId();
        final Object signalContent = aSignalData.getSignalContent();

        // Decode Jabsorb arrays
        final String[] stringContent = Utilities.getArray(signalContent,
                String.class);

        /* Factory signals */
        if (ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY.equals(aSignalName)) {
            // An isolate has some new factories
            pComposer.handleFactoriesState(senderId, stringContent,
                    EFactoryState.REGISTERED);

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_REMOVE_FACTORY
                .equals(aSignalName)) {
            // An isolate lost some factories
            pComposer.handleFactoriesState(senderId, stringContent,
                    EFactoryState.UNREGISTERED);

        } else if (ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_GONE
                .equals(aSignalName)) {
            // An isolate agent is gone
            pComposer.handleIsolateGone(senderId);
        }

        /* Component signals */
        else if (ComposerAgentSignals.SIGNAL_RESPONSE_HANDLES_COMPONENTS
                .equals(aSignalName)) {
            // An isolate can handle some components
            if (signalContent instanceof Map) {
                handleComponentHandlingSignal(senderId,
                        (Map<String, Object>) signalContent);

            } else {
                pLogger.logWarn(this, "handleReceivedSignal",
                        "Unhandled response for a 'can handle' signal content=",
                        signalContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS
                .equals(aSignalName)) {

            // An isolate started some components
            if (signalContent instanceof Map) {
                handleInstantiationResult(senderId,
                        (Map<String, Object>) signalContent);

            } else {
                pLogger.logWarn(this, "handleReceivedSignal",
                        "Unhandled instantiation result content=",
                        signalContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_COMPONENT_CHANGED
                .equals(aSignalName)) {
            // A component state changed
            if (signalContent instanceof Map) {
                handleComponentChangedSignal(senderId,
                        (Map<String, Object>) signalContent);

            } else {
                pLogger.logWarn(this, "handleReceivedSignal",
                        "Unhandled component changed signal content=",
                        signalContent);
            }
        }

        // Nothing to return
        return null;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        pLogger.logInfo(this, "invalidate", "Component Monitor gone");
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        pLogger.logInfo(this, "validate", "Component Monitor ready");
    }
}
