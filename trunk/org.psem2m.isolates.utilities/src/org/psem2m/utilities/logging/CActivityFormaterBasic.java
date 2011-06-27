package org.psem2m.utilities.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXStringUtils;

public class CActivityFormaterBasic extends CActivityFormater {

	private final static int LENGTH_LEVEL = 7;
	private final static int LENGTH_WHAT = 20;
	private final static int LENGTH_WHO = 20;
	final static char SEP_COLUMN = ';';
	final static char SEP_MILLI = '.';
	private boolean pMultiline = false;

	/**
   * 
   */
	public CActivityFormaterBasic() {
		super();
	}

	/**
	 * @param aAccepted
	 */
	public void acceptMultiline(final boolean aAccepted) {
		pMultiline = aAccepted;
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized String format(final LogRecord aRecord) {

		return format(aRecord.getMillis(), aRecord.getLevel(),
				aRecord.getSourceClassName(), aRecord.getSourceMethodName(),
				aRecord.getMessage()) + SEP_LINE;
	}

	/**
	 * @param aMillis
	 * @param aLevel
	 * @param aSourceClassName
	 * @param aSourceMethodName
	 * @param aLine
	 * @return
	 */
	public String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aLine) {
		pSB.delete(0, pSB.length());

		pSB.append(aMillis);
		pSB.append(SEP_COLUMN);
		pSB.append(getFormatedNanoSecs());
		pSB.append(SEP_COLUMN);
		pSB.append(formatDate(aMillis));
		pSB.append(SEP_COLUMN);
		pSB.append(formatTime(aMillis));
		pSB.append(SEP_COLUMN);
		pSB.append(formatLevel(aLevel));
		pSB.append(SEP_COLUMN);
		addThreadNameInLogLine(pSB, Thread.currentThread().getName());
		pSB.append(SEP_COLUMN);
		pSB.append(formatWho(aSourceClassName));
		pSB.append(SEP_COLUMN);
		pSB.append(formatWhat(aSourceMethodName));
		pSB.append(SEP_COLUMN);
		pSB.append((pMultiline) ? aLine : aLine.replace(SEP_LINE, '§'));
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
	 * @param aLevel
	 * @return
	 */
	private String formatLevel(final Level aLevel) {
		return CXStringUtils.strAdjustLeft(aLevel.getName(), LENGTH_LEVEL, ' ');

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatTime(final long aMillis) {
		return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatWhat(final String aMethod) {
		return CXStringUtils.strAdjustLeft(
				aMethod.replace(SEP_COLUMN, REPLACE_COLUMN), LENGTH_WHAT, ' ');

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatWho(final String aWho) {
		return CXStringUtils.strAdjustLeft(
				aWho.replace(SEP_COLUMN, REPLACE_COLUMN), LENGTH_WHO, ' ');

	}

}
