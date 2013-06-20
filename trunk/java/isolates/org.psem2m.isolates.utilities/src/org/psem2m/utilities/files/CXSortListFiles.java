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
import java.util.Collection;

import org.psem2m.utilities.CXAbstractListComparator;
import org.psem2m.utilities.CXSortList;

/**
 * @author ogattaz
 * 
 */
public class CXSortListFiles extends CXSortList<File> {

	private static final long serialVersionUID = 3978703982749364787L;

	/**
	 */
	public CXSortListFiles() {
		super();
	}

	/**
	 * @param aSortAsc
	 */
	public CXSortListFiles(boolean aSortAsc) {
		super(aSortAsc);
	}

	/**
	 * @param c
	 * @throws Exception
	 */
	public CXSortListFiles(Collection<File> c) throws Exception {
		super(c);
	}

	/**
	 * @param c
	 * @param aComp
	 * @throws Exception
	 */
	public CXSortListFiles(Collection<File> c, CXSortListFileAbstractComparator<File> aComp)
			throws Exception {
		super(c, aComp);
	}

	/**
	 * @param aComp
	 */
	public CXSortListFiles(CXSortListFileAbstractComparator<File> aComp) {
		super(aComp);
	}

	/**
	 * Constructeur par defaut
	 */
	@Override
	protected CXAbstractListComparator<File> getDefaultComparator() {
		return new CAdminFilePathComparator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXSortList#getDefaultComparator(boolean)
	 */
	@Override
	protected CXAbstractListComparator<File> getDefaultComparator(boolean aSortAsc) {
		return new CAdminFilePathComparator(aSortAsc);
	}

	/**
	 * 
	 */
	public void sortByDateAsc() {
		sort(new CAdminFileDateComparator(CXSortList.ASCENDING));
	}

	/**
	 * 
	 */
	public void sortByDateDesc() {
		sort(new CAdminFileDateComparator(false));
	}

	/**
	 * 
	 */
	public void sortByPathAsc() {
		sort(new CAdminFilePathComparator());
	}

	/**
	 * 
	 */
	public void sortByPathDesc() {
		sort(new CAdminFilePathComparator(false));
	}

	/**
	 * 
	 */
	public void sortBySizeAsc() {
		sort(new CAdminFileSizeComparator());
	}

	/**
	 * 
	 */
	public void sortBySizeDesc() {
		sort(new CAdminFileSizeComparator(false));
	}
}