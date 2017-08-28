package org.psem2m.isolates.loggers;

/**
 * @author ogattaz
 *
 */
public class CLoggersException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1109486025824328252L;

	/**
	 * @param aFormat
	 * @param aArgs
	 */
	public CLoggersException(final String aFormat, final Object... aArgs) {
		super(String.format(aFormat, aArgs));
	}

	/**
	 * @param aCause
	 * @param aFormat
	 * @param aArgs
	 */
	public CLoggersException(final Throwable aCause, final String aFormat,
			final Object... aArgs) {
		super(String.format(aFormat, aArgs), aCause);
	}

}
