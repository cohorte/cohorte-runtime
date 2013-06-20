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
 * Renvoie un comparateur pour les Strings
 * 
 * @author ogattaz
 * 
 */
class CStringListComparator extends CXAbstractListComparator<String> {

	// True --> 2 Strings qui ont la meme valeur ne sont pas considerees comme
	// egales
	// ---> Permet d'ajouter des doublons (equalsObjects compare les 'pointeurs'
	// sur les strings)
	// False --> 2 Strings qui ont la meme valeur sont considerees comme egales

	private boolean pAcceptDoublons = true;
	private final boolean pCaseSensitive;

	/**
	 * @param aAcceptDoublons
	 * @param aCaseSensitive
	 */
	public CStringListComparator(boolean aAcceptDoublons, boolean aCaseSensitive) {
		this(true, aAcceptDoublons, aCaseSensitive);
	}

	/**
	 * @param aSortAsc
	 * @param aAcceptDoublons
	 * @param aCaseSensitive
	 */
	public CStringListComparator(boolean aSortAsc, boolean aAcceptDoublons, boolean aCaseSensitive) {
		super(aSortAsc);
		pAcceptDoublons = aAcceptDoublons;
		pCaseSensitive = aCaseSensitive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adonix.adminsrv.utils.CXAbstractListComparator#compareObjects(java
	 * .lang.Object, java.lang.Object)
	 */
	@Override
	protected int compareObjects(Object a, Object b) {
		if (pCaseSensitive) {
			return ((String) a).compareTo((String) b);
		} else {
			return ((String) a).toLowerCase().compareTo(((String) b).toLowerCase());
		}
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
		if (pAcceptDoublons) {
			return a == b;
		} else if (pCaseSensitive) {
			return ((String) a).equals(b);
		} else {
			return ((String) a).equalsIgnoreCase((String) b);
		}
	}
}

/**
 * GESTION D'UNE LISTE DE STRINGS
 * 
 * @author ogattaz
 */
public class CXSortListStrings extends CXSortList<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3257002159643242803L;

	/**
	 * Parametres du comparateur par defaut
	 * 
	 * @param aAcceptDoublons
	 *            =True --> 2 Strings qui ont la meme valeur pourront etre
	 *            ajoutees dans la liste
	 * @param aCaseSensitive
	 *            =True --> on utilise String.compareTo() sinon on compare en
	 *            toLowerCase()
	 */
	public CXSortListStrings(boolean aAcceptDoublons, boolean aCaseSensitive) {
		super(new CStringListComparator(aAcceptDoublons, aCaseSensitive));
	}

	/**
	 * @param aComp
	 */
	public CXSortListStrings(CXAbstractListComparator<String> aComp) {
		super(aComp);
	}

	/**
	 * @param aObject
	 * @return
	 */
	protected boolean checkObject(Object aObject) {
		return aObject instanceof String;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXSortList#getDefaultComparator()
	 */
	@Override
	protected CXAbstractListComparator<String> getDefaultComparator() {
		return new CStringListComparator(false, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXSortList#getDefaultComparator(boolean)
	 */
	@Override
	protected CXAbstractListComparator<String> getDefaultComparator(boolean aSortAsc) {
		return new CStringListComparator(false, aSortAsc);
	}
}
