package org.psem2m.isolates.slave.agent;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;
import org.psem2m.isolates.base.activators.IActivatorBase;

/**
 * @author ogattaz
 * 
 */
public class CBundleSlaveAgentActivator extends CActivatorBase implements
        IActivatorBase {

    /** Current valid instance */
    private static CBundleSlaveAgentActivator sInstance = null;

    /**
     * Retrieves the current instance of the activator
     * 
     * @return the current instance of the activator
     */
    public static CBundleSlaveAgentActivator getInstance() {

        return sInstance;
    }

    public CBundleSlaveAgentActivator() {

        super();
        sInstance = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CActivatorBase#getBundleId()
     */
    @Override
    public String getBundleId() {

        return getClass().getPackage().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext aBundleContext) throws Exception {

        sInstance = this;
        super.start(aBundleContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext aBundleContext) throws Exception {

        super.stop(aBundleContext);
        sInstance = null;
    }
}
