/**
 * File:   BridgeClient.java
 * Author: Thomas Calmant
 * Date:   17 janv. 2013
 */
package org.cohorte.pyboot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.pyboot.api.ComponentBean;
import org.cohorte.pyboot.api.IPyBridge;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Uses the Python bridge to retrieve data for the lower level
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-java-bridge-client-factory")
@Instantiate(name = "cohorte-java-bridge-client")
public class BridgeClient implements ServiceListener {

    /** The instance name of a component */
    private static final String INSTANCE_NAME = "instance.name";

    /** The Python bridge service */
    @Requires
    private IPyBridge pBridge;

    /** List of components waiting to be instantiated */
    private final List<ComponentBean> pComponents = new ArrayList<ComponentBean>();

    /** Bundle context */
    private final BundleContext pContext;

    /**
     * Constructor
     * 
     * @param aContext
     *            The bundle context
     */
    public BridgeClient(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Instantiate all components that can be handled by the given factory
     * 
     * @param aFactoryRef
     *            A reference to a factory service
     */
    private void instantiateComponents(final ServiceReference<?> aFactoryRef) {

        if (pComponents.isEmpty()) {
            // Nothing to do
            return;
        }

        // Get the factory service
        final Factory factory = (Factory) pContext.getService(aFactoryRef);

        try {
            // Get the factory name
            final String name = factory.getName();

            // List of started component
            final List<ComponentBean> toRemove = new ArrayList<ComponentBean>();

            // Try to start components
            for (final ComponentBean component : pComponents) {

                if (name.equals(component.getFactory())) {
                    try {
                        pBridge.debug("Instantiating {0}", component.toString());

                        // Instantiate the component
                        factory.createComponentInstance(prepareInstanceProperties(component));

                    } catch (final Exception ex) {
                        pBridge.error("Error creating component {0}: {1}",
                                component.toString(), ex.getMessage());
                    }
                }
            }

            // Remove started components
            pComponents.removeAll(toRemove);

        } finally {
            // Clear the service usage
            pContext.ungetService(aFactoryRef);
        }
    }

    /**
     * ComponentBean invalidated
     */
    @Invalidate
    public void invalidate() {

        pComponents.clear();
        pBridge.debug("Bridge client gone");
    }

    /**
     * Tries to instantiate components with the existing factories
     */
    public void pollFactories() {

        final Collection<ServiceReference<Factory>> references;
        try {
            // Find all factories
            references = pContext.getServiceReferences(Factory.class, null);

        } catch (final InvalidSyntaxException ex) {
            // Can't happen (we don't use a filter)
            pBridge.error("Error retrieving factories: {0}", ex.getMessage());
            return;
        }

        // Try to instantiate components
        for (final ServiceReference<Factory> reference : references) {
            instantiateComponents(reference);
        }
    }

    /**
     * @param aComponent
     * @return
     */
    private Properties prepareInstanceProperties(final ComponentBean aComponent) {

        final Properties result = new Properties();

        // Configured properties
        for (final Entry<String, Object> entry : aComponent.getProperties()
                .entrySet()) {
            final Object value = entry.getValue();
            if (value != null) {
                // Ignore null values
                result.put(entry.getKey(), value);
            }
        }

        // Special properties
        result.put(INSTANCE_NAME, aComponent.getName());

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
     * ServiceEvent)
     */
    @Override
    public void serviceChanged(final ServiceEvent aEvent) {

        if (aEvent.getType() == ServiceEvent.REGISTERED) {
            // New factory
            instantiateComponents(aEvent.getServiceReference());
        }
    }

    /**
     * ComponentBean validated
     */
    @Validate
    public void validate() {

        pBridge.debug("Bridge client validated");

        // Store all components to start
        pComponents.addAll(pBridge.getComponents());
        pBridge.debug("Components list: {0}", pComponents.toString());

        // Register as a service listener
        final String filter = "(" + Constants.OBJECTCLASS + "="
                + Factory.class.getName() + ")";
        try {
            pContext.addServiceListener(this, filter);

        } catch (final InvalidSyntaxException ex) {
            // Shoudln't happen
            pBridge.error("Error registering the service listener: %s",
                    ex.getMessage());
        }

        // Try to instantiate the first components
        pollFactories();
    }
}
