package org.cohorte.isolates.slf4j_ocil;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author ogattaz
 *
 */
public class Activator implements BundleActivator {

	private static BundleContext sBundleContext;

	private static Activator sMe;

	/**
	 * @return
	 */
	public static BundleContext getContext() {
		return sBundleContext;
	}

	/**
	 * @return
	 */
	public static Activator getSingleton() {
		return sMe;
	}

	/**
	 *
	 */
	public Activator() {
		super();
		sMe = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		sBundleContext = context;
		final Bundle wBundle = context.getBundle();
		System.out.printf("%50s | Bundle=[%50s][%s] started\n",
				"Activator.start()", wBundle.getSymbolicName(),
				wBundle.getVersion());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		System.out.printf("%50s | Bundle=[%50s] stopped\n", "Activator.stop()",
				context.getBundle().getSymbolicName());

		sBundleContext = null;
	}
}
