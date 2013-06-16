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
package org.psem2m.utilities.files;

import java.io.IOException;
import java.util.Properties;

import org.psem2m.utilities.CXStringUtils;

/**
 * Classe de gestion de fichiers de type "properties"
 * 
 * Prend en compte l'encodage ISO_8859_1 et la gestion des caracteres non
 * encodables (> 255 ) sous forme de sequence escape : "\ u 0 0 0 0"
 * 
 * !! MonoThread
 * 
 * @author ogattaz
 */
public class CXFileProperties extends CXFileText {

	public static final int PROP_LOWERCASE = 2;
	// Options pour readProperties
	public static final int PROP_UNCHANGE = 0;
	public static final int PROP_UPPERCASE = 1;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3258688827626239288L;

	/**
	 * @param aText
	 * @return
	 */
	private static boolean containsUnicodeChar(String aText) {
		return (countUnicodeChar(aText) > 0);
	}

	/**
	 * @param aText
	 * @return
	 */
	private static boolean containsUnicodeEscapes(String aText) {
		return (countUnicodeEscapes(aText) > 0);
	}

	/**
	 * @param aText
	 * @return
	 */
	private static int countUnicodeChar(String aText) {
		int wNbUnicodeChar = 0;
		int wMax = aText.length();
		int wI = 0;
		while (wI < wMax) {
			if (aText.charAt(wI) > 255) {
				wNbUnicodeChar++;
			}
			wI++;
		}
		return wNbUnicodeChar;
	}

	/**
	 * @param aText
	 * @return
	 */
	private static int countUnicodeEscapes(String aText) {
		int wNbUnicodeEscapes = 0;
		int wMax = aText.length();
		int wI = 0;
		while (wI < wMax) {
			/*
			 * presence d'une sequence du type "\ u 0 0 0 0"
			 */
			if (aText.charAt(wI) == '\\' && (wI + 5 < wMax) && aText.charAt(wI + 1) == 'u'
					&& nextFourCharAreDigits(aText, wI + 1)) {
				wNbUnicodeEscapes++;
				wI += 5;
			}
			wI++;
		}
		return wNbUnicodeEscapes;
	}

	/**
	 * retourne vrai si les 4 caracteres situes apres l'offset sont des chiffres
	 * 
	 * @return
	 */
	private static boolean nextFourCharAreDigits(String aText, int aOffset) {
		int wI = aOffset + 1;
		int wMax = wI + 4;
		boolean wAreDigits = (wMax < aText.length());

		while (wI < wMax && wAreDigits) {
			wAreDigits = (wAreDigits & Character.isDigit(aText.charAt(wI)));
			wI++;
		}
		return wAreDigits;
	}

	/**
	 * @param aFile
	 */
	public CXFileProperties(CXFile aFile) {
		super(aFile);
		myInit();
	}

	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileProperties(CXFileDir aParentDir, String aFileName) {
		super(aParentDir, aFileName);
		myInit();
	}

	/**
	 * @param aFullPath
	 */
	public CXFileProperties(String aFullPath) {
		super(aFullPath);
		myInit();
	}

	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileProperties(String aParentDir, String aFileName) {
		super(aParentDir, aFileName);
		myInit();
	}

	/**
	 * @param aText
	 * @return
	 */
	private String convertNonIso8859ToUnicodeEscapes(String aText) {
		if (!containsUnicodeChar(aText)) {
			return aText;
		}

		StringBuilder wSB = new StringBuilder();
		char wChar;
		int wMax = aText.length();
		int wI = 0;
		while (wI < wMax) {
			wChar = aText.charAt(wI);
			if (wChar > 255) {
				wSB.append(CXStringUtils.UNICODE_PREFIX);
				wSB.append(CXStringUtils.strAdjustRight(wChar, 4));
			} else {
				wSB.append(wChar);
			}
			wI++;
		}

		return wSB.toString();
	}

	/**
	 * @param aText
	 * @return
	 */
	private String convertUnicodeEscapesToNonIso8859(String aText) {
		if (!containsUnicodeEscapes(aText)) {
			return aText;
		}

		StringBuilder wSB = new StringBuilder();
		char wChar;
		int wMax = aText.length();
		int wI = 0;
		while (wI < wMax) {
			wChar = aText.charAt(wI);
			// presence d'une sequence du type "\u0000"
			if (wChar == '\\' && (wI + 5 < wMax) && aText.charAt(wI + 1) == 'u'
					&& nextFourCharAreDigits(aText, wI + 1)) {

				wSB.append((char) Integer.parseInt(aText.substring(wI + 2, wI + 5)));
				wI += 5;
			} else {
				wSB.append(wChar);
			}
			wI++;
		}

		return aText;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public Properties getProperties() throws IOException {

		return getProperties(PROP_UNCHANGE, PROP_UNCHANGE);
	}

	/**
	 * @param aOptionKey
	 *            si true --> Cles en majuscules
	 * @param aOptionValue
	 *            si true --> Valeurs en majuscules
	 * @return
	 * @throws IOException
	 */
	public Properties getProperties(final int aOptionKey, final int aOptionValue)
			throws IOException {

		Properties wResult = new Properties();
		openReadLine();
		String wLine = readLine();
		while (wLine != null) {
			wLine = CXStringUtils.strFullTrim(wLine);
			if (wLine.length() != 0 && !wLine.startsWith("#")) {
				// Key
				String wKey = CXStringUtils.strLeft(wLine, "=");

				// nothing if aOptionKey == PROP_UNCHANGE
				if (aOptionKey == PROP_UPPERCASE) {
					wKey = wKey.toUpperCase();
				} else if (aOptionKey == PROP_LOWERCASE) {
					wKey = wKey.toLowerCase();
				}

				// Value
				String wValue = CXStringUtils.strRight(wLine, "=");
				// nothing if aOptionValue == PROP_UNCHANGE
				if (aOptionValue == PROP_UPPERCASE) {
					wValue = wValue.toUpperCase();
				} else if (aOptionValue == PROP_LOWERCASE) {
					wValue = wValue.toLowerCase();
				}
				// Put
				wResult.put(wKey, wValue);
			}
			wLine = readLine();
		}
		close();
		return wResult;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public Properties getPropertiesLowCase() throws IOException {

		return getProperties(PROP_LOWERCASE, PROP_LOWERCASE);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public Properties getPropertiesUpperCase() throws IOException {

		return getProperties(PROP_UPPERCASE, PROP_UPPERCASE);
	}

	/**
	 * realise l'initialisation specifique de ce type de fichier
	 */
	protected void myInit() {
		/*
		 * On force l'encdage par defaut a ISO-8859-1
		 */
		setDefaultEncoding(ENCODING_ISO_8859_1);
	}

	/*
	 * decode les "escape sequence Unicode" presentes dans la chaine lue dans le
	 * fichier sous-jascent
	 * 
	 * Cf. When saving properties to a stream or loading them from a stream, the
	 * ISO 8859-1 character encoding is used. For characters that cannot be
	 * directly represented in this encoding, Unicode escapes are used; however,
	 * only a single 'u' character is allowed in an escape sequence.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.adonix.adminsrv.utils.CXFileText#readAll()
	 */
	@Override
	public String readAll() throws Exception {
		return convertUnicodeEscapesToNonIso8859(super.readAll());
	}

	/*
	 * decode les "escape sequence Unicode" presentes dans la chaine lue dans le
	 * fichier sous-jascent
	 * 
	 * Cf. When saving properties to a stream or loading them from a stream, the
	 * ISO 8859-1 character encoding is used. For characters that cannot be
	 * directly represented in this encoding, Unicode escapes are used; however,
	 * only a single 'u' character is allowed in an escape sequence.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.adonix.adminsrv.utils.CXFileText#readLine()
	 */
	@Override
	public String readLine() throws IOException {
		return convertUnicodeEscapesToNonIso8859(super.readLine());
	}

	/*
	 * code en "escape sequence Unicode" les caracteres non encodables (cf. en
	 * ISO-8859) avant d'ecrire la chaine dans le fichier sous-jascent
	 * 
	 * Cf. When saving properties to a stream or loading them from a stream, the
	 * ISO 8859-1 character encoding is used. For characters that cannot be
	 * directly represented in this encoding, Unicode escapes are used; however,
	 * only a single 'u' character is allowed in an escape sequence.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.adonix.adminsrv.utils.CXFileText#write(java.lang.String)
	 */
	@Override
	public void write(String aString) throws Exception {
		/*
		 * ecrit la chaine de caractere dans le fichier text sous jascent (cf
		 * ISO-8859-1) en convertissant tous les caracteres non representable en
		 * ISO-8859-1 (cf. > 255) en utilisant le codage "Unicode escapes" (cf.
		 * "\ u X X X X" )
		 * 
		 * voir :
		 * http://java.sun.com/docs/books/jls/second_edition/html/lexical.
		 * doc.html#44591
		 */
		super.write(convertNonIso8859ToUnicodeEscapes(aString));
	}
}
