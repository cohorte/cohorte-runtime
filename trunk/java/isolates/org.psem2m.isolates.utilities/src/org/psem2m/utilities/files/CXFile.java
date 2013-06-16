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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.psem2m.utilities.CXStringUtils;

/**
 * Classe de gestion de fichiers binaires ou text !! MonoThread
 */
public class CXFile extends CXFileBase {

	public final static boolean APPEND = true;
	public final static boolean DELETE_IF_EXIST = true;
	public static final String sepExtension = ".";
	public static final char sepExtensionChar = '.';

	private static final long serialVersionUID = 4122260644587321138L;

	/**
	 * @param aPref
	 * @param aSuff
	 * @param aParentDir
	 * @return
	 * @throws Exception
	 */
	public static CXFile createTempFile(String aPref, String aSuff, CXFileDir aParentDir)
			throws Exception {
		File wFile = File.createTempFile(aPref, aSuff, aParentDir);
		return new CXFile(wFile.getPath());
	}

	/**
	 * @param aDestFile
	 * @param aContentFile
	 * @return
	 * @throws Exception
	 */
	private static CXFile replaceContentBy(CXFile aDestFile, CXFile aContentFile) throws Exception {
		RandomAccessFile wRAF = new RandomAccessFile(aDestFile, "rw");
		wRAF.setLength(0);// clean file
		wRAF.write(aContentFile.readAllBytes());
		wRAF.getFD().sync();
		wRAF.close();

		return aDestFile;
	}

	/**
	 * 
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFile(CXFileDir aParentDir, String aFileName) {
		super(aParentDir, aFileName);
	}

	/**
	 * 
	 * @param aFile
	 */
	public CXFile(File aFile) {
		super(aFile);
	}

	/**
	 * 
	 * @param aFullPath
	 */
	public CXFile(String aFullPath) {
		super(aFullPath);
	}

	/**
	 * 
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFile(String aParentDir, String aFileName) {
		super(aParentDir, aFileName);
	}

	/**
	 * @param aAction
	 * @throws Exception
	 */
	private void assertExist(String aAction) throws IOException {
		if (!exists()) {
			throw new IOException("Can't " + aAction + " file '" + getAbsolutePath()
					+ "'.\nFile not found.");
		}
	}

	/**
	 * 
	 */
	public void close() {
		// Rien - A reimplementer
	}

	/**
	 * Deplacement d'un fichier
	 * 
	 * @return aDestFile
	 * @throws Exception
	 */
	public CXFile copyTo(CXFile aDestFile, boolean aDeleteIfExists) throws Exception {
		assertExist("copy");
		this.close();
		if ((!aDestFile.exists()) || (aDestFile.exists() && aDeleteIfExists)) {
			FileInputStream wFis = getInputStream();
			FileOutputStream wFos = aDestFile.getOutputStream(false);
			int wSize = wFis.available();
			int wBuffSize = 4096;
			int wCopySize = 0;
			int wReadSize = 0;
			byte[] wData = new byte[wBuffSize];
			while (wCopySize < wSize) {
				wReadSize = wFis.read(wData, 0, wBuffSize);
				wFos.write(wData, 0, wReadSize);
				wCopySize += wReadSize;
			}
			wFos.close();
			wFis.close();
		}
		// else
		// throw new IOException("Can't copy file '" + getPath() + "' to '" +
		// aDestFile.getAbsolutePath() +
		// "'.\nDestination file already exists.");
		return aDestFile;
	}

	/**
	 * Copie d'un fichier
	 * 
	 * @param aDir
	 *            - Repertoire de copie - null--> Repertoire courant
	 * @param aName
	 *            - nom du fichier de destination
	 * @param aDeleteIfExists
	 *            - Suppresion du fichier destination
	 * @return fichier de destination
	 * @throws Exception
	 */
	public CXFile copyTo(CXFileDir aDir, String aName, boolean aDeleteIfExists) throws Exception {
		CXFileDir wDir = aDir;
		if (wDir == null) {
			wDir = getParentDirectory();
		}
		wDir.failIfNotExist();
		CXFile wDestFile = new CXFile(aDir, aName);
		return copyTo(wDestFile, aDeleteIfExists);
	}

	/**
	 * Cree les repertoire peres de this
	 * 
	 * @return true si repertoire a ete cree false si il existait deja
	 */
	public boolean createParentDirectories() throws Exception {
		return getParentDirectory().createHierarchy();
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#delete()
	 */
	@Override
	public boolean delete() {
		this.close();
		return super.delete();
	}

	/**
	 * @param aDestFile
	 * @param aCreateIfNotExists
	 * @return
	 * @throws Exception
	 */
	public CXFile duplicateIn(CXFile aDestFile, boolean aCreateIfNotExists) throws Exception {
		assertExist("duplicateIn");

		if (aCreateIfNotExists && !aDestFile.exists()) {
			aDestFile.createNewFile();
		}

		aDestFile.assertExist("duplicateIn");

		return replaceContentBy(aDestFile, this);
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		close();
	}

	/**
	 * @return
	 */
	public String getExtension() {
		return CXStringUtils.strRightBack(getAbsolutePath(), sepExtension);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public FileInputStream getInputStream() throws IOException {
		assertExist("open (read)");
		return new FileInputStream(this); // CXInputStream(this);
	}

	/**
	 * @return
	 */
	public String getNameWithoutExtension() {
		if (hasExtension()) {
			return CXStringUtils.strLeftBack(getName(), sepExtension);
		} else {
			return getName();
		}
	}

	/**
	 * @param aAppend
	 * @return
	 * @throws Exception
	 */
	public FileOutputStream getOutputStream(boolean aAppend) throws Exception {
		if (aAppend) {
			assertExist("open (append)");
		}
		return new FileOutputStream(this.getAbsolutePath(), aAppend);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public CXFileDir getParentDirectory() throws Exception {
		return new CXFileDir(getParentFile().getAbsolutePath());
	}

	/**
	 * @return
	 */
	public boolean hasExtension() {
		return getAbsolutePath().indexOf(sepExtension) != -1;
	}

	/**
	 * @param aExtension
	 * @return
	 */
	public boolean hasExtension(String aExtension) {
		return (hasExtension() && getExtension().equals(aExtension));
	}

	/**
	 * @param aDestFile
	 * @param aDeleteIfExists
	 * @return
	 * @throws Exception
	 */
	public CXFile moveTo(CXFile aDestFile, boolean aDeleteIfExists) throws Exception {
		assertExist("move");
		close();

		if (aDestFile.exists()) {
			if (aDeleteIfExists) {
				aDestFile.delete();
			} else {
				throw new IOException("Can't move file '" + getAbsolutePath() + "' to '"
						+ aDestFile.getAbsolutePath() + "'.\nDestination file already exists.");
			}
		}
		if (!renameTo(aDestFile)) {
			throw new IOException("Can't move file '" + getAbsolutePath() + "' to '"
					+ aDestFile.getAbsolutePath() + "'.\nMethode \"renameTo\" return false.");
		}
		return aDestFile;
	}

	/**
	 * Deplacement d'un fichier
	 * 
	 * @param aDir
	 *            - Repertoire de copie - null--> Repertoire courant
	 * @param aName
	 *            - nom du fichier de destination
	 * @param aDeleteIfExists
	 *            - Suppresion du fichier destination
	 * @throws Exception
	 */
	public CXFile moveTo(CXFileDir aDir, String aName, boolean aDeleteIfExists) throws Exception {
		CXFileDir wDir = aDir;
		if (wDir == null) {
			wDir = getParentDirectory();
		}
		wDir.failIfNotExist();
		CXFile wDestFile = new CXFile(aDir, aName);
		return moveTo(wDestFile, aDeleteIfExists);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public byte[] readAllBytes() throws Exception {
		return readAllBytes(Integer.MAX_VALUE);
	}

	/**
	 * @param aCount
	 * @return
	 * @throws Exception
	 */
	public byte[] readAllBytes(int aCount) throws Exception {
		if (super.canRead()) {
			FileInputStream wStream = getInputStream();
			int wMax = wStream.available();
			byte[] wData = new byte[aCount > wMax ? wMax : aCount];
			wStream.read(wData);
			wStream.close();
			return wData;
		} else {
			throw new IOException("File not opened - Can't read file '" + getAbsolutePath() + "'");
		}
	}

	/**
	 * @param aContentFile
	 * @return
	 * @throws Exception
	 */
	public CXFile replaceContentBy(CXFile aContentFile) throws Exception {
		assertExist("replaceContentBy");

		aContentFile.assertExist("replaceContentBy");

		return replaceContentBy(this, aContentFile);
	}

	/**
	 * @param aData
	 * @throws Exception
	 */
	public void writeAll(byte[] aData) throws Exception {
		if (aData != null) {
			FileOutputStream wStream = getOutputStream(false);
			wStream.write(aData);
			wStream.close();
		}
	}

}
