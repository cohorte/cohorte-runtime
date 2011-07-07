package org.psem2m.isolates.loggers;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.CActivatorBase;

public class CBundleLoggersActivator extends CActivatorBase implements
		IBundleLoggersActivator {

	/** first instance **/
	private static IBundleLoggersActivator sBundleLoggersActivator = null;

	/**
	 * @return
	 */
	public static IBundleLoggersActivator getInstance() {
		return sBundleLoggersActivator;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleLoggersActivator() {
		super();
		if (sBundleLoggersActivator == null) {
			sBundleLoggersActivator = this;
		}
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
	 * @see org.psem2m.isolates.utilities.osgi.CActivatorBase#getBundleId()
	 */
	@Override
	public String getBundleId() {
		return CBundleLoggersActivator.class.getPackage().getName();
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
		super.start(bundleContext);
		// ...
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
		// ...
	}

}
