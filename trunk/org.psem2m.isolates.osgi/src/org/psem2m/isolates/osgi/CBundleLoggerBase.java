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

import java.util.logging.Level;

import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.CLogLineBuffer;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.logging.IActivityRequester;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CBundleLoggerBase extends CPojoBase implements
		IBundleLoggerService {

	/** The activity logger of this bundle **/
	protected IActivityLogger pBundleLogger = null;

	/**
	 * Explicit default constructor
	 */
	public CBundleLoggerBase() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#close()
	 */
	@Override
	public void close() {
		pBundleLogger.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {
		if (pBundleLogger != null) {
			close();
			pBundleLogger = null;
		}
	}

	public abstract String getBundleId();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#getRequester()
	 */
	@Override
	public IActivityRequester getRequester() {
		return pBundleLogger.getRequester();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogDebugOn()
	 */
	@Override
	public boolean isLogDebugOn() {
		return pBundleLogger.isLogDebugOn();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#isLoggable(java.util
	 * .logging.Level)
	 */
	@Override
	public boolean isLoggable(final Level aLevel) {
		return pBundleLogger.isLoggable(aLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogInfoOn()
	 */
	@Override
	public boolean isLogInfoOn() {
		return pBundleLogger.isLogInfoOn();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogSevereOn()
	 */
	@Override
	public boolean isLogSevereOn() {
		return pBundleLogger.isLogSevereOn();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogWarningOn()
	 */
	@Override
	public boolean isLogWarningOn() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
	 * .Level, java.lang.Object, java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void log(final Level aLevel, final Object aWho,
			final CharSequence aWhat, final Object... aInfos) {
		pBundleLogger.log(aLevel, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logDebug(java.lang.Object
	 * , java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logDebug(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		pBundleLogger.logDebug(aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logInfo(java.lang.Object
	 * , java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logInfo(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		pBundleLogger.logInfo(aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logSevere(java.lang.
	 * Object, java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logSevere(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		pBundleLogger.logSevere(aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logWarn(java.lang.Object
	 * , java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logWarn(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		pBundleLogger.logWarn(aWho, aWhat, aInfos);

	}

	/**
	 * @return
	 */
	protected IActivityLogger newBundleLogger(final CXFileDir aLogDir)
			throws Exception {
		String wBundleId = getBundleId();
		CXFile wLogFile = new CXFile(aLogDir, wBundleId + "_%g.log");
		return CActivityLoggerBasic.newLogger(wBundleId,
				wLogFile.getAbsolutePath(), IActivityLogger.ALL,
				1024 * 1024 * 100, 5);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#popLogLineBuffer()
	 */
	@Override
	public CLogLineBuffer popLogLineBuffer() {
		return pBundleLogger.popLogLineBuffer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#pushLogLineBuffer(org.psem2m
	 * .utilities.logging.CLogLineBuffer)
	 */
	@Override
	public void pushLogLineBuffer(final CLogLineBuffer aLoggerLineBuffer) {
		pBundleLogger.pushLogLineBuffer(aLoggerLineBuffer);
	}

}
