package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.IConstants;

/**
 * 
 * <pre>
 * TimeStamp TimeStamp TimeStamp TimeStamp Level Thread name Instance id
 * Method LogLine (millis) (nano) (date) (hhmmss.sss)
 * 1309180295049;00000065317000;2011/06/27;15:11:35:049;INFO ;
 * FelixStartLevel;CIsolateLogger_2236 ;__validatePojo ;EnvContext:
 * </pre>
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CActivityFormaterBasic extends CActivityFormater {

	/** the width of the level column **/
	protected final static int LENGTH_LEVEL = 7;

	/** the width of the what column **/
	protected final static int LENGTH_WHAT = 25;

	/** the width of the who column **/
	protected final static int LENGTH_WHO = 27;

	/** the column separator **/
	protected final static char SEP_COLUMN = ';';
	protected final static String SEP_COLUMN_DELIM = SEP_COLUMN + " ";

	/** the milliseconds separator **/
	protected final static char SEP_MILLI = '.';

	/**
	 * @return
	 */
	public static IActivityFormater getInstance() {

		return getInstance(LINE_FULL);
	}

	/**
	 * @param aShortLine
	 *            line without unformated milli and nano id true
	 * @return
	 */
	public static IActivityFormater getInstance(boolean aShortLine) {

		return getInstance((aShortLine) ? LINE_SHORT : LINE_FULL);
	}

	/**
	 * @param aColumsDef
	 *            the list of column in the line
	 * @return
	 */
	public static IActivityFormater getInstance(EActivityLogColumn[] aLineDef) {

		return new CActivityFormaterBasic(aLineDef);
	}

	private final boolean pWithC1Milli;
	private final boolean pWithC2Nano;
	private final boolean pWithC3Date;
	private final boolean pWithC4Time;
	private final boolean pWithC5Level;
	private final boolean pWithC6Thread;
	private final boolean pWithC7Instance;
	private final boolean pWithC8Method;
	private final boolean pWithC9Text;

	/**
	 * Explicit default constructor
	 */
	public CActivityFormaterBasic() {

		this(LINE_FULL);
	}

	/**
	 * 
	 * @param aShortFormat
	 */
	public CActivityFormaterBasic(boolean aShortLine) {

		this((aShortLine) ? LINE_SHORT : LINE_FULL);
	}
	/**
	 * @param aLineDef
	 *            the list of column in the line
	 */
	public CActivityFormaterBasic(EActivityLogColumn[] aLineDef) {

		super(aLineDef);
		pWithC1Milli = EActivityLogColumn.isColumnOn(
				EActivityLogColumn.C1_MILLI, aLineDef);
		pWithC2Nano = EActivityLogColumn.isColumnOn(EActivityLogColumn.C2_NANO,
				aLineDef);
		pWithC3Date = EActivityLogColumn.isColumnOn(EActivityLogColumn.C3_DATE,
				aLineDef);
		pWithC4Time = EActivityLogColumn.isColumnOn(EActivityLogColumn.C4_TIME,
				aLineDef);
		pWithC5Level = EActivityLogColumn.isColumnOn(
				EActivityLogColumn.C5_LEVEL, aLineDef);
		pWithC6Thread = EActivityLogColumn.isColumnOn(
				EActivityLogColumn.C6_THREAD, aLineDef);
		pWithC7Instance = EActivityLogColumn.isColumnOn(
				EActivityLogColumn.C7_INSTANCE, aLineDef);
		pWithC8Method = EActivityLogColumn.isColumnOn(
				EActivityLogColumn.C8_METHOD, aLineDef);
		pWithC9Text = EActivityLogColumn.isColumnOn(EActivityLogColumn.C9_TEXT,
				aLineDef);
	}

	/**
	 * Add a column delimitor in the log line
	 * 
	 * @param aSB
	 * @return
	 */
	StringBuilder addColummnDelimitorInLogLine(final StringBuilder aSB) {

		return super.addColummnDelimitorInLogLine(aSB, SEP_COLUMN_DELIM);
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

		StringBuilder pSB = new StringBuilder(192);

		if (pWithC1Milli) {
			pSB.append(aMillis);
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC2Nano) {
			pSB.append(getFormatedNanoSecs());
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC3Date) {
			pSB.append(formatDate(aMillis));
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC4Time) {
			pSB.append(formatTime(aMillis));

			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC5Level) {
			pSB.append(formatLevel(aLevel));
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC6Thread) {
			addThreadNameInLogLine(pSB, Thread.currentThread().getName());
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC7Instance) {
			pSB.append(formatWho(aSourceClassName));
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC8Method) {
			pSB.append(formatWhat(aSourceMethodName));
			addColummnDelimitorInLogLine(pSB);
		}
		if (pWithC9Text) {
			pSB.append(formatText(aText));
		}
		if (aWhithEndLine) {
			pSB.append(SEP_LINE);
		}
		return pSB.toString();
	}

	/**
	 * @param aLevel
	 * @return
	 */
	protected String formatDate(final long aMillis) {

		return CXDateTime.time2StrAAAAMMJJ(aMillis, SEP_DATE);

	}

	/**
	 * @param aLevel
	 * @return
	 */
	protected String formatLevel(final Level aLevel) {

		return CXStringUtils.strAdjustLeft(aLevel != null ? aLevel.getName()
				: IConstants.LIB_NULL, LENGTH_LEVEL, ' ');

	}

	protected String formatText(final String aText) {

		if (aText == null) {
			return IConstants.LIB_NULL;
		} else {
			return isMultiline() ? aText : aText.replace(SEP_LINE, '§');
		}
	}

	/**
	 * @param aLevel
	 * @return
	 */
	protected String formatTime(final long aMillis) {

		return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);

	}

	/**
	 * @param aLevel
	 * @return
	 */
	protected String formatWhat(final String aMethod) {

		return CXStringUtils.strAdjustRight(
				aMethod != null ? aMethod.replace(SEP_COLUMN, REPLACE_COLUMN)
						: IConstants.EMPTY, LENGTH_WHAT, ' ');

	}

	/**
	 * @param aLevel
	 * @return
	 */
	protected String formatWho(final String aWho) {

		return CXStringUtils.strAdjustRight(
				aWho != null ? aWho.replace(SEP_COLUMN, REPLACE_COLUMN)
						: IConstants.EMPTY, LENGTH_WHO, ' ');

	}



}
