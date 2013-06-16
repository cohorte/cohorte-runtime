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
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public final class CXJvmUtils {

	private final static int ID_WITDH = 30;

	private final static int LINE_WITDH = 130;
	public final static int MASK_INFOS_JAVA = 1;

	public final static int MASK_INFOS_OS = 2;
	public final static int MASK_INFOS_PATHS = 8;
	public final static int MASK_INFOS_USER = 4;
	public final static int MASK_OTHER_PROPS = 16;
	public final static char SEP_NUL = (char) 255;

	public final static String SYSPROP_DEFAULT_CHARSET = "defaultCharset";

	public final static String SYSPROP_JAVA_CLASS_PATH = "java.class.path";
	public final static String SYSPROP_JAVA_CLASS_VERS = "java.class.version";
	public final static String SYSPROP_JAVA_ENDORSED_DIR = "java.endorsed.dirs";
	public final static String SYSPROP_JAVA_EXT_DIR = "java.ext.dirs";
	public final static String SYSPROP_JAVA_HOME = "java.home";
	public final static String SYSPROP_JAVA_IO_TMPDIR = "java.io.tmpdir";
	public final static String SYSPROP_JAVA_RUN_NAME = "java.runtime.name";
	public final static String SYSPROP_JAVA_RUN_VERS = "java.runtime.version";
	public final static String SYSPROP_JAVA_SPEC_VERS = "java.specification.version";
	public final static String SYSPROP_JAVA_VENDOR = "java.vendor";
	public final static String SYSPROP_JAVA_VENDOR_URL = "java.vendor.url";
	public final static String SYSPROP_JAVA_VERS = "java.version";
	public final static String SYSPROP_JAVA_VM_INFO = "java.vm.info";
	public final static String SYSPROP_JAVA_VM_NAME = "java.vm.name";
	public final static String SYSPROP_JAVA_VM_VENDOR = "java.vm.vendor";
	public final static String SYSPROP_JAVA_VM_VERSION = "java.vm.version";
	public final static String SYSPROP_LIB_PATH = "java.library.path";
	public final static String SYSPROP_OS_ARCH = "os.arch";
	public final static String SYSPROP_OS_NAME = "os.name";
	public final static String SYSPROP_OS_VERS = "os.version";
	public final static String SYSPROP_SUPPORTED_ENCODING = "supported.encodings";
	public final static String SYSPROP_USER_COUNTRY = "user.country";
	public final static String SYSPROP_USER_DIR = "user.dir";
	public final static String SYSPROP_USER_HOME = "user.home";
	public final static String SYSPROP_USER_LANG = "user.language";
	public final static String SYSPROP_USER_NAME = "user.name";
	public final static String SYSPROP_USER_REGION = "user.region";
	public final static String SYSPROP_USER_TIMEZONE = "user.timezone";

	public final static String[] SYSPROPS = { SYSPROP_DEFAULT_CHARSET, SYSPROP_JAVA_CLASS_PATH,
			SYSPROP_JAVA_CLASS_VERS, SYSPROP_JAVA_ENDORSED_DIR, SYSPROP_JAVA_EXT_DIR,
			SYSPROP_JAVA_HOME, SYSPROP_JAVA_IO_TMPDIR, SYSPROP_JAVA_RUN_NAME,
			SYSPROP_JAVA_RUN_VERS, SYSPROP_JAVA_VENDOR, SYSPROP_JAVA_VERS, SYSPROP_JAVA_VM_INFO,
			SYSPROP_JAVA_VM_NAME, SYSPROP_JAVA_VM_VENDOR, SYSPROP_JAVA_VM_VERSION,
			SYSPROP_LIB_PATH, SYSPROP_OS_ARCH, SYSPROP_OS_NAME, SYSPROP_OS_VERS,
			SYSPROP_JAVA_SPEC_VERS, SYSPROP_SUPPORTED_ENCODING, SYSPROP_USER_DIR,
			SYSPROP_USER_HOME, SYSPROP_USER_LANG, SYSPROP_USER_NAME, SYSPROP_USER_COUNTRY,
			SYSPROP_USER_REGION, SYSPROP_USER_TIMEZONE, SYSPROP_JAVA_VENDOR_URL };

	public final static boolean VALUE_MULTI_LINE = true;

	public final static boolean VALUE_ONE_LINE = false;
	public final static int VECTOR_FULL_INFOS = MASK_INFOS_JAVA + MASK_INFOS_OS + MASK_INFOS_USER
			+ MASK_INFOS_PATHS + MASK_OTHER_PROPS;

	public final static int VECTOR_INFOS_LESS_PATHS = MASK_INFOS_JAVA + MASK_INFOS_OS
			+ MASK_INFOS_USER + MASK_OTHER_PROPS;

	/**
	 * @param aSB
	 * @param aId
	 * @param aIdSize
	 * @param aValue
	 * @param aValueSize
	 * @param aEndLine
	 *            a endline character (eg ':' in a list of paths )
	 * @return
	 */
	public static StringBuilder addDescrAlignInSB(final StringBuilder aSB, final String aId,
			final int aIdSize, final String aValue, final int aValueSize, final char aEndLine) {

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
					aSB.append('\n').append(CXStringUtils.strFromChar(' ', aIdSize + 2));
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
	private static StringBuilder addJavaInfoDescrInSB(final StringBuilder aSB, final String aId) {

		return addJavaInfoDescrInSB(aSB, aId, System.getProperty(aId), SEP_NUL);
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aEndLine
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(final StringBuilder aSB, final String aId,
			final char aEndLine) {

		return addJavaInfoDescrInSB(aSB, aId, System.getProperty(aId), aEndLine);
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	private static StringBuilder addJavaInfoDescrInSB(final StringBuilder aSB, final String aId,
			final String aValue, final char aEndLine) {

		return addDescrAlignInSB(aSB, aId, ID_WITDH, aValue, LINE_WITDH - ID_WITDH, aEndLine);
	}

	/**
	 * requete de test du service d'administration
	 * 
	 * @param aSB
	 * @param aSep
	 * @return
	 */
	static StringBuilder addSeparatorInSB(final StringBuilder aSB, final char aSep) {

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

		return appendJavaContextInSB(aSB, '\n', VECTOR_FULL_INFOS, VALUE_MULTI_LINE);
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
			final char aSeparator, final int aInformationMask, final boolean aValueMultiLine) {

		if (aValueMultiLine) {
			addSeparatorInSB(aSB, aSeparator);
		}

		if ((aInformationMask & MASK_INFOS_JAVA) > 0) {
			appendSepLineInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_CLASS_VERS);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_ENDORSED_DIR);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_EXT_DIR);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_HOME);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_IO_TMPDIR);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_RUN_NAME);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_RUN_VERS);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_SPEC_VERS);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VENDOR);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VENDOR_URL);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VERS);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VM_INFO);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VM_NAME);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VM_VENDOR);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_VM_VERSION);
		}
		if ((aInformationMask & MASK_INFOS_OS) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
			appendSepLineInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_OS_ARCH);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_OS_NAME);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_OS_VERS);
		}
		if ((aInformationMask & MASK_INFOS_USER) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
			appendSepLineInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_DIR);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_HOME);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_LANG);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_NAME);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_REGION);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_COUNTRY);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_USER_TIMEZONE);
		}
		if ((aInformationMask & MASK_INFOS_PATHS) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
			appendSepLineInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_JAVA_CLASS_PATH,
					aValueMultiLine ? File.pathSeparatorChar : SEP_NUL);
			addSeparatorInSB(aSB, aSeparator);
			addJavaInfoDescrInSB(aSB, SYSPROP_LIB_PATH, aValueMultiLine ? File.pathSeparatorChar
					: SEP_NUL);
			addSeparatorInSB(aSB, aSeparator);

			addJavaInfoDescrInSB(aSB, SYSPROP_DEFAULT_CHARSET, Charset.defaultCharset()
					.displayName(), SEP_NUL);
			addSeparatorInSB(aSB, aSeparator);

			addJavaInfoDescrInSB(aSB, SYSPROP_SUPPORTED_ENCODING,
					formatEncodings(aValueMultiLine, CXOSUtils.dumpSupportedEncodings()),
					aValueMultiLine ? ';' : SEP_NUL);
		}

		if ((aInformationMask & MASK_OTHER_PROPS) > 0) {
			if (aSB.length() > 0) {
				addSeparatorInSB(aSB, aSeparator);
			}
			appendSepLineInSB(aSB, aSeparator);
			appendOtherPropsInSB(aSB, aSeparator);
		}
		return aSB;
	}

	/**
	 * Adds all the other properties
	 * 
	 * @param aSB
	 * @return
	 */
	private static StringBuilder appendOtherPropsInSB(final StringBuilder aSB, final char aSeparator) {

		CXSortListProperties wProps = new CXSortListProperties(System.getProperties(),
				CXSortList.ASCENDING);
		TreeSet<Entry<Object, Object>> wEntries = wProps.getTreeSet();
		String wPropId;
		int wI = 0;
		for (Entry<Object, Object> wEntry : wEntries) {
			wPropId = wEntry.getKey().toString();
			if (!isStandardSysProp(wPropId)) {
				if (wI > 0) {
					addSeparatorInSB(aSB, aSeparator);
				}
				addJavaInfoDescrInSB(aSB, wPropId);
				wI++;
			}
		}
		return aSB;
	}

	/**
	 * @param aSB
	 * @param aSeparator
	 * @return
	 */
	static StringBuilder appendSepLineInSB(final StringBuilder aSB, final char aSeparator) {

		aSB.append(CXStringUtils.strFromChar('-', LINE_WITDH));
		addSeparatorInSB(aSB, aSeparator);
		return aSB;
	}

	/**
	 * @param aValue
	 * @param aPos
	 * @param aEndLine
	 * @param aDefaultSize
	 * @return
	 */
	private static int calcValueSize(final String aValue, final int aPos, final char aEndLine,
			final int aDefaultSize) {

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
	 * <pre> </pre>
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
	public static String getJavaContext(final char aSeparator, final int aInformationMask,
			final boolean aValueMultiLineLine) {

		return appendJavaContextInSB(new StringBuilder(512), aSeparator, aInformationMask,
				aValueMultiLineLine).toString();
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

	/**
	 * @param aId
	 * @return
	 */
	public static boolean isStandardSysProp(final String aId) {

		if (aId == null || aId.isEmpty()) {
			return false;
		}
		for (String wId : SYSPROPS) {
			if (wId.equals(aId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	private CXJvmUtils() {
		super();
	}
}
