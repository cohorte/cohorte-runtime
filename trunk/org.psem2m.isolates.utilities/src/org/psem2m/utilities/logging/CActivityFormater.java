package org.psem2m.utilities.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.CXStringUtils;

abstract class CActivityFormater extends Formatter {

	/**
	 * long: The long data type is a 64-bit signed two's complement integer.
	 * 
	 * It has a minimum value of -9,223,372,036,854,775,808 and a maximum value
	 * of 9,223,372,036,854,775,807
	 * 
	 * 1 an : 365*24*3600*1000000000 => 32850000000000000 nanosecondes => 17
	 * chiffres
	 * 
	 * 1 jour : 24*3600*1000000000 => 86400000000000 nanosecondes => 14 chiffres
	 * 
	 * 1 heure : 3600*1000000000 => 3600000000000 nanosecondes => 13 chiffres
	 */
	private final static long DAY = 86400000000000L;

	/** 1 day duration => 14 digits **/
	private final static int LENGTH_NANO = 14;

	/**
	 * the value of the multi-lines-text flag to not replace the end-line in the
	 * text of the log line
	 **/
	public final static boolean MULTILINES_TEXT = true;

	final static char REPLACE_COLUMN = '_';

	final static String SEP_DATE = "/";

	final static char SEP_LINE = '\n';
	final static char SEP_MILLI = '.';
	final static String SEP_TIME = ":";

	final static int SIZE_LOG_THREADNAME = 18;

	/**
	 * the value of the end-line flag to obtain an end-line at the end of the
	 * formated line
	 **/
	public final static boolean WITH_END_LINE = true;

	/**
	 * the flag to control the replacement of the end-lin in the text of the log
	 * line.
	 **/
	protected boolean pMultiline = !MULTILINES_TEXT;

	StringBuilder pSB = new StringBuilder(128);

	/**
	 * Differences in successive calls that span greater than approximately 292
	 * years (2<sup>63</sup> nanoseconds) will not accurately compute elapsed
	 * time due to numerical overflow.
	 */
	private long pStartTime = 0;

	private final CLogTools pTools = CLogTools.getInstance();

	/**
	 * 
	 */
	public CActivityFormater() {
		super();
		initStartTime();
	}

	/**
	 * @param aAccepted
	 */
	public void acceptMultiline(final boolean aAccepted) {
		pMultiline = aAccepted;
	}

	/**
	 * @param aSB
	 * @param aThreadName
	 * @return
	 */
	StringBuilder addThreadNameInLogLine(final StringBuilder aSB,
			final String aThreadName) {
		aSB.append(pTools.strAdjustRight(aThreadName, SIZE_LOG_THREADNAME, ' '));
		return aSB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(final LogRecord aRecord) {
		return format(aRecord, WITH_END_LINE);

	}

	/**
	 * @param record
	 *            the log record to be formatted.
	 * @param aWhithEndLine
	 *            append an end line if true
	 * @return the formatted log record
	 */
	public String format(final LogRecord aRecord, final boolean aWhithEndLine) {
		return format(aRecord.getMillis(), aRecord.getLevel(),
				aRecord.getSourceClassName(), aRecord.getSourceMethodName(),
				aRecord.getMessage(), aWhithEndLine);
	}

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
			final String aText) {
		return format(aMillis, aLevel, aSourceClassName, aSourceMethodName,
				aText, !WITH_END_LINE);
	}

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
	public abstract String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aText, final boolean aWhithEndLine);

	/**
	 * @return
	 */
	String getFormatedNanoSecs() {
		return CXStringUtils.strAdjustRight(getMaximizedNanos(), LENGTH_NANO);
	}

	/**
	 * @return
	 */
	private long getMaximizedNanos() {
		long wDuration = getNanos();
		if (wDuration > getNanosInDay()) {
			initStartTime();
			wDuration = getNanos();
		}
		return wDuration;
	}

	/**
	 * @return
	 */
	private long getNanos() {
		return System.nanoTime() - pStartTime;
	}

	/**
	 * @return
	 */
	private long getNanosInDay() {
		return DAY;
	}

	/**
   * 
   */
	private void initStartTime() {
		pStartTime = System.nanoTime();
	}
}
