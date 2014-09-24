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

import java.util.Comparator;

/**
 * Base class for the comparators
 * 
 * Methods to be overwrited:
 * <ul>
 * <li>abstract protected int compareObjects(Object a, Object b);
 * <li>protected boolean equalsObjects(Object a, Object b); --> a==b by default
 * </ul>
 * 
 */
public abstract class CXAbstractListComparator<E> implements Comparator<E> {

	// ascending sort by default
	private boolean pAsc;

	/**
	 * @param aSortAsc
	 */
	public CXAbstractListComparator(final boolean aSortAsc) {
		super();
		pAsc = aSortAsc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final E a, final E b) {
		int wRes = 0;
		if (!equalsObjects(a, b)) {
			wRes = compareObjects(a, b);
			// !!!! Le TreeSet considere les elements egaux comme identique et
			// ne les ajoute pas (Ordre total)
			// --> on force la difference
			// --> Ca ralentit l'insertion mais ca n'a d'impact que sur l'ordre
			// de classement des elements egaux
			if (wRes == 0) {
				wRes = 1;
			}
			// On inverse le tri si ordre descendant
			if (isDesc()) {
				wRes = wRes * -1;
			}
		} // Else renvoie 0 (b ecrase a) car un meme objet ne peut etre ajoute 2
			// fois dans la liste
		return wRes;
	}

	/**
	 * A reimplementer pour le critere de tri Idem methode compare
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	abstract protected int compareObjects(Object a, Object b);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj != null && obj instanceof Comparator<?>;
	}

	/**
	 * <pre>
	 * A reimplementer pour le critere de tri
	 *  Renvoie true si les objets a et b sont identiques (au sens du pointeur)
	 *  Ex : Liste de fichiers triee par taille
	 *  --&gt; a et b sont identique si les path sont identiques
	 *  -----&gt; equalsObjects compare les path de a et b
	 *  --&gt; Si a et b ont la meme taille ils ne sont pas consideres comme
	 *  identiques
	 *  -----&gt; equalsObjects renvoie false
	 * </pre>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean equalsObjects(final Object a, final Object b) {
		return a == b;
	}

	/*
	 * Sonar : Checks that classes that override equals() also override
	 * hashCode().
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @return
	 */
	public boolean isAsc() {
		return pAsc;
	}

	/**
	 * @return
	 */
	public boolean isDesc() {
		return !pAsc;
	}

	/**
	 * @return
	 */
	public CXAbstractListComparator<E> reverseSort() {
		pAsc = !pAsc;
		return this;
	}
}