/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/

package org.psem2m.utilities;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CXStringUtils implements IConstants {

	private final static String FORMAT_EXCEPTION = "Exception=[%s] ";
	private final static String FORMAT_MESAGE = "Message=[%s] ";
	private final static String LIB_APPEND_ERROR = "ERROR DURING AN APPEND IN A APPENDABLE. ";

	// les caracteres trimables : space, tabulation, LineFeed, CariageReturn
	private final static String TRIMABLE_CHARS = " \t\n\r";
	// les controles trimables : tabulation, LineFeed, CariageReturn
	private final static String TRIMABLE_CONTROL = "\t\n\r";

	public static final String UNICODE_PREFIX = "\\u";

	public static final String VAL_FALSE = "false";
	public static final String VAL_KO = "ko";
	public static final String VAL_NO = "no";
	public static final String VAL_OFF = "off";
	public static final String VAL_OK = "ok";
	public static final String VAL_ON = "on";
	public static final String VAL_TRUE = "true";
	public static final String VAL_YES = "yes";
	// pas le underscore
	private final static String WORD_SPARATOR_CHARS = TRIMABLE_CHARS
			+ "./e?,;:!%e^$ee*e=+&\"#\'{([-|`\\^@)]=}+e";

	/**
	 * @param aBuffer
	 * @param aChar
	 * @return
	 */
	public static Appendable appendCharInBuff(final Appendable aBuffer,
			final char aChar) {
		try {
			return aBuffer.append(aChar);
		} catch (Exception e) {
			System.out.println(buildAppendErrorMess(e));
			return aBuffer;
		}
	}

	/**
	 * @param aBuffer
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	public static Appendable appendChars(final Appendable aBuffer,
			final char aChar, final int aLen) {
		if (aLen < 1) {
			return aBuffer;
		}
		try {

			for (int wI = 0; wI < aLen; wI++) {
				aBuffer.append(aChar);
			}
			return aBuffer;
		} catch (Throwable e) {
			return new StringBuilder().append(CXException.eInString(e));
		}
	}

	/**
	 * @param aBuffer
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public static Appendable appendFormatStrInBuff(final Appendable aBuffer,
			final String aFormat, final Object... aArgs) {
		try {
			return aBuffer.append(String.format(aFormat, aArgs));
		} catch (Exception e) {
			System.out.println(buildAppendErrorMess(e));
			return aBuffer;
		}
	}

	/**
	 * @param aBuffer
	 * @param aKey
	 * @param aValue
	 * @return
	 */
	public static Appendable appendKeyValInBuff(final Appendable aBuffer,
			final String aKey, final Object aValue) {
		try {

			return aBuffer.append(' ').append(aKey).append("=[")
					.append(aValue == null ? "null" : aValue.toString())
					.append(']');
		} catch (Exception e) {
			System.out.println(buildAppendErrorMess(e));
			return aBuffer;
		}
	}

	/**
	 * @param aBuffer
	 * @param aKey
	 * @param aValue
	 * @param aValueB
	 * @return
	 */
	public static Appendable appendKeyValsInBuff(final Appendable aBuffer,
			final String aKey, final Object aValue, final Object aValueB) {
		try {
			return aBuffer.append(' ').append(aKey).append("=[")
					.append(aValue == null ? "null" : aValue.toString())
					.append('|')
					.append(aValueB == null ? "null" : aValueB.toString())
					.append(']');
		} catch (Exception e) {
			System.out.println(buildAppendErrorMess(e));
			return aBuffer;
		}
	}

	/**
	 * @param aBuffer
	 * @param aValue
	 * @param aLen
	 * @param aLeadingChar
	 * @return
	 */
	public static Appendable appendSeqAdjustLeft(final Appendable aBuffer,
			final CharSequence aValue, final int aLen, final char aLeadingChar) {
		try {
			int wLen = aValue.length();
			if (wLen < aLen) {
				aBuffer.append(aValue);
				return appendChars(aBuffer, aLeadingChar, aLen - wLen);
			} else if (wLen > aLen) {
				return aBuffer.append(aValue.subSequence(0, aLen));
			} else {
				return aBuffer.append(aValue);
			}
		} catch (Throwable e) {
			return new StringBuilder().append(CXException.eInString(e));
		}
	}

	/**
	 * @param aBuffer
	 * @param aValue
	 * @param aLen
	 * @param aLeadingChar
	 * @return
	 */
	static public Appendable appendSeqAdjustRight(final Appendable aBuffer,
			final String aValue, final int aLen, final char aLeadingChar) {
		try {
			int wLen = aValue.length();
			if (wLen < aLen) {
				appendChars(aBuffer, aLeadingChar, aLen - wLen);
				return aBuffer.append(aValue);
			} else if (wLen > aLen) {
				return aBuffer.append(aValue.subSequence(wLen - aLen, wLen));
			} else {
				return aBuffer.append(aValue);
			}
		} catch (Throwable e) {
			return new StringBuilder().append(CXException.eInString(e));
		}
	}

	/**
	 * @param aBuffer
	 * @param aStrs
	 * @return
	 */
	public static Appendable appendStringsInBuff(final Appendable aBuffer,
			final String... aStrs) {
		try {
			if (aStrs != null && aStrs.length > 0) {
				for (String wStr : aStrs) {
					aBuffer.append(' ').append(wStr);
				}
			}
			return aBuffer;
		} catch (Exception e) {
			System.out.println(buildAppendErrorMess(e));
			return aBuffer;
		}
	}

	/**
	 * @param aBool
	 * @return
	 */
	public static String boolToOkKo(final boolean aBool) {
		return (aBool) ? VAL_OK : VAL_KO;
	}

	/**
	 * @param aBool
	 * @return
	 */
	public static String boolToOnOff(final boolean aBool) {
		return (aBool) ? VAL_ON : VAL_OFF;
	}

	/**
	 * @param aBool
	 * @return
	 */
	public static String boolToTrueFalse(final boolean aBool) {
		return (aBool) ? VAL_TRUE : VAL_FALSE;
	}

	/**
	 * @param aBool
	 * @return
	 */
	public static String boolToYesNo(final boolean aBool) {
		return (aBool) ? VAL_YES : VAL_NO;
	}

	/**
	 * @param e
	 * @return
	 */
	private static String buildAppendErrorMess(final Exception e) {
		StringBuilder wSB = new StringBuilder();
		wSB.append(LIB_APPEND_ERROR);
		if (e != null) {
			wSB.append(String.format(FORMAT_EXCEPTION, e.getClass()
					.getSimpleName()));
			wSB.append(String.format(FORMAT_MESAGE, e.getMessage()));
			wSB.append(CXException.getCleanedStackOfThrowable(e));
		}
		return wSB.toString();
	}

	/**
	 * 
	 * @param aString
	 * @param aChar
	 * @return le nombre d'instance du caractere dans la chaene
	 */
	public static int countChar(final String aString, final char aChar) {
		if (aString == null) {
			return -1;
		}

		int wCount = 0;
		int wMax = aString.length();
		int wI = 0;
		while (wI < wMax) {
			if (aString.charAt(wI) == aChar) {
				wCount++;
			}
			wI++;
		}
		return wCount;
	}

	/**
	 * @param aKey
	 * @param aValue
	 * @return
	 */
	public static String formatKeyValueInString(final String aKey,
			final Object aValue) {
		StringBuilder wSB = new StringBuilder();
		return wSB.append(aKey).append("=[")
				.append(aValue == null ? "null" : aValue.toString())
				.append(']').toString();
	}

	public static String getExceptionStack(final Throwable e) {
		java.io.StringWriter wSW = new java.io.StringWriter();
		e.printStackTrace(new java.io.PrintWriter(wSW));
		return wSW.toString();
	}

	/**
	 * @param aStr
	 * @return
	 */
	public static boolean isNumeric(final String aStr) {
		if (aStr == null) {
			return false;
		}
		int wMax = aStr.length();
		int wI = 0;
		while (wI < wMax) {
			if (!Character.isDigit(aStr.charAt(wI))) {
				return false;
			}
			wI++;
		}
		return true;
	}

	/**
	 * @param aValue
	 * @return true if all the characters are "trimable"
	 */
	public static boolean isTrimable(final String aValue) {
		return (aValue != null) ? isTrimable(aValue, 0, aValue.length())
				: false;
	}

	/**
	 * @param aValue
	 * @param aOffset
	 * @param aLen
	 * @return true if all the characters are "trimable"
	 */
	public static boolean isTrimable(final String aValue, final int aOffset,
			final int aOffsetEnd) {
		if (aValue == null) {
			return false;
		}

		int wMax = aOffsetEnd;
		int wI = aOffset;
		while (wI < wMax) {
			if (!isTrimableChar(aValue.charAt(wI))) {
				return false;
			}
			wI++;
		}
		return true;

	}

	/**
	 * @param aChar
	 * @return
	 */
	public static boolean isTrimableChar(final char aChar) {
		return (TRIMABLE_CHARS.indexOf(aChar) != -1);
	}

	/**
	 * @param aChar
	 * @return
	 */
	public static boolean isWordSeparatorChar(final char aChar) {
		return (WORD_SPARATOR_CHARS.indexOf(aChar) != -1);
	}

	/**
	 * @param aValue
	 * @param aLen
	 * @param aLeadingChar
	 * @return
	 */
	static public String strAdjustLeft(final String aValue, final int aLen,
			final char aLeadingChar) {
		int wLen = aValue.length();
		if (wLen < aLen) {
			return aValue + strFromChar(aLeadingChar, aLen - wLen);
		} else if (wLen > aLen) {
			return aValue.substring(0, aLen);
		} else {
			return aValue;
		}
	}

	static public String strAdjustRight(final long aValue, final int aLen) {
		return strAdjustRight(String.valueOf(aValue), aLen, '0');
	}

	/**
	 * @param aValue
	 * @param aLen
	 * @param aLeadingChar
	 * @return
	 */
	static public String strAdjustRight(final String aValue, final int aLen,
			final char aLeadingChar) {
		int wLen = aValue.length();
		if (wLen < aLen) {
			return strFromChar(aLeadingChar, aLen - wLen) + aValue;
		} else if (wLen > aLen) {
			return aValue.substring(aValue.length() - aLen);
		} else {
			return aValue;
		}
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	static public String strFromChar(final char aChar, final int aLen) {
		if (aLen < 1) {
			return EMPTY;
		}
		if (aLen == 1) {
			return String.valueOf(aChar);
		}
		char[] wBuffer = new char[aLen];
		for (int wI = 0; wI < aLen; wI++) {
			wBuffer[wI] = aChar;
		}
		return String.valueOf(wBuffer);
	}

	/**
	 * Supprime tous les caracteres trimables en entete et fin de aStr.
	 * <p>
	 * Voir TRIMABLE_CHARS = " \t\n\r".
	 * </p>
	 * 
	 * @param aStr
	 * @return
	 */
	public static String strFullTrim(final String aStr) {
		return strFullTrim(aStr, TRIMABLE_CHARS);
	}

	/**
	 * Supprime tous les caracteres de aBadChars en entete et fin de aStr.
	 * <p>
	 * aBadChars =null --> aBadChars = " \t\n\r"
	 * </p>
	 * 
	 * @param aStr
	 * @param aBadChars
	 * @return
	 */
	public static String strFullTrim(final String aStr, String aBadChars) {
		if (aBadChars == null) {
			aBadChars = TRIMABLE_CHARS;
		}
		return strFullTrim(aStr, aBadChars, aBadChars);
	}

	/**
	 * 
	 * @param aStr
	 * @param aBadCharsPrefix
	 * @param aBadCharsSuffix
	 * @return
	 */
	public static String strFullTrim(String aStr, final String aBadCharsPrefix,
			final String aBadCharsSuffix) {
		if (aStr != null && aStr.length() != 0) {
			if (aBadCharsPrefix != null) {
				int wLen = aStr.length();
				int wPos = 0;
				while (wPos < wLen
						&& aBadCharsPrefix.indexOf(aStr.charAt(wPos)) != -1) {
					wPos++;
				}
				if (wPos > 0) {
					aStr = aStr.substring(wPos, wLen);
				}
			}
			if (aBadCharsSuffix != null && aStr.length() != 0) {
				int wLen = aStr.length();
				int wPos = wLen - 1;
				while (wPos >= 0
						&& aBadCharsSuffix.indexOf(aStr.charAt(wPos)) != -1) {
					wPos--;
				}
				if (wPos < wLen - 1) {
					aStr = aStr.substring(0, wPos + 1);
				}
			}
		}
		return aStr;
	}

	/**
	 * Supress all the trimable characters at the begining and at the end of the
	 * string
	 * 
	 * @see TRIMABLE_CONTROL = "\t\n\r".
	 * @see TRIMABLE_CHARS = " \t\n\r". *
	 * @param aStr
	 * @return
	 */
	public static String strFullTrimKeepingFrefixSpaces(final String aStr) {
		return strFullTrim(aStr, TRIMABLE_CONTROL, TRIMABLE_CHARS);
	}

	/**
	 * @param aValues
	 * @return
	 */
	public static String stringTableToString(final String[] aValues) {
		return stringTableToString(aValues, ",");
	}

	/**
	 * @param strings
	 * @param sep
	 * @return
	 */
	public static String stringTableToString(final String[] aValues,
			final String aSeparator) {
		StringBuilder wSB = new StringBuilder(256);
		if (aValues != null) {
			int wMax = aValues.length;
			for (int wI = 0; wI < wMax; wI++) {
				if (wI > 0) {
					wSB.append(aSeparator);
				}
				wSB.append(aValues[wI]);
			}
		}
		return wSB.toString();
	}

	/**
	 * @param aStr
	 * @return
	 */
	public static boolean strIsInt(final String aStr) {
		try {
			Integer.parseInt(aStr);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * @param aStr
	 * @return
	 */
	public static String strKeapOnlyAlpha(final String aStr) {
		return strKeepCharGreaterThan(aStr, 'A');
	}

	/**
	 * @param aStr
	 * @param aCharLimit
	 * @return
	 */
	public static String strKeepCharGreaterThan(final String aStr,
			final char aCharLimit) {
		int wLen = (aStr != null) ? aStr.length() : 0;
		if (wLen < 1) {
			return aStr;
		}

		StringBuilder wSB = new StringBuilder(wLen);
		char wChar;
		int wI = 0;
		while (wI < wLen) {
			wChar = aStr.charAt(wI);
			if (wChar >= aCharLimit) {
				wSB.append(wChar);
			}
			wI++;
		}
		return wSB.toString();
	}

	public static String strLeft(final String aStr, final char aDelim) {
		return strLeft(aStr, String.valueOf(aDelim));
	}

	public static String strLeft(final String aStr, final String aDelim) {
		String wRes = EMPTY;
		if (aStr != null && aDelim != null) {
			int wPos = aStr.indexOf(aDelim);
			if (wPos != -1 && wPos != 0) {
				wRes = aStr.substring(0, wPos);
			}
		}
		return wRes;
	}

	public static String strLeftBack(final String aStr, final char aDelim) {
		return strLeftBack(aStr, String.valueOf(aDelim));
	}

	public static String strLeftBack(final String aStr, final String aDelim) {
		String wRes = "";
		if (aStr != null && aDelim != null) {
			int wPos = aStr.lastIndexOf(aDelim);
			if (wPos != -1 && wPos != 0) {
				wRes = aStr.substring(0, wPos);
			}
		}
		return wRes;
	}

	/**
	 * Remplace toutes les occurences de aWhat par aBy dasn aStr
	 * 
	 * @param aStr
	 * @param aWhat
	 * @param aBy
	 * @return la nouvelle chaine de caracteres
	 */
	public static String strReplaceAll(final String aStr, final String aWhat,
			final String aBy) {
		if (aStr != null && aStr.length() != 0 && aWhat != null
				&& aWhat.length() != 0 && aBy != null) {
			StringBuilder wRes = new StringBuilder(aStr);
			int wWhatLength = aWhat.length();
			int wPos = aStr.lastIndexOf(aWhat);
			// Pour bloquer la recusivite si aWhat contient aBy
			int wLastPos = aStr.length();
			while (wPos != -1 && wPos < wLastPos) {
				wRes.replace(wPos, wPos + wWhatLength, aBy);
				wLastPos = wPos - 1;
				wPos = aStr.lastIndexOf(aWhat, wLastPos);
			}
			return wRes.toString();
		} else {
			return aStr;
		}
	}

	/**
	 * @param aStr
	 * @param aDelim
	 * @return
	 */
	public static String strRight(final String aStr, final char aDelim) {
		return strRight(aStr, String.valueOf(aDelim));
	}

	/**
	 * @param aStr
	 * @param aDelim
	 * @return
	 */
	public static String strRight(final String aStr, final String aDelim) {
		String wRes = "";
		if (aStr != null && aDelim != null) {
			int wPos = aStr.indexOf(aDelim);
			if (wPos != -1 && wPos != (aStr.length() - 1)) {
				wRes = aStr.substring(wPos + aDelim.length());
			}
		}
		return wRes;
	}

	public static String strRightBack(final String aStr, final char aDelim) {
		return strRightBack(aStr, String.valueOf(aDelim));
	}

	public static String strRightBack(final String aStr, final String aDelim) {
		String wRes = "";
		if (aStr != null && aDelim != null) {
			int wPos = aStr.lastIndexOf(aDelim);
			if (wPos != -1 && wPos != (aStr.length() - 1)) {
				wRes = aStr.substring(wPos + aDelim.length());
			}
		}
		return wRes;
	}

	public static boolean strToBoolean(final String aStr) {
		return (aStr != null && (aStr.equals(VAL_YES) || aStr.equals(VAL_ON)
				|| aStr.equals(VAL_OK) || aStr.equals(VAL_TRUE)));
	}

	/**
	 * @param aStr
	 * @return a String contain hexadecimal that correspond to caractere of aStr
	 */
	public static String strtoHexadecimal(final String aStr) {
		String result = "";
		for (int i = 0; i < aStr.length(); i++) {
			result += Integer.toHexString(aStr.charAt(i)) + "00";
		}
		return result;
	}

	/**
	 * @param aStr
	 * @param aDefValue
	 * @return
	 */
	public static int strToInt(final String aStr, final int aDefValue) {
		try {
			return Integer.parseInt(aStr);
		} catch (Throwable e) {
			return aDefValue;
		}
	}

	/**
	 * @param aText
	 * @return
	 */
	public static String toFirstCharUpperCase(final String aText) {
		if (aText == null) {
			return null;
		}
		int wMax = aText.length();
		if (wMax == 0) {
			return aText;
		}
		char[] wChars = aText.toCharArray();
		int wI = 0;
		char wChar;
		boolean wIsWordSeparator = false;
		boolean wIsInWord = false;
		while (wI < wMax) {
			wChar = wChars[wI];
			wIsWordSeparator = isWordSeparatorChar(wChar);
			if (wIsWordSeparator) {
				if (wIsInWord) {
					wIsInWord = false;
				}
			} else {
				if (!wIsInWord) {
					wChars[wI] = Character.toUpperCase(wChar);
					wIsInWord = true;
				}
			}
			wI++;
		}
		return new String(wChars);
	}

}