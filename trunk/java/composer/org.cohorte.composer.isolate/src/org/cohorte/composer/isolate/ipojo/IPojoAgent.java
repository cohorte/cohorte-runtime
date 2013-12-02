/**
 * File:   IPojoAgent.java
 * Author: Thomas Calmant
 * Date:   22 oct. 2013
 */
package org.cohorte.composer.isolate.ipojo;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.cohorte.composer.api.ComposerConstants;
import org.cohorte.composer.api.IAgent;
import org.cohorte.composer.api.RawComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * The iPOJO composer agent
 * 
 * @author Thomas Calmant
 */
@Component
@Provides(specifications = IAgent.class)
@Instantiate(name = "cohorte-composer-agent-ipojo")
public class IPojoAgent implements IAgent, InstanceStateListener {

    /** iPOJO factories dependency ID */
    private static final String IPOJO_ID_FACTORIES = "ipojo-factories";

    /** The bundle context */
    private final BundleContext pContext;

    /** Local factories: Name -&gt; iPOJO Factory */
    private final Map<String, Factory> pFactories = new LinkedHashMap<>();

    /**
     * Maps fields names and IDs for each component type: Factory -&gt; {Field
     * name -&gt; ID}
     */
    private final Map<String, Map<String, String>> pFactoriesFieldsIds = new LinkedHashMap<>();

    /** Instance name -&gt; iPOJO instance */
    private final Map<String, ComponentInstance> pInstances = new LinkedHashMap<>();

    /** Host isolate name */
    private String pIsolateName;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** Host node Name */
    private String pNodeName;

    /** Factory name -&gt; Remaining components */
    private final Map<String, Set<RawComponent>> pRemainingFactories = new LinkedHashMap<>();

    /** Instance name -&gt; Remaining component */
    private final Map<String, RawComponent> pRemainingNames = new LinkedHashMap<>();

    /** Component validation flag */
    private boolean pValidated;

    /**
     * Sets up the component
     * 
     * @param aContext
     *            The bundle context
     */
    public IPojoAgent(final BundleContext aContext) {

        pContext = aContext;
        pValidated = false;
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

        // Prepare a field -> ID map and attach it to the component type
        final Map<String, String> fieldIdMap = new LinkedHashMap<>();
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
        pLogger.log(LogService.LOG_INFO, "Factory bound: " + factoryName);

        if (pValidated) {
            // Component is valid, try instantiation
            final Set<RawComponent> remaining = pRemainingFactories
                    .get(factoryName);
            if (remaining != null) {
                handle(remaining);
            }
        }
    }

    /**
     * Prepares the basic properties that a component and its provided service
     * must have
     * 
     * @param aComponent
     *            A component bean
     * @return The basic properties of the given component
     */
    private Properties computeCommonProperties(final RawComponent aComponent) {

        // Set up common properties
        final Properties properties = new Properties();

        // Add configured properties
        properties.putAll(aComponent.getProperties());

        // Set up forced properties
        properties.put(ComposerConstants.PROP_INSTANCE_NAME,
                aComponent.getName());
        properties.put(ComposerConstants.PROP_ISOLATE_NAME, pIsolateName);
        properties.put(ComposerConstants.PROP_NODE_NAME, pNodeName);
        return properties;
    }

    /**
     * Prepares the binding filters for iPOJO
     * 
     * @param aComponent
     *            A component bean
     * @return A dictionary Field name -&gt; LDAP filter string
     */
    private Map<String, String> computeFilters(final RawComponent aComponent) {

        // Computed filters: Field -> LDAP filter
        final Map<String, String> filters = new LinkedHashMap<>(
                aComponent.getFilters());

        // Field -> component name
        final Map<String, String> fieldsWires = aComponent.getWires();
        for (final Entry<String, String> entry : fieldsWires.entrySet()) {
            // For each field, prepare & merge filters
            final String field = entry.getKey();
            final String wire = entry.getValue();

            // Prepare the corresponding LDAP filter
            final String wire_filter = String.format("(%s=%s)",
                    ComposerConstants.PROP_INSTANCE_NAME, wire);

            final String previous = filters.get(field);
            if (previous != null) {
                // Merge filters
                filters.put(field,
                        String.format("(&%s%s)", wire_filter, previous));

            } else {
                // Store it directly
                filters.put(field, wire_filter);
            }
        }

        return filters;
    }

    /**
     * Sets up the properties that must be associated to the component
     * 
     * @param aComponent
     *            A component bean
     * @return The component instance properties
     */
    private Properties computeInstanceProperties(final RawComponent aComponent) {

        // Start with the basic properties
        final Properties properties = computeCommonProperties(aComponent);

        // Instance name
        properties.put(IPojoConstants.INSTANCE_NAME, aComponent.getName());

        // Set up field filters
        final Map<String, String> fieldIdMapping = pFactoriesFieldsIds
                .get(aComponent.getFactory());
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

    /**
     * Sets up the properties that must be associated to the service provided by
     * the component
     * 
     * @param aComponent
     *            A component bean
     * @return The service properties
     */
    private Properties computeServiceProperties(final RawComponent aComponent) {

        // Start with the basic properties
        final Properties properties = computeCommonProperties(aComponent);

        // Export the service
        properties.put(Constants.SERVICE_EXPORTED_INTERFACES, "*");
        return properties;
    }

    /**
     * Generates the iPOJO properties to force the filters to apply to
     * configured required field
     * 
     * @param aComponent
     *            A component bean
     * @param aFieldIdMapping
     *            Field name -&gt; iPOJO required field ID mapping
     * @return The iPOJO requires.filters property value (never null)
     */
    private Properties generateFieldsFilters(final RawComponent aComponent,
            final Map<String, String> aFieldIdMapping) {

        // Field -> LDAP Filter
        final Map<String, String> fieldsFilters = computeFilters(aComponent);

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
                // TODO: Default : filter on the composite name
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
     * Retrieves or creates the set of remaining components of the given factory
     * 
     * @param aFactory
     *            A factory name
     * @return The set of remaining components (never null)
     */
    private Set<RawComponent> getRemainingByFactory(final String aFactory) {

        Set<RawComponent> set = pRemainingFactories.get(aFactory);
        if (set == null) {
            set = new LinkedHashSet<>();
            pRemainingFactories.put(aFactory, set);
        }

        return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IAgent#handle(java.util.Set)
     */
    @Override
    public synchronized Set<RawComponent> handle(
            final Set<RawComponent> aComponents) {

        final Set<RawComponent> instantiated = new LinkedHashSet<>();
        for (final RawComponent component : aComponents) {
            try {
                if (tryInstantiate(component)) {
                    instantiated.add(component);
                }

            } catch (final Throwable ex) {
                // Instantiation error
                pLogger.log(LogService.LOG_ERROR,
                        "Error instantiating component " + component + ": "
                                + ex, ex);
            }
        }

        // Store the remaining components
        final Set<RawComponent> remaining = new LinkedHashSet<>(aComponents);
        remaining.removeAll(instantiated);
        for (final RawComponent component : remaining) {
            getRemainingByFactory(component.getFactory()).add(component);
        }

        return instantiated;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public synchronized void invalidate() {

        // Clean up values
        pValidated = false;
        pIsolateName = null;
        pNodeName = null;

        // TODO: kill all components ?
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IAgent#kill(java.lang.String)
     */
    @Override
    public synchronized void kill(final String aName) {

        final ComponentInstance instance = pInstances.remove(aName);
        if (instance != null) {
            // Kill the component
            instance.dispose();
            pLogger.log(LogService.LOG_INFO, "Component " + aName + " disposed");

        } else if (pRemainingNames.containsKey(aName)) {
            // Remove the entry from the remaining components
            final RawComponent component = pRemainingNames.remove(aName);
            pRemainingFactories.get(component.getFactory()).remove(component);

        } else {
            // FIXME Unknown component
            pLogger.log(LogService.LOG_WARNING, "Unknown component: " + aName);
        }
    }

    /**
     * Removes the given component from the remaining components maps
     * 
     * @param aComponent
     *            The component to be removed
     */
    private void removeRemaining(final RawComponent aComponent) {

        // By name
        pRemainingNames.remove(aComponent.getName());

        // By factory
        final Set<RawComponent> remaining = pRemainingFactories.get(aComponent
                .getFactory());
        if (remaining != null) {
            remaining.remove(aComponent);
        }
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
                pLogger.log(LogService.LOG_WARNING, "Received new status "
                        + aState + " for component " + name
                        + " which should no longer change...");
            }

            // Ignore
            return;
        }

        // TODO: handle events
    }

    /**
     * Tries to instantiate a component
     * 
     * @param aComponent
     *            The component to instantiate
     * @return True if the component has been instantiated
     * @throws ConfigurationException
     *             Invalid configuration
     * @throws UnacceptableConfiguration
     *             Invalid configuration
     */
    private boolean tryInstantiate(final RawComponent aComponent)
            throws UnacceptableConfiguration, ConfigurationException {

        // Get the component factory
        final Factory factory = pFactories.get(aComponent.getFactory());
        if (factory == null) {
            // Factory not available yet
            return false;
        }

        // Prepare properties
        final Properties instanceProperties = computeInstanceProperties(aComponent);
        final Properties serviceProperties = computeServiceProperties(aComponent);

        // Instantiate the component
        final ComponentInstance instance;
        try {
            instance = factory.createComponentInstance(instanceProperties);

        } catch (final MissingHandlerException ex) {
            // A handler is missing, try later
            return false;
        }

        if (instance instanceof InstanceManager) {
            // We can get the control of the provided service handler
            final InstanceManager instanceManager = (InstanceManager) instance;
            final ProvidedServiceHandler serviceHandler = (ProvidedServiceHandler) instanceManager
                    .getHandler(IPojoConstants.HANDLER_PROVIDED_SERVICE);
            if (serviceHandler != null) {
                serviceHandler.addProperties(serviceProperties);
            }
        }

        // Register to the component events
        instance.addInstanceStateListener(this);

        // Keep a reference to this component
        pInstances.put(aComponent.getName(), instance);

        // Update the 'remaining' information
        removeRemaining(aComponent);
        return true;
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

    /**
     * Component validated
     */
    @Validate
    public synchronized void validate() {

        // Store isolate information
        pIsolateName = pContext
                .getProperty(IPlatformProperties.PROP_ISOLATE_NAME);
        pNodeName = pContext.getProperty(IPlatformProperties.PROP_NODE_NAME);

        // Allow bindings
        pValidated = true;
    }
}
