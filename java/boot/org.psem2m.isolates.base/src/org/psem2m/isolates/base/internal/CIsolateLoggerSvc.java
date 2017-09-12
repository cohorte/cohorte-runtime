/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.base.internal;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityLoggerNull;
import org.psem2m.utilities.logging.IActivityLoggerJul;
import org.psem2m.utilities.logging.IActivityRequester;

/**
 * CIsolateLoggerSvc is registered as the IIsolateLoggerSvc service in the start
 * method of the CIsolateLoggerChannel
 *
 * @author ogattaz
 *
 */
public class CIsolateLoggerSvc implements IIsolateLoggerSvc {

	/**
	 * The logger presence key name
	 */
	private static final String LIB_HAS_AL = "hasActivityLogger";

	/**
	 * Reference to the wrapped ActivityLogger (never null)
	 */
	private IActivityLoggerJul pActivityLogger;

	/**
	 * MOD_OG_1.0.14
	 *
	 * @param aActivityLogger
	 *            Reference to the ActivityLogger
	 * @throws Exception
	 */
	CIsolateLoggerSvc(final IActivityLoggerJul aActivityLogger)
			throws Exception {

		super();
		if (aActivityLogger == null) {
			throw new Exception(
					"Unable to instanciate CIsolateLoggerSvc using a nul ActivityLogger");
		}
		pActivityLogger = aActivityLogger;
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

		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_HAS_AL,
				hasActivityLogger());
		return aBuffer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLogger#close()
	 */
	@Override
	public void close() {
		pActivityLogger.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLogger#getJulLogger()
	 */
	@Override
	public Logger getJulLogger() {
		return pActivityLogger.getJulLogger();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#getLevel()
	 */
	@Override
	public Level getLevel() {

		return pActivityLogger.getLevel();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLogger#getRequester()
	 */
	@Override
	public IActivityRequester getRequester() {

		return pActivityLogger.getRequester();
	}

	/**
	 * Tests if the reference to activity logger is valid
	 *
	 * @return True if the reference is valid
	 */
	public boolean hasActivityLogger() {

		return pActivityLogger != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogDebugOn()
	 */
	@Override
	public boolean isLogDebugOn() {

		return pActivityLogger != null ? pActivityLogger.isLogDebugOn() : false;
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

		return pActivityLogger != null ? pActivityLogger.isLoggable(aLevel)
				: false;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogInfoOn()
	 */
	@Override
	public boolean isLogInfoOn() {

		return pActivityLogger != null ? pActivityLogger.isLogInfoOn() : false;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogSevereOn()
	 */
	@Override
	public boolean isLogSevereOn() {

		return pActivityLogger != null ? pActivityLogger.isLogSevereOn()
				: false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogWarningOn()
	 */
	@Override
	public boolean isLogWarningOn() {

		return pActivityLogger != null ? pActivityLogger.isLogWarningOn()
				: false;
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

		pActivityLogger.log(aLevel, aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
	 * .LogRecord)
	 */
	@Override
	public void log(final LogRecord record) {

		pActivityLogger.log(record);

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

		pActivityLogger.logDebug(aWho, aWhat, aInfos);

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

		pActivityLogger.logInfo(aWho, aWhat, aInfos);

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

		pActivityLogger.logSevere(aWho, aWhat, aInfos);

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

		pActivityLogger.logWarn(aWho, aWhat, aInfos);

	}

	/**
	 * @param aActivityLogger
	 */
	protected void setActivityLoggerBase(
			final IActivityLoggerJul aActivityLogger) {

		pActivityLogger = aActivityLogger != null ? aActivityLogger
				: CActivityLoggerNull.getInstance();
	}

	@Override
	public void setLevel(Level aLevel) {
		pActivityLogger.setLevel(aLevel);

	}

	@Override
	public void setLevel(String aLevelName) {
		pActivityLogger.setLevel(aLevelName);

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
