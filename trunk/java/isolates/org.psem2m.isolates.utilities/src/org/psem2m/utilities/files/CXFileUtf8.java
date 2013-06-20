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

/**
 * Lecture/Ecriture de fichiers XML Encodage UTF-8 par defaut
 * 
 * @author ogattaz
 * 
 */
public class CXFileUtf8 extends CXFileText {

	private static final long serialVersionUID = 3617289017402931254L;

	/**
	 * @param aFile
	 */
	public CXFileUtf8(CXFile aFile) {
		super(aFile);
		myInit();
	}

	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileUtf8(CXFileDir aParentDir, String aFileName) {
		super(aParentDir, aFileName);
		myInit();
	}

	/**
	 * @param aFullPath
	 */
	public CXFileUtf8(String aFullPath) {
		super(aFullPath);
		myInit();
	}

	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileUtf8(String aParentDir, String aFileName) {
		super(aParentDir, aFileName);
		myInit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.files.CXFile#finalize()
	 */
	@Override
	public void finalize() {
		super.finalize();
	}

	/**
	 * 
	 */
	protected void myInit() {
		// On reecrit le BOM utf-8 standard meme si le fichiher existe
		setKeepExistingBOM(false);
		// On force l'encdage par defaut a UTF-8
		setDefaultEncoding(ENCODING_UTF_8);
	}
}
