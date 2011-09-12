package org.psem2m.isolates.monitor;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;

public class CBundleMonitorActivator extends CActivatorBase implements
		IBundleMonitorActivator {

	/** first instance **/
	private static IBundleMonitorActivator sBundleMonitorActivator = null;

	/**
	 * @return
	 */
	public static IBundleMonitorActivator getInstance() {
		return sBundleMonitorActivator;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleMonitorActivator() {
		super();
		if (sBundleMonitorActivator == null) {
			sBundleMonitorActivator = this;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.utilities.osgi.CActivatorBase#getBundleId()
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
		super.start(aBundleContext);
		// ...
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
		// ...
	}
}
