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
import java.util.StringTokenizer;

import org.psem2m.utilities.CXListUtils;
import org.psem2m.utilities.CXStringUtils;

/**
 * 16j_102 - modification du comportement du filtre "CXFileFilterExtension" pour
 * qu'il traite les extensions multiples
 * 
 * @author ogattaz
 * 
 */
class CExtension implements Comparable<CExtension> {
	private final String pExt;
	private final boolean pMultiple;

	CExtension(String aExtension) {
		pExt = aExtension;
		pMultiple = CXStringUtils.countChar(pExt, CXFile.sepExtensionChar) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CExtension aExtension) {
		if (pExt == null) {
			return 0;
		} else {
			return pExt.compareTo(aExtension.getExt());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CExtension) {
			return (pExt != null && pExt.equals(((CExtension) obj).getExt()));
		} else {
			return false;
		}
	}

	String getExt() {
		return pExt;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	boolean isMultiple() {
		return pMultiple;
	}

	@Override
	public String toString() {
		return getExt();
	}
}

/**
 * 16j_102 - modification du comportement du filtre "CXFileFilterExtension" pour
 * qu'il traite les extensions multiples
 * 
 * @author ogattaz
 * 
 */
class CExtensions extends HashSet<CExtension> {

	private static final long serialVersionUID = 3085394256462223179L;

	private boolean pHasMultiple = false;

	/**
	 * @param aExtensions
	 * @param aSep
	 */
	CExtensions(String aExtensions, String aSep) {
		if (aExtensions != null && aSep != null) {
			load(aExtensions, aSep);
		}
	}

	boolean contains(String aExt) {
		// je necomprend pas pourquoi cela ne fonctionne pas ...
		// return contains(new CExtension(aExt));

		if (aExt == null || aExt.length() == 0) {
			return false;
		}
		Iterator<CExtension> wExtensions = iterator();
		while (wExtensions.hasNext()) {
			if (aExt.equals(wExtensions.next().getExt())) {
				return true;
			}
		}
		return false;
	}

	boolean hasMultipleExtension() {
		return pHasMultiple;
	}

	/**
	 * @param aExtensions
	 * @param aSep
	 */
	void load(String aExtensions, String aSep) {
		StringTokenizer wSt = new StringTokenizer(aExtensions, aSep);
		CExtension wExtension;
		while (wSt.hasMoreTokens()) {
			wExtension = new CExtension(wSt.nextToken());
			add(wExtension);
			if (!pHasMultiple) {
				pHasMultiple = wExtension.isMultiple();
			}
		}
	}

	boolean match(String aFilename) {
		if (aFilename == null || aFilename.length() < 2
				|| aFilename.indexOf(CXFile.sepExtensionChar) == -1) {
			return false;
		}
		Iterator<CExtension> wExtensions = iterator();
		while (wExtensions.hasNext()) {
			if (aFilename.endsWith(wExtensions.next().getExt())) {
				return true;
			}
		}
		return false;
	}

}

/**
 * Filtre sur l'extension
 * 
 * @author ogattaz
 * 
 */
public class CXFileFilterExtension extends CXFileFilter implements FileFilter {

	private final CExtensions pListExt;

	/**
	 * aListExt : Liste des extension s�par�es par ";"
	 * 
	 * @param aListExt
	 */
	public CXFileFilterExtension(String aListExt) {
		this(aListExt, null, INCLUDE);
	}

	/**
	 * @param aListExt
	 *            Liste des extension s�par�es par ";"
	 * @param aSubFileFilter
	 * @param aInclude
	 */
	public CXFileFilterExtension(String aListExt, FileFilter aSubFileFilter, boolean aInclude) {
		super(aSubFileFilter, aInclude);
		pListExt = new CExtensions(aListExt, SEPARATOR);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File pathname) {
		boolean wRes = !include();
		if (pathname.isDirectory()) {
			wRes = true;
		} else {
			String wFileName = pathname.getName();
			// test de l'extension situ�e derri�re le dernier "sepExtension"
			if (pListExt.contains(CXStringUtils.strRightBack(wFileName, CXFile.sepExtension))) {
				wRes = include();
			} else if (nameWithMultipleExtension(wFileName) && pListExt.hasMultipleExtension()
					&& pListExt.match(wFileName)) {
				wRes = include();
			}
		}
		if (wRes && hasSubFileFilter()) {
			wRes = getSubFileFilter().accept(pathname);
		}
		return wRes;
	}

	/**
	 * 
	 * 16j_102 - modification du comportement du filtre "CXFileFilterExtension"
	 * pour qu'il traite les extensions multiples
	 * 
	 * @param aFileName
	 * @return
	 */
	private boolean nameWithMultipleExtension(String aFileName) {
		return CXStringUtils.countChar(aFileName, CXFile.sepExtensionChar) > 1;
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
		wSB.append(String.format("FilterExtension(%s)=[%s]", includer(),
				CXListUtils.collectionToString(pListExt, ";")));
		if (hasSubFileFilter()) {
			wSB.append(SEPARATOR).append(getSubFileFilter().toString());
		}
		return wSB.toString();
	}
}
