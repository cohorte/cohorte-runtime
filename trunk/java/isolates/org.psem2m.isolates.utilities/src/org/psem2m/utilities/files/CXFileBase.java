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

import java.io.File;

/**
 * Classe de base pour la gestion de fichiers et répertoires
 * 
 * @author Sage - Grenoble
 * 
 */
public class CXFileBase extends File {

	public final static String MANIFEST_MF = "MANIFEST.MF";
	public final static String META_INF = "META-INF";
	public static String pEOL = "\n";
	public static char pEOLChar = '\n';
	private static final long serialVersionUID = 3258131349545432374L;
	public final static String TMP = "tmp";

	/**
	 * @param aPath
	 * @return
	 */
	public static String checkSeparator(String aPath) {
		if (aPath == null) {
			return null;
		} else {
			return aPath.replace(CXFileBase.getBadSeparatorChar(), separatorChar);
		}
	}

	/**
	 * @return
	 */
	public static char getBadSeparatorChar() {
		if (separatorChar == '\\') {
			return '/';
		} else {
			return '\\';
		}
	}

	/**
	 * @return
	 */
	public static String getEOL() {
		return pEOL;
	}

	/**
	 * @return
	 */
	public static char getEOLChar() {
		return pEOLChar;
	}

	/**
	 * 16w_104 - suppression des ajout systématique d'un "separatorChar" en fin
	 * de path d'un Dir !
	 * 
	 * @param aFile
	 * @param aSubPath
	 */
	public CXFileBase(CXFileDir aFileDir, String aSubPath) {
		super(aFileDir.getAbsolutePath(), checkSeparator(aSubPath));
	}

	public CXFileBase(File aFile) {
		super(aFile.getAbsolutePath());
	}

	/**
	 * @param aPath
	 */
	public CXFileBase(String aPath) {
		super(aPath);
	}

	/**
	 * 16w_104 - suppression des ajout systématique d'un "separatorChar" en fin
	 * de path d'un Dir !
	 * 
	 * @param aPath
	 * @param aSubPath
	 */
	public CXFileBase(String aPath, String aSubPath) {
		super(aPath, checkSeparator(aSubPath));
	}

	/**
	 * @param obj
	 * @return
	 */
	public boolean equals(CXFileBase obj) {
		if (obj == null) {
			return false;
		} else if ((this.isFile() && obj.isFile()) || (this.isDirectory() && obj.isDirectory())) {
			if (isPathCaseSensitive()) {
				return obj.getAbsolutePath().equals(getAbsolutePath());
			} else {
				return obj.getAbsolutePath().equalsIgnoreCase(getAbsolutePath());
			}
		} else {
			return false;
		}
	}

	/**
	 * FDB - Méthode volontairement passée en @Deprecated pour forcer
	 * l'utilisation de getAbsolutePath
	 */
	@Override
	@Deprecated
	public String getPath() {
		return super.getPath();
	}

	/**
	 * Retourne un path relatif
	 * 
	 * @param aRacine
	 * @return
	 * @throws Exception
	 */
	public String getRelativePath(String aRacine) throws Exception {
		String wResult = this.getAbsolutePath();
		String wDirCanonical = aRacine;
		String wCurrentCanonical = this.getAbsolutePath();

		if (wCurrentCanonical.indexOf(wDirCanonical) == 0) {
			wResult = wCurrentCanonical.substring(wDirCanonical.length());
		}
		if (wResult.startsWith(separator)) {
			wResult = wResult.substring(1);
		}
		return wResult;
	}

	/**
	 * @return true if the separatorchar used by the file system is a slash
	 */
	protected boolean isPathCaseSensitive() {
		return separatorChar == '/';
	}

	/**
	 * @param aMethod
	 * @param aMessage
	 */
	protected void traceError(String aMethod, String aMessage) {
		System.out.print("FILE ERROR - Path='" + getAbsolutePath() + "'\n");
		if (aMessage != null && aMessage != null) {
			System.out.print("--> Method=" + aMethod + "\n");
			System.out.print("--> Message=" + aMessage + "\n");
		}
	}

	/**
	 * @param aMethod
	 * @param a
	 */
	protected void traceError(String aMethod, Throwable a) {
		if (a != null) {
			traceError(aMethod, a.getMessage());
		}
	}
}
