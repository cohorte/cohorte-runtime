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
package org.psem2m.isolates.tool.restserver;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.CActivatorBase;
import org.psem2m.isolates.base.IActivatorBase;
import org.psem2m.isolates.base.IIsolateLoggerSvc;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleRestServerActivator extends CActivatorBase implements
		IActivatorBase, IIsolateLoggerSvc {

	/** first instance **/
	private static CBundleRestServerActivator sSingleton = null;

	/**
	 * @return
	 */
	public static CBundleRestServerActivator getInstance() {
		return sSingleton;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleRestServerActivator() {
		super();
		if (sSingleton == null) {
			sSingleton = this;
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
