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
 * 
 * 16j_103
 * 
 * @author ogattaz
 * 
 */
public class CXFileFilter {

	protected final static boolean INCLUDE = true;

	final static String SEPARATOR = ";";

	/**
	 * @param aListExt
	 * @return
	 */
	public static FileFilter getExcluderExtension(String aListExt) {
		return getExcluderExtension(aListExt, null);
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListExt
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderExtension(String aListExt, FileFilter aSubFileFilter) {
		return new CXFileFilterExtension(aListExt, aSubFileFilter, !CXFileFilter.INCLUDE);
	}

	/**
	 * @param aListRegExp
	 * @return
	 */
	public static FileFilter getExcluderName(String aListRegExp) {
		return getExcluderName(aListRegExp, null);
	}

	/**
	 * @param aListRegExp
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderName(String aListRegExp, FileFilter aSubFileFilter) {
		return new CXFileFilterName(aListRegExp, aSubFileFilter, !CXFileFilter.INCLUDE);
	}

	/**
	 * @param aListSubPath
	 * @return
	 */
	public static FileFilter getExcluderSubPath(String aListSubPath) {
		return getExcluderSubPath(aListSubPath, null);
	}

	/**
	 * @param aListSubPath
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderSubPath(String aListSubPath, FileFilter aSubFileFilter) {
		return new CXFileFilterSubPath(aListSubPath, aSubFileFilter, !CXFileFilter.INCLUDE);
	}

	/**
	 * @return
	 */
	public static FileFilter getExcluderSvn() {
		return getExcluderSvn(null);
	}

	/**
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderSvn(FileFilter aSubFileFilter) {
		return new CXFileFilterSvn(aSubFileFilter, !CXFileFilter.INCLUDE);
	}

	/**
	 * @param aListExt
	 * @return
	 */
	public static FileFilter getFilterExtension(String aListExt) {
		return getFilterExtension(aListExt, null);
	}

	/**
	 * @param aListExt
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterExtension(String aListExt, FileFilter aSubFileFilter) {
		return new CXFileFilterExtension(aListExt, aSubFileFilter, CXFileFilter.INCLUDE);
	}

	/**
	 * @param aListRegExp
	 * @return
	 */
	public static FileFilter getFilterName(String aListRegExp) {
		return getFilterName(aListRegExp, null);
	}

	/**
	 * @param aListRegExp
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterName(String aListRegExp, FileFilter aSubFileFilter) {
		return new CXFileFilterName(aListRegExp, aSubFileFilter, CXFileFilter.INCLUDE);
	}

	/**
	 * @param aMaxLevel
	 * @return
	 */
	public static FileFilter getFilterSubLevel(int aMaxLevel) {
		return getFilterSubLevel(aMaxLevel, null);
	}

	/**
	 * @param aMaxLevel
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterSubLevel(int aMaxLevel, FileFilter aSubFileFilter) {
		return new CXFileFilterSubLevel(aMaxLevel, aSubFileFilter, CXFileFilter.INCLUDE);
	}

	/**
	 * @param aListSubPath
	 * @return
	 */
	public static FileFilter getFilterSubPath(String aListSubPath) {
		return getFilterSubPath(aListSubPath, null);
	}

	/**
	 * @param aListSubPath
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterSubPath(String aListSubPath, FileFilter aSubFileFilter) {
		return new CXFileFilterSubPath(aListSubPath, aSubFileFilter, CXFileFilter.INCLUDE);
	}

	/**
	 * @return
	 */
	public static FileFilter getFilterSvn() {
		return getFilterSvn(null);
	}

	/**
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterSvn(FileFilter aSubFileFilter) {
		return new CXFileFilterSvn(aSubFileFilter, CXFileFilter.INCLUDE);
	}

	private final boolean pInclude;
	private final FileFilter pSubFileFilter;

	/**
	 * @param aSubFileFilter
	 * @param aInclude
	 */
	CXFileFilter(FileFilter aSubFileFilter, boolean aInclude) {
		pSubFileFilter = aSubFileFilter;
		pInclude = aInclude;
	}

	protected FileFilter getSubFileFilter() {
		return pSubFileFilter;
	}

	protected boolean hasSubFileFilter() {
		return pSubFileFilter != null;
	}

	protected boolean include() {
		return pInclude;
	}

	protected String includer() {
		return include() ? "includer" : "excluder";
	}
}
