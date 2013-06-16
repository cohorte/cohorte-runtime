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
 * Filtre sur un / des sous dossiers
 * 
 * <li>16w_104 - Fiche 50792 - Admin - Erreur de detection du fonctionnement de
 * tomcat <li>16w_109 - enrichissement de la log
 * 
 * @author ogattaz
 * 
 */
public class CXFileFilterSubPath extends CXFileFilter implements FileFilter {

	private final HashSet<String> pListSubPath = new HashSet<String>();

	/**
	 * @param aListSubPath
	 *            Liste des sous dossiers separees par ";"
	 */
	public CXFileFilterSubPath(String aListSubPath) {
		this(aListSubPath, null, INCLUDE);
	}

	/**
	 * @param aListSubPath
	 *            Liste des sous dossiers separees par ";"
	 * @param aSubFileFilter
	 *            sous filtre s'il y en a un
	 * @param aInclude
	 *            inclusion ou exclusion si un des sous dossier present dans le
	 *            path
	 */
	public CXFileFilterSubPath(String aListSubPath, FileFilter aSubFileFilter, boolean aInclude) {
		super(aSubFileFilter, aInclude);
		if (aListSubPath != null) {
			CXListUtils.loadStrCollection(pListSubPath, aListSubPath, SEPARATOR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File pathname) {
		// si filter => hypothese false donc "not include"
		// si excluder => hypothese true => donc "not include"
		boolean wRes = !include();
		String wAbsolutePath = pathname.getAbsolutePath();
		Iterator<String> wSubPaths = pListSubPath.iterator();
		String wSubPath;
		while (wSubPaths.hasNext()) {
			wSubPath = wSubPaths.next();
			// si subpath contenu dans AbsolutePath :
			// - si filter => true => donc "include"
			// - si excluder => false => donc "include"
			if (wAbsolutePath.contains(wSubPath)) {
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
	// 16w_109 - enrichissement de la log
	@Override
	public String toString() {
		String[] wStrs = pListSubPath.toArray(new String[0]);
		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("FilterSubPath(%s)=[%s]", includer(),
				CXStringUtils.stringTableToString(wStrs)));
		if (hasSubFileFilter()) {
			wSB.append(SEPARATOR).append(getSubFileFilter().toString());
		}
		return wSB.toString();
	}

}