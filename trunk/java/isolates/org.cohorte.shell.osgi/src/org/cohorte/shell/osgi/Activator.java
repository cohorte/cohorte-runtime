/**
 * File:   Activator.java
 * Author: Thomas Calmant
 * Date:   3 juil. 2013
 */
package org.cohorte.shell.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Registers the OSGi utility commands
 *
 * @author Thomas Calmant
 */
public class Activator implements BundleActivator {

    /** The service registration */
    private ServiceRegistration<OsgiCommands> pRegistration;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext aContext) {

        // Set up properties
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("osgi.command.scope", "cohorte");
        properties.put("osgi.command.function", new String[] { "services",
                "providers", "service" });

        // Register the service
        pRegistration = aContext.registerService(OsgiCommands.class,
                new OsgiCommands(aContext), properties);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext aContext) {

        // Unregister the service
        if (pRegistration != null) {
            pRegistration.unregister();
            pRegistration = null;
        }
    }
}
