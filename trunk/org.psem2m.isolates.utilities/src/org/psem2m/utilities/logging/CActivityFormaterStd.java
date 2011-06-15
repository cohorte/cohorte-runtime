package org.psem2m.utilities.logging;

import java.util.logging.LogRecord;

import org.psem2m.utilities.CXDateTime;

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
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized String format(LogRecord aRecord) {
		pSB.delete(0, pSB.length());

		pSB.append(PREFIX_LINE);
		pSB.append(SEP_COLUMN);
		pSB.append(aRecord.getMillis());
		pSB.append(SEP_COLUMN);
		pSB.append(getFormatedNanoSecs());
		pSB.append(SEP_COLUMN);
		pSB.append(formatDate(aRecord.getMillis()));
		pSB.append(SEP_COLUMN);
		pSB.append(formatTime(aRecord.getMillis()));
		pSB.append(SEP_COLUMN);
		addThreadNameInLogLine(pSB, Thread.currentThread().getName());
		pSB.append(SEP_COLUMN);
		pSB.append(aRecord.getSourceClassName().replace(SEP_COLUMN,
				REPLACE_COLUMN));
		pSB.append(SEP_COLUMN);
		pSB.append(aRecord.getSourceMethodName().replace(SEP_COLUMN,
				REPLACE_COLUMN));
		pSB.append(SEP_LINE);
		pSB.append(aRecord.getLevel().getName());
		pSB.append(':');
		pSB.append(SEP_COLUMN);
		pSB.append(aRecord.getMessage().replace(PREFIX_LINE, REPLACE_PREFIX));
		pSB.append(SEP_LINE);
		return pSB.toString();
	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatDate(long aMillis) {
		return CXDateTime.time2StrAAAAMMJJ(aMillis, SEP_DATE);
	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatTime(long aMillis) {
		return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);
	}
}
