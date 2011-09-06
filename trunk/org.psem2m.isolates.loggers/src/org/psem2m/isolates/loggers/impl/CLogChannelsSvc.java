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
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * 
 * This service is a Service factory.
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogChannelsSvc extends CPojoBase implements ILogChannelsSvc {

	/**
	 * @author isandlatech (www.isandlatech.com) - ogattaz
	 * 
	 */
	class CActivityLoggerPsem2m extends CActivityLoggerBasic implements
			ILogChannelSvc {

		/**
		 * @param aLoggerName
		 * @param aFilePathPattern
		 * @param aLevel
		 * @param aFileLimit
		 * @param aFileCount
		 * @throws Exception
		 */
		CActivityLoggerPsem2m(final String aLoggerName,
				final String aFilePathPattern, final String aLevel,
				final int aFileLimit, final int aFileCount) throws Exception {
			super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
			initFileHandler();
			open();
		}

	}

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 * 
	 * This service is the logger of the current bundle
	 **/
	private IIsolateLoggerSvc pIsolateLoggerSvc;

	/** the "repository" of the opened logging channels **/
	TreeMap<String, ILogChannelSvc> pLoggers = new TreeMap<String, ILogChannelSvc>();

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
		for (Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers.entrySet()) {
			wLoggerEntry.getValue().close();
		}
		pLoggers.clear();
	}

	/**
	 * @return the list of available channels
	 */
	@Override
	public List<ILogChannelSvc> getChannels() {
		List<ILogChannelSvc> wLoggers = new ArrayList<ILogChannelSvc>();
		for (Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers.entrySet()) {
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
		for (Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers.entrySet()) {
			wIds.add(wLoggerEntry.getKey());
		}
		return wIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#getLogChannel(java.lang.String
	 * )
	 */
	@Override
	public ILogChannelSvc getLogChannel(final String aChannelId)
			throws Exception {

		ILogChannelSvc wLogger = pLoggers.get(aChannelId);
		if (wLogger != null) {
			return wLogger;
		}

		return newLogChannel(aChannelId);
	}

	/**
	 * @param aChannelId
	 * @param aLogDir
	 * @return
	 * @throws Exception
	 */
	private ILogChannelSvc instanciateLogChannel(final String aChannelId,
			final CXFileDir aLogDir) throws Exception {
		CXFile wLogFile = new CXFile(aLogDir, aChannelId + "_%g.log");
		return new CActivityLoggerPsem2m(aChannelId,
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
		pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
				toDescription());
		destroy();
	}

	/**
	 * @param aChannelId
	 * @return
	 */
	public ILogChannelSvc newLogChannel(final String aChannelId)
			throws Exception {

		ILogChannelSvc wLogger = instanciateLogChannel(aChannelId,
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

		// logs in the isolate logger
		pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
				toDescription());

		try {
			IActivityLogger wLogger = newLogChannel("isolate-"
					+ pPlatformDirsSvc.getIsolateId());

			wLogger.logInfo(this, "validatePojo", CXJvmUtils.getJavaContext());

			CLogIsolatesRedirector.getInstance().setListener(wLogger);

		} catch (Exception e) {
			pIsolateLoggerSvc.logSevere(this, "validatePojo", e);
		}
	}
}
