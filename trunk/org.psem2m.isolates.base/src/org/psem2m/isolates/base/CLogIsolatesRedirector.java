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

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.logging.CLogLineTextBuilder;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * Redirects the log lines produced by the Activity logger of each bundle in the
 * Activity logger of the current isolate
 * 
 * @see org.psem2m.isolates.base.CActivatorBase
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogIsolatesRedirector {

	/** the instance of the CLogIsolatesRedirector singleton **/
	private final static CLogIsolatesRedirector sCLogIsolatesRedirector = new CLogIsolatesRedirector();

	/**
	 * @return the instance of the CLogIsolatesRedirector singleton
	 */
	public static CLogIsolatesRedirector getInstance() {
		return sCLogIsolatesRedirector;
	}

	/** the instance of logger associated to this redirector **/
	private IActivityLogger pActivityLogger = null;

	/** The formater which converts the array of objects to a line **/
	private final CLogLineTextBuilder pLogLineTextBuilder = CLogLineTextBuilder
			.getInstance();

	/** the stack of memorized log records **/
	private final Stack<LogRecord> pRecords = new Stack<LogRecord>();

	/**
	 * Explicit default constructor
	 */
	private CLogIsolatesRedirector() {
		super();
	}

	/**
	 * @return true if a logger is in place
	 */
	private synchronized boolean hasActivityLogger() {
		return pActivityLogger != null;
	}

	/**
	 * @param aLevel
	 * @param aWho
	 * @param aWhat
	 * @param aInfos
	 */
	public void log(final Level aLevel, final Object aWho,
			final CharSequence aWhat, final Object... aInfos) {
		if (hasActivityLogger()) {
			pActivityLogger.log(aLevel, aWho, aWhat, aInfos);
		} else {

			LogRecord wRecord = new LogRecord(aLevel,
					pLogLineTextBuilder.formatLogLine(aInfos));

			wRecord.setSourceClassName(pLogLineTextBuilder
					.formatWhoObjectId(aWho));
			wRecord.setSourceMethodName(String.valueOf(aWhat));
			log(wRecord);
		}
	}

	/**
	 * @param record
	 *            the LogRecord to log
	 */
	public void log(final LogRecord record) {
		if (hasActivityLogger()) {
			pActivityLogger.log(record);
		} else {
			// marks the memorized log records
			record.setMessage("## " + record.getMessage());
			// adds the record on the "top" of the stack to be popped
			pRecords.add(0, record);
		}
	}

	/**
	 * @param aActivityLogger
	 *            the logger to associate with this redirector
	 */
	public synchronized void setListener(final IActivityLogger aActivityLogger) {
		pActivityLogger = aActivityLogger;
		pActivityLogger
				.logInfo(this, null,
						"++++++++++++ isolate logger redirector set ! re-log stored LogRecords begin.");

		int wNbRec = 0;
		// if there is one or more memorized record
		if (pRecords.size() > 0) {
			try {
				while (true) {
					// pop the records (cf. removeElementAt(len-1) ).
					pActivityLogger.log(pRecords.pop());
					wNbRec++;
				}
			} catch (EmptyStackException e) {
				// ...
			}

		}
		pActivityLogger.logInfo(this, null,
				"++++++++++++ re-log stored LogRecords end. NbLogRecord=[%d]",
				wNbRec);
	}
}
