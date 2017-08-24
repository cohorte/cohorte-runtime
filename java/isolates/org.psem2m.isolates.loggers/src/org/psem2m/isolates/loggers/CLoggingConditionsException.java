package org.psem2m.isolates.loggers;

/**
 * @author ogattaz
 *
 */
public class CLoggingConditionsException extends CLoggersException {

	/**
	 *
	 */
	private static final long serialVersionUID = 2756725230374068011L;

	/**
	 * @param aFormat
	 * @param aArgs
	 */
	public CLoggingConditionsException(final String aFormat,
			final Object... aArgs) {
		super(String.format(aFormat, aArgs));
	}

	/**
	 * @param aCause
	 * @param aFormat
	 * @param aArgs
	 */
	public CLoggingConditionsException(final Throwable aCause,
			final String aFormat, final Object... aArgs) {
		super(String.format(aFormat, aArgs), aCause);
	}

}
