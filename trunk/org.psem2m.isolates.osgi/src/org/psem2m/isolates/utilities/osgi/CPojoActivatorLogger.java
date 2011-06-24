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
package org.psem2m.isolates.utilities.osgi;

import java.util.logging.Level;

import org.osgi.service.log.LogService;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityFormaterBasic;
import org.psem2m.utilities.logging.CLogFormater;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * This is an Activator which implement the IActivityLoggerBase interface to
 * provide a direct access to the logger of the isolate .
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CPojoActivatorLogger extends CPojoActivatorBase implements
		IActivityLoggerBase {

	/**
	 * @param aLevel
	 * @return
	 */
	private static int levelToLogServiceLevel(final Level aLevel) {
		if (aLevel.intValue() == Level.INFO.intValue()) {
			return LogService.LOG_ERROR;
		} else if (aLevel.intValue() == Level.FINE.intValue()) {
			return LogService.LOG_ERROR;
		} else if (aLevel.intValue() == Level.WARNING.intValue()) {
			return LogService.LOG_ERROR;
		} else if (aLevel.intValue() == Level.SEVERE.intValue()) {
			return LogService.LOG_ERROR;
		}
		return LogService.LOG_INFO;
	}

	private final CActivityFormaterBasic pCActivityFormaterBasic = new CActivityFormaterBasic();

	private final CLogFormater pLogFormater = new CLogFormater();

	/**
	 * Explicit default constructor
	 */
	public CPojoActivatorLogger() {
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
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_ID, getIdentifier());
		return aBuffer;
	}

	/**
	 * @return
	 */
	public IActivityLoggerBase getIsolateLogger() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogDebugOn()
	 */
	@Override
	public boolean isLogDebugOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#isLoggable(java.util.logging
	 * .Level)
	 */
	@Override
	public boolean isLoggable(final Level aLevel) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogInfoOn()
	 */
	@Override
	public boolean isLogInfoOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogSevereOn()
	 */
	@Override
	public boolean isLogSevereOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#log(java.util.logging.Level,
	 * java.lang.Object, java.lang.CharSequence, java.lang.Object[])
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogWarningOn()
	 */
	@Override
	public boolean isLogWarningOn() {
		return true;
	}

	@Override
	public void log(final Level aLevel, final Object aWho,
			final CharSequence aWhat, final Object... aInfos) {

		CharSequence wWhat = (aWhat != null) ? aWhat : CXJavaRunContext
				.getPreCallingMethod();

		logInLogService(levelToLogServiceLevel(aLevel),
				pCActivityFormaterBasic.format(System.currentTimeMillis(),
						aLevel, pLogFormater.getWhoObjectId(aWho),
						wWhat.toString(), pLogFormater.formatLogLine(aInfos)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logDebug(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logDebug(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.FINE, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logInfo(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logInfo(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.INFO, aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logSevere(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logSevere(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.SEVERE, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logWarn(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logWarn(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.WARNING, aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(new StringBuilder(128)).toString();
	}
}
