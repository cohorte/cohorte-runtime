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

import java.io.File;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
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
	public static StringBuilder addDescrAlignInSB(final StringBuilder aSB,
			final String aId, final int aIdSize, final String aValue,
			final int aValueSize, final char aEndLine) {
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
				if (wPos + wValueSize < wMax) {
					aSB.append(aValue.substring(wPos, wPos + wValueSize));
				} else {
					aSB.append(aValue.substring(wPos));
				}
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
	private static StringBuilder addJavaInfoDescrInSB(final StringBuilder aSB,
			final String aId) {
		return addJavaInfoDescrInSB(aSB, aId, System.getProperty(aId), SEP_NUL);
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aEndLine
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(final StringBuilder aSB,
			final String aId, final char aEndLine) {
		return addJavaInfoDescrInSB(aSB, aId, System.getProperty(aId), aEndLine);
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(final StringBuilder aSB,
			final String aId, final String aValue, final char aEndLine) {
		return addDescrAlignInSB(aSB, aId, 20, aValue, 100, aEndLine);
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @param aSB
	 * @param aSep
	 * @return
	 */
	private static StringBuilder addSeparatorInSB(final StringBuilder aSB,
			final char aSep) {
		if (aSep != SEP_NUL) {
			aSB.append(aSep);
		}
		return aSB;
	}

	/**
	 * 14w_009 - Intégration WebServices
	 * 
	 * @param wSB
	 * @return
	 */
	public static StringBuilder appendJavaContextInSB(final StringBuilder aSB) {
		return appendJavaContextInSB(aSB, '\n', VECTOR_FULL_INFOS,
				VALUE_MULTI_LINE);
	}

	/**
	 * Append the dump the context of the JVM in a char buffer
	 * 
	 * @param aSB
	 *            the char buffer
	 * @param aSeparator
	 *            the separator included between each information
	 * @param aInformationMask
	 *            the mask to select information
	 * @param aValueMultiLineLine
	 *            accepts format multiline information if true
	 * @return the description of the context of the JVM as a name-value pairs
	 *         list separated by the separator
	 */
	private static StringBuilder appendJavaContextInSB(final StringBuilder aSB,
			final char aSeparator, final int aInformationMask,
			final boolean aValueMultiLine) {
		if ((aInformationMask & MASK_INFOS_JAVA) > 0) {
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
		if ((aInformationMask & MASK_INFOS_OS) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
			addJavaInfoDescrInSB(aSB, "os.arch");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "os.name");
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, "os.version");
		}
		if ((aInformationMask & MASK_INFOS_USER) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
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
		if ((aInformationMask & MASK_INFOS_PATHS) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
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
	private static int calcValueSize(final String aValue, final int aPos,
			final char aEndLine, final int aDefaultSize) {
		if (aEndLine == SEP_NUL) {
			return aDefaultSize;
		}

		int wValueSize = aDefaultSize;
		if (aPos + 1 < aValue.length()) {
			wValueSize = aValue.indexOf(aEndLine, aPos + 1) - aPos;
		}

		if (wValueSize < 0) {
			return aDefaultSize;
		}

		return Math.min(wValueSize + 1, aDefaultSize);
	}

	/**
	 * @param aValueMultiLine
	 * @param adumpSupportedEncodings
	 * @return
	 */
	private static String formatEncodings(final boolean aValueMultiLine,
			final String aDumpSupportedEncodings) {
		if (!aValueMultiLine) {
			return aDumpSupportedEncodings;
		}

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
	 * Dump the context of the JVM
	 * 
	 * <pre>
	 * </pre>
	 * 
	 * @return the description of the context of the JVM as a name-value pairs
	 *         table
	 */
	public static String getJavaContext() {
		return getJavaContext('\n', VECTOR_FULL_INFOS, VALUE_MULTI_LINE);
	}

	/**
	 * Dump the context of the JVM
	 * 
	 * 
	 * @param aSeparator
	 *            the separator included between each information
	 * @param aInformationMask
	 *            the mask to select information
	 * @param aValueMultiLineLine
	 *            accepts format multiline information if true
	 * @return the description of the context of the JVM as a name-value pairs
	 *         list separated by the separator
	 * 
	 * @see getJavaContext()
	 */
	public static String getJavaContext(final char aSeparator,
			final int aInformationMask, final boolean aValueMultiLineLine) {
		return appendJavaContextInSB(new StringBuilder(512), aSeparator,
				aInformationMask, aValueMultiLineLine).toString();
	}

	/**
	 * Dump the context of the JVM
	 * 
	 * @param aInformationMask
	 *            the mask to select information
	 * @param aValueMultiLineLine
	 *            accepts format multiline information if true
	 * @return the description of the context of the JVM as a name-value pairs
	 *         table. Each pair in a separate line.
	 * 
	 * @see getJavaContext()
	 */
	public static String getJavaContext(final int aInformationMask,
			final boolean aValueMultiLineLine) {
		return getJavaContext('\n', aInformationMask, aValueMultiLineLine);
	}
}