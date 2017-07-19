package org.psem2m.isolates.loggers;

/**
 * @author ogattaz
 *
 */
public class CLogChannelException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 2424862007434516208L;

	/**
	 * @param aFormat
	 * @param aArgs
	 */
	public CLogChannelException(final String aFormat, final Object... aArgs) {
		super(String.format(aFormat, aArgs));
	}

	/**
	 * @param aCause
	 * @param aFormat
	 * @param aArgs
	 */
	public CLogChannelException(final Throwable aCause, final String aFormat,
			final Object... aArgs) {
		super(String.format(aFormat, aArgs), aCause);
	}

}
