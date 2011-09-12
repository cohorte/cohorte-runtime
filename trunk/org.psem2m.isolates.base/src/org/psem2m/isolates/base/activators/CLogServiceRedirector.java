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
package org.psem2m.isolates.base.activators;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * this singleton redirects the log lines of the OSGI log service in the
 * Activity logger of the current isolate.
 * 
 * The first extends of CActivatorBase which can bind the OSGI LogReader service
 * put in place a LogListener which will redirect all the logs to the logger of
 * the isolate.
 * 
 * @see org.psem2m.isolates.base.activators.CActivatorBase
 * 
 *      The redirection is manageable using the system property
 *      "org.psem2m.platform.isolate.redirect.logservice"
 * 
 *      <pre>
 * -Dorg.psem2m.platform.isolate.redirect.logservice=true
 * </pre>
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogServiceRedirector {

	/**
	 * @author isandlatech (www.isandlatech.com) - ogattaz
	 * 
	 */
	class CLogListener implements LogListener {

		/** the reference to the CLogIsolatesRedirector singleton **/
		private final CLogIsolatesRedirector pLogIsolatesRedirector = CLogIsolatesRedirector
				.getInstance();

		/**
		 * Explicit default constructor
		 */
		CLogListener() {
			super();
		}

		/**
		 * @param aLogServiceLevel
		 * @return
		 */
		private Level convertLogServiceLevel(final int aLogServiceLevel) {

			if (aLogServiceLevel == LogService.LOG_INFO) {
				return Level.INFO;
			}
			if (aLogServiceLevel == LogService.LOG_DEBUG) {
				return Level.FINE;
			}
			if (aLogServiceLevel == LogService.LOG_ERROR) {
				return Level.SEVERE;
			}
			if (aLogServiceLevel == LogService.LOG_WARNING) {
				return Level.WARNING;
			}
			return Level.ALL;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.service.log.LogListener#logged(org.osgi.service.log.LogEntry
		 * )
		 */
		@Override
		public void logged(final LogEntry aLogEntry) {

			Level wLevel = convertLogServiceLevel(aLogEntry.getLevel());

			// build a log record and gives it to the the isolate logger
			LogRecord wRecord = new LogRecord(wLevel, aLogEntry.getMessage());
			wRecord.setMillis(aLogEntry.getTime());
			wRecord.setSourceClassName(aLogEntry.getBundle().getSymbolicName());
			wRecord.setSourceMethodName("ls.");
			pLogIsolatesRedirector.log(wRecord);

		}
	}

	/** the instance of the CLogServiceRedirector singleton **/
	private final static CLogServiceRedirector sCLogServiceRedirector = new CLogServiceRedirector();

	/**
	 * @return the instance of the CLogServiceRedirector singleton
	 */
	private static CLogServiceRedirector getInstance() {
		return sCLogServiceRedirector;
	}

	/**
	 * @return true if a Listener exists
	 */
	public static boolean hasListener() {
		return getInstance().doHasListener();
	}

	/**
	 * @return true if a LogListener is put in place. False it is already done.
	 */
	static boolean putInPlaceLogListener(
			final LogReaderService aLogReaderService) {
		return getInstance().doPutInPlaceLogListener(aLogReaderService);
	}

	/**
	 * @return
	 */
	static int relogLogEntries() {
		return getInstance().doRelogLogEntries();
	}

	/**
	 * @return true if a LogListener
	 */
	static boolean removeLogListener() {
		return getInstance().doRemoveLogListener();
	}

	/** **/
	private CLogListener pLogListener = null;

	/** **/
	private LogReaderService pLogReaderService = null;

	/**
	 * Explicit default constructor
	 */
	private CLogServiceRedirector() {
		super();
	}

	/**
	 * @return
	 */
	private synchronized boolean doHasListener() {
		return pLogListener != null;
	}

	/**
	 * @return an instance if no instance has never be delivered
	 */
	private synchronized boolean doPutInPlaceLogListener(
			final LogReaderService aLogReaderService) {
		// if the redirection mustn't put in place
		if (!mustBePutInPlace()) {
			return false;
		}
		// return null if the pLogListener exists ( if this method was already
		// called)
		if (pLogListener != null) {
			return false;
		}
		pLogReaderService = aLogReaderService;
		pLogReaderService.addLogListener(newLogServiceListener());

		return true;
	}

	/**
	 * re-log all the lines aready logged (stored in the aLogReaderService).
	 * This method is called
	 * 
	 */
	private int doRelogLogEntries() {
		int wI = 0;
		if (pLogReaderService != null) {
			@SuppressWarnings("unchecked")
			final Enumeration<LogEntry> wEntries = pLogReaderService.getLog();
			LogEntry wEntry;
			while (wEntries.hasMoreElements()) {
				wEntry = wEntries.nextElement();
				// re-log an LogEntry
				pLogListener.logged(wEntry);
				wI++;
			}
		}
		return wI;
	}

	/**
	 * @return true if a LogListener is removed
	 */
	private synchronized boolean doRemoveLogListener() {
		if (pLogReaderService == null || pLogListener == null) {
			return false;
		}
		pLogReaderService.removeLogListener(pLogListener);
		pLogListener = null;
		pLogReaderService = null;
		return true;
	}

	/**
	 * @return
	 */
	private boolean mustBePutInPlace() {
		String wRedirectLogService = System
				.getProperty(IPlatformProperties.PROP_PLATFORM_REDIRECT_LOGSVC);
		return "true".equalsIgnoreCase(wRedirectLogService);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	private synchronized CLogListener newLogServiceListener() {
		if (pLogListener == null) {
			pLogListener = new CLogListener();
		}
		return pLogListener;
	}

}
