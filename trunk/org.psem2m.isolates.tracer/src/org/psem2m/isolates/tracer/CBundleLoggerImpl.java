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

import org.psem2m.isolates.osgi.CBundleLoggerBase;
import org.psem2m.isolates.osgi.IBundleLoggerService;
import org.psem2m.isolates.osgi.IIsolateLoggerService;
import org.psem2m.isolates.osgi.IPlatformDirsService;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleLoggerImpl extends CBundleLoggerBase implements
		IBundleLoggerService {

	private static CBundleLoggerImpl sBundleLogger;

	/**
	 * @return
	 */
	public static CBundleLoggerImpl getInstance() {
		return sBundleLogger;
	}

	/** LogService reference managed by iPojo (see metadata.xml) **/
	private IIsolateLoggerService pIsolateLoggerService;

	/** LogService reference managed by iPojo (see metadata.xml) **/
	private IPlatformDirsService pPlatformDirsService;

	/**
	 * Explicit default constructor
	 */
	public CBundleLoggerImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#getPojoId()
	 */
	@Override
	public String getPojoId() {
		return "isolates-tracer-bundle-logger";
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

		destroy();
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
		try {
			pBundleLogger = newBundleLogger(pPlatformDirsService
					.getIsolateLogDir());
		} catch (Throwable e) {
			pIsolateLoggerService.logSevere(this, null, e);
		}
	}
}
