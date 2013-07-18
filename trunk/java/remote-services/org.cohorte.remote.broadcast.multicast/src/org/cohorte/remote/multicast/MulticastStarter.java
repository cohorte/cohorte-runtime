/**
 * File:   MulticastStarter.java
 * Author: Thomas Calmant
 * Date:   18 juil. 2013
 */
package org.cohorte.remote.multicast;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * Multicast broadcaster component instantiator (w/o Stallone)
 * 
 * @author Thomas Calmant
 */
@Component(name = "multicast-starter-factory")
@Instantiate(name = "multicast-starter")
public class MulticastStarter {

    /**
     * Default multicast group: IPv4 site multicast
     * 
     * @see http://fr.wikipedia.org/wiki/Multicast
     */
    private static final String DEFAULT_GROUP = "239.0.0.1";

    /**
     * Default component instance name
     */
    private static final String DEFAULT_NAME = "cohorte-remote-multicast";

    /**
     * Default listening port (must be opened in firewall, UDP mode)
     * 
     * @see H2G2
     */
    private static final String DEFAULT_PORT = "42000";

    /**
     * Multicast group component property
     */
    private static final String PROP_GROUP = "multicast.group";

    /**
     * Multicast port component property
     */
    private static final String PROP_PORT = "multicast.port";

    /**
     * Multicast group system property
     */
    private static final String SYSPROP_GROUP = "remote.multicast.group";

    /**
     * Component name system property
     */
    private static final String SYSPROP_NAME = "remote.multicast.component.name";

    /**
     * Multicast port system property
     */
    private static final String SYSPROP_PORT = "remote.multicast.port";

    private final BundleContext pContext;

    /** The multicast component instance */
    private ComponentInstance pInstance;

    /** The logger */
    @Requires
    private LogService pLogger;

    @Requires(filter = "(factory.name=cohorte-remote-broadcast-multicast)")
    private Factory pMulticastFactory;

    /**
     * Sets up members
     * 
     * @param aContext
     *            The bundle context
     */
    public MulticastStarter(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Gets a bundle context / system / default property
     * 
     * @param aKey
     *            Property name
     * @param aDefault
     *            Value to return if absent of context and system properties
     * @return The found / default value
     */
    private String getProperty(final String aKey, final String aDefault) {

        final String value = pContext.getProperty(aKey);
        if (value == null) {
            return aDefault;
        }

        return value;
    }

    /**
     * Component gone
     */
    @Invalidate
    public void invalidate() {

        // Kill'em all
        stopComponent();
    }

    /**
     * Starts the multicast broadcaster iPOJO component according to system
     * properties
     */
    private void startComponent() {

        if (pInstance != null) {
            pLogger.log(LogService.LOG_ERROR, "Can't run twice");
            return;
        }

        // Set up properties
        final Dictionary<String, String> props = new Hashtable<>();
        props.put(Factory.INSTANCE_NAME_PROPERTY,
                getProperty(SYSPROP_NAME, DEFAULT_NAME));
        props.put(PROP_GROUP, getProperty(SYSPROP_GROUP, DEFAULT_GROUP));
        props.put(PROP_PORT, getProperty(SYSPROP_PORT, DEFAULT_PORT));

        try {
            // Create the instance
            pInstance = pMulticastFactory.createComponentInstance(props);

            final StringBuilder builder = new StringBuilder(
                    "Multicast broadcaster instantiated: ");
            builder.append("iPOJO Instance=").append(pInstance);

            if (pInstance instanceof InstanceManager) {
                // Try to grab more details
                final InstanceManager instMan = (InstanceManager) pInstance;
                final Object realComponent = instMan.getPojoObject();
                builder.append(" - ").append(realComponent);
            }

            pLogger.log(LogService.LOG_DEBUG, builder.toString());

        } catch (UnacceptableConfiguration | MissingHandlerException
                | ConfigurationException ex) {
            // What a Terrible Failure
            pLogger.log(
                    LogService.LOG_ERROR,
                    "Multicast broadcaster instantiation error: "
                            + ex.getMessage(), ex);
        }
    }

    /**
     * Kills the multicast broadcaster, if any
     */
    private void stopComponent() {

        if (pInstance == null) {
            pLogger.log(LogService.LOG_WARNING,
                    "No multicast broadcaster instantiated (nice try)");
            return;
        }

        pInstance.dispose();
        pInstance = null;

        pLogger.log(LogService.LOG_DEBUG,
                "Multicast broadcaster gone with the wind");
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Let's roll
        startComponent();
    }
}
