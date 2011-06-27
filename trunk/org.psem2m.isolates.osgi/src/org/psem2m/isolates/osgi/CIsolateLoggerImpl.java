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

import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityFormaterBasic;
import org.psem2m.utilities.logging.CLogFormater;

/**
 * This is an Activator which implement the IIsolateLogger interface to provide
 * a direct access to the logger of the isolate .
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CIsolateLoggerImpl extends CPojoBase implements
		IIsolateLoggerService {

	public static String LIB_ILL = "IsolateLoggerLevel";

	/**
	 * @param aLevel
	 * @return
	 */
	private static int levelToLogServiceLevel(final Level aLevel) {
		if (aLevel.intValue() == Level.INFO.intValue()) {
			return LogService.LOG_INFO;
		} else if (aLevel.intValue() == Level.FINE.intValue()) {
			return LogService.LOG_DEBUG;
		} else if (aLevel.intValue() == Level.WARNING.intValue()) {
			return LogService.LOG_WARNING;
		} else if (aLevel.intValue() == Level.SEVERE.intValue()) {
			return LogService.LOG_ERROR;
		}
		return LogService.LOG_INFO;
	}

	/**
	 * The formater which format jthe log line
	 * 
	 * <pre>
	 * TimeStamp     TimeStamp      TimeStamp  TimeStamp    Level    Thread name       Instance id          Method               LogLine
	 * (millis)      (nano)         (date)     (hhmmss.sss) 
	 * 1309180295049;00000065317000;2011/06/27;15:11:35:049;INFO   ;   FelixStartLevel;CIsolateLogger_2236 ;__validatePojo      ;EnvContext:
	 * </pe>
	 **/
	private final CActivityFormaterBasic pCActivityFormaterBasic;

	/** The formater which converts the array of objects to a line **/
	private final CLogFormater pLogFormater;

	/** LogReaderService reference managed by iPojo (see metadata.xml) **/
	private LogReaderService pLogReaderService;

	/** LogService reference managed by iPojo (see metadata.xml) **/
	private LogService pLogService;

	/**
	 * Explicit default constructor
	 */
	public CIsolateLoggerImpl() {
		super();

		pCActivityFormaterBasic = new CActivityFormaterBasic();
		pCActivityFormaterBasic.acceptMultiline(true);

		pLogFormater = new CLogFormater();
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
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_ILL,
				Level.FINE.toString());
		return aBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {

	}

	/**
	 * @return
	 */
	public IIsolateLoggerService getIsolateLogger() {
		return this;
	}

	/**
	 * @return
	 */
	public Level getIsolateLoggerLevel(final int aLogServiceLevel) {
		switch (aLogServiceLevel) {
		case LogService.LOG_INFO:
			return Level.INFO;
		case LogService.LOG_DEBUG:
			return Level.FINE;
		case LogService.LOG_WARNING:
			return Level.WARNING;
		case LogService.LOG_ERROR:
		default:
			return Level.SEVERE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CActivatorBase#getPojoId()
	 */
	@Override
	public String getPojoId() {
		return "isolates-osgi-isolate-logger";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() {
		// log in the main logger of the isolate
		logInfo(this, null, "INVALIDATE", toDescription());

		destroy();
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

		String wWhat = EMPTY;
		if (aWhat != null) {
			wWhat = aWhat.toString();
		} else {
			// @see CXJavaRunContext getPreCallingMethod()
			int wlevel = 3;
			wWhat = CXJavaRunContext.getMethod("log", wlevel);
			while (wWhat != null
					&& (wWhat.startsWith("__") || wWhat.startsWith("log"))) {
				wlevel++;
				wWhat = CXJavaRunContext.getMethod("log", wlevel);
			}
			if (wWhat == null) {
				wWhat = "???";
			}

		}

		pLogService.log(levelToLogServiceLevel(aLevel), pCActivityFormaterBasic
				.format(System.currentTimeMillis(), aLevel,
						pLogFormater.getWhoObjectId(aWho), wWhat.toString(),
						pLogFormater.formatLogLine(aInfos)));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {
		// configure the main logger of the isolate
		pLogReaderService.addLogListener(new CIsolateLoggerListener());

		// log in the main logger of the isolate
		logInfo(this, null, "VALIDATE", toDescription());

		// org.psem2m.platform.isolate.id=[development]
		if (!("development".equalsIgnoreCase(CPlatformDirsImpl
				.getCurrentIsolateId()))) {

			logInfo(this, null, "JavaContext:\n%s", CXJvmUtils.getJavaContext());

			logInfo(this, null, "EnvContext:\n%s", CXOSUtils.getEnvContext());
		}

	}
}
