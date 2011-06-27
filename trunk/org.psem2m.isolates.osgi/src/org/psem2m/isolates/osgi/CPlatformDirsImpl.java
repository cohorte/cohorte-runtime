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
package org.psem2m.isolates.osgi;

import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.files.CXFileDir;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CPlatformDirsImpl extends CPojoBase implements
		IPlatformDirsService, IPojoBase {

	public static String LIB_ISOLATE_ID = "IsolateId";

	/**
	 * <pre>
	 * org.psem2m.platform.isolate.id=[development]
	 * </pre>
	 * 
	 * @return
	 */
	static String getCurrentIsolateId() {
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

	private IIsolateLoggerService pIsolateLoggerService;

	/**
	 * Explicit default constructor
	 */
	public CPlatformDirsImpl() {
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
	 * @see org.psem2m.isolates.osgi.IPojoBase#getPojoId()
	 */
	@Override
	public String getPojoId() {
		return "isolates-osgi-platform-dirs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPojoBase#invalidatePojo()
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
	 * @see org.psem2m.isolates.osgi.IPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {
		// log in the main logger of the isolate
		pIsolateLoggerService.logInfo(this, null, "VALIDATE", toDescription());
	}

}
