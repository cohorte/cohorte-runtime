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
package org.psem2m.isolates.forker.tester;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;

public class CBundleForkerTestActivator extends CActivatorBase implements
		IBundleForkerTestActivator {

	/** first instance **/
	private static IBundleForkerTestActivator sBundleForkerTestActivator = null;

	/**
	 * @return
	 */
	public static IBundleForkerTestActivator getInstance() {
		return sBundleForkerTestActivator;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleForkerTestActivator() {
		super();
		if (sBundleForkerTestActivator == null) {
			sBundleForkerTestActivator = this;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {

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
