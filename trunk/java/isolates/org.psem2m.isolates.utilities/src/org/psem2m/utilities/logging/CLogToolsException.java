package org.psem2m.utilities.logging;

import org.psem2m.utilities.CXException;

/**
 * @author ogattaz
 * 
 */
public class CLogToolsException {

	private final static String FORMAT_EXCEPTION = "Exception={%s] ";
	private final static String FORMAT_MESAGE = "Message={%s] ";
	private final static String FORMAT_THROWABLE = "InitialThrowable={%s]";

	private final static String LIB_THROWABLE_DUMP_ERROR = "EXCEPTION DURING THE DUMP OF A THROWABLE";

	private final static String MESS_CANT_GET_FLOS = "Can't get first the first line of the stack ! errot=[%s]\n%s";
	private final static String MESS_CANT_GET_FPLIS = "Can't get first the first line of package [%s] in the stack ! errot=[%s]\n%s";
	private final static String MESS_CANT_GET_STACK = "Can't get Stack in string ! errot=[%s]\n%s";
	private final static String NO_STACK = "no stack available";

	private static final String PREFIX_CLASS = "class";
	private static final String PREFIX_MESS = "mess";
	private static final String PREFIX_STACK = "stack";

	private final static String PSEM2M_CLASSES_PREFIX = "org.psem2m.";

	private final static CLogToolsException sCLogToolsException = new CLogToolsException();

	static final char SEPARATOR_COMA = ',';

	static final char SEPARATOR_LINE = '\n';

	/**
	 * @return
	 */
	public static CLogToolsException getInstance() {
		return sCLogToolsException;
	}

	/**
	 * 
	 */
	private CLogToolsException() {
		super();
	}

	/**
	 * Ajoute la composante (classe) d'une exception dans un StringBuilder
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private Appendable addEClassInBuffer(final Appendable aSB, final Throwable e)
			throws Exception {
		return aSB.append(PREFIX_CLASS).append('=').append('[')
				.append(e.getClass().getName()).append(']');
	}

	/**
	 * @param aSB
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	private Appendable addEDescrFullInBuffer(final Appendable aSB,
			final Throwable e, final char aSeparator) throws Exception {
		if (aSeparator == SEPARATOR_LINE) {
			aSB.append(aSeparator);
		}
		addEClassInBuffer(aSB, e).append(SEPARATOR_LINE);

		addEMessInBuffer(aSB, e, aSeparator).append(SEPARATOR_LINE);

		addEStackInBuffer(aSB, e, aSeparator).append(SEPARATOR_LINE);

		return aSB;
	}

	/**
	 * Ajoute la composante (mess) d'une exception dans un Buffer *
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private Appendable addEMessInBuffer(final Appendable aSB,
			final Throwable e, final char aSeparator) throws Exception {

		if (e == null) {
			return aSB.append("no message, throwable is null");
		}

		String wMess = e.getLocalizedMessage();
		if (wMess == null || wMess.isEmpty()) {
			wMess = e.getMessage();
		}
		if (wMess == null || wMess.isEmpty()) {
			wMess = "no message in " + e.getClass().getSimpleName();
		}

		return aSB.append(PREFIX_MESS).append('=').append('[').append(wMess)
				.append(']');
	}

	/**
	 * Ajoute la composante (stack) d'une exception dans un StringBuilder
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private Appendable addEStackInBuffer(final Appendable aSB,
			final Throwable e, final char aSeparator) throws Exception {
		return aSB.append(PREFIX_STACK).append('=').append('[')
				.append(eStack(e, aSeparator)).append(']');
	}

	/**
	 * @param e
	 * @param aDumpedThrowable
	 * @return
	 */
	private String buildExceptionDumpErrorMess(final Exception e,
			final Throwable aDumpedThrowable) {
		StringBuilder wSB = new StringBuilder();
		wSB.append(LIB_THROWABLE_DUMP_ERROR);
		if (e != null) {
			wSB.append(String.format(FORMAT_EXCEPTION, e.getClass()
					.getSimpleName()));
			wSB.append(String.format(FORMAT_MESAGE, e.getMessage()));
			wSB.append(CXException.getFirstLineOfStack(e));
		}
		if (aDumpedThrowable != null) {
			wSB.append(String.format(FORMAT_THROWABLE, aDumpedThrowable
					.getClass().getSimpleName()));
			wSB.append(String.format(FORMAT_MESAGE,
					aDumpedThrowable.getMessage()));
			wSB.append(CXException.getCleanedStackOfThrowable(aDumpedThrowable));
		}
		return wSB.toString();
	}

	/**
	 * @param aStatck
	 * @return
	 */
	private String cutStackAfterOrgPsem2m(String aStatck) {
		int wPos = aStatck.lastIndexOf(PSEM2M_CLASSES_PREFIX);
		if (wPos > -1) {
			wPos = aStatck.indexOf('\n', wPos);
			if (wPos > 0) {
				aStatck = aStatck.substring(0, wPos);
			}
		}
		return aStatck;
	}

	/**
	 * Retourne les composantes (classe,why ,mess,stack) d'une exception dans
	 * une string
	 * 
	 * @param e
	 * @return
	 */
	public String eInString(final Throwable e) {
		return eInString(e, SEPARATOR_LINE);
	}

	/**
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	public String eInString(final Throwable aThrowable, final char aSeparator) {
		try {
			return addEDescrFullInBuffer(new StringBuilder(512), aThrowable,
					aSeparator).toString();
		} catch (Exception e) {
			e.initCause(aThrowable);
			e.printStackTrace();
			return buildExceptionDumpErrorMess(e, aThrowable);
		}
	}

	/**
	 * @param aShift
	 * @param e
	 * @return
	 */
	private String eStack(final String aShift, final Throwable e,
			final char aSeparator) {
		StringBuilder wTmp = new StringBuilder();
		StackTraceElement[] wStackElements = e.getStackTrace();
		StackTraceElement wStackElement;
		int wMax = wStackElements.length;
		if (wMax == 0) {
			wTmp.append("No stack information found");
		} else {
			int wI = 0;
			while (wI < wMax) {
				wStackElement = wStackElements[wI];
				if (wI > 0) {
					wTmp.append(aSeparator);
				}
				if (aShift != null) {
					wTmp.append(aShift);
				}
				wTmp.append(wStackElement.getClassName());
				wTmp.append('(');
				wTmp.append(wStackElement.getMethodName());
				wTmp.append(':');
				wTmp.append(wStackElement.getLineNumber());
				wTmp.append(')');
				wI++;
			}
		}
		return wTmp.toString();
	}

	/**
	 * @param e
	 * @return
	 */
	private String eStack(final Throwable e) {
		return eStack(null, e, SEPARATOR_LINE);
	}

	/**
	 * @param e
	 * @return
	 */
	private String eStack(final Throwable e, final char aSeparator) {
		return eStack(null, e, aSeparator);
	}

	/**
	 * Retourne la stack d'une exception dans une String
	 * 
	 * @param e
	 * @return
	 */
	String eStackToString(final Throwable e) {
		try {
			String wS = eStack(e);
			if (wS == null) {
				return NO_STACK;
			}

			return cutStackAfterOrgPsem2m(wS);
		} catch (Throwable e2) {
			return messCantGetStack(e2);
		}
	}

	/**
	 * @param e
	 * @return
	 */
	String firstLineOfStackInString(final Throwable e) {
		try {
			String wStack = eStack(e);
			if (wStack == null) {
				return NO_STACK;
			}
			int wPos = wStack.indexOf(SEPARATOR_LINE);
			if (wPos > 0) {
				return wStack.substring(0, wPos);
			} else {
				return wStack;
			}
		} catch (Throwable e2) {
			return messCantGetFirstLine(e2);
		}
	}

	/**
	 * 
	 * 
	 * @param e
	 * @param aPackagePrefix
	 * @return
	 */
	private String getFirstPackageLineInStack(final Throwable e,
			final String aPackagePrefix) {
		try {
			String wS = eStack(e);

			int wPos = (aPackagePrefix != null) ? wS.indexOf(aPackagePrefix)
					: -1;

			if (wPos > -1) {
				wS = wS.substring(wPos);
				wPos = wS.indexOf('\n');
				if (wPos > 0) {
					wS = wS.substring(0, wPos);
				}

			}
			return wS;
		} catch (Throwable e2) {
			return messCantGetFirstPackageLine(e2, aPackagePrefix);
		}
	}

	/**
	 * 
	 * @param e
	 * @return the first line of the stack in a string
	 */
	String getFirstPsem2mLineInStack(final Throwable e) {
		return getFirstPackageLineInStack(e, PSEM2M_CLASSES_PREFIX);
	}

	/**
	 * @param e
	 * @param aPackagePrefix
	 * @return
	 */
	private String messCantGetFirstLine(final Throwable e) {
		return String.format(MESS_CANT_GET_FLOS, e.getMessage(), eStack(e));
	}

	/**
	 * @param e
	 * @param aPackagePrefix
	 * @return
	 */
	private String messCantGetFirstPackageLine(final Throwable e,
			final String aPackagePrefix) {
		return String.format(MESS_CANT_GET_FPLIS, aPackagePrefix,
				e.getMessage(), eStack(e));
	}

	/**
	 * @param e
	 */
	private String messCantGetStack(final Throwable e) {
		return String.format(MESS_CANT_GET_STACK, e.getMessage(), eStack(e));
	}
}
