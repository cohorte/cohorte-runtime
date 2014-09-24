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

/**
 * abstract class that extends a CXSortedListSearch; permit to find a Element of
 * a list and sort list with Id of Element and not elements.
 * 
 * @author ogattaz
 * 
 * @param <E>
 */
public abstract class CXSortedListSearchId<E extends IXIdentifiable<?>> extends
		CXSortedListSearch<E> {

	/**
	 * Comparator
	 * 
	 * @author ogattaz
	 * 
	 */
	protected class CXIdElemListComparator extends CXAbstractListComparator<E> {

		/**
		 * @param aSortAsc
		 */
		public CXIdElemListComparator(boolean aSortAsc) {
			super(aSortAsc);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.psem2m.utilities.CXAbstractListComparator#compareObjects(java
		 * .lang.Object, java.lang.Object)
		 */
		@Override
		protected int compareObjects(Object a, Object b) {
			IXIdentifiable<?> wDefElem1 = ((IXIdentifiable<?>) a);
			IXIdentifiable<?> wDefElem2 = ((IXIdentifiable<?>) b);
			int i = wDefElem1.compareTo(wDefElem2);
			return i;
		}

	}

	private static final long serialVersionUID = 2588347263330351611L;

	/**
	 * default constructor
	 */
	public CXSortedListSearchId() {
		super();
		setComparator(new CXIdElemListComparator(CXSortList.ASCENDING));
	}

	/**
	 * return true if the first element of sorted list is the element that we're
	 * looking for else false
	 * 
	 * @param aId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected boolean isFistElem(Object[] aTreeSet, Object aId) {
		// on exclude le premier element pour chercher dans un tableau [1..n]
		if (compareElem(((E) aTreeSet[0]).getIdentifier(), aId) == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * do a Dichotomous research in Sorted list Override research method to
	 * search with id of objectand not object
	 * 
	 * @param begin
	 * @param end
	 *            : end of
	 * @param search
	 *            : id of an Element of a list
	 * @return a E type element that admit "search" id else null
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected E searchDich(Object[] aTable, int begin, int end, Object search) {
		int middle = (begin + end) / 2;
		Object[] aTreeSet = this.getTreeSet().toArray();
		E wMiddle = (E) aTreeSet[middle];
		// comparaison sur l'id
		if (aTreeSet.length < middle || aTreeSet[middle] == null) {
			return null;
		} else if (compareElem(wMiddle.getIdentifier(), search) == 0) {
			// find
			return wMiddle;
		} else if (begin >= end) { // not find searched element
			return null;
		} else if (compareElem(wMiddle.getIdentifier(), search) > 0) {
			// object recherche est plus bas dans la liste
			return searchDich(aTable, begin, middle - 1, search);
		} else if (compareElem(wMiddle.getIdentifier(), search) < 0) {
			// object recherche est plus haut dans la liste
			return searchDich(aTable, middle + 1, end, search);
		} else {
			return null;
		}
	}
}
