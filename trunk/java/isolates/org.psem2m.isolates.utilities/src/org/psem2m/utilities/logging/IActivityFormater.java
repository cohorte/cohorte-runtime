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
package org.psem2m.utilities.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface IActivityFormater {

	/**
	 * the value of the multi-lines-text flag to not replace the end-line in the
	 * text of the log line
	 **/
	public final static boolean MULTILINES_TEXT = true;

	public final static boolean SHORT_LINE = true;
	
	/**
	 * the value of the end-line flag to obtain an end-line at the end of the
	 * formated line
	 **/
	public final static boolean WITH_END_LINE = true;

	/**
	 * @param aAccepted
	 *            don't replace the line separators if true
	 */
	public void acceptMultiline(final boolean aAccepted);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	public String format(final LogRecord aRecord);

	/**
	 * @param record
	 *            the log record to be formatted.
	 * @param aWhithEndLine
	 *            append an end line if true
	 * @return the formatted log record
	 */
	public String format(final LogRecord aRecord, final boolean aWhithEndLine);

	/**
	 * @param aMillis
	 *            the timestamp ofthe line
	 * @param aLevel
	 *            the level of the log
	 * @param aSourceClassName
	 *            the name of the class which fired the log line
	 * @param aSourceMethodName
	 *            the name of the method which fired the log line
	 * @param aText
	 *            the text of the line
	 * @return the formatted log line without an end line
	 */
	public String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aText);

	/**
	 * @param aMillis
	 *            the timestamp ofthe line
	 * @param aLevel
	 *            the level of the log
	 * @param aSourceClassName
	 *            the name of the class which fired the log line
	 * @param aSourceMethodName
	 *            the name of the method which fired the log line
	 * @param aText
	 *            the text of the line
	 * @param aWhithEndLine
	 *            append an end line if true
	 * @return the formatted log line
	 */
	public String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aText, final boolean aWhithEndLine);

}
