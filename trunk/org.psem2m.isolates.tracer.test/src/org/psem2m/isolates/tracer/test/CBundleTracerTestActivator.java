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
package org.psem2m.isolates.tracer.test;

import org.psem2m.isolates.base.CActivatorBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleTracerTestActivator extends CActivatorBase implements
		IBundleTracerTestActivator {

	/** first instance **/
	private static IBundleTracerTestActivator sBundleTracerTesterActivator = null;

	/**
	 * @return
	 */
	public static IBundleTracerTestActivator getInstance() {
		return sBundleTracerTesterActivator;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleTracerTestActivator() {
		super();
		if (sBundleTracerTesterActivator == null) {
			sBundleTracerTesterActivator = this;
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

}
