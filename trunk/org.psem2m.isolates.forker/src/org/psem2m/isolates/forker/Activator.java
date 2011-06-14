package org.psem2m.isolates.forker;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	/** Bundle context */
	private static BundleContext sBundleContext;

	/**
	 * Retrieves the current bundle context
	 * 
	 * @return The bundle context
	 */
	public static BundleContext getContext() {
		return sBundleContext;
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
		Activator.sBundleContext = aBundleContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext aBundleContext) throws Exception {
		Activator.sBundleContext = null;
	}
}
