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

public class CXFileFilterSubLevel extends CXFileFilter implements FileFilter {

	private final int pMaxSubLevel;

	public CXFileFilterSubLevel(int aMaxSubLevel) {
		this(aMaxSubLevel, null, INCLUDE);
	}

	public CXFileFilterSubLevel(int aMaxSubLevel, FileFilter subFileFilter, boolean include) {
		super(subFileFilter, include);
		pMaxSubLevel = aMaxSubLevel;
	}

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

	protected int getMaxSubLevel() {
		return pMaxSubLevel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// 16w_109 - enrichissement de la log
	@Override
	public String toString() {
		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("FilterSubLevel(%s)=[%d]", includer(), getMaxSubLevel()));
		if (hasSubFileFilter()) {
			wSB.append(SEPARATOR).append(getSubFileFilter().toString());
		}
		return wSB.toString();
	}

}
