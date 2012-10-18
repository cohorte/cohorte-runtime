package org.psem2m.signals.impl;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;
import org.psem2m.isolates.base.activators.IActivatorBase;

/**
 * @author ogattaz
 * 
 */
public class CBundleSignalsImplActivator extends CActivatorBase implements
        IActivatorBase {

    /** Current valid instance */
    private static CBundleSignalsImplActivator sInstance = null;

    /**
     * Retrieves the current instance of the activator
     * 
     * @return the current instance of the activator
     */
    public static CBundleSignalsImplActivator getInstance() {

        return sInstance;
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
        sInstance = this;

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
        sInstance = null;
    }
}
