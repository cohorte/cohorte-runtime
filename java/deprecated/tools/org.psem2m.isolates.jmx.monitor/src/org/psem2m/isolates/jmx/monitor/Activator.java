/**
 * File:   Activator.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2011
 */
package org.psem2m.isolates.jmx.monitor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.psem2m.isolates.jmx.monitor.impl.JmxMonitor;
import org.psem2m.isolates.services.monitoring.IThreadCpuUsageMonitor;

/**
 * JMX thread monitor bundle activator
 *
 * @author Thomas Calmant
 */
public class Activator implements BundleActivator {

	/** The bundle context */
	private static BundleContext sContext;

	/**
	 * Retrieves the current bundle context
	 * 
	 * @return The bundle context
	 */
	public static BundleContext getContext() {

		return sContext;
	}

	/** The JMX Monitor service registration */
	private ServiceRegistration<IThreadCpuUsageMonitor> pJmxMonitorRegistration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext aBundleContext) throws Exception {

		sContext = aBundleContext;

		// Register the JMX Monitor service
		pJmxMonitorRegistration = aBundleContext.registerService(
				IThreadCpuUsageMonitor.class, new JmxMonitor(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext aBundleContext) throws Exception {

		sContext = null;

		// Unregister the JMX monitor service
		pJmxMonitorRegistration.unregister();
	}
}
