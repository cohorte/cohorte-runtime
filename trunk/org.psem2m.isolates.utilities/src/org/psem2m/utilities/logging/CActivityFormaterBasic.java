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

	/**
   * 
   */
	public CActivityFormaterBasic() {
		super();
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized String format(LogRecord aRecord) {
		pSB.delete(0, pSB.length());

		pSB.append(aRecord.getMillis());
		pSB.append(SEP_COLUMN);
		pSB.append(getFormatedNanoSecs());
		pSB.append(SEP_COLUMN);
		pSB.append(formatDate(aRecord.getMillis()));
		pSB.append(SEP_COLUMN);
		pSB.append(formatTime(aRecord.getMillis()));
		pSB.append(SEP_COLUMN);
		pSB.append(formatLevel(aRecord.getLevel()));
		pSB.append(SEP_COLUMN);
		addThreadNameInLogLine(pSB, Thread.currentThread().getName());
		pSB.append(SEP_COLUMN);
		pSB.append(formatWho(aRecord.getSourceClassName()));
		pSB.append(SEP_COLUMN);
		pSB.append(formatWhat(aRecord.getSourceMethodName()));
		pSB.append(SEP_COLUMN);
		pSB.append(aRecord.getMessage().replace(SEP_LINE, '§'));
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
	private String formatLevel(Level aLevel) {
		return CXStringUtils.strAdjustLeft(aLevel.getName(), LENGTH_LEVEL, ' ');

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatTime(long aMillis) {
		return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatWhat(String aMethod) {
		return CXStringUtils.strAdjustLeft(
				aMethod.replace(SEP_COLUMN, REPLACE_COLUMN), LENGTH_WHAT, ' ');

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatWho(String aWho) {
		return CXStringUtils.strAdjustLeft(
				aWho.replace(SEP_COLUMN, REPLACE_COLUMN), LENGTH_WHO, ' ');

	}

}
