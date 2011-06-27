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

import org.psem2m.isolates.osgi.CPojoBase;
import org.psem2m.isolates.osgi.IIsolateLoggerService;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CTracerImpl extends CPojoBase implements ITracerService {

	/** LogService reference managed by iPojo (see metadata.xml) **/
	private IIsolateLoggerService pIsolateLoggerService;

	/**
	 * Explicite default constructor
	 */
	public CTracerImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {
		// ...
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#getPojoId()
	 */
	@Override
	public String getPojoId() {
		return "isolates-tracer-tracer";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() {
		// log in the main logger of the isolate
		pIsolateLoggerService
				.logInfo(this, null, "INVALIDATE", toDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {
		// log in the main logger of the isolate
		pIsolateLoggerService.logInfo(this, null, "VALIDATE", toDescription());
	}

}
