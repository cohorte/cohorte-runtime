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
package org.psem2m.isolates.base.impl;

import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IPlatformDirsSvc;
import org.psem2m.isolates.base.IPojoBase;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CPlatformDirsSvc extends CPojoBase implements IPlatformDirsSvc,
		IPojoBase {

	public static String LIB_ISOLATE_ID = "IsolateId";

	/**
	 * <pre>
	 * org.psem2m.platform.isolate.id=[development]
	 * </pre>
	 * 
	 * @return
	 */
	private static String getCurrentIsolateId() {
		return System.getProperty(PROP_PLATFORM_ISOLATE_ID);
	}

	/**
	 * <pre>
	 * org.psem2m.platform.base=[/Users/ogattaz/workspaces/psem2m/psem2m/platforms/felix.user.dir]
	 * </pre>
	 * 
	 * @return
	 */
	static String getCurrentPlatformBase() {
		return System.getProperty(PROP_PLATFORM_BASE);
	}

	/** Service reference managed by iPojo (see metadata.xml) **/
	private IActivityLoggerBase pLoggerSvc;

	/**
	 * Explicit default constructor
	 */
	public CPlatformDirsSvc() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_ISOLATE_ID,
				getCurrentIsolateId());
		return aBuffer;
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
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getCurrentIsolateId()
	 */
	@Override
	public String getIsolateId() {
		return getCurrentIsolateId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir()
	 */
	@Override
	public CXFileDir getIsolateLogDir() throws Exception {
		return getIsolateLogDir(getCurrentIsolateId());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir(java.lang.String)
	 */
	@Override
	public CXFileDir getIsolateLogDir(final String aIsolateId) throws Exception {
		CXFileDir wLogDir = new CXFileDir(getPlatformLogDir(), aIsolateId);
		if (!wLogDir.exists()) {
			wLogDir.createHierarchy();
		}
		return wLogDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformBaseDir()
	 */
	@Override
	public CXFileDir getPlatformBaseDir() {
		return new CXFileDir(getCurrentPlatformBase());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformLogDir()
	 */
	@Override
	public CXFileDir getPlatformLogDir() throws Exception {
		CXFileDir wLogDir = new CXFileDir(getPlatformBaseDir(),
				"var/log/psem2m");
		if (!wLogDir.exists()) {
			wLogDir.createHierarchy();
		}
		return wLogDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() {
		// logs in the bundle output
		pLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
				toDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {
		// logs in the bundle output
		pLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
				toDescription());
	}

}
