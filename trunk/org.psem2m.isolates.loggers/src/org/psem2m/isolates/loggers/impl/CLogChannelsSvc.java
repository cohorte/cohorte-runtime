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
package org.psem2m.isolates.loggers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.psem2m.isolates.base.CLogIsolatesRedirector;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IPlatformDirsSvc;
import org.psem2m.isolates.loggers.IBundleLoggersLoggerSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogChannelsSvc extends CPojoBase implements ILogChannelsSvc {

	/** Service reference managed by iPojo (see metadata.xml) **/
	private IBundleLoggersLoggerSvc pBundleLoggersLoggerSvc;

	TreeMap<String, IActivityLogger> pLoggers = new TreeMap<String, IActivityLogger>();

	/** Service reference managed by iPojo (see metadata.xml) **/
	private IPlatformDirsSvc pPlatformDirsSvc;

	/**
	 * Explicit default constructor
	 */
	public CLogChannelsSvc() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {
		for (Entry<String, IActivityLogger> wLoggerEntry : pLoggers.entrySet()) {
			wLoggerEntry.getValue().close();
		}
		pLoggers.clear();
	}

	/**
	 * @return the list of available channels
	 */
	@Override
	public List<IActivityLogger> getChannels() {
		List<IActivityLogger> wLoggers = new ArrayList<IActivityLogger>();
		for (Entry<String, IActivityLogger> wLoggerEntry : pLoggers.entrySet()) {
			wLoggers.add(wLoggerEntry.getValue());
		}

		return wLoggers;
	}

	/**
	 * @return the list of the ids of the available channels
	 */
	@Override
	public List<String> getChannelsIds() {
		List<String> wIds = new ArrayList<String>();
		for (Entry<String, IActivityLogger> wLoggerEntry : pLoggers.entrySet()) {
			wIds.add(wLoggerEntry.getKey());
		}
		return wIds;
	}

	@Override
	public IActivityLoggerBase getLogChannel(final String aChannelId)
			throws Exception {

		IActivityLogger wLogger = pLoggers.get(aChannelId);
		if (wLogger != null) {
			return wLogger;
		}

		return newLogger(aChannelId);
	}

	/**
	 * @param aChannelId
	 * @param aLogDir
	 * @return
	 * @throws Exception
	 */
	private IActivityLogger instanciateLogger(final String aChannelId,
			final CXFileDir aLogDir) throws Exception {
		CXFile wLogFile = new CXFile(aLogDir, aChannelId + "_%g.log");
		return CActivityLoggerBasic.newLogger(aChannelId,
				wLogFile.getAbsolutePath(), IActivityLogger.ALL,
				1024 * 1024 * 100, 5);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() {
		// logs in the bundle output
		pBundleLoggersLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
				toDescription());
		destroy();
	}

	/**
	 * @param aChannelId
	 * @return
	 */
	private IActivityLogger newLogger(final String aChannelId) throws Exception {

		IActivityLogger wLogger = instanciateLogger(aChannelId,
				pPlatformDirsSvc.getIsolateLogDir());

		pLoggers.put(aChannelId, wLogger);

		return wLogger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {

		// logs in the bundle output
		pBundleLoggersLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
				toDescription());

		try {
			IActivityLogger wLogger = newLogger("isolate-"
					+ pPlatformDirsSvc.getIsolateId());

			wLogger.logInfo(this, "validatePojo", CXJvmUtils.getJavaContext());

			CLogIsolatesRedirector.getInstance().setListener(wLogger);

		} catch (Exception e) {
			pBundleLoggersLoggerSvc.logSevere(this, "validatePojo", e);
		}
	}
}
