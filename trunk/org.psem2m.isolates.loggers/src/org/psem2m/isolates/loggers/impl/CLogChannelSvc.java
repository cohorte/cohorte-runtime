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

import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.utilities.logging.CActivityLoggerBasic;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogChannelSvc extends CActivityLoggerBasic implements
		ILogChannelSvc {

	/**
	 * @param aTracer
	 * @param aLoggerName
	 * @param aFilePathPattern
	 *            the pattern for naming the output file
	 * @param aLevel
	 *            the value for the log level (may be null)
	 * @param aFileLimit
	 *            the maximum number of bytes to write to any one file
	 * @param aFileCount
	 *            the number of files to use
	 * @return
	 * @throws Exception
	 */
	public static ILogChannelSvc newLogger(final String aLoggerName,
			final String aFilePathPattern, final String aLevel,
			final int aFileLimit, final int aFileCount) throws Exception {

		CLogChannelSvc wLogger = new CLogChannelSvc(aLoggerName,
				aFilePathPattern, aLevel, aFileLimit, aFileCount);
		wLogger.initFileHandler();
		wLogger.open();
		return wLogger;
	}

	/**
	 * @param aLoggerName
	 * @param aFilePathPattern
	 * @param aLevel
	 * @param aFileLimit
	 * @param aFileCount
	 * @throws Exception
	 */
	protected CLogChannelSvc(final String aLoggerName,
			final String aFilePathPattern, final String aLevel,
			final int aFileLimit, final int aFileCount) throws Exception {
		super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
		// TODO Auto-generated constructor stub
	}

}
