package org.psem2m.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ogattaz
 * 
 */
class CExceptionMessage implements IXDescriber {

	private final static String LABEL_MESS = "Mess=[%s]";

	private final static String LABEL_TYP = "Typ=[%s]";

	private final String pMessage;

	/**
   * 
   */
	private final EExceptionMessageTyp pTyp;

	/**
	 * @param pTyp
	 */
	CExceptionMessage(EExceptionMessageTyp aTyp, String aMessage) {
		pTyp = aTyp;
		pMessage = aMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		try {
			aSB.append(String.format(LABEL_MESS, getMessage()));
			aSB.append(String.format(LABEL_TYP, pTyp.toString()));
		} catch (Exception e) {
			return new StringBuilder().append(aSB).append(e);
		}
		return aSB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IDescriber#calcLengthOfDescription()
	 */
	public int calcDescriptionLength() {
		return 256;
	}

	/**
	 * @param aTyp
	 * @return
	 */
	boolean filter(EExceptionMessageTyp... aTypes) {
		return pTyp.filter(aTypes);
	}

	/**
	 * @return
	 */
	String getMessage() {
		return pMessage;
	}

	/**
	 * @return
	 */
	String getMessageFull() throws Exception {

		return toDescription();

	}

	/**
	 * @return
	 */
	EExceptionMessageTyp getType() {
		return pTyp;
	}

	/**
	 * @return
	 */
	boolean isTechnical() {
		return pTyp.isTechnical();
	}

	/**
	 * @param aTyp
	 * @return
	 */
	boolean isTyp(EExceptionMessageTyp aTyp) {
		return pTyp.isTyp(aTyp);
	}

	/**
	 * @return
	 */
	boolean isUser() {
		return pTyp.isUser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IDescriber#getDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pMessage;
	}
}

/**
 * @author ogattaz
 * 
 */
class CExceptionMessages extends ArrayList<CExceptionMessage> implements
		IXDescriber {

	private final static String LABEL_SIZE = "Size";

	private static final long serialVersionUID = 8088582612614427127L;

	/**
   * 
   */
	CExceptionMessages() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		try {
			aBuffer.append(String.format(LABEL_SIZE + "[%d]", size()));
		} catch (Throwable e) {
			return new StringBuilder().append(aBuffer).append(e);
		}
		return aBuffer;
	}

	/**
	 * @param aMessage
	 */
	void addDetailMessage(String aMessage) {
		add(new CExceptionMessage(EExceptionMessageTyp.DETAIL, aMessage));
	}

	/**
	 * @param aSB
	 * @param aTyp
	 * @param aSeparator
	 * @return
	 */
	StringBuilder addMessagesInSB(StringBuilder aSB, EExceptionMessageTyp aTyp,
			char aSeparator) {
		CExceptionMessage wExceptionMessage;
		int wMax = size();
		int wI = 0;
		while (wI < wMax) {
			wExceptionMessage = get(wI);
			if (wExceptionMessage.isTyp(aTyp)) {
				if (aSB.length() > 0) {
					aSB.append(aSeparator);
				}
				aSB.append(wExceptionMessage.getMessage());
			}
			wI++;
		}
		return aSB;
	}

	/**
	 * @param aMessage
	 */
	void addTechnicalMessage(String aMessage) {
		add(new CExceptionMessage(EExceptionMessageTyp.TECHNICAL, aMessage));
	}

	/**
	 * @param aMessage
	 */
	void addUserMessage(String aMessage) {
		add(new CExceptionMessage(EExceptionMessageTyp.USER, aMessage));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IDescriber#calcLengthOfDescription()
	 */
	public int calcDescriptionLength() {
		return 256 * size();
	}

	/**
	 * @return
	 */
	String getFirstMessage() {
		return getFirstMessage(EExceptionMessageTyp.USER);
	}

	String getFirstMessage(EExceptionMessageTyp aTyp) {
		String wMess = CXStringUtils.EMPTY;
		CExceptionMessage wExceptionMessage;
		int wMax = size();
		int wI = 0;
		while (wI < wMax) {
			wExceptionMessage = get(wI);
			if (wExceptionMessage.isUser()) {
				return wExceptionMessage.getMessage();
			}
			wI++;
		}
		return wMess;
	}

	/**
	 * @return
	 */
	public List<String> getListOfMessages() {
		return getListOfMessages(EExceptionMessageTyp.ALL);
	}

	/**
	 * 16j_000
	 * 
	 * @param aTyp
	 * @return
	 */
	public List<String> getListOfMessages(EExceptionMessageTyp... aTyp) {
		ArrayList<String> wList = new ArrayList<String>();
		CExceptionMessage wExceptionMessage;
		int wMax = size();
		int wI = 0;
		while (wI < wMax) {
			wExceptionMessage = get(wI);
			if (wExceptionMessage.filter(aTyp)) {
				wList.add(wExceptionMessage.getMessage());
			}
			wI++;
		}
		return wList;
	}

	/**
	 * @param aTypes
	 * @return une liste de lmessages satisfaisants aux types
	 */
	CExceptionMessages getMessages(EExceptionMessageTyp... aTypes) {
		CExceptionMessages wMessages = new CExceptionMessages();

		CExceptionMessage wExceptionMessage;
		int wMax = size();
		int wI = 0;
		while (wI < wMax) {
			wExceptionMessage = get(wI);
			if (wExceptionMessage.filter(aTypes)) {
				wMessages.add(wExceptionMessage);
			}
			wI++;
		}
		return wMessages;
	}

	/**
	 * @param aTyp
	 * @param aSeparator
	 * @return
	 */
	String getMessages(EExceptionMessageTyp aTyp, char aSeparator) {
		return addMessagesInSB(new StringBuilder(256 * size()), aTyp,
				aSeparator).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IDescriber#getDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();
	}
}

public class CXException extends Exception implements IXDescriber {

	private static final char CHAR_OD = 0x0D;

	private static final String EMPTY = "";

	private final static String FORMAT_EXCEPTION = "Exception={%s] ";

	private final static String FORMAT_MESAGE = "Message={%s] ";

	private final static String FORMAT_THROWABLE = "InitialThrowable={%s]";

	private final static String LIB_THROWABLE_DUMP_ERROR = "EXCEPTION DURING THE DUMP OF A THROWABLE";

	private final static String MESS_CANT_CLEAN = "Can't clean";

	private final static String MESS_CANT_GET = "Can't get";

	private final static String MESS_DETAIL_PREFIX = "| ";

	private final static String MESS_LINES_OF_STACK = "the lines of stack of the Throwable ";

	private final static String MESS_NO_STACK = "No stack available";

	private static final boolean MESS_WHITH_STACK = true;

	private final static String MESSAGE_SEPARATOR = " , ";

	private final static char MESSAGES_SEPARATOR = CHAR_OD;

	private final static String PART_SEPARATOR = " | ";

	private static final String PREFIX_CLASS = "class";

	private static final String PREFIX_MESS = "mess";

	private static final String PREFIX_STACK = "stack";

	private static final String PREFIX_WHY = "why";

	private final static String PSEM2M_CLASSES_PREFIX = "org.psem2m.";

	public static final char SEPARATOR_COMA = ',';

	// Cause non specifie

	public static final char SEPARATOR_LINE = '\n';

	private static final long serialVersionUID = 3257852086359437881L;

	// Cause inconue
	public final static int WHY_UNKNOWN = -1;

	// Message explicite de la cause inconue
	public final static String WHY_UNKNOWN_MESS = "Unknown 'Why'";

	public final static int WHY_UNSPECIFIED = -2;

	/**
	 * ajoute le message de l'erreur et tous les messages de la chaine de cause
	 * separes par le
	 * 
	 * <pre>
	 * MESSAGE_SEPARATOR
	 * </pre>
	 * 
	 * dans le StringBuilder. *
	 * 
	 * @param wSB
	 * @param aThrowable
	 * @return
	 */
	public static Appendable addECauseMessagesInBuffer(Appendable wSB,
			Throwable aThrowable) throws Exception {
		String wMess;
		int wNbMess = 1;
		while (aThrowable != null) {
			if (wNbMess > 1)
				wSB.append(MESSAGE_SEPARATOR);
			wSB.append('(');
			wSB.append(String.valueOf(wNbMess));
			wSB.append(')');
			wSB.append(' ');
			wMess = aThrowable.getLocalizedMessage();
			boolean wHasMess = (wMess != null && wMess.length() > 0);

			if (wHasMess) {
				if (wNbMess > 1)
					wSB.append(aThrowable.getClass().getSimpleName()).append(
							':');
				wSB.append(wMess);
			} else {
				// s'il n'y a pas de message on met la classe
				wSB.append(aThrowable.getClass().getName());
			}

			wNbMess++;
			aThrowable = aThrowable.getCause();
		}

		return wSB;
	}

	/**
	 * @param wSB
	 * @param aThrowable
	 * @return
	 */
	public static Appendable addEClassAndMessInBuffer(Appendable wSB,
			Throwable aThrowable) throws Exception {
		String wMess = aThrowable.getMessage();
		boolean wHasMess = (wMess != null && wMess.length() > 0);
		if (wHasMess)
			wSB.append(wMess).append(PART_SEPARATOR);

		wSB.append(aThrowable.getClass().getSimpleName());
		return wSB;
	}

	/**
	 * Ajoute la composante (classe) d'une exception dans un StringBuilder
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private static Appendable addEClassInBuffer(Appendable aSB, Throwable e)
			throws Exception {
		return aSB.append(PREFIX_CLASS).append('=').append('[')
				.append(e.getClass().getName()).append(']');
	}

	/**
	 * @param aSB
	 * @param e
	 * @return
	 */
	public static Appendable addEDescrFullInBuffer(Appendable aSB, Throwable e)
			throws Exception {
		return addEDescrFullInBuffer(aSB, e, SEPARATOR_COMA);
	}

	/**
	 * @param aSB
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	public static Appendable addEDescrFullInBuffer(Appendable aSB, Throwable e,
			char aSeparator) throws Exception {
		if (aSeparator == SEPARATOR_LINE) {
			aSB.append(aSeparator);
		}
		addEClassInBuffer(aSB, e).append(SEPARATOR_LINE);

		if (e instanceof CXException)
			addEWhyInBuffer(aSB, (CXException) e).append(SEPARATOR_LINE);

		addEMessInBuffer(aSB, e, aSeparator).append(SEPARATOR_LINE);

		addEStackInBuffer(aSB, e, aSeparator).append(SEPARATOR_LINE);

		return aSB;
	}

	/**
	 * @param aSB
	 * @param e
	 * @return
	 */
	public static Appendable addEDescrMiniInSB(Appendable aSB,
			Throwable aThrowable) {
		try {
			addEClassAndMessInBuffer(aSB, aThrowable);
			aSB.append(PART_SEPARATOR);

			if (aThrowable instanceof CXException)
				addWhyStrAndNumInBuffer(aSB, (CXException) aThrowable).append(
						PART_SEPARATOR);
			aSB.append(getFirstLineOfStack(aThrowable));
		} catch (Throwable e) {
			return new StringBuilder().append(aSB).append(e);
		}
		return aSB;
	}

	/**
	 * Ajoute la composante (mess) d'une exception dans un Buffer *
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private static Appendable addEMessInBuffer(Appendable aSB, Throwable e,
			char aSeparator) throws Exception {
		if (e instanceof CXException) {
			CXException wE = (CXException) e;

			CExceptionMessages wComplement = wE.getExceptionMessages();
			int wI = 0;
			int wMax = wComplement.size();
			while (wI < wMax) {
				if (wI > 0)
					aSB.append(aSeparator);
				aSB.append(PREFIX_MESS);
				aSB.append('(').append(String.valueOf(wI + 1)).append(')');
				aSB.append('=').append('[');
				aSB.append(wComplement.get(wI).toString());
				aSB.append(']');
				wI++;
			}
		} else {
			Throwable wThrowable = e;
			int wNbMess = 1;
			while (wThrowable != null) {
				if (wNbMess > 1)
					aSB.append(aSeparator);
				aSB.append(PREFIX_MESS);
				aSB.append('(').append(String.valueOf(wNbMess)).append(')');
				aSB.append('=').append('[');

				addEDescrMiniInSB(aSB, wThrowable);

				aSB.append(']');
				wNbMess++;
				wThrowable = wThrowable.getCause();
			}
		}

		return aSB;
	}

	/**
	 * Ajoute la composante (stack) d'une exception dans un StringBuilder
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private static Appendable addEStackInBuffer(Appendable aSB, Throwable e,
			char aSeparator) throws Exception {
		return aSB.append(PREFIX_STACK).append('=').append('[')
				.append(getCleanedStackOfThrowable(e, aSeparator)).append(']');
	}

	/**
	 * Ajoute la composante (why) d'une exception dans un StringBuilder
	 * 
	 * 14w_008 - Integration WebService
	 * 
	 * @param aSB
	 * @param e
	 * @return
	 */
	private static Appendable addEWhyInBuffer(Appendable aSB, CXException e)
			throws Exception {
		if (e != null && !e.isWhy(WHY_UNSPECIFIED)) {
			aSB.append(PREFIX_WHY);
			aSB.append('=');
			aSB.append('[');
			// 16w_106 - Fiche 52942 - Retourner un status http explicite sur
			// xml non parssable
			addWhyStrAndNumInBuffer(aSB, e);
			aSB.append(']');
		}
		return aSB;
	}

	/**
	 * @param wSB
	 * @param aThrowable
	 * @return
	 */
	public static Appendable addUserMessagesInBuffer(Appendable wSB,
			Throwable aThrowable) throws Exception {
		if (aThrowable != null) {
			if (aThrowable instanceof CXException) {
				CXException wException = (CXException) aThrowable;
				CExceptionMessage wExceptionMessage;
				CExceptionMessages wMessages = wException
						.getExceptionMessages(EExceptionMessageTyp.USER);
				int wMax = wMessages.size();
				int wI = 0;
				while (wI < wMax) {
					wExceptionMessage = wMessages.get(wI);
					if (wI > 0)
						wSB.append(' ');
					wSB.append(wExceptionMessage.getMessage());
					wI++;
				}

			} else {
				addEClassAndMessInBuffer(wSB, aThrowable);
			}
		}
		return wSB;
	}

	/**
	 * @param aSB
	 * @param e
	 * @return
	 */
	private static Appendable addWhyStrAndNumInBuffer(Appendable aSB,
			CXException e) throws Exception {
		if (e != null && !e.isWhy(WHY_UNSPECIFIED)) {
			aSB.append(e.getWhyString());
			aSB.append(SEPARATOR_COMA);
			aSB.append(String.valueOf(e.getWhy()));
		}
		return aSB;
	}

	/**
	 * @param e
	 * @param aDumpedThrowable
	 * @return
	 */
	private static String buildExceptionDumpErrorMess(Exception e,
			Throwable aDumpedThrowable) {
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
	 * @param aContext
	 * @param e
	 * @return
	 */
	private static String buildStackExceptionMessage(String aContext,
			Throwable e, boolean aWithStack) {
		StringBuilder wMess = new StringBuilder(256);
		wMess.append(aContext);
		wMess.append(' ');
		wMess.append(MESS_LINES_OF_STACK);
		wMess.append('.');
		wMess.append(' ');
		wMess.append('[');
		wMess.append(e.getClass().getSimpleName());
		wMess.append(']');
		wMess.append('.');
		if (e.getMessage() != null) {
			wMess.append(' ');
			wMess.append('[');
			wMess.append(e.getMessage());
			wMess.append(']');
			wMess.append('.');
		}
		if (aWithStack) {
			putStackOfThrowableInSB(wMess, e);
		}
		return wMess.toString();
	}

	/**
	 * @param aThrowable
	 * @return le message de l'erreur et tous les messages de la chaine de cause
	 *         separes par le MESSAGE_SEPARATOR
	 */
	public static String eCauseMessagesInString(Throwable aThrowable) {
		try {
			return addECauseMessagesInBuffer(new StringBuilder(128), aThrowable)
					.toString();
		} catch (Exception e) {
			return buildExceptionDumpErrorMess(e, aThrowable);
		}
	}

	/**
	 * @param e
	 * @return
	 */
	public static String eClassAndMessInString(Throwable aThrowable) {
		try {
			return addEClassAndMessInBuffer(new StringBuilder(128), aThrowable)
					.toString();
		} catch (Exception e) {
			return buildExceptionDumpErrorMess(e, aThrowable);
		}
	}

	/**
	 * Retourne les composantes (classe,why ,mess,stack) d'une exception dans
	 * une string
	 * 
	 * @param e
	 * @return
	 */
	public static String eInString(Throwable e) {
		return eInString(e, SEPARATOR_LINE);
	}

	/**
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	public static String eInString(Throwable aThrowable, char aSeparator) {
		try {
			return addEDescrFullInBuffer(new StringBuilder(512), aThrowable,
					aSeparator).toString();
		} catch (Exception e) {
			return buildExceptionDumpErrorMess(e, aThrowable);
		}
	}

	/**
	 * Retourne les composantes (classe,mess,firstLineOfstack) d'une exception
	 * dans une string
	 * 
	 * @param e
	 * @return
	 */
	public static String eMiniInString(Throwable aThrowable) {
		try {
			return addEDescrMiniInSB(new StringBuilder(128), aThrowable)
					.toString();
		} catch (Exception e) {
			return buildExceptionDumpErrorMess(e, aThrowable);
		}
	}

	/**
	 * @param e
	 * @return
	 */
	public static String eStackInString(Throwable e) {
		return eStackInString(e, SEPARATOR_COMA);
	}

	/**
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	public static String eStackInString(Throwable e, char aSeparator) {
		return getCleanedStackOfThrowable(e, aSeparator);
	}

	/**
	 * @param aThrowable
	 * @return
	 */
	public static String eUserMessagesInString(Throwable aThrowable) {
		try {
			return addUserMessagesInBuffer(new StringBuilder(128), aThrowable)
					.toString();
		} catch (Exception e) {
			return buildExceptionDumpErrorMess(e, aThrowable);
		}
	}

	/**
	 * @param wStack
	 * @return
	 */
	private static String extractFirstLineOfStack(String aStack, char aSeparator) {

		int wPos = aStack.indexOf(aSeparator);
		if (wPos > -1) {
			aStack = aStack.substring(0, wPos);
		}

		return aStack;
	}

	/**
	 * @param wStack
	 * @return
	 */
	private static String extractFirstPsem2mLineOfStack(String aStack) {
		int wPos = aStack.indexOf(PSEM2M_CLASSES_PREFIX);

		if (wPos > -1) {
			aStack = aStack.substring(wPos);
			wPos = aStack.indexOf(',');
			if (wPos > -1) {
				aStack = aStack.substring(0, wPos);
			}
		}
		return aStack;
	}

	public static String getCleanedStackOfThrowable(Throwable aThrowable) {
		return getCleanedStackOfThrowable(aThrowable, SEPARATOR_COMA);
	}

	/**
	 * @param e
	 * @return
	 */
	private static String getCleanedStackOfThrowable(Throwable aThrowable,
			char aSeparator) {
		String wCleanedStack = EMPTY;
		try {
			wCleanedStack = putStackInString(aThrowable);

			if (wCleanedStack.length() > 0 && aSeparator != SEPARATOR_LINE) {
				wCleanedStack = wCleanedStack.replace('\n', aSeparator);
			}
		} catch (Throwable e) {
			System.out.println(buildStackExceptionMessage(MESS_CANT_CLEAN, e,
					MESS_WHITH_STACK));
		}
		return wCleanedStack;
	}

	/**
	 * retourne la premiere ligne d'une stack et la premiere ligne contenant
	 * ADONIX_CLASSES_PREFIX
	 * 
	 * @param e
	 * @return
	 */
	public static String getFirstLineAndFirstPsem2mOfStack(Throwable e) {
		String wStack = getCleanedStackOfThrowable(e, SEPARATOR_COMA);

		if (wStack.length() == 0) {
			return wStack;
		} else if (isFisrtLineIsAdonixLine(wStack)) {
			return extractFirstLineOfStack(wStack, SEPARATOR_COMA);
		} else {
			StringBuilder wSB = new StringBuilder(256);
			wSB.append(extractFirstLineOfStack(wStack, SEPARATOR_COMA));
			wSB.append('/');
			wSB.append(extractFirstPsem2mLineOfStack(wStack));
			return wSB.toString();
		}
	}

	/**
	 * Retourne la premiere ligne de la stack dans une String
	 * 
	 * @param e
	 * @return
	 */
	public static String getFirstLineOfStack(Throwable e) {
		String wStack = getCleanedStackOfThrowable(e, SEPARATOR_COMA);

		if (wStack.length() == 0) {
			return wStack;
		} else {
			return extractFirstLineOfStack(wStack, SEPARATOR_COMA);
		}
	}

	/**
	 * retourne la premiere ligne d'une stack contenant PSEM2M_CLASSES_PREFIX
	 * 
	 * @param e
	 * @return
	 */
	static public String getFirstPsem2mLineOfStack(Throwable e) {
		String wStack = getCleanedStackOfThrowable(e, SEPARATOR_COMA);

		if (wStack.length() == 0) {
			return wStack;
		} else {
			return extractFirstPsem2mLineOfStack(wStack);
		}
	}

	/**
	 * @param aThrowable
	 * @return
	 */
	private static StackTraceElement getFirstPsem2mStackElement(
			Throwable aThrowable) {
		StackTraceElement[] wStackElements = aThrowable.getStackTrace();
		StackTraceElement wStackElement = null;
		int wMax = wStackElements.length;
		if (wMax != 0) {
			int wI = 0;
			while (wI < wMax) {
				wStackElement = wStackElements[wI];

				if (wStackElement.getMethodName().contains(
						PSEM2M_CLASSES_PREFIX))
					break;

				wI++;
			}
		}
		return wStackElement;
	}

	// /**
	// * @param e
	// * @return
	// */
	// public static String getStackInString(Throwable e) {
	// return getStackOfThrowable(e);
	// }
	//
	// /**
	// * @param e
	// * @return
	// */
	// private static String getStackOfThrowable(Throwable aThrowable) {
	// return putStackInString(aThrowable);
	// }

	/**
	 * @param aStack
	 * @return
	 */
	private static boolean isFisrtLineIsAdonixLine(String aStack) {
		int wPos = aStack.indexOf(PSEM2M_CLASSES_PREFIX);

		return (wPos > -1 && wPos < 5);
	}

	/**
	 * 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
	 * 
	 * @param aWhy
	 * @return
	 */
	public static boolean isValidWhy(int aWhy) {
		return (aWhy > CXException.WHY_UNKNOWN);
	}

	/**
	 * @param aThrowable
	 * @return
	 */
	public static int numLineOfFirstAdonixLineOfStack(Throwable aThrowable) {
		StackTraceElement wStackTraceElement = getFirstPsem2mStackElement(aThrowable);
		if (wStackTraceElement != null)
			return wStackTraceElement.getLineNumber();
		else
			return -1;
	}

	/**
	 * @param aThrowable
	 * @return
	 */
	private static String putStackInString(Throwable aThrowable) {
		StringBuilder wRoughStack = new StringBuilder(1024);
		try {
			putStackOfThrowableInSB(wRoughStack, aThrowable);
		} catch (Throwable e) {
			System.out.println(buildStackExceptionMessage(MESS_CANT_GET, e,
					!MESS_WHITH_STACK));
		}
		return wRoughStack.toString();
	}

	/**
	 * @param aMessage
	 * @param e
	 */
	private static StringBuilder putStackOfThrowableInSB(StringBuilder aSB,
			Throwable e) {
		StackTraceElement[] wStackElements = e.getStackTrace();
		StackTraceElement wStackElement;
		int wMax = wStackElements.length;
		if (wMax == 0) {
			aSB.append(MESS_NO_STACK);
		} else {
			int wI = 0;
			while (wI < wMax) {
				wStackElement = wStackElements[wI];
				if (wI > 0) {
					aSB.append('\n');
				}
				aSB.append(wStackElement.getClassName());
				aSB.append('(');
				aSB.append(wStackElement.getMethodName());
				aSB.append(':');
				aSB.append(wStackElement.getLineNumber());
				aSB.append(')');
				wI++;
			}
		}
		return aSB;
	}

	/**
	 * 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
	 * 
	 * Pourquoi de l'exception "complementaire"
	 */
	private int pCauseWhy = WHY_UNSPECIFIED;

	/**
	 * Complement d'explication de de l'exception
	 */
	private final CExceptionMessages pMessages = new CExceptionMessages();

	// Le ourquoi de l'exception
	private int pWhy = WHY_UNSPECIFIED;

	/**
	 * @param aWhy
	 */
	public CXException(int aWhy) {
		super();
		init(aWhy, null);

	}

	/**
	 * @param aWhy
	 * @param aMessage
	 */
	public CXException(int aWhy, String aMessage) {
		super();
		init(aWhy, aMessage);
	}

	/**
	 * 16w_104 - refonte interface de CException
	 * 
	 * @param aWhy
	 * @param aFormat
	 * @param aArgs
	 */
	public CXException(int aWhy, String aFormat, Object... aArgs) {
		this(aWhy, String.format(aFormat, aArgs));
	}

	/**
	 * 16w_104 - refonte interface de CException
	 * 
	 * @param aWhy
	 * @param aMessage
	 * @param aComplement
	 */
	public CXException(int aWhy, Throwable aComplement) {
		this(aWhy, aComplement, aComplement.getMessage());
	}

	/**
	 * 16w_104 - refonte interface de CException
	 * 
	 * @param aWhy
	 * @param aComplement
	 * @param aMessage
	 */
	public CXException(int aWhy, Throwable aComplement, String aMessage) {
		super(aComplement);
		init(aWhy, aMessage);
		addComplement(aComplement);
	}

	/**
	 * 16w_104 - refonte interface de CException
	 * 
	 * @param aWhy
	 * @param aComplement
	 * @param aFormat
	 * @param aArgs
	 */
	public CXException(int aWhy, Throwable aComplement, String aFormat,
			Object... aArgs) {
		this(aWhy, aComplement, String.format(aFormat, aArgs));
	}

	/**
	 * 14w_008 - Int�gration WebServices
	 * 
	 * @param aMessage
	 */
	public CXException(String aMessage) {
		this(WHY_UNSPECIFIED, aMessage);
	}

	public CXException(String aFormat, Object... aArgs) {
		this(WHY_UNSPECIFIED, String.format(aFormat, aArgs));
	}

	/**
	 * 14w_008 - Int2gration WebServices 16w_104 - refonte interface de
	 * CException
	 * 
	 * @param aComplement
	 */
	public CXException(Throwable aComplement) {
		this(WHY_UNSPECIFIED, aComplement, aComplement.getMessage());
	}

	/**
	 * @param aComplement
	 */
	public void addComplement(String aComplement) {
		if (aComplement != null) {
			pMessages.addTechnicalMessage(aComplement);
		}
	}

	/**
	 * @param aComplement
	 */
	public void addComplement(Throwable aComplement) {
		if (aComplement != null) {
			if (aComplement instanceof CXException) {
				// 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
				CXException wException = ((CXException) aComplement);
				// 15j_003
				pMessages
						.addAll(wException.getExceptionMessages(
								EExceptionMessageTyp.USER,
								EExceptionMessageTyp.DETAIL));
				setCauseWhy((wException.hasCauseWhy()) ? wException
						.getCauseWhy() : wException.getWhy());
			} else {
				// 15j_003
				StringBuilder wSB = new StringBuilder();
				wSB.append(aComplement.getClass().getSimpleName());
				wSB.append(':');
				wSB.append(aComplement.getLocalizedMessage());
				pMessages.addUserMessage(wSB.toString());
				pMessages
						.addDetailMessage(throwableComplementToString(aComplement));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IDescriber#addDescrInSB(java.lang.StringBuilder)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		try {
			aSB.append('{');
			aSB.append(getName());
			aSB.append(MESSAGE_SEPARATOR);
			aSB.append(getMessage());
			aSB.append('}');
			// liste des messages
			if (hasComplement()) {
				aSB.append(MESSAGES_SEPARATOR);
				addMessagesInBuffer(aSB, MESSAGES_SEPARATOR);
			}
		} catch (Throwable e) {
			return new StringBuilder().append(aSB).append(e);
		}
		return aSB;
	}

	/**
	 * @param aSB
	 * @return
	 */
	public Appendable addDescriptionMiniInBuffer(Appendable aSB) {
		return addEDescrMiniInSB(aSB, this);
	}

	/**
	 * @param aSB
	 * @param aFormat
	 * @return
	 */
	public StringBuilder addFormatedMessagesInSB(StringBuilder aSB,
			String aFormat) {
		int wI = 0;
		int wMax = pMessages.size();
		while (wI < wMax) {
			aSB.append(String.format(aFormat, pMessages.get(wI).toString()));
			wI++;
		}
		return aSB;
	}

	/**
	 * @param aSB
	 * @param aSeparator
	 * @return
	 */
	public Appendable addMessagesInBuffer(Appendable aSB, char aSeparator)
			throws Exception {

		int wI = 0;
		int wMax = pMessages.size();
		while (wI < wMax) {
			if (wI > 0) {
				aSB.append(aSeparator);
			}

			aSB.append(pMessages.get(wI).toString());
			wI++;
		}
		return aSB;
	}

	/**
	 * 16j_000 - Int�gration interpr�teur
	 * 
	 * @param wSB
	 * @param aThrowable
	 * @return
	 */
	private StringBuilder addThrowableComplementInSB(StringBuilder wSB,
			Throwable aThrowable) {

		// addECauseMessagesInSB(wSB, aThrowable);

		wSB.append(MESS_DETAIL_PREFIX);
		wSB.append(aThrowable.getClass().getSimpleName());

		if (aThrowable instanceof CXException) {
			wSB.append(PART_SEPARATOR).append(
					((CXException) aThrowable).getWhyString());
		}

		wSB.append(PART_SEPARATOR);
		wSB.append(getFirstLineAndFirstPsem2mOfStack(aThrowable));

		return wSB;
	}

	/**
	 * retourne la longueur de la description
	 * 
	 * @return
	 */
	public int calcDescriptionLength() {
		return 512;
	}

	/**
	 * @return
	 */
	public String eInString() {
		return eInString(this);
	}

	/**
	 * @return
	 */
	public String eMiniInString() {
		return eMiniInString(this);
	}

	/**
	 * 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
	 * 
	 * @return
	 */
	public int getCauseWhy() {
		return pCauseWhy;
	}

	/**
	 * 16j_000
	 * 
	 * @param aType
	 * @return
	 */
	public CExceptionMessages getComplementMessages() {
		return pMessages.getMessages(EExceptionMessageTyp.TECHNICAL,
				EExceptionMessageTyp.USER);
	}

	/**
	 * @return
	 */
	public CExceptionMessages getExceptionMessages() {
		return pMessages;
	}

	/**
	 * @param aType
	 * @return
	 */
	public CExceptionMessages getExceptionMessages(
			EExceptionMessageTyp... aType) {
		return pMessages.getMessages(aType);
	}

	/**
	 * @return
	 */
	private String getFirstMessage() {

		return pMessages.getFirstMessage();
	}

	/**
	 * @return
	 */
	public List<String> getListOfMessages() {
		return pMessages.getListOfMessages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return getFirstMessage();
	}

	/**
	 * @param aSeparator
	 * @return
	 */
	public String getMessages(char aSeparator) throws Exception {
		return addMessagesInBuffer(new StringBuilder(32 * pMessages.size()),
				aSeparator).toString();
	}

	/**
	 * @return
	 */
	private String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return
	 */
	public int getWhy() {
		return pWhy;
	}

	/**
	 * Attention: appel�e par reflexion dans CThrowableUtils (formater le logne
	 * de log)
	 * 
	 * @return
	 */
	public String getWhyString() {
		return whyToString(pWhy);
	}

	/**
	 * 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
	 * 
	 * @return
	 */
	public boolean hasCauseWhy() {
		return isValidWhy(pCauseWhy);
	}

	/**
	 * @return
	 */
	public boolean hasComplement() {
		return (pMessages.size() > 1);
	}

	/**
	 * @param aWhy
	 * @param aMessage
	 */
	private void init(int aWhy, String aMessage) {
		setWhy(aWhy);
		if (aMessage != null)
			pMessages.addUserMessage(aMessage);
		pMessages.addDetailMessage(throwableComplementToString(this));
	}

	/**
	 * @param aWhy
	 * @return
	 */
	public boolean isWhy(int aWhy) {
		return pWhy == aWhy;
	}

	/**
	 * verify if current className of cexception ou cause exception match with
	 * parameter aClassName
	 * 
	 * @param aWhy
	 *            : aWhy value in string or className
	 * @return true if it match or false
	 */
	public boolean match(String aWhy) {
		if (this.getClass().getName() == aWhy) {
			return true;
		} else if (CXStringUtils.isNumeric(aWhy)
				&& this.getWhy() == Integer.parseInt(aWhy)) {
			return true;
		} else if (this.getCause() != null
				&& this.getCause() instanceof CXException) {
			return ((CXException) this.getCause()).match(aWhy);
		} else {
			if (this.getCause() != null) {
				return this.getCause().getClass().getName() == aWhy;
			} else {
				return false;
			}
		}

	}

	/**
	 * 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
	 * 
	 * @param aCauseWhy
	 */
	private void setCauseWhy(int aCauseWhy) {
		pCauseWhy = aCauseWhy;
	}

	/**
	 * @param aWhy
	 */
	private void setWhy(int aWhy) {
		pWhy = aWhy;
		// 15w_001 - Fiche 36012 - X3Client: Supervision des sockets
		setCauseWhy(aWhy);
	}

	/**
	 * @param aThrowable
	 * @return
	 */
	private String throwableComplementToString(Throwable aThrowable) {
		return addThrowableComplementInSB(new StringBuilder(256), aThrowable)
				.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IDescriber#toStringDescr()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();
	}

	/**
	 * @param aWhy
	 * @return
	 */
	public String whyToString(int aWhy) {
		if (aWhy == WHY_UNSPECIFIED)
			return EMPTY;
		else
			return String.valueOf(aWhy);
	}
}

enum EExceptionMessageTyp {
	ALL, DETAIL, TECHNICAL, USER;

	/**
	 * @param aTyp
	 * @return
	 */
	boolean filter(EExceptionMessageTyp... aTypes) {
		if (aTypes.length == 0 || ALL == aTypes[0])
			return true;

		int wMax = aTypes.length;
		int wI = 0;
		while (wI < wMax) {
			if (isTyp(aTypes[wI]))
				return true;
			wI++;
		}
		return false;
	}

	/**
	 * @return
	 */
	boolean isDetail() {
		return DETAIL == this;
	}

	/**
	 * @return
	 */
	boolean isTechnical() {
		return TECHNICAL == this;
	}

	/**
	 * @param aTyp
	 * @return
	 */
	boolean isTyp(EExceptionMessageTyp aTyp) {
		return this == aTyp;
	}

	/**
	 * @return
	 */
	boolean isUser() {
		return USER == this;
	}

}
