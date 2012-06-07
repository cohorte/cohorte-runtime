package org.psem2m.utilities;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Renvoie un comparateur pour les properties
 */
class CPropertiesComparator<E> extends CXAbstractListComparator<E> {
	boolean pSortByKey = true;

	/**
 * 
 */
	public CPropertiesComparator() {
		super();
	}

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
		if (pSortByKey)
			return (String) ((Entry<?, ?>) a).getKey();
		else
			return (String) ((Entry<?, ?>) a).getValue();
	}
}

public class CXSortListProperties extends CXSortList<Entry<Object, Object>> {
	/**
   * 
   */
	private static final long serialVersionUID = 9156273794753617100L;

	/**
   * 
   */
	public CXSortListProperties() {
		super();
	}

	/**
   */
	public CXSortListProperties(boolean aSortAsc) {
		super(aSortAsc);
	}

	/**
   */
	public CXSortListProperties(
			CPropertiesComparator<Entry<Object, Object>> aComp) {
		super(aComp);
	}

	/**
	 * 
	 * HashTable et properties aSortAsc --> Tri Tri sur les cles
	 */
	public CXSortListProperties(Properties aData, boolean aSortAsc) {
		this(aData, aSortAsc, SORTBYKEY);
	}

	/**
	 * 
	 * HashTable et properties aSortAsc --> Tri aSortByKey --> Type de tri (True
	 * --> Tri par Key - false --> tri par valeurs)
	 */
	public CXSortListProperties(Properties aData, boolean aSortAsc,
			boolean aSortByKey) {
		super();
		init(aData.entrySet().iterator(),
				new CPropertiesComparator<Entry<Object, Object>>(aSortAsc,
						aSortByKey));
	}

	/**
	 * @param aIt
	 * @param aComp
	 */
	private void init(Iterator<Entry<Object, Object>> aIt,
			CPropertiesComparator<Entry<Object, Object>> aComp) {
		setComparator(aComp);
		if (aIt != null) {
			while (aIt.hasNext())
				add(aIt.next());
		}
	}

}