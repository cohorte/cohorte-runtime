package org.psem2m.utilities;

/**
 * abstract class that extends a CXSortedListSearch; permit to find a Element of
 * a list and sort list with Id of Element and not elements.
 */
public abstract class CXSortedListSearchId<E extends IXIdentifiable<?>> extends
		CXSortedListSearch<E> {

	/**
	 * Compara
	 * 
	 * @author apisu
	 * 
	 */
	protected class CXIdElemListComparator extends CXAbstractListComparator<E> {

		@Override
		protected int compareObjects(Object a, Object b) {
			IXIdentifiable<?> wDefElem1 = ((IXIdentifiable<?>) a);
			IXIdentifiable<?> wDefElem2 = ((IXIdentifiable<?>) b);
			int i = wDefElem1.compareTo(wDefElem2);
			return i;
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * default constructor
	 */
	public CXSortedListSearchId() {
		super();
		setComparator(new CXIdElemListComparator());
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
