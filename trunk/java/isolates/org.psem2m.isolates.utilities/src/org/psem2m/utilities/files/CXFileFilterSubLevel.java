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
import java.io.FileFilter;

import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CXFileFilterSubLevel extends CXFileFilter implements FileFilter {

	private final int pMaxSubLevel;

	/**
	 * @param aMaxSubLevel
	 */
	public CXFileFilterSubLevel(int aMaxSubLevel) {
		this(aMaxSubLevel, null, INCLUDE);
	}

	/**
	 * @param aMaxSubLevel
	 * @param subFileFilter
	 * @param include
	 */
	public CXFileFilterSubLevel(int aMaxSubLevel, FileFilter subFileFilter, boolean include) {
		super(subFileFilter, include);
		pMaxSubLevel = aMaxSubLevel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File aFile) {
		String wPath = aFile.getAbsolutePath();
		int wLevel = CXStringUtils.countChar(wPath, File.separatorChar);
		boolean wOK = wLevel < pMaxSubLevel;
		if (wOK && this.hasSubFileFilter()) {
			wOK = getSubFileFilter().accept(aFile);
		}
		return wOK;
	}

	/**
	 * @return
	 */
	protected int getMaxSubLevel() {
		return pMaxSubLevel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("FilterSubLevel(%s)=[%d]", includer(), getMaxSubLevel()));
		if (hasSubFileFilter()) {
			wSB.append(FILTERS_SEPARATOR).append(getSubFileFilter().toString());
		}
		return wSB.toString();
	}

}
