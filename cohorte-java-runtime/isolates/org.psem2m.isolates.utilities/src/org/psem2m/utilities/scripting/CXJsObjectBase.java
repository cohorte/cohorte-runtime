package org.psem2m.utilities.scripting;

import java.util.StringTokenizer;

import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.IXDescriber;

/**
 * @author ogattaz
 * 
 */
public class CXJsObjectBase implements IXDescriber {

	protected final static String DESCR_CHAR_TITLE = "*";
	public final static char DESCR_NEWLINE = '\n';
	public final static String DESCR_NEWLINE_STR = "\n";
	public final static String DESCR_NONE = "None";
	public final static String DESCR_NULL = "Null";
	public final static char DESCR_SPACE = ' ';
	public final static String DESCR_STD_INDENT = "   ";

	protected final static String DESCR_STR_SUBTITLE1 = CXStringUtils.strFromChar('-', 20);
	protected final static String DESCR_STR_SUBTITLE2 = DESCR_STR_SUBTITLE1 + '\n';

	protected final static String DESCR_STR_TITLE = CXStringUtils.strFromChar('*', 100) + '\n';

	public final static String DESCR_VALUE_BEGIN = "=[";
	public final static char DESCR_VALUE_END = ']';
	public final static String DESCR_VALUE_SEP = " - ";

	public final static String EMPTY_STR = "";

	public final static String TOKEN_NEWLINE = "\r\n";

	/**
	 * @param aToBuff
	 * @param aToIndentBuff
	 * @param aIndent
	 * @return
	 */
	public static Appendable descrAddIndent(Appendable aToBuff, CharSequence aToIndentBuff,
			CharSequence aIndent) {
		try {
			CharSequence wIndent = aIndent == null ? DESCR_STD_INDENT : aIndent;
			Appendable wResult = aToBuff == null ? new StringBuilder() : aToBuff;
			if (aToIndentBuff != null && aToIndentBuff.length() != 0) {
				StringTokenizer wTok = new StringTokenizer(aToIndentBuff.toString(), TOKEN_NEWLINE,
						false);
				while (wTok.hasMoreTokens()) {
					wResult.append(wIndent).append(wTok.nextToken()).append(DESCR_NEWLINE);
				}
			}
			return wResult;
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aToBuff
	 * @param aToIndent
	 * @return
	 */
	public static Appendable descrAddIndent(Appendable aToBuff, String aToIndent) {
		return aToIndent == null ? aToBuff : descrAddIndent(aToBuff, aToIndent, null);
	}

	/**
	 * @param aToBuff
	 * @param aToIndentBuff
	 * @return
	 */
	public static Appendable descrAddIndent(Appendable aToBuff, StringBuilder aToIndentBuff) {
		return descrAddIndent(aToBuff, aToIndentBuff.toString(), null);
	}

	/**
	 * @param aToBuff
	 * @param aToIndentBuff
	 * @param aIndent
	 * @return
	 */
	public static Appendable descrAddIndent(Appendable aToBuff, StringBuilder aToIndentBuff,
			String aIndent) {
		return descrAddIndent(aToBuff, aToIndentBuff.toString(), aIndent);
	}

	/**
	 * @param aToIndent
	 * @return
	 */
	public static Appendable descrAddIndent(CharSequence aToIndent) {
		StringBuilder wSb = new StringBuilder();
		return aToIndent == null ? wSb : descrAddIndent(wSb, aToIndent, null);
	}

	/**
	 * @param aBuff
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff) {
		try {
			return descrCheckBuffer(aBuff).append(DESCR_NEWLINE);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aLine
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, CharSequence aLine) {
		try {
			return descrCheckBuffer(aBuff).append(aLine).append(DESCR_NEWLINE);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, String aLib, boolean aValue) {
		return descrAddLine(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, String aLib, double aValue) {
		return descrAddLine(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, String aLib, int aValue) {
		return descrAddLine(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, String aLib, long aValue) {
		return descrAddLine(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, String aLib, String aValue) {
		try {
			return descrAddProp(aBuff, aLib, aValue).append(DESCR_NEWLINE);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aLine
	 * @return
	 */
	public static Appendable descrAddLine(Appendable aBuff, StringBuilder aLine) {
		try {
			return descrCheckBuffer(aBuff).append(aLine).append(DESCR_NEWLINE);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aLine
	 * @param aIndent
	 * @return
	 */
	public static Appendable descrAddLineIndent(Appendable aBuff, CharSequence aLine,
			CharSequence aIndent) {
		try {
			return descrAddLine(aBuff.append(aIndent), aLine);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @param aIndent
	 * @return
	 */
	public static Appendable descrAddLineIndent(Appendable aBuff, String aLib, String aValue,
			String aIndent) {
		try {
			return descrAddLine(aBuff.append(aIndent), aLib, aValue);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddProp(Appendable aBuff, String aLib, boolean aValue) {
		return descrAddProp(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddProp(Appendable aBuff, String aLib, char aValue) {
		return descrAddProp(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddProp(Appendable aBuff, String aLib, double aValue) {
		return descrAddProp(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddProp(Appendable aBuff, String aLib, long aValue) {
		return descrAddProp(aBuff, aLib, String.valueOf(aValue));
	}

	/**
	 * @param aBuff
	 * @param aLib
	 * @param aValue
	 * @return
	 */
	public static Appendable descrAddProp(Appendable aBuff, String aLib, String aValue) {
		try {
			Appendable wBuff = descrCheckBuffer(aBuff);
			boolean wSep = mustAddSep(wBuff);

			if (wSep) {
				wBuff.append(DESCR_VALUE_SEP);
			}
			if (aLib == null || aLib.isEmpty()) {
				wBuff.append('[').append(aValue == null ? EMPTY_STR : aValue).append(']');
			} else {
				wBuff.append(aLib).append(DESCR_VALUE_BEGIN)
						.append(aValue == null ? EMPTY_STR : aValue).append(DESCR_VALUE_END);
			}
			return wBuff;
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @param aSubTitle
	 * @return
	 */
	public static Appendable descrAddSubTitle(Appendable aBuff, CharSequence aSubTitle) {
		try {
			Appendable wBuff = descrCheckBuffer(aBuff).append(DESCR_STR_SUBTITLE1);
			if (aSubTitle != null && aSubTitle.length() != 0) {
				wBuff.append(DESCR_SPACE).append(aSubTitle).append(DESCR_SPACE)
						.append(DESCR_STR_SUBTITLE2);
			} else {
				wBuff.append(DESCR_STR_SUBTITLE2);
			}
			return wBuff;
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aBuff
	 * @return
	 */
	public static Appendable descrAddSubTitleLine(Appendable aBuff) {
		return descrAddSubTitle(aBuff, null);
	}

	/**
	 * @param aBuff
	 * @param atext
	 * @return
	 */
	public static Appendable descrAddText(Appendable aBuff, CharSequence atext) {
		try {
			return descrCheckBuffer(aBuff).append(atext);
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * aTitle=null -> trace une lignes -> utilisee pour marquer la fin du
	 * paragraphe
	 * 
	 * @param aBuff
	 * @param aTitle
	 * @return
	 */
	public static Appendable descrAddTitle(Appendable aBuff, CharSequence aTitle) {
		try {
			Appendable wBuff = descrCheckBuffer(aBuff).append(DESCR_STR_TITLE);
			if (aTitle != null && aTitle.length() != 0) {
				wBuff.append(DESCR_CHAR_TITLE).append(DESCR_SPACE)
						.append(aTitle.toString().toUpperCase()).append(DESCR_NEWLINE)
						.append(DESCR_STR_TITLE);
			}
			return wBuff;
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @param aTitle
	 * @return
	 */
	public static Appendable descrAddTitle(CharSequence aTitle) {
		return descrAddTitle(null, aTitle);
	}

	/**
	 * @param aBuff
	 * @return
	 */
	public static Appendable descrAddTitleLine(Appendable aBuff) {
		return descrAddTitle(aBuff, null);
	}

	/**
	 * @param aSB
	 * @return
	 */
	public static Appendable descrCheckBuffer(Appendable aSB) {
		return aSB == null ? new StringBuilder() : aSB;
	}

	/**
	 * @param aBuff
	 * @return
	 */
	private static boolean mustAddSep(Appendable aBuff) {
		boolean wSep = false;
		if (aBuff != null && aBuff instanceof StringBuilder) {
			CharSequence wCS = (CharSequence) aBuff;
			wSep = wCS.length() != 0;
			if (wSep) {
				wSep = wCS.charAt(wCS.length() - 1) != DESCR_NEWLINE;
			}
		}
		return wSep;
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
			return aBuffer.append("null");
		} catch (Exception e) {
			return new StringBuilder().append(e);
		}
	}

	/**
	 * @return
	 */
	public int calcDescriptionLength() {
		return 0;
	}

	/**
	 * @return
	 */
	public String descrClassName() {
		return getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(new StringBuilder(calcDescriptionLength())).toString();
	}
}
