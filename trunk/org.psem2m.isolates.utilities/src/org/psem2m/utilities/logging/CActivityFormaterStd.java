package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXStringUtils;

/**
 * 
 * @author Adonix Grenoble
 * @version 140
 */
public class CActivityFormaterStd extends CActivityFormater {

	final static char PREFIX_LINE = '@';
	final static char REPLACE_PREFIX = '£';
	final static char SEP_COLUMN = ' ';

	/**
   * 
   */
	public CActivityFormaterStd() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.CActivityFormater#format(long,
	 * java.util.logging.Level, java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public synchronized String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aText, final boolean aWhithEndLine) {
		pSB.delete(0, pSB.length());

		pSB.append(PREFIX_LINE);
		pSB.append(SEP_COLUMN);
		pSB.append(aMillis);
		pSB.append(SEP_COLUMN);
		pSB.append(getFormatedNanoSecs());
		pSB.append(SEP_COLUMN);
		pSB.append(formatDate(aMillis));
		pSB.append(SEP_COLUMN);
		pSB.append(formatTime(aMillis));
		pSB.append(SEP_COLUMN);
		addThreadNameInLogLine(pSB, Thread.currentThread().getName());
		pSB.append(SEP_COLUMN);
		pSB.append((aSourceClassName != null) ? aSourceClassName.replace(
				SEP_COLUMN, REPLACE_COLUMN) : CXStringUtils.LIB_NULL);
		pSB.append(SEP_COLUMN);
		pSB.append((aSourceMethodName != null) ? aSourceMethodName.replace(
				SEP_COLUMN, REPLACE_COLUMN) : CXStringUtils.LIB_NULL);
		pSB.append(SEP_LINE);
		pSB.append((aLevel != null) ? aLevel.getName() : CXStringUtils.LIB_NULL);
		pSB.append(':');
		pSB.append(SEP_COLUMN);
		pSB.append(formatText(aText));
		pSB.append(SEP_LINE);
		return pSB.toString();
	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatDate(final long aMillis) {
		return CXDateTime.time2StrAAAAMMJJ(aMillis, SEP_DATE);
	}

	/**
	 * @param aText
	 * @return
	 */
	private String formatText(final String aText) {
		return (aText != null) ? aText.replace(PREFIX_LINE, REPLACE_PREFIX)
				: CXStringUtils.LIB_NULL;
	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatTime(final long aMillis) {
		return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);
	}
}
