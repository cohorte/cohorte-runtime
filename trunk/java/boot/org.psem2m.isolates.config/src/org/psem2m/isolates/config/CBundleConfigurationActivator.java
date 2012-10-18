/**
 * File:   CBundleConfigurationActivator.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;

/**
 * @author Thomas Calmant
 * 
 */
public class CBundleConfigurationActivator extends CActivatorBase {

    /** An instance of the bundle activator */
    private static CBundleConfigurationActivator sBundleInstance = null;

    /**
     * Retrieves an already created instance of the bundle activator, null if
     * the activator was never called
     * 
     * @return An instance of the activator or null
     */
    public static CBundleConfigurationActivator getInstance() {

        return sBundleInstance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext bundleContext) throws Exception {

        // Store the singleton reference
        sBundleInstance = this;

        super.start(bundleContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext bundleContext) throws Exception {

        super.stop(bundleContext);

        // Forget the singleton reference
        sBundleInstance = null;
    }
}
