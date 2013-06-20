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
package org.psem2m.utilities;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Renvoie un comparateur pour les properties
 */
class CPropertiesComparator<E> extends CXAbstractListComparator<E> {

	private boolean pSortByKey = true;

	/**
	 * @param aSortAsc
	 */
	public CPropertiesComparator(boolean aSortAsc) {
		super(aSortAsc);
	}

	/**
	 * @param aSortAsc
	 * @param aSortByKey
	 */
	public CPropertiesComparator(boolean aSortAsc, boolean aSortByKey) {
		super(aSortAsc);
		pSortByKey = aSortByKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.CXAbstractListComparator#compareObjects(java.lang
	 * .Object, java.lang.Object)
	 */
	@Override
	protected int compareObjects(Object a, Object b) {
		return getData(a).compareTo(getData(b));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adonix.adminsrv.utils.CXAbstractListComparator#equalsObjects(java
	 * .lang.Object, java.lang.Object)
	 */
	@Override
	protected boolean equalsObjects(Object a, Object b) {
		String wKeyA = (String) ((Entry<?, ?>) a).getKey();
		String wKeyB = (String) ((Entry<?, ?>) b).getKey();
		return wKeyA.equals(wKeyB);
	}

	/**
	 * @param a
	 * @return
	 */
	protected String getData(Object a) {
		if (pSortByKey) {
			return (String) ((Entry<?, ?>) a).getKey();
		} else {
			return (String) ((Entry<?, ?>) a).getValue();
		}
	}
}

/**
 * @author ogattaz
 * 
 */
public class CXSortListProperties extends CXSortList<Entry<Object, Object>> {

	private static final long serialVersionUID = 9156273794753617100L;

	/**
   * 
   */
	public CXSortListProperties() {
		this(CXSortList.ASCENDING);
	}

	/**
	 * @param aSortAsc
	 */
	public CXSortListProperties(boolean aSortAsc) {
		super(aSortAsc);
	}

	/**
	 * @param aComp
	 */
	public CXSortListProperties(CPropertiesComparator<Entry<Object, Object>> aComp) {
		super(aComp);
	}

	/**
	 * 
	 * @param aData
	 * @param aSortAsc
	 *            Tri sur les cles
	 */
	public CXSortListProperties(Properties aData, boolean aSortAsc) {
		this(aData, aSortAsc, SORTBYKEY);
	}

	/**
	 * 
	 * @param aData
	 * @param aSortAsc
	 *            -Tri
	 * @param aSortByKey
	 *            TrueType de tri (True --> Tri par Key - false --> tri par
	 *            valeurs)
	 */
	public CXSortListProperties(Properties aData, boolean aSortAsc, boolean aSortByKey) {
		super();
		init(aData.entrySet().iterator(), new CPropertiesComparator<Entry<Object, Object>>(
				aSortAsc, aSortByKey));
	}

	/**
	 * @param aIt
	 * @param aComp
	 */
	private void init(Iterator<Entry<Object, Object>> aIt,
			CPropertiesComparator<Entry<Object, Object>> aComp) {
		setComparator(aComp);
		if (aIt != null) {
			while (aIt.hasNext()) {
				add(aIt.next());
			}
		}
	}

}