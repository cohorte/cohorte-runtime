package org.psem2m.utilities.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
abstract class CActivityFormater extends Formatter implements IActivityFormater {

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

	final static char REPLACE_COLUMN = '_';

	final static String SEP_DATE = "/";

	final static char SEP_LINE = '\n';
	final static char SEP_MILLI = '.';
	final static String SEP_TIME = ":";

	final static int SIZE_LOG_THREADNAME = 16;

	private final EActivityLogColumn[] pLineDef;

	/**
	 * the flag to control the replacement of the end-lin in the text of the log
	 * line.
	 **/
	protected boolean pMultiline = !MULTILINES_TEXT;

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
		this(IActivityFormater.LINE_FULL);
	}

	/**
	 * @param aLineDef
	 */
	public CActivityFormater(EActivityLogColumn[] aLineDef) {

		super();
		pLineDef = aLineDef;
		initStartTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityFormater#acceptMultiline(boolean)
	 */
	@Override
	public void acceptMultiline(final boolean aAccepted) {

		pMultiline = aAccepted;
	}

	/**
	 * @param aSB
	 * @param aThreadName
	 * @return
	 */
	StringBuilder addColoumnDelimitorInLogLine(final StringBuilder aSB,
			final String aThreadName) {

		aSB.append(pTools.strAdjustLeft(aThreadName, SIZE_LOG_THREADNAME, ' '));
		return aSB;
	}

	/**
	 * Add a column delimitor in the log line
	 * 
	 * @param aSB
	 * @param aSepColumn
	 * @return
	 */
	StringBuilder addColummnDelimitorInLogLine(final StringBuilder aSB,
			final String aColumnDelimitor) {

		aSB.append(aColumnDelimitor);
		return aSB;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityFormater#format(java.util.logging
	 * .LogRecord, boolean)
	 */
	@Override
	public String format(final LogRecord aRecord, final boolean aWhithEndLine) {

		return format(aRecord.getMillis(), aRecord.getLevel(),
				aRecord.getSourceClassName(), aRecord.getSourceMethodName(),
				aRecord.getMessage(), aWhithEndLine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityFormater#format(long,
	 * java.util.logging.Level, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aText) {

		return format(aMillis, aLevel, aSourceClassName, aSourceMethodName,
				aText, !WITH_END_LINE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityFormater#format(long,
	 * java.util.logging.Level, java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public abstract String format(final long aMillis, final Level aLevel,
			final String aSourceClassName, final String aSourceMethodName,
			final String aText, final boolean aWhithEndLine);

	/**
	 * @return
	 */
	String getFormatedNanoSecs() {

		return CXStringUtils.strAdjustRight(getMaximizedNanos(), LENGTH_NANO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityFormater#getLineDef()
	 */
	@Override
	public EActivityLogColumn[] getLineDef() {
		return pLineDef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityFormater#getLineDefInString()
	 */
	@Override
	public String getLineDefInString() {
		return EActivityLogColumn.lineDefToString(getLineDef());
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

	/**
	 * @return true if this formater accept multiline text
	 */
	protected boolean isMultiline() {

		return pMultiline;
	}
}
