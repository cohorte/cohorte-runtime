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
import java.util.HashSet;
import java.util.Iterator;

import org.psem2m.utilities.CXListUtils;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CXFileFilterName extends CXFileFilter implements FileFilter {

	private final HashSet<String> pListRegExp = new HashSet<String>();

	/**
	 * @param aListRegExp
	 */
	public CXFileFilterName(String aListRegExp) {
		this(aListRegExp, null, INCLUDE);
	}

	/**
	 * @param aListRegExp
	 * @param aSubFileFilter
	 * @param aInclude
	 */
	public CXFileFilterName(String aListRegExp, FileFilter aSubFileFilter, boolean aInclude) {
		super(aSubFileFilter, aInclude);
		if (aListRegExp != null) {
			CXListUtils.loadStrCollection(pListRegExp, aListRegExp, FILTERS_SEPARATOR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return true;
		}

		boolean wRes = !include();
		String wName = new CXFile(pathname).getNameWithoutExtension();

		Iterator<String> wRegExps = pListRegExp.iterator();
		String wRegExp;
		while (wRegExps.hasNext()) {
			wRegExp = wRegExps.next();
			// si subpath contenu dans AbsolutePath :

			if (wName.matches(wRegExp)) {
				wRes = include();
				break;
			}
		}
		if (wRes && hasSubFileFilter()) {
			wRes = getSubFileFilter().accept(pathname);
		}

		return wRes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String[] wStrs = pListRegExp.toArray(new String[0]);
		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("FilterName(%s)=[%s]", includer(),
				CXStringUtils.stringTableToString(wStrs)));
		if (hasSubFileFilter()) {
			wSB.append(FILTERS_SEPARATOR).append(getSubFileFilter().toString());
		}
		return wSB.toString();
	}

}
