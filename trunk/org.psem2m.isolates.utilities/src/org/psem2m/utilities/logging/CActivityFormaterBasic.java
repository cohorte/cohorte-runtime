package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXStringUtils;

/**
 * 
 * <pre>
 * TimeStamp     TimeStamp      TimeStamp  TimeStamp    Level    Thread name       Instance id          Method               LogLine
 * (millis)      (nano)         (date)     (hhmmss.sss) 
 * 1309180295049;00000065317000;2011/06/27;15:11:35:049;INFO   ;   FelixStartLevel;CIsolateLogger_2236 ;__validatePojo      ;EnvContext:
 * </pre>
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CActivityFormaterBasic extends CActivityFormater {

	/** the width of the level column **/
	private final static int LENGTH_LEVEL = 7;

	/** the width of the what column **/
	private final static int LENGTH_WHAT = 25;

	/** the width of the who column **/
	private final static int LENGTH_WHO = 25;
	private final static CActivityFormaterBasic sActivityFormaterBasic = new CActivityFormaterBasic();
	/** the column separator **/
	final static char SEP_COLUMN = ';';

	/** the milliseconds separator **/
	final static char SEP_MILLI = '.';

	/**
	 * @return
	 */
	public static IActivityFormater getInstance() {
		return sActivityFormaterBasic;
	}

	/**
	 * Explicit default constructor
	 */
	public CActivityFormaterBasic() {
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
		pSB.append(formatText(aText));
		if (aWhithEndLine) {
			pSB.append(SEP_LINE);
		}
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
		return CXStringUtils.strAdjustLeft((aLevel != null) ? aLevel.getName()
				: CXStringUtils.LIB_NULL, LENGTH_LEVEL, ' ');

	}

	private String formatText(final String aText) {
		if (aText == null) {
			return CXStringUtils.LIB_NULL;
		} else {
			return (pMultiline) ? aText : aText.replace(SEP_LINE, '§');
		}
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
		return CXStringUtils.strAdjustRight(
				(aMethod != null) ? aMethod.replace(SEP_COLUMN, REPLACE_COLUMN)
						: CXStringUtils.EMPTY, LENGTH_WHAT, ' ');

	}

	/**
	 * @param aLevel
	 * @return
	 */
	private String formatWho(final String aWho) {
		return CXStringUtils.strAdjustRight(
				(aWho != null) ? aWho.replace(SEP_COLUMN, REPLACE_COLUMN)
						: CXStringUtils.EMPTY, LENGTH_WHO, ' ');

	}

}
