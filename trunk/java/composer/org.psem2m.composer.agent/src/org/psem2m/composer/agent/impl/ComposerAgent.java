/**
 * File:   ComposerAgent.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.agent.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleException;
import org.psem2m.composer.ECompositionEvent;
import org.psem2m.composer.agent.ComposerAgentConstants;
import org.psem2m.composer.agent.ComposerAgentSignals;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Implementation of a composer agent
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-agent-factory", publicFactory = false)
@Instantiate(name = "psem2m-composer-agent")
public class ComposerAgent extends CPojoBase implements ISignalListener,
        InstanceStateListener {

    /** iPOJO signal broadcaster dependency ID */
    private static final String IPOJO_ID_BROADCASTER = "signals-broadcaster";

    /** iPOJO factories dependency ID */
    private static final String IPOJO_ID_FACTORIES = "ipojo-factories";

    /** Components instances */
    private final Map<String, ComponentInstance> pComponentsInstances = new HashMap<String, ComponentInstance>();

    /** Local factories */
    private final Map<String, Factory> pFactories = new HashMap<String, Factory>();

    /** Maps fields names and IDs for each component type */
    private final Map<String, Map<String, String>> pFactoriesFieldsIds = new HashMap<String, Map<String, String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Platform properties */
    @Requires
    private IPlatformDirsSvc pPlatformDirs;

    /** Signal broadcaster */
    @Requires(id = IPOJO_ID_BROADCASTER)
    private ISignalBroadcaster pSignalBroadcaster;

    /**
     * Default constructor
     */
    public ComposerAgent() {

        super();
    }

    /**
     * Called by iPOJO when a Factory service is bound
     * 
     * @param aFactory
     *            A new factory service
     */
    @Bind(id = IPOJO_ID_FACTORIES, aggregate = true, optional = true)
    protected void bindFactory(final Factory aFactory) {

        // Store the factory name (component type name)
        final String factoryName = aFactory.getName();

        // Prepare a field - ID map and attach it to the component type
        final Map<String, String> fieldIdMap = new HashMap<String, String>();
        pFactoriesFieldsIds.put(factoryName, fieldIdMap);

        // Set up the map content
        final Element componentModel = aFactory.getComponentMetadata();

        // @Requires elements IDs
        final Element[] requiresElems = componentModel
                .getElements(IpojoConstants.REQUIRES_ELEMENT_NAME);
        if (requiresElems != null) {
            for (final Element requires : requiresElems) {

                final String name = requires
                        .getAttribute(IpojoConstants.REQUIRES_FIELD);
                if (name != null) {
                    // The name is the most important part
                    final String id = requires
                            .getAttribute(IpojoConstants.REQUIRES_ID);
                    fieldIdMap.put(name, id);
                }
            }
        }

        // @Temporal elements IDs
        final Element[] temporalElems = componentModel
                .getElements(IpojoConstants.TEMPORAL_ELEMENT_NAME);
        if (temporalElems != null) {
            for (final Element temporal : temporalElems) {

                final String name = temporal
                        .getAttribute(IpojoConstants.TEMPORAL_FIELD);
                if (name != null) {
                    // The name is the most important part
                    final String id = temporal
                            .getAttribute(IpojoConstants.TEMPORAL_ID);
                    fieldIdMap.put(name, id);
                }
            }
        }

        pFactories.put(factoryName, aFactory);

        // Signal the arrival to others
        pSignalBroadcaster.fireGroup(
                ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY,
                new String[] { factoryName }, "ALL");

        pSignalBroadcaster.fire(
                ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY,
                new String[] { factoryName }, "{local}");

        pLogger.logInfo(this, "bindFactory", "Factory bound :", factoryName);
    }

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            The bound service
     */
    @Bind(optional = false)
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        // Register to all composer agent signals
        aSignalReceiver.registerListener(
                ComposerAgentSignals.FILTER_ALL_REQUESTS, this);

        // Indicate all our factories
        pSignalBroadcaster.fireGroup(
                ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY, pFactories
                        .keySet().toArray(new String[0]), "ALL");

        pLogger.logInfo(this, "bindSignalReceiver",
                "Bound to a signal receiver");
    }

    /**
     * Sends a signal to the given isolate to indicate which components can be
     * instantiated here.
     * 
     * @param aIsolateId
     *            The isolate to answer to
     * @param aComponents
     *            Components to find in the isolate
     */
    protected void canHandleComponents(final String aIsolateId,
            final ComponentBean[] aComponents) {

        // Don't forget : the isolate ID can change due to the SlaveAgent
        final String currentIsolateId = pPlatformDirs.getIsolateId();

        // Result list
        final List<ComponentBean> handledComponents = new ArrayList<ComponentBean>();

        // Test all beans...
        for (final ComponentBean component : aComponents) {

            // ... host isolate ID
            final String componentHostId = component.getIsolate();
            if (componentHostId != null
                    && !componentHostId.equals(currentIsolateId)) {
                // Bad host
                continue;
            }

            // ... factory presence
            final String componentType = component.getType();
            if (componentType != null && pFactories.containsKey(componentType)) {
                // We can do it
                handledComponents.add(component);
            }
        }

        // Reply to the requester with a signal
        final ComponentBean[] resultArray = handledComponents
                .toArray(new ComponentBean[handledComponents.size()]);

        pSignalBroadcaster.fire(
                ComposerAgentSignals.SIGNAL_RESPONSE_HANDLES_COMPONENTS,
                resultArray, aIsolateId);
    }

    /**
     * Generates the iPOJO instance properties of the given component.
     * 
     * Sets up the instance name and required fields filters properties
     * 
     * @param aComponent
     *            The instantiated component description
     * 
     * @return The iPOJO component properties
     */
    protected Properties generateComponentInstanceProperties(
            final ComponentBean aComponent) {

        // Prepare the result
        final Properties properties = new Properties();

        // Set the configured component properties
        // (sensitive keys will be overridden)
        properties.putAll(aComponent.getProperties());

        // Instance name
        properties.put(IpojoConstants.INSTANCE_NAME, aComponent.getName());

        // Set up fields filters
        final Map<String, String> fieldIdMapping = pFactoriesFieldsIds
                .get(aComponent.getType());

        if (fieldIdMapping != null) {
            // Mapping available, use it
            final Properties fieldsFilters = generateFieldsFilters(aComponent,
                    fieldIdMapping);

            /*
             * Convert fields filters to an array : it avoids an error in the
             * log each time a component is created...
             */
            final String[] fieldsFiltersArray = new String[fieldsFilters.size() * 2];

            int i = 0;
            for (final Entry<?, ?> entry : fieldsFilters.entrySet()) {

                fieldsFiltersArray[i++] = (String) entry.getKey();
                fieldsFiltersArray[i++] = (String) entry.getValue();
            }

            // @Requires annotations
            properties.put(IpojoConstants.REQUIRES_FILTERS, fieldsFiltersArray);

            // @Temporal annotations
            properties.put(IpojoConstants.TEMPORAL_FILTERS, fieldsFiltersArray);
        }

        return properties;
    }

    /**
     * Generates the iPOJO properties to force the filters to apply to
     * configured required field
     * 
     * @param aComponent
     *            Instantiated component
     * @param aFieldIdMapping
     *            Field name -&gt; iPOJO required field ID mapping
     * @return The iPOJO requires.filters property value (never null)
     */
    protected Properties generateFieldsFilters(final ComponentBean aComponent,
            final Map<String, String> aFieldIdMapping) {

        // Store a reference to the component fields filters map
        final Map<String, String> fieldsFilters = aComponent.getFieldsFilters();

        // Set requires.filter property
        final Properties requiresFilterProperties = new Properties();

        for (final Entry<String, String> fieldIdEntry : aFieldIdMapping
                .entrySet()) {

            // Field name is constant
            final String fieldName = fieldIdEntry.getKey();

            // Use the field ID if possible, else the field name
            String fieldId = fieldIdEntry.getValue();
            if (fieldId == null) {
                fieldId = fieldName;
            }

            // Compute the field filter
            String filter = null;

            if (fieldsFilters.containsKey(fieldName)) {
                // Field name found
                filter = fieldsFilters.get(fieldName);

            } else if (fieldsFilters.containsKey(fieldId)) {
                // Field ID found
                filter = fieldsFilters.get(fieldId);

            } else {
                // Default : filter on the composite name
                final StringBuilder builder = new StringBuilder();

                builder.append("(");
                builder.append(ComposerAgentConstants.COMPOSITE_NAME);
                builder.append("=");
                builder.append(aComponent.getParentName());
                builder.append(")");

                filter = builder.toString();
            }

            if (filter != null) {
                // Trim the filter for the next test
                filter = filter.trim();

                if (!filter.isEmpty()) {
                    // Non-empty filter, ready to be used
                    requiresFilterProperties.put(fieldId, filter);
                }
            }
        }

        return requiresFilterProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String, org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        final String signalSender = aSignalData.getIsolateId();
        final Object signalContent = aSignalData.getSignalContent();

        // For Jabsorb results...
        final String[] stringContent = Utilities.getArray(signalContent,
                String.class);
        final ComponentBean[] componentsArray = Utilities.getArray(
                signalContent, ComponentBean.class);

        if (ComposerAgentSignals.SIGNAL_CAN_HANDLE_COMPONENTS
                .equals(aSignalName)) {
            // Test if the isolate can instantiate the given components
            if (componentsArray != null) {

                canHandleComponents(signalSender, componentsArray);
            }

        } else if (ComposerAgentSignals.SIGNAL_INSTANTIATE_COMPONENTS
                .equals(aSignalName)) {
            // Instantiate requested components
            if (componentsArray != null) {

                try {
                    instantiateComponents(signalSender, componentsArray);

                } catch (final Exception e) {
                    pLogger.logSevere(this,
                            "handleReceivedSignal : instantiateComponents", e);
                }
            }

        } else if (ComposerAgentSignals.SIGNAL_STOP_COMPONENTS
                .equals(aSignalName)) {
            // Stop requested components

            try {
                if (stringContent != null) {
                    // Only names
                    stopComponents(signalSender, stringContent);

                } else if (componentsArray != null) {
                    // Beans
                    stopComponents(signalSender, componentsArray);
                }

            } catch (final Exception e) {
                pLogger.logSevere(this,
                        "handleReceivedSignal : stopComponents", e);
            }
        }

        return null;
    }

    /**
     * Tries to instantiate the given components in the current isolate, then
     * sends a signal to the given isolate with the result.
     * 
     * @param aIsolateId
     *            The isolate to answer to
     * @param aComponents
     *            Components to instantiate in the isolate
     */
    protected void instantiateComponents(final String aIsolateId,
            final ComponentBean[] aComponents) {

        // Current isolate ID
        final String isolateId = pPlatformDirs.getIsolateId();

        // List of the successfully started components
        final List<String> succeededComponents = new ArrayList<String>(
                aComponents.length);

        // List of the components that failed
        final List<String> failedComponents = new ArrayList<String>(
                aComponents.length);

        // Find the composite name from the first component
        String compositeName = null;

        // Try to instantiate each component
        for (final ComponentBean component : aComponents) {

            pLogger.logInfo(this, "instantiateComponents",
                    "Instantiating component :", component, "...");

            if (compositeName == null) {
                compositeName = component.getRootName();
            }

            // Get the factory
            final Factory factory = pFactories.get(component.getType());
            if (factory == null) {
                // Unknown component type
                pLogger.logWarn(this, "instantiateComponents",
                        "Factory not found :", component.getType());

                failedComponents.add(component.getName());
                continue;
            }

            // Prepare instance properties
            final Properties instanceProperties = generateComponentInstanceProperties(component);

            // Prepare provided service(s) properties
            final Properties serviceProperties = new Properties();

            // Composite name
            serviceProperties.put(ComposerAgentConstants.COMPOSITE_NAME,
                    component.getParentName());
            // Host isolate
            serviceProperties.put(ComposerAgentConstants.HOST_ISOLATE,
                    isolateId);
            // Exported service
            serviceProperties.put("service.exported.interfaces", "*");

            try {
                // Instantiate the component
                final ComponentInstance compInst = factory
                        .createComponentInstance(instanceProperties);

                if (compInst instanceof InstanceManager) {
                    // We can get the control of the provided service handler
                    final InstanceManager inst = (InstanceManager) compInst;
                    final ProvidedServiceHandler serviceHandler = (ProvidedServiceHandler) inst
                            .getHandler(IpojoConstants.HANDLER_PROVIDED_SERVICE);
                    if (serviceHandler != null) {
                        serviceHandler.addProperties(serviceProperties);
                    }
                }

                // Register to the component events
                compInst.addInstanceStateListener(this);

                // Keep a reference to this component
                pComponentsInstances.put(component.getName(), compInst);

                succeededComponents.add(component.getName());

            } catch (final Exception e) {

                // Fail !
                failedComponents.add(component.getName());
                pLogger.logSevere(this, "instantiateComponents",
                        "Error instantiating component '", component.getName(),
                        "'", e);
            }
        }

        // Set the composite name
        final HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(ComposerAgentSignals.RESULT_KEY_COMPOSITE, compositeName);

        // Set the succeeded components names
        final String[] succeededArray = succeededComponents
                .toArray(new String[succeededComponents.size()]);
        resultMap.put(ComposerAgentSignals.RESULT_KEY_INSTANTIATED,
                succeededArray);

        // Set the failed components names
        final String[] failedArray = failedComponents
                .toArray(new String[failedComponents.size()]);
        resultMap.put(ComposerAgentSignals.RESULT_KEY_FAILED, failedArray);

        // Send the signal
        pSignalBroadcaster.fire(
                ComposerAgentSignals.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                resultMap, aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Composer agent Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.felix.ipojo.InstanceStateListener#stateChanged(org.apache.
     * felix.ipojo.ComponentInstance, int)
     */
    @Override
    public void stateChanged(final ComponentInstance aComponentInstance,
            final int aState) {

        final String name = aComponentInstance.getInstanceName();
        if (!pComponentsInstances.containsKey(name)) {
            // Component is no more handled...

            if (aState != ComponentInstance.DISPOSED
                    && aState != ComponentInstance.STOPPED) {
                // Incoherent state
                pLogger.logWarn(this, "Component-State-Changed",
                        "Received new status", aState, "for component", name,
                        "which should no longer change...");
            }

            return;
        }

        if (aState == ComponentInstance.STOPPED) {
            // Component stopped (disappeared)
            pComponentsInstances.remove(name);
        }

        final ECompositionEvent newState;
        switch (aState) {

        case ComponentInstance.INVALID:
            newState = ECompositionEvent.STOP;
            break;

        case ComponentInstance.DISPOSED:
        case ComponentInstance.STOPPED:
            newState = ECompositionEvent.REMOVE;
            break;

        case ComponentInstance.VALID:
            newState = ECompositionEvent.START;
            break;

        default:
            pLogger.logWarn(this, "Component-State-Changed",
                    "Unknown new iPOJO state :", aState);
            // Don't send signal when new state is unknown
            return;
        }

        // Notify composer core
        final HashMap<String, Serializable> resultMap = new HashMap<String, Serializable>();
        resultMap.put(ComposerAgentSignals.COMPONENT_CHANGED_KEY_NAME, name);
        resultMap.put(ComposerAgentSignals.COMPONENT_CHANGED_KEY_STATE,
                newState);

        // Send the signal
        pSignalBroadcaster
                .fireGroup(ComposerAgentSignals.SIGNAL_COMPONENT_CHANGED,
                        resultMap, "ALL");

        pSignalBroadcaster.fire(ComposerAgentSignals.SIGNAL_COMPONENT_CHANGED,
                resultMap, "{local}");
    }

    /**
     * Tries to stop the given components in the current isolate, then sends a
     * signal to the monitors with the result.
     * 
     * @param aIsolateId
     *            The isolate that sent the stop request
     * @param aComponents
     *            Components to stop in the isolate
     */
    protected void stopComponents(final String aIsolateId,
            final ComponentBean[] aComponents) {

        final String[] componentsNames = new String[aComponents.length];

        for (int i = 0; i < aComponents.length; i++) {
            // Get names only
            componentsNames[i] = aComponents[i].getName();
        }

        stopComponents(aIsolateId, componentsNames);
    }

    /**
     * Tries to stop the given components in the current isolate, then sends a
     * signal to answer to the composer.
     * 
     * @param aIsolateId
     *            The isolate that sent the stop request
     * @param aComponents
     *            Names if the components to stop in the isolate
     */
    protected void stopComponents(final String aIsolateId,
            final String[] aComponents) {

        // List of the successfully stopped components
        final List<String> stoppedComponents = new ArrayList<String>(
                aComponents.length);

        // List of unknown components
        final List<String> unknownComponents = new ArrayList<String>(
                aComponents.length);

        // Try to instantiate each component
        for (final String componentName : aComponents) {

            pLogger.logInfo(this, "stopComponents", "Stopping component :",
                    componentName, "...");

            // Get the component instance
            final ComponentInstance instance = pComponentsInstances
                    .get(componentName);

            if (instance == null) {
                // Not found
                unknownComponents.add(componentName);
                continue;
            }

            // Dispose the instance and deletes the references to it
            instance.getFactory().deleted(componentName);
            stoppedComponents.add(componentName);
        }

        // Set the composite name
        final HashMap<String, Object> resultMap = new HashMap<String, Object>();

        // Set the succeeded components names
        final String[] succeededArray = stoppedComponents
                .toArray(new String[stoppedComponents.size()]);
        resultMap.put(ComposerAgentSignals.RESULT_KEY_STOPPED, succeededArray);

        // Set the failed components names
        final String[] failedArray = unknownComponents
                .toArray(new String[unknownComponents.size()]);
        resultMap.put(ComposerAgentSignals.RESULT_KEY_UNKNOWN, failedArray);

        // Send the signal
        pSignalBroadcaster.fire(
                ComposerAgentSignals.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                resultMap, aIsolateId);
    }

    /**
     * Called by iPOJO when a factory service is unbound
     * 
     * @param aFactory
     *            A factory service going away
     */
    @Unbind(id = IPOJO_ID_FACTORIES, aggregate = true, optional = true)
    protected void unbindFactory(final Factory aFactory) {

        final String factoryName = aFactory.getName();

        // Remove the factory from the map
        pFactories.remove(factoryName);

        // Signal the removal to others
        pSignalBroadcaster.fireGroup(
                ComposerAgentSignals.SIGNAL_ISOLATE_REMOVE_FACTORY,
                new String[] { factoryName }, "ALL");

        pSignalBroadcaster.fire(
                ComposerAgentSignals.SIGNAL_ISOLATE_REMOVE_FACTORY,
                new String[] { factoryName }, "{local}");
    }

    /**
     * Called by iPOOJO when a signal broadcaster is gone, which means just
     * before this agent is invalidated.
     * 
     * @param aSignalBroadcaster
     *            A signal broadcaster
     */
    @Unbind(id = IPOJO_ID_BROADCASTER)
    protected void unbindSignalBroadcaster(
            final ISignalBroadcaster aSignalBroadcaster) {

        // Send a last signal to monitors to forget this agent
        pSignalBroadcaster
                .fireGroup(ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_GONE,
                        null, "ALL");

        pSignalBroadcaster.fire(
                ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_GONE, null,
                "{local}");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Composer agent Ready");
    }
}
