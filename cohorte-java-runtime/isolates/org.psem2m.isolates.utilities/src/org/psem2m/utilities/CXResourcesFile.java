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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.StringTokenizer;

/**
 * @author isandlaTech - ogattaz
 * 
 */
public class CXResourcesFile extends CXResources {

	/**
	 * Insert le code langue (ex: "_fr") entre le nom et l'extension de
	 * l'absolutePath du File :
	 * 
	 * <pre>
	 * "D:\_devs_intradon\X3WebHEAD\SHARED_JAVA\src\com\adonix\gui\dialog\CDialog.properties"
	 * ou
	 * "D:\_devs_intradon\X3WebHEAD\SHARED_JAVA\src\com\adonix\gui\dialog\CDialoCDialog_en.properties"
	 * </pre>
	 * 
	 * deviennent
	 * 
	 * <pre>
	 * "D:\_devs_intradon\X3WebHEAD\SHARED_JAVA\src\com\adonix\gui\dialog\CDialog_fr.properties"
	 * </pre>
	 * 
	 * si la locale est "fr_FR".
	 * 
	 * @param aFile
	 * @param aLocale
	 * @return
	 * @throws Exception
	 */
	private static File adjustLocaleInFile(File aFile, Locale aLocale) throws Exception {
		return new File(aFile.getParent(), adjustLocaleInFileName(aFile.getName(), aLocale));
	}

	/**
	 * Insert le code langue (ex: "_fr") entre le nom et l'extension :
	 * "CDialog.properties" "CDialog_en.properties" deviennent
	 * "CDialog_fr.properties" si la locale est "fr_FR"
	 * 
	 * @param aName
	 * @param aLocale
	 * @return
	 * @throws Exception
	 */
	private static String adjustLocaleInFileName(String aName, Locale aLocale) throws Exception {
		String wExtension = CXStringUtils.EMPTY;
		int wDotPos = aName.indexOf('.');
		if (wDotPos > -1) {
			wExtension = aName.substring(wDotPos);
			aName = aName.substring(0, wDotPos);
		}
		int wUnderScorePos = aName.indexOf('_');
		if (wUnderScorePos > -1) {
			aName = aName.substring(0, wUnderScorePos);
		}

		StringBuilder wSB = new StringBuilder(64);
		wSB.append(aName);
		wSB.append('_');
		wSB.append(aLocale.getCountry());
		if (wDotPos > -1) {
			wSB.append(wExtension);
		}
		return wSB.toString();
	}

	/**
	 * @param aName
	 * @return
	 */
	private static Locale extractLocaleFromName(String aName) {
		int wDotPos = aName.indexOf('.');
		if (wDotPos > -1) {
			aName = aName.substring(0, wDotPos);
		}
		int wUnderScorePos = aName.indexOf('_');
		if (wUnderScorePos > -1) {
			StringTokenizer wST = new StringTokenizer(aName.substring(wUnderScorePos), "_");

			String wLanguage = ((wST.hasMoreTokens()) ? wST.nextToken() : CXStringUtils.EMPTY);
			String wCountry = ((wST.hasMoreTokens()) ? wST.nextToken() : CXStringUtils.EMPTY);
			String wVariant = ((wST.hasMoreTokens()) ? wST.nextToken() : CXStringUtils.EMPTY);
			return new Locale(wLanguage, wCountry, wVariant);
		}
		return null;
	}

	/**
	 * @param aFileName
	 * @return true if the file name ends by "_xx"
	 */
	private static boolean hasLocaleInFileName(String aFileName) {
		if (aFileName != null && aFileName.length() > 0) {
			return false;
		}
		int wPosPoint = aFileName.indexOf('.');
		if (wPosPoint > -1) {
			aFileName = aFileName.substring(0, wPosPoint);
		}
		return (aFileName.lastIndexOf('_') >= aFileName.length() - 2);
	}

	/**
	 * retourne vrai si la locale est egual a une locale simplifiee (une locale
	 * presente sous forme de suffixe d'un nom de fichier)
	 * 
	 * @param aLocale
	 * @param aFileLocale
	 * @return
	 */
	private static boolean isLocaleEqualsSimplifiedLocale(Locale aLocale, Locale aFileLocale) {
		// si la locale est simplifee (uniquement un code langue = test de la
		// langue
		if (aFileLocale.getCountry().equals(CXStringUtils.EMPTY)) {
			return aLocale.getLanguage().equals(aFileLocale.getLanguage());
		}
		// sinon, test d'egaulite complete
		else {
			return aLocale.equals(aFileLocale);
		}
	}

	/**
	 * Verifie et ajuste le fait que le nom du fichier "aFile" contient bien la
	 * langue de la locale "aLocale"
	 * 
	 * @param aFile
	 * @param aLocale
	 * @return
	 * @throws Exception
	 */
	public static File validResourcesFile(File aFile, Locale aLocale) throws Exception {
		String wName = aFile.getName();
		// le nom du fichier contient une "locale" (sous la forme "_fr" ) et si
		// la langue de cette locale et egual a la laonge de la locale passée en
		// parametre

		if (hasLocaleInFileName(wName)
				&& isLocaleEqualsSimplifiedLocale(aLocale, extractLocaleFromName(wName))) {
			return aFile;
		}
		// sinon, ajustement
		else {
			return adjustLocaleInFile(aFile, aLocale);
		}
	}

	protected File pFile = null;

	/**
	 * 
	 * 
	 * @param aId
	 *            identifiant du "ResourceBundle"
	 * @param aLocale
	 *            "locale" de fonctionnement: fr_FR, ...
	 * @param aPath
	 */
	public CXResourcesFile(File aFile, Locale aLocale) {
		super();
		pId = aFile.getName();
		setAskedLocale(aLocale);
		loadResourcFile(aFile, aLocale);
	}

	/**
	 * Retourne l'inputStream du fichier
	 * 
	 * @param aFile
	 * @return
	 * @throws Exception
	 */
	private InputStream buildInputStream(File aFile) throws Exception {
		if (!aFile.exists()) {
			throw new Exception(aFile.getAbsolutePath() + "does not exist");
		}
		return new FileInputStream(aFile);
	}

	/**
	 * @return
	 */
	public File getFile() {
		return pFile;
	}

	/**
	 * @return
	 */
	public boolean hasFile() {
		return (pFile != null);
	}

	/**
	 * @param aFile
	 * @param aLocale
	 */
	void loadResourcFile(File aFile, Locale aLocale) {
		try {
			pFile = validResourcesFile(aFile, aLocale);
			pResourceBundle = new PropertyResourceBundle(buildInputStream(pFile));
		} catch (Exception e) {
			pWhy = e.getLocalizedMessage();
			pResourceBundle = null;
		}
		// si on ne trouve pas la ressource de la locale , ou de la locale par
		// defaut, on charge la ressource de la locale US
		if (pResourceBundle == null) {
			try {
				Locale wAdjustedLocale = getDefaultLocale();
				pFile = validResourcesFile(aFile, wAdjustedLocale);
				pResourceBundle = new PropertyResourceBundle(buildInputStream(pFile));
				setCurrentLocale(wAdjustedLocale);
			} catch (Exception e) {
				pWhy = e.getLocalizedMessage();
				pResourceBundle = null;
			}
		}
	}
}
