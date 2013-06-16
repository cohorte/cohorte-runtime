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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author ogattaz
 * 
 */
public final class CXOSUtils {

	/**
	 * Cp858 Variant of Cp850 with Euro character
	 * 
	 * Cp850 MS-DOS Latin-1
	 * 
	 * Microsoft Windows OEM Codepage : 858 (Multilingual Latin I + Euro)
	 * 
	 * http://www.microsoft.com/globaldev/reference/oem/858.htm
	 * 
	 * for example a small 'e' acute is character 0xe9 in ISO-8859-1 but
	 * character 0x82 in Windows OEM fonts.
	 */
	public final static String ENCODING_CP_858 = "Cp858";

	/**
	 * http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html
	 * 
	 * ISO 8859-1, same as 8859_1, USA, Europe, Latin America, Caribbean,
	 * Canada, Africa, Latin-1, (Danish, Dutch, English, Faeroese, Finnish,
	 * French, German, Icelandic, Irish, Italian, Norwegian, Portuguese, Spanish
	 * and Swedish). Beware, for NT, the default is Cp1252 a variant of Latin-1,
	 * controlled by the control panel regional settings.
	 */
	public final static String ENCODING_ISO_8859_1 = "ISO-8859-1";

	public final static String ENCODING_UTF_16 = "UTF-16";

	public final static String ENCODING_UTF_16BE = "UTF-16BE";

	public final static String ENCODING_UTF_16LE = "UTF-16LE";

	public final static String ENCODING_UTF_32BE = "UTF-32BE";

	public final static String ENCODING_UTF_32LE = "UTF-32LE";

	public final static String ENCODING_UTF_8 = "UTF-8";

	/**
	 * http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html
	 * 
	 * windows-1250 Cp1250 Windows Eastern European
	 * 
	 */
	public final static String ENCODING_WINDOWS_1250 = "windows-1250";

	/**
	 * http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html
	 * 
	 * windows-1252 Cp1252 Windows Latin-1
	 * 
	 * Microsoft Windows variant of Latin-1, NT default. Beware. Some unexpected
	 * translations occur when you read with this default encoding, e.g. codes
	 * 128..159 are translated to 16 bit chars with bits in the high order byte
	 * on. It does not just truncate the high byte on write and pad with 0 on
	 * read. For true Latin-1 see 8859-1.
	 */
	public final static String ENCODING_WINDOWS_1252 = "windows-1252";

	private final static String FILE_EXT_TXT = "txt";

	private final static String FILE_NAME_DUMMY = "dummy";
	private final static String OSKEY_MACOSX = "Mac OS X";

	private final static String OSKEY_WIN2000 = "2000";
	private final static String OSKEY_WIN2003 = "2003";
	private final static String OSKEY_WIN2008 = "2008";
	private final static String OSKEY_WIN7 = "Windows 7";
	private final static String OSKEY_WIN8 = "Windows 8";
	private final static String OSKEY_WINNT = "NT";
	private final static String OSKEY_WINVISTA = "Vista";
	private final static String OSKEY_WINXP = "XP";
	private final static String SYS_PROPERTY_FILEENCODING = "file.encoding";

	private final static String SYS_PROPERTY_OSNAME = "os.name";

	public final static String SYS_PROPERTY_TMPDIR = "java.io.tmpdir";

	public final static String SYS_PROPERTY_USERDIR = "user.dir";

	/**
	 * add a pair
	 * 
	 * <pre>
	 * ANT_PATH=[/Applications/apache-ant-1.8.2]
	 * APP_ICON_286=[../Resources/Eclipse.icns]
	 * </pre>
	 * 
	 * @param aSB
	 *            the buffer
	 * @param aId
	 *            the id
	 * @param wMaxIdLen
	 *            the len a the id
	 * @param aValue
	 *            the value
	 * @param aValueMultiLineLine
	 *            if false, the line separator present in the value are replaced
	 *            by '§'
	 * @return
	 */
	private static StringBuilder addEnvPropertiesInfoDescrInSB(final StringBuilder aSB,
			final String aId, final int wMaxIdLen, String aValue, final boolean aValueMultiLineLine) {

		if (!aValueMultiLineLine && aValue.contains(CXStringUtils.LINE_SEP)) {
			aValue = aValue.replace('\n', '§');
		}

		return CXJvmUtils.addDescrAlignInSB(aSB, aId, wMaxIdLen, aValue, 120 - wMaxIdLen,
				CXJvmUtils.SEP_NUL);
	}

	/**
	 * @return
	 */
	public static String dumpSupportedEncodings() {

		Iterator<String> wEncodings = getSupportedEncodings();
		StringBuilder wSB = new StringBuilder(256);
		while (wEncodings.hasNext()) {
			if (wSB.length() > 0) {
				wSB.append(',');
			}
			wSB.append(wEncodings.next());
		}
		return wSB.toString();
	}

	/**
	 * @return the defaut encoding which must be used to store character files.
	 */
	public static String getDefaultFileEncoding() {

		return ENCODING_UTF_8;
	}

	/**
	 * Dump the environment variables
	 * 
	 * @return the environment variables as a name-value pairs table
	 */
	public static String getEnvContext() {

		return getEnvContext('\n', CXJvmUtils.VALUE_MULTI_LINE);
	}

	/**
	 * 
	 * Dump the environment variables
	 * 
	 * @param aSeparator
	 *            the separator included between each information
	 * @param aValueMultiLineLine
	 *            accepts format multiline information if true
	 * @return the environment variables as a name-value pairs list separated by
	 *         the separator
	 * @throws java.io.IOException
	 */
	public static String getEnvContext(final char aSeparator, final boolean aValueMultiLineLine) {

		StringBuilder wSB = new StringBuilder();

		if (aValueMultiLineLine) {
			CXJvmUtils.addSeparatorInSB(wSB, aSeparator);
			CXJvmUtils.appendSepLineInSB(wSB, '\n');
		}

		try {
			Properties wEnv = getEnvironment();

			int wMaxKeyLen = -1;
			int wLen;
			for (Object wKey : wEnv.keySet()) {
				wLen = String.valueOf(wKey).length();
				if (wLen > wMaxKeyLen) {
					wMaxKeyLen = wLen;
				}
			}

			CXSortListProperties wSortedEnv = new CXSortListProperties(wEnv, CXSortList.ASCENDING,
					CXSortList.SORTBYKEY);

			Set<Entry<Object, Object>> wProps = wSortedEnv.getTreeSet();

			int wI = 0;
			for (Entry<Object, Object> wProp : wProps) {
				if (wI > 0) {
					wSB.append(aSeparator);
				}
				addEnvPropertiesInfoDescrInSB(wSB, String.valueOf(wProp.getKey()), wMaxKeyLen,
						String.valueOf(wProp.getValue()), aValueMultiLineLine);
				wI++;
			}
		} catch (Exception e) {
			wSB.append(CXException.eInString(e));
		}

		return wSB.toString();
	}

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	public static Properties getEnvironment() throws java.io.IOException {

		return isOsWindowsFamily() ? getEnvWindows() : getEnvUnix();
	}

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	public static Properties getEnvUnix() throws java.io.IOException {

		Properties env = new Properties();
		env.load(Runtime.getRuntime().exec("env").getInputStream());
		return env;
	}

	/**
	 * @return ans
	 * @throws java.io.IOException
	 */
	public static Properties getEnvWindows() throws java.io.IOException {

		Properties env = new Properties();
		// doesn't suport 95,98, millenium...
		if (isOsWindowsFamily()) {
			env.load(Runtime.getRuntime().exec("cmd.exe /c set").getInputStream());
		}
		return env;
	}

	/**
	 * @return the encoding currently used
	 */
	public static String getOsFileEncoding() {

		return System.getProperty(SYS_PROPERTY_FILEENCODING);
	}

	/**
	 * @return the name of the current OS
	 */
	public static String getOsName() {

		return System.getProperty(SYS_PROPERTY_OSNAME);
	}

	/**
	 * @return the encoding of the outputstream "StdOut"
	 */
	public static String getStdOutEncoding() {

		return getStdOutEncoding(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return the encoding of the outputstream "StdOut" of the specified OS
	 */
	public static String getStdOutEncoding(final String aOsName) {

		if (aOsName == null || aOsName.length() == 0) {
			return ENCODING_ISO_8859_1;
		} else if (isOsWindowsFamily(aOsName)) {
			return ENCODING_CP_858;
		} else {
			return ENCODING_ISO_8859_1;
		}
	}

	/**
	 * @return
	 */
	public static Iterator<String> getSupportedEncodings() {

		return java.nio.charset.Charset.availableCharsets().keySet().iterator();
	}

	/**
	 * @return
	 */
	private static String getTempAbsolutePath() {

		String wPath = null;
		try {
			File wTempFile = File.createTempFile(FILE_NAME_DUMMY, FILE_EXT_TXT);
			wPath = wTempFile.getParent();
			wTempFile.delete();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return wPath;
	}

	/**
	 * @return
	 */
	public static String getTempPath() {

		String wPath = System.getProperty(SYS_PROPERTY_TMPDIR);

		if (wPath == null || wPath.length() == 0) {
			// System.out.println(
			// "Temporary directory is unknown ('java.io.tmpdir'), using directory used by 'File.createTempFile'."
			// );

			wPath = getTempAbsolutePath();

			if (wPath == null || wPath.length() == 0) {
				// System.out.println("'File.createTempFile' doesn't return directory, using current directory ('user.dir').");

				wPath = System.getProperty(SYS_PROPERTY_USERDIR);
			}
		}
		return wPath;
	}

	/**
	 * @return true if the current OS is Mac OS X
	 */
	public static boolean isOsMacOsX() {

		return isOsMacOsX(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Mac OS X
	 */
	public static boolean isOsMacOsX(final String aOsName) {

		return aOsName.indexOf(OSKEY_MACOSX) > -1;
	}

	/**
	 * @return true if the current OS is an Unix one
	 */
	public static boolean isOsUnixFamily() {

		return isOsUnixFamily(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of an Unix one
	 */
	public static boolean isOsUnixFamily(final String aOsName) {

		return !isOsWindowsFamily();
	}

	/**
	 * @return true if the current OS is Windows 2000
	 */
	public static boolean isOsWindows2000() {

		return isOsWindows2000(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows 2000
	 */
	public static boolean isOsWindows2000(final String aOsName) {

		return aOsName.indexOf(OSKEY_WIN2000) > -1;
	}

	/**
	 * @return true if the current OS is Windows 2003
	 */
	public static boolean isOsWindows2003() {

		return isOsWindows2003(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows 2003
	 */
	public static boolean isOsWindows2003(final String aOsName) {

		return aOsName.indexOf(OSKEY_WIN2003) > -1;
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows 2008
	 */
	public static boolean isOsWindows2008(final String aOsName) {

		return aOsName.indexOf(OSKEY_WIN2008) > -1;
	}

	/**
	 * @return true if the current OS is Windows 7
	 */
	public static boolean isOsWindowsEight() {

		return isOsWindowsNT(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows 7
	 */
	public static boolean isOsWindowsEight(final String aOsName) {

		return aOsName.indexOf(OSKEY_WIN8) > -1;
	}

	/**
	 * @return true if the current OS is a Windows one
	 */
	public static boolean isOsWindowsFamily() {

		return isOsWindowsFamily(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of a Windows one
	 */
	public static boolean isOsWindowsFamily(final String aOsName) {

		return isOsWindowsXP(aOsName) || isOsWindowsSeven(aOsName) || isOsWindowsEight(aOsName)
				|| isOsWindowsVista(aOsName) || isOsWindows2003(aOsName)
				|| isOsWindows2008(aOsName) || isOsWindows2000(aOsName) || isOsWindowsNT(aOsName);
	}

	/**
	 * @return true if the current OS is Windows NT
	 */
	public static boolean isOsWindowsNT() {

		return isOsWindowsNT(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows NT
	 */
	public static boolean isOsWindowsNT(final String aOsName) {

		return aOsName.indexOf(OSKEY_WINNT) > -1;
	}

	/**
	 * @return true if the current OS is Windows 7
	 */
	public static boolean isOsWindowsSeven() {

		return isOsWindowsNT(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows 7
	 */
	public static boolean isOsWindowsSeven(final String aOsName) {

		return aOsName.indexOf(OSKEY_WIN7) > -1;
	}

	/**
	 * @return true if the current OS is Windows Vista
	 */
	public static boolean isOsWindowsVista() {

		return isOsWindowsVista(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows Vista
	 */
	public static boolean isOsWindowsVista(final String aOsName) {

		return aOsName.indexOf(OSKEY_WINVISTA) > -1;
	}

	/**
	 * @return true if the current OS is Windows XP
	 */
	public static boolean isOsWindowsXP() {

		return isOsWindowsXP(getOsName());
	}

	/**
	 * @param aOsName
	 *            the OS name to test
	 * @return true if the OS name match the identifier of Windows XP
	 */
	public static boolean isOsWindowsXP(final String aOsName) {

		return aOsName.indexOf(OSKEY_WINXP) > -1;
	}

	/**
	 * 
	 */
	private CXOSUtils() {
		super();
	}
}
