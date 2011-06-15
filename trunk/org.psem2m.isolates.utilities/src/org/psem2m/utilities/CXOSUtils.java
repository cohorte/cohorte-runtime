package org.psem2m.utilities;

import java.io.File;
import java.util.Iterator;

public class CXOSUtils {

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
	private final static String OSKEY_WIN2000 = "2000";
	private final static String OSKEY_WIN2003 = "2003";
	private final static String OSKEY_WIN2008 = "2008";

	private final static String OSKEY_WINNT = "NT";
	private final static String OSKEY_WINXP = "XP";

	private final static String SYS_PROPERTY_FILEENCODING = "file.encoding";

	private final static String SYS_PROPERTY_OSNAME = "os.name";

	public final static String SYS_PROPERTY_TMPDIR = "java.io.tmpdir";

	public final static String SYS_PROPERTY_USERDIR = "user.dir";

	/**
	 * @return
	 */
	public static String dumpSupportedEncodings() {
		Iterator<String> wEncodings = getSupportedEncodings();
		StringBuilder wSB = new StringBuilder(256);
		while (wEncodings.hasNext()) {
			if (wSB.length() > 0)
				wSB.append(',');
			wSB.append(wEncodings.next());
		}
		return wSB.toString();
	}

	/**
	 * @return
	 */
	public static String getDefaultFileEncoding() {
		return ENCODING_UTF_8;
	}

	/**
	 * @return
	 */
	public static String getOsFileEncoding() {
		return System.getProperty(SYS_PROPERTY_FILEENCODING);
	}

	/**
	 * @return
	 */
	public static String getOsName() {
		return System.getProperty(SYS_PROPERTY_OSNAME);
	}

	/**
	 * retourne l'encodage de l'outputstream "StdOut" de l'os "aOsName"
	 * 
	 * Encodage par defaut pour le systeme
	 */
	public static String getStdOutEncoding() {
		return getStdOutEncoding(getOsName());
	}

	/**
	 * retourne l'encodage de l'outputstream "StdOut" de l'os "aOsName"
	 * 
	 * @param aOsName
	 * @return
	 */
	public static String getStdOutEncoding(String aOsName) {
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
	 * @return
	 */
	public static boolean isOsWindows2000() {
		return isOsWindows2000(getOsName());
	}

	/**
	 * @return
	 */
	public static boolean isOsWindows2000(String aOsName) {
		return (aOsName.indexOf(OSKEY_WIN2000) > -1);
	}

	/**
	 * @return
	 */
	public static boolean isOsWindows2003() {
		return isOsWindows2003(getOsName());
	}

	/**
	 * @return
	 */
	public static boolean isOsWindows2003(String aOsName) {
		return (aOsName.indexOf(OSKEY_WIN2003) > -1);
	}

	/**
	 * @param aOsName
	 * @return
	 */
	public static boolean isOsWindows2008(String aOsName) {
		return (aOsName.indexOf(OSKEY_WIN2008) > -1);
	}

	/**
	 * @return
	 */
	public static boolean isOsWindowsFamily() {
		return isOsWindowsFamily(getOsName());
	}

	/**
	 * @return
	 */
	public static boolean isOsWindowsFamily(String aOsName) {
		return isOsWindowsXP(aOsName) || isOsWindows2000(aOsName)
				|| isOsWindows2003(aOsName) || isOsWindows2008(aOsName)
				|| isOsWindowsNT(aOsName);
	}

	/**
	 * @return
	 */
	public static boolean isOsWindowsNT() {
		return isOsWindowsNT(getOsName());
	}

	/**
	 * @return
	 */
	public static boolean isOsWindowsNT(String aOsName) {
		return (aOsName.indexOf(OSKEY_WINNT) > -1);
	}

	/**
	 * @return
	 */
	public static boolean isOsWindowsXP() {
		return isOsWindowsXP(getOsName());
	}

	/**
	 * @return
	 */
	public static boolean isOsWindowsXP(String aOsName) {
		return (aOsName.indexOf(OSKEY_WINXP) > -1);
	}

}
