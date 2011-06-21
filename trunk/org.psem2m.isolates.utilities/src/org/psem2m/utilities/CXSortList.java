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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 
 * GESTION D'UNE LISTE TRIEE AVEC POSSIBILITE DE MODIFIER LES CRITERES DE TRI
 * 
 * --> Fonctionne avec des comparateurs qui heritent de
 * CAdminAbstractListComparator --> Gere tri ascendant et descendant -
 * resverSort();
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 * @param <E>
 */
public class CXSortList<E> extends ArrayList<E> {

	public final static boolean ASCENDING = true;
	public final static boolean DESCENDING = false;

	private static final long serialVersionUID = 3256437014961468726L;

	public final static boolean SORTBYKEY = true;

	/** Comparator courant **/
	private CXAbstractListComparator<E> pComparator = null;

	/** Contient la liste triee des objets en phase avec ArrayList **/
	TreeSet<E> pTreeSet = null;

	/**
	 */
	public CXSortList() {
		super();
		setComparator(getDefaultComparator());
	}

	/**
	 */
	public CXSortList(final boolean aSortAsc) {
		super();
		setComparator(getDefaultComparator(aSortAsc));
	}

	/**
	 */
	public CXSortList(final Collection<E> aCol) throws Exception {
		super();
		init(aCol.iterator(), getDefaultComparator());
	}

	/**
	 */
	public CXSortList(final Collection<E> aCol,
			final CXAbstractListComparator<E> aComp) throws Exception {
		super();
		init(aCol.iterator(), aComp);
	}

	/**
	 */
	public CXSortList(final CXAbstractListComparator<E> aComp) {
		super();
		setComparator(aComp);
	}

	/**
	 */
	public CXSortList(final Enumeration<E> aEnum) throws Exception {
		super();
		init(aEnum, getDefaultComparator());
	}

	/**
	 */
	public CXSortList(final Enumeration<E> aEnum,
			final CXAbstractListComparator<E> aComp) throws Exception {
		super();
		init(aEnum, aComp);
	}

	/**
	 */
	public CXSortList(final Iterator<E> aIt) throws Exception {
		super();
		init(aIt, getDefaultComparator());
	}

	/**
		*/
	public CXSortList(final Iterator<E> aIt,
			final CXAbstractListComparator<E> aComp) throws Exception {
		super();
		init(aIt, aComp);
	}

	/**
	 * Interface collection
	 */
	@Override
	public boolean add(final E o) {
		if (getTreeSet().add(o)) {
			return super.add(o);
		} else {
			return false;
		}
	}

	/**
	 * Interface collection
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		if (c != null) {
			Iterator<? extends E> wIt = c.iterator();
			while (wIt.hasNext()) {
				this.add(wIt.next());
			}
		}
		return true;
	}

	/**
	 * Interface collection
	 */
	@Override
	public void clear() {
		getTreeSet().clear();
		super.clear();
	}

	/**
	 */
	public CXAbstractListComparator<E> getComparator() {
		return pComparator;
	}

	/**
	 * Comparateur par defaut
	 * <p>
	 * A reimplementer : MUST BE OVERWRITED
	 */
	protected CXAbstractListComparator<E> getDefaultComparator() {
		return null;
	}

	/**
	 * Comparateur par defaut
	 * <p>
	 * A reimplementer : MUST BE OVERWRITED
	 */
	protected CXAbstractListComparator<E> getDefaultComparator(
			final boolean aSortAsc) {
		return null;
	}

	/**
	 * Liste triee
	 */
	public TreeSet<E> getTreeSet() {
		return pTreeSet;
	}

	/**
	 * TODO - X - Voir si c'est correct de constituer la liste triee a partir de
	 * aEnum Cf constructeurs associes
	 * 
	 * @param aEnum
	 * @param aComp
	 */
	private void init(final Enumeration<E> aEnum,
			final CXAbstractListComparator<E> aComp) {
		setComparator(aComp);
		if (aEnum != null) {
			while (aEnum.hasMoreElements()) {
				add(aEnum.nextElement());
			}
		}
	}

	/**
	 * TODO - X - Voir si c'est correct de constituer la liste triee a partir de
	 * aIt Cf constructeurs associes
	 * 
	 * @param aIt
	 * @param aComp
	 */
	private void init(final Iterator<E> aIt,
			final CXAbstractListComparator<E> aComp) {
		setComparator(aComp);
		if (aIt != null) {
			while (aIt.hasNext()) {
				add(aIt.next());
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isAsc() {
		if (getComparator() != null) {
			return getComparator().isAsc();
		} else {
			return false;
		}
	}

	/**
	 * @return
	 */
	public boolean isDesc() {
		if (getComparator() != null) {
			return getComparator().isDesc();
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		if (getTreeSet() != null) {
			return getTreeSet().iterator();
		} else {
			return this.iterator();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o) {
		getTreeSet().remove(o);
		return super.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> aCollection) {
		getTreeSet().remove(aCollection);
		return super.removeAll(aCollection);
	}

	/**
	 */
	public void reverseSort() {
		if (getTreeSet() != null && getComparator() != null) {
			sort(getComparator().reverseSort());
		}
	}

	/**
	 * Renvoie true si aComp a ete pris en compte (liste recalculee)
	 * 
	 * @param aComp
	 * @return
	 */
	protected boolean setComparator(final CXAbstractListComparator<E> aComp) {
		boolean wRecalc = false;
		if (aComp == null) {
			wRecalc = pComparator != null;
		} else if (pComparator != null) {
			wRecalc = true; // On ne compare pas aComp et pComparator
							// volontairement pour forcer le recalcul -> voir
							// reverseSort
		} else {
			wRecalc = true;
		}

		wRecalc = wRecalc || pTreeSet == null;

		if (wRecalc) {
			pComparator = aComp;
			if (pComparator == null) {
				pTreeSet = new TreeSet<E>();
			} else {
				pTreeSet = new TreeSet<E>(pComparator);
			}

			Iterator<E> wIt = super.iterator();
			while (wIt.hasNext()) {
				pTreeSet.add(wIt.next());
			}
		}

		return wRecalc;
	}

	/**
	 * aComp comporte le type de tri Asc ou Desc On peut l'inverser par
	 * reverseSort
	 * 
	 * @param aComp
	 */
	public void sort(final CXAbstractListComparator<E> aComp) {
		setComparator(aComp);
	}

	/**
	 * @param aSep
	 * @return
	 */
	public String toString(final String aSep) {
		StringBuilder wRes = new StringBuilder(1024);
		Iterator<E> wIt = getTreeSet().iterator();
		while (wIt.hasNext()) {
			wRes.append(wIt.next()).append(aSep);
		}
		return wRes.toString();
	}
}
