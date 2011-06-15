package org.psem2m.utilities;

import java.io.File;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

public class CXJvmUtils {

	public final static int MASK_INFOS_JAVA = 1;

	public final static int MASK_INFOS_OS = 2;

	public final static int MASK_INFOS_PATHS = 8;

	public final static int MASK_INFOS_USER = 4;

	public final static char SEP_NUL = (char) 255;

	public final static boolean VALUE_MULTI_LINE = true;

	public final static boolean VALUE_ONE_LINE = false;

	public final static int VECTOR_FULL_INFOS = MASK_INFOS_JAVA + MASK_INFOS_OS
			+ MASK_INFOS_USER + MASK_INFOS_PATHS;

	public final static int VECTOR_INFOS_LESS_PATHS = MASK_INFOS_JAVA
			+ MASK_INFOS_OS + MASK_INFOS_USER;

	/**
	 * @param aSB
	 * @param aId
	 * @param aIdSize
	 * @param aValue
	 * @param aValueSize
	 * @return
	 */
	public static StringBuilder addDescrAlignInSB(StringBuilder aSB,
			String aId, int aIdSize, String aValue, int aValueSize,
			char aEndLine) {
		aSB.append(CXStringUtils.strAdjustRight(aId, aIdSize, ' '));
		aSB.append('=');
		aSB.append('[');

		if (aValue == null || aValue.length() <= aValueSize) {
			aSB.append(aValue);
		} else {
			int wMax = aValue.length();
			int wPos = 0;
			int wValueSize;
			while (wPos < wMax) {
				if (wPos > 0) {
					aSB.append('\n').append(
							CXStringUtils.strFromChar(' ', aIdSize + 2));
				}
				wValueSize = calcValueSize(aValue, wPos, aEndLine, aValueSize);
				if (wPos + wValueSize < wMax)
					aSB.append(aValue.substring(wPos, wPos + wValueSize));
				else
					aSB.append(aValue.substring(wPos));
				wPos += wValueSize;
			}
		}
		aSB.append(']');
		return aSB;
	}

	/**
	 * @param aSB
	 * @param aId
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(StringBuilder aSB,
			String aId) {
		return addJavaInfoDescrInSB(aSB, aId, System.getProperty(aId), SEP_NUL);
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aEndLine
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(StringBuilder aSB,
			String aId, char aEndLine) {
		return addJavaInfoDescrInSB(aSB, aId, System.getProperty(aId), aEndLine);
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(StringBuilder aSB,
			String aId, String aValue, char aEndLine) {
		return addDescrAlignInSB(aSB, aId, 20, aValue, 100, aEndLine);
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @param aSB
	 * @param aSep
	 * @return
	 */
	private static StringBuilder addSeparatorInSB(StringBuilder aSB, char aSep) {
		if (aSep != SEP_NUL)
			aSB.append(aSep);
		return aSB;
	}

	/**
	 * 14w_009 - Intégration WebServices
	 * 
	 * @param wSB
	 * @return
	 */
	public static StringBuilder appendJavaContextInSB(StringBuilder aSB) {
		return appendJavaContextInSB(aSB, '\n', VECTOR_FULL_INFOS,
				VALUE_MULTI_LINE);
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @param aSB
	 * @param aSep
	 * @return
	 */
	private static StringBuilder appendJavaContextInSB(StringBuilder aSB,
			char aSeparator, int aInfosVector, boolean aValueMultiLine) {
		if ((aInfosVector & MASK_INFOS_JAVA) > 0) {
			addJavaInfoDescrInSB(aSB, "java.class.version");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.runtime.name");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.runtime.version");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.specification.version");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.home");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.vendor");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.vendor.url");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.version");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.home");
		}
		if ((aInfosVector & MASK_INFOS_OS) > 0) {
			if (aSB.length() > 0)
				addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "os.arch");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "os.name");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "os.version");
		}
		if ((aInfosVector & MASK_INFOS_USER) > 0) {
			if (aSB.length() > 0)
				addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "user.dir");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "user.home");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "user.language");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "user.name");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "user.region");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "user.timezone");
		}
		if ((aInfosVector & MASK_INFOS_PATHS) > 0) {
			if (aSB.length() > 0)
				addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.class.path",
					(aValueMultiLine) ? File.pathSeparatorChar : SEP_NUL);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "java.library.path",
					(aValueMultiLine) ? File.pathSeparatorChar : SEP_NUL);
			addSeparatorInSB(aSB, aSeparator);

			addJavaInfoDescrInSB(aSB, "defaultCharset", Charset
					.defaultCharset().displayName(), SEP_NUL);
			addSeparatorInSB(aSB, aSeparator);

			addJavaInfoDescrInSB(
					aSB,
					"supported.encodings",
					formatEncodings(aValueMultiLine,
							CXOSUtils.dumpSupportedEncodings()),
					(aValueMultiLine) ? ';' : SEP_NUL);
		}
		return aSB;
	}

	/**
	 * @param aValue
	 * @param aPos
	 * @param aSep
	 * @param aDefaultSize
	 * @return
	 */
	private static int calcValueSize(String aValue, int aPos, char aEndLine,
			int aDefaultSize) {
		if (aEndLine == SEP_NUL)
			return aDefaultSize;

		int wValueSize = aDefaultSize;
		if (aPos + 1 < aValue.length()) {
			wValueSize = aValue.indexOf(aEndLine, aPos + 1) - aPos;
		}

		if (wValueSize < 0)
			return aDefaultSize;

		return Math.min(wValueSize + 1, aDefaultSize);
	}

	/**
	 * @param aValueMultiLine
	 * @param adumpSupportedEncodings
	 * @return
	 */
	private static String formatEncodings(boolean aValueMultiLine,
			String aDumpSupportedEncodings) {
		if (!aValueMultiLine)
			return aDumpSupportedEncodings;

		StringTokenizer wST = new StringTokenizer(aDumpSupportedEncodings, ",");

		StringBuilder wSB = new StringBuilder(wST.countTokens() * 20);
		int wI = 0;
		while (wST.hasMoreTokens()) {
			wSB.append(CXStringUtils.strAdjustLeft(wST.nextToken(), 19, ' '));
			wI++;
			if (wI == 5) {
				wSB.append(';');
				wI = 0;
			}
		}
		return wSB.toString();
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @return
	 */
	public static String getJavaContext() {
		return getJavaContext('\n', VECTOR_FULL_INFOS, VALUE_MULTI_LINE);
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @return
	 */
	public static String getJavaContext(char aSeparator, int aVectorInfos,
			boolean aValueMultiLineLine) {
		return appendJavaContextInSB(new StringBuilder(512), aSeparator,
				aVectorInfos, aValueMultiLineLine).toString();
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @return
	 */
	public static String getJavaContext(int aVectorInfos,
			boolean aValueMultiLineLine) {
		return getJavaContext('\n', aVectorInfos, aValueMultiLineLine);
	}
}