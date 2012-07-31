package org.psem2m.composer.ui;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;

/**
 * @author ogattaz
 * 
 */
public class CComposerUiActivator extends CActivatorBase implements
        BundleActivator {

    /** Current instance **/
    private static CComposerUiActivator sSingleton = null;

    /**
     * Retrieves the current bundle instance
     * 
     * @return The bundle instance
     */
    public static CComposerUiActivator getInstance() {

        return sSingleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // nothing...
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
        sSingleton = this;

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
        sSingleton = null;
    }

}
