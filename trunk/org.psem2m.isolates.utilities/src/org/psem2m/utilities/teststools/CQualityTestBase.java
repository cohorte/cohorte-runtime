package org.psem2m.utilities.teststools;

import java.util.logging.Level;

import org.psem2m.utilities.CXDateTime;

/**
 * @author isandlaTech - ogattaz
 * 
 */
public class CQualityTestBase {

	final static String SEP_TIME = ":";

	/**
	 * @param aWho
	 * @param aLevel
	 * @param aWhat
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	private String formatLog(final Object aWho, final Level aLevel,
			final CharSequence aWhat, final CharSequence aFormat,
			final Object... aArgs) {

		return "[" + formatTime(System.currentTimeMillis()) + "][" + aLevel
				+ "][" + Thread.currentThread().getName() + "]"
				+ aWho.getClass().getSimpleName() + ":" + aWhat.toString()
				+ " | " + String.format(String.valueOf(aFormat), aArgs);
	}

	/**
	 * @param aMillis
	 * @return
	 */
	private String formatTime(final long aMillis) {
		return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);

	}

	/**
	 * @param aWho
	 * @param aLevel
	 * @param aWhat
	 * @param aFormat
	 * @param aArgs
	 */
	protected void log(final Object aWho, final Level aLevel,
			final CharSequence aWhat, final CharSequence aFormat,
			final Object... aArgs) {
		System.out.println(formatLog(aWho, aLevel, aWhat, aFormat, aArgs));
	}

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aFormat
	 * @param aArgs
	 */
	protected void logDebug(final Object aWho, final CharSequence aWhat,
			final CharSequence aFormat, final Object... aArgs) {
		log(aWho, Level.SEVERE, aWhat, aFormat, aArgs);
	}

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aFormat
	 * @param aArgs
	 */
	protected void logFine(final Object aWho, final CharSequence aWhat,
			final CharSequence aFormat, final Object... aArgs) {
		log(aWho, Level.FINE, aWhat, aFormat, aArgs);
	}

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aFormat
	 * @param aArgs
	 */
	protected void logInfo(final Object aWho, final CharSequence aWhat,
			final CharSequence aFormat, final Object... aArgs) {
		log(aWho, Level.INFO, aWhat, aFormat, aArgs);
	}

	/**
	 * @param aWho
	 * @param aMethodName
	 */
	protected void logMethodName(final Object aWho,
			final CharSequence aMethodName) {
		log(aWho, Level.INFO, aMethodName, null);
	}

}
