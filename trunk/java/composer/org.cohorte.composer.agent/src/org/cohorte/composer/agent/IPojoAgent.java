/**
 * File:   IPojoAgent.java
 * Author: Thomas Calmant
 * Date:   28 mars 2013
 */
package org.cohorte.composer.agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.cohorte.composer.api.IAgentConstants;
import org.cohorte.composer.api.IComposerAgent;
import org.cohorte.composer.api.IComposerCore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * import org.apache.felix.ipojo.annotations.Provides; Implementation of the
 * Composer Agent to handle iPOJO components
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-composer-agent-ipojo-factory")
@Provides(specifications = IComposerAgent.class,
        properties = {
                @StaticServiceProperty(name = "service.exported.interfaces",
                        type = "String",
                        value = "org.cohorte.composer.api.IComposerAgent"),
                @StaticServiceProperty(name = "cohorte.remote.synonyms",
                        type = "String[]",
                        value = "{python:/cohorte.composer.agent}") })
public class IPojoAgent implements IComposerAgent, InstanceStateListener {

    /** iPOJO factories dependency ID */
    private static final String IPOJO_ID_FACTORIES = "ipojo-factories";

    /** Composer core service */
    @Requires
    private IComposerCore pComposer;

    /** Bundle context */
    private final BundleContext pContext;

    /** Local factories: Name -&gt; iPOJO Factory */
    private final Map<String, Factory> pFactories = new HashMap<String, Factory>();

    /**
     * Maps fields names and IDs for each component type: Factory -&gt; {Field
     * name -&gt; ID}
     */
    private final Map<String, Map<String, String>> pFactoriesFieldsIds = new HashMap<String, Map<String, String>>();

    /** Components instances: Name -&gt; iPOJO ComponentInstance */
    private final Map<String, ComponentInstance> pInstances = new HashMap<String, ComponentInstance>();

    /** Link component name and UID: Name -&gt; UID */
    private final Map<String, String> pInstanceUid = new HashMap<String, String>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Mapping of components waiting for a factory to appear: Factory -&gt;
     * Component descriptions
     */
    private Map<String, List<ComponentDescription>> pWaitings;

    /**
     * Sets up the component
     * 
     * @param aContext
     *            The bundle context
     */
    public IPojoAgent(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Called by iPOJO when a Factory service is bound
     * 
     * @param aFactory
     *            A new factory service
     */
    @Bind(id = IPOJO_ID_FACTORIES, aggregate = true, optional = true)
    protected synchronized void bindFactory(final Factory aFactory) {

        // Store the factory name (component type name)
        final String factoryName = aFactory.getName();

        // Prepare a field - ID map and attach it to the component type
        final Map<String, String> fieldIdMap = new HashMap<String, String>();
        pFactoriesFieldsIds.put(factoryName, fieldIdMap);

        // Set up the map content
        final Element componentModel = aFactory.getComponentMetadata();

        // @Requires elements IDs
        final Element[] requiresElems = componentModel
                .getElements(IPojoConstants.REQUIRES_ELEMENT_NAME);
        if (requiresElems != null) {
            for (final Element requires : requiresElems) {

                final String name = requires
                        .getAttribute(IPojoConstants.REQUIRES_FIELD);
                if (name != null) {
                    // The name is the most important part
                    final String id = requires
                            .getAttribute(IPojoConstants.REQUIRES_ID);
                    fieldIdMap.put(name, id);
                }
            }
        }

        // @Temporal elements IDs
        final Element[] temporalElems = componentModel
                .getElements(IPojoConstants.TEMPORAL_ELEMENT_NAME);
        if (temporalElems != null) {
            for (final Element temporal : temporalElems) {

                final String name = temporal
                        .getAttribute(IPojoConstants.TEMPORAL_FIELD);
                if (name != null) {
                    // The name is the most important part
                    final String id = temporal
                            .getAttribute(IPojoConstants.TEMPORAL_ID);
                    fieldIdMap.put(name, id);
                }
            }
        }

        pFactories.put(factoryName, aFactory);
        pLogger.logInfo(this, "bindFactory", "Factory bound :", factoryName);
    }

    /**
     * Generates the iPOJO properties to force the filters to apply to
     * configured required field
     * 
     * @param aComponent
     *            Component configuration dictionary
     * @param aFieldIdMapping
     *            Field name -&gt; iPOJO required field ID mapping
     * @return The iPOJO requires.filters property value (never null)
     */
    @SuppressWarnings("unchecked")
    private Properties generateFieldsFilters(final Map<String, ?> aComponent,
            final Map<String, String> aFieldIdMapping) {

        // Store a reference to the component fields filters map
        final Map<String, String> fieldsFilters = (Map<String, String>) aComponent
                .get("fieldsFilters");

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
            final String parentName = (String) aComponent.get("parentName");

            if (fieldsFilters.containsKey(fieldName)) {
                // Field name found
                filter = fieldsFilters.get(fieldName);

            } else if (fieldsFilters.containsKey(fieldId)) {
                // Field ID found
                filter = fieldsFilters.get(fieldId);

            } else if (parentName != null) {
                // Default : filter on the composite name
                final StringBuilder builder = new StringBuilder();

                builder.append("(");
                builder.append(IAgentConstants.PROP_COMPOSITE_NAME);
                builder.append("=");
                builder.append(parentName);
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

    /**
     * Generates the iPOJO instance properties of the given component.
     * 
     * Sets up the instance name and required fields filters properties
     * 
     * @param aComponent
     *            The description of the component
     * 
     * @return The iPOJO component properties
     */
    @SuppressWarnings("unchecked")
    private Properties generateIPojoProperties(final Map<String, ?> aComponent) {

        // Prepare the result
        final Properties properties = new Properties();

        // Set the configured component properties
        // (sensitive keys will be overridden)
        final Map<String, ?> rawProperties = (Map<String, ?>) aComponent
                .get("properties");
        if (rawProperties != null) {
            properties.putAll(rawProperties);
        }

        // Instance name
        properties.put(IPojoConstants.INSTANCE_NAME, aComponent.get("name"));

        // Set up fields filters
        final Map<String, String> fieldIdMapping = pFactoriesFieldsIds
                .get(aComponent.get("type"));

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
            properties.put(IPojoConstants.REQUIRES_FILTERS, fieldsFiltersArray);

            // @Temporal annotations
            properties.put(IPojoConstants.TEMPORAL_FILTERS, fieldsFiltersArray);
        }

        return properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IComposerAgent#get_factories()
     */
    @Override
    public String[] get_factories() {

        return pFactories.keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.composer.api.IComposerAgent#instantiate(java.util.Collection,
     * boolean)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IComposerAgent#get_isolate()
     */
    @Override
    public String[] get_isolate() {

        return new String[] {
                pContext.getProperty(IPlatformProperties.PROP_ISOLATE_UID),
                pContext.getProperty(IPlatformProperties.PROP_ISOLATE_NAME) };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void instantiate(final Collection<Map<String, Object>> aComponents,
            final boolean aUntilPossible) {

        final List<ComponentDescription> toInstantiate = new LinkedList<ComponentDescription>();

        pLogger.logInfo(this, "instantiate", "COMPONENTS=", aComponents,
                "untilpossible=", aUntilPossible);

        for (final Map<String, ?> component : aComponents) {
            // Extract basic component information
            final String name = (String) component.get("name");
            final String factory = (String) component.get("factory");

            // Normalize service properties
            final Properties serviceProperties = new Properties();
            final Map<String, ?> rawProperties = (Map<String, ?>) component
                    .get("properties");
            if (rawProperties != null) {
                serviceProperties.putAll(rawProperties);
            }

            // Prepare instance properties
            final Properties instanceProperties = generateIPojoProperties(component);

            for (final Properties properties : new Properties[] {
                    instanceProperties, serviceProperties }) {

                // Set up component properties
                properties.put(IAgentConstants.PROP_COMPONENT_UID,
                        component.get("uid"));
                properties.put(IAgentConstants.PROP_COMPONENT_NAME, name);
                properties.put(IAgentConstants.PROP_COMPOSITE_NAME,
                        component.get("parentName"));
                properties
                        .put(IAgentConstants.PROP_HOST_ISOLATE, get_isolate());
            }

            // Exported service
            serviceProperties.put(Constants.SERVICE_EXPORTED_INTERFACES, "*");

            // Create the component description
            final ComponentDescription description = new ComponentDescription(
                    name, factory, instanceProperties, serviceProperties);

            if (aUntilPossible && !pFactories.containsKey(factory)) {
                // Put the component in the waiting list
                List<ComponentDescription> waiting = pWaitings.get(factory);
                if (waiting == null) {
                    waiting = new LinkedList<ComponentDescription>();
                    pWaitings.put(factory, waiting);
                }

                waiting.add(description);

            } else {
                // Put it in the instantiation
                toInstantiate.add(description);
            }
        }

        pLogger.logInfo(this, "instantiate", "pWaitings=", pWaitings);
        pLogger.logInfo(this, "instantiate", "toInstantiate=", toInstantiate);

        // Run the instantiation loop
        instantiationLoop(toInstantiate);
    }

    /**
     * Instantiate the given components descriptions
     * 
     * @param aComponents
     *            A list of component descriptions
     */
    private void instantiationLoop(
            final Collection<ComponentDescription> aComponents) {

        if (aComponents == null || aComponents.isEmpty()) {
            // Nothing to do
            return;
        }

        // Instantiated components UID -> Name
        final Map<String, String> successes = new LinkedHashMap<String, String>();

        // List of already running components
        final List<String> running = new LinkedList<String>();

        // Component name -> Error message
        final Map<String, String> errors = new LinkedHashMap<String, String>();

        for (final ComponentDescription component : aComponents) {

            // Component name and properties
            final String name = component.getName();

            if (pInstances.containsKey(component.getName())) {
                // Already instantiated component
                running.add(name);

            } else {
                // Get the properties
                final Properties instanceProperties = component
                        .getInstanceProperties();
                final Properties serviceProperties = component
                        .getServiceProperties();

                // Retrieves the component UID
                final String uid = (String) instanceProperties
                        .get(IAgentConstants.PROP_COMPONENT_UID);

                // Create the instance entry first
                pInstances.put(uid, null);
                pInstanceUid.put(name, uid);

                try {
                    // Get the factory
                    final Factory factory = pFactories.get(component
                            .getFactory());

                    // Instantiate the component
                    final ComponentInstance compInst = factory
                            .createComponentInstance(instanceProperties);

                    if (compInst instanceof InstanceManager) {
                        // We can get the control of the provided service
                        // handler
                        final InstanceManager inst = (InstanceManager) compInst;
                        final ProvidedServiceHandler serviceHandler = (ProvidedServiceHandler) inst
                                .getHandler(IPojoConstants.HANDLER_PROVIDED_SERVICE);
                        if (serviceHandler != null) {
                            serviceHandler.addProperties(serviceProperties);
                        }
                    }

                    // Register to the component events
                    compInst.addInstanceStateListener(this);

                    // Keep a reference to this component
                    pInstances.put(name, compInst);

                    // Update the success map
                    successes.put(uid, name);

                    pLogger.logDebug(this, "instantiationLoop",
                            "Instantiated=", name);

                } catch (final Exception ex) {
                    // Clean up
                    pInstances.remove(name);
                    pInstanceUid.put(name, uid);

                    // Store the error
                    errors.put(name, ex.toString());

                    // Log
                    pLogger.logSevere(this, "instantiationLoop",
                            "Error instantiating component=", name, ex);
                }
            }
        }

        // Notify the composer
        pComposer.components_instantiation(get_isolate(), successes, running,
                errors);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IComposerAgent#is_running(java.util.Map)
     */
    @Override
    public int is_running(final Map<String, Object> aComponent) {

        // Extract component information
        final String name = (String) aComponent.get("name");
        final String factory = (String) aComponent.get("type");

        final ComponentInstance instance = pInstances.get(name);
        if (instance != null) {
            // Known component
            if (factory == null || instance.getFactory().getName() == factory) {
                // Factory names match: component is running
                return 1;

            } else {
                // Names match, but not the factories
                return -1;
            }
        }

        // Unknown component
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IComposerAgent#is_running(java.lang.String)
     */
    @Override
    public int is_running(final String aName) {

        if (pInstances.containsKey(aName)) {
            // Component running
            return 1;
        }

        // Unknown component
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IComposerAgent#kill(java.util.Collection)
     */
    @Override
    public String[][] kill(final Collection<String> aComponentNames) {

        // List of the successfully stopped components
        final List<String> killed = new LinkedList<String>();

        // List of unknown components
        final List<String> unknown = new LinkedList<String>();

        // Try to instantiate each component
        for (final String componentName : aComponentNames) {
            // Get the component instance
            final ComponentInstance instance = pInstances.get(componentName);
            if (instance == null) {
                // Not found
                unknown.add(componentName);
                continue;
            }

            // Dispose the instance and deletes the references to it
            instance.getFactory().deleted(componentName);
            killed.add(componentName);
        }

        return new String[][] { killed.toArray(new String[0]),
                unknown.toArray(new String[0]) };
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

        // Get the component name
        final String name = aComponentInstance.getInstanceName();
        if (!pInstances.containsKey(name)) {
            // Component is no more handled...
            if (aState != ComponentInstance.DISPOSED
                    && aState != ComponentInstance.STOPPED) {
                // Incoherent state
                pLogger.logWarn(this, "Component-State-Changed",
                        "Received new status", aState, "for component", name,
                        "which should no longer change...");
            }

            // Ignore
            return;
        }

        // Compute the Composer FSM event
        final String composerEvent;
        switch (aState) {

        case ComponentInstance.INVALID:
            composerEvent = "/invalidated";
            break;

        case ComponentInstance.DISPOSED:
        case ComponentInstance.STOPPED:
            composerEvent = "/gone";

            // Also, clean up the references
            pInstances.remove(name);
            break;

        case ComponentInstance.VALID:
            composerEvent = "/validated";
            break;

        default:
            pLogger.logWarn(this, "ComponentStateChanged",
                    "Unknown new iPOJO state=", aState, "for component=", name);
            // Don't send signal when new state is unknown
            return;
        }

        // Notify composer core
        pComposer.component_changed(get_isolate(), pInstanceUid.get(name),
                name, aComponentInstance.getFactory().getName(), composerEvent);
    }

    /**
     * Called by iPOJO when a factory service is unbound
     * 
     * @param aFactory
     *            A factory service going away
     */
    @Unbind(id = IPOJO_ID_FACTORIES)
    protected void unbindFactory(final Factory aFactory) {

        final String factoryName = aFactory.getName();

        // Remove the factory from the map
        pFactories.remove(factoryName);
    }
}
