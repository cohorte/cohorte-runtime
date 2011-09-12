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
package org.psem2m.isolates.base;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;
import org.psem2m.isolates.base.activators.IActivatorBase;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleBaseActivator extends CActivatorBase implements
		IActivatorBase, IActivityLoggerBase {

	/** first instance **/
	private static CBundleBaseActivator sSingleton = null;

	/**
	 * @return
	 */
	public static CBundleBaseActivator getInstance() {
		return sSingleton;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleBaseActivator() {
		super();
		if (sSingleton == null) {
			sSingleton = this;
		}
	}

	@Override
	public void destroy() {
		// ...
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CActivatorBase#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception {
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
		// ...
	}

}
