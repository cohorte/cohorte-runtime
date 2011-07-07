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

import org.psem2m.utilities.files.CXFileDir;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface IPlatformDirsService {

	public final static String PROP_PLATFORM_BASE = "org.psem2m.platform.base";
	public final static String PROP_PLATFORM_ISOLATE_ID = "org.psem2m.platform.isolate.id";

	/**
	 * @return the id of the current isolate
	 */
	public String getIsolateId();

	/**
	 * @return the log directory of the current isolate
	 * @throws Exception
	 */
	public CXFileDir getIsolateLogDir() throws Exception;

	/**
	 * @param aIsolateId
	 *            the id of an isolate
	 * @return the log directory of the isolate
	 * @throws Exception
	 *             if the hiearchy doesn't exist and can't be created
	 */
	public CXFileDir getIsolateLogDir(final String aIsolateId) throws Exception;

	/**
	 * <pre>
	 * -Dorg.psem2m.platform.base=${workspace_loc}/psem2m/platforms/felix.user.dir/logs
	 * </pre>
	 * 
	 * @return the base directory of the platform
	 */
	public CXFileDir getPlatformBaseDir();

	/**
	 * @return the log directory of the platform
	 * @throws Exception
	 */
	public CXFileDir getPlatformLogDir() throws Exception;
}
