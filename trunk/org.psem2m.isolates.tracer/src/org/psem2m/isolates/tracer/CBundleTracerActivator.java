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
package org.psem2m.isolates.tracer;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.CActivatorBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleTracerActivator extends CActivatorBase implements
		IBundleTracerActivator {

	/** first instance **/
	private static IBundleTracerActivator sBundleTracerActivator = null;

	/**
	 * @return
	 */
	public static IBundleTracerActivator getInstance() {
		return sBundleTracerActivator;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleTracerActivator() {
		super();
		if (sBundleTracerActivator == null) {
			sBundleTracerActivator = this;
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
		return CBundleTracerActivator.class.getPackage().getName();

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
