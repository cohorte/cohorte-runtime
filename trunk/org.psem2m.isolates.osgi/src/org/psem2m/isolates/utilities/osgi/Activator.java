/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.utilities.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class Activator extends CPojoActivatorLogger {

	/** The activity logger of this bundle **/
	private final IActivityLogger pBundleLogger;

	// LogService reference managed by iPojo (see metadata.xml)
	private LogService pLogService;

	/**
	 * Explicit default constructor
	 */
	public Activator() {
		super();

		System.out.println(CXJvmUtils.getJavaContext());

		System.out.println(CXOSUtils.getEnvContext());

		// instanciate the activity logger of this bundle
		pBundleLogger = newBundleLogger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {

		if (pBundleLogger != null) {
			pBundleLogger.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.utilities.osgi.CActivatorBase#getBundleId()
	 */
	@Override
	public String getBundleId() {
		return Activator.class.getPackage().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.utilities.osgi.CActivatorBase#getBundleLogger()
	 */
	@Override
	public IActivityLogger getBundleLogger() {
		return pBundleLogger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.utilities.osgi.CActivatorBase#logInLogService(int,
	 * java.lang.String)
	 */
	@Override
	protected void logInLogService(final int aLevel, final String aLine) {
		pLogService.log(aLevel, aLine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void startPojo(final BundleContext context) throws Exception {

		// configure the main logger of the isolate
		ServiceReference wLogReaderServiceRef = context
				.getServiceReference(LogReaderService.class.getName());
		if (wLogReaderServiceRef != null) {
			LogReaderService reader = (LogReaderService) context
					.getService(wLogReaderServiceRef);
			reader.addLogListener(new CLogListener());
		}

		// log in the main logger of the isolate
		logInfo(this, null, "START", toDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stopPojo(final BundleContext context) throws Exception {
		// log in the main logger of the isolate
		logInfo(this, null, "STOP", toDescription());

		destroy();

	}

}
