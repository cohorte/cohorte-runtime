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

import java.io.FileFilter;

/**
 * @author ogattaz
 * 
 */
public class CXFileFilterSvn extends CXFileFilterSubPath implements FileFilter {

	private final static String SVN_DIR_NAME = ".svn";

	/**
	 * @param aSubFileFilter
	 * @param aInclude
	 */
	public CXFileFilterSvn(FileFilter aSubFileFilter, boolean aInclude) {
		super(SVN_DIR_NAME, aSubFileFilter, aInclude);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("CXFileFilterSvn(%s)", includer()));
		if (hasSubFileFilter()) {
			wSB.append(FILTERS_SEPARATOR).append(getSubFileFilter().toString());
		}
		return wSB.toString();
	}
}
