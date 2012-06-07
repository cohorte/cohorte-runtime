package org.psem2m.utilities;

/**
 * Renvoie un comparateur pour les Strings
 */
class CStringListComparator extends CXAbstractListComparator<String> {

	// True --> 2 Strings qui ont la meme valeur ne sont pas considerees comme
	// egales
	// ---> Permet d'ajouter des doublons (equalsObjects compare les 'pointeurs'
	// sur les strings)
	// False --> 2 Strings qui ont la meme valeur sont considerees comme egales
	private boolean pAcceptDoublons = true;
	private final boolean pCaseSensitive;

	public CStringListComparator(boolean aAcceptDoublons, boolean aCaseSensitive) {
		super();
		pAcceptDoublons = aAcceptDoublons;
		pCaseSensitive = aCaseSensitive;
	}

	public CStringListComparator(boolean aSortAsc, boolean aAcceptDoublons,
			boolean aCaseSensitive) {
		super(aSortAsc);
		pAcceptDoublons = aAcceptDoublons;
		pCaseSensitive = aCaseSensitive;
	}

	// METHODES ABSTRAITES DE CAdminAbstractListComparator

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adonix.adminsrv.utils.CXAbstractListComparator#compareObjects(java
	 * .lang.Object, java.lang.Object)
	 */
	@Override
	protected int compareObjects(Object a, Object b) {
		if (pCaseSensitive)
			return ((String) a).compareTo((String) b);
		else
			return ((String) a).toLowerCase().compareTo(
					((String) b).toLowerCase());
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
		if (pAcceptDoublons)
			return a == b;
		else if (pCaseSensitive)
			return ((String) a).equals(b);
		else
			return ((String) a).equalsIgnoreCase((String) b);
	}
}

/**
 * GESTION D'UNE LISTE DE STRINGS
 */
public class CXSortListStrings extends CXSortList<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3257002159643242803L;

	// CONSTRUCTEURS
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

	public CXSortListStrings(CXAbstractListComparator<String> aComp) {
		super(aComp);
	}

	// A REIMPLEMENTER

	protected boolean checkObject(Object aObject) {
		return aObject instanceof String;
	}

	@Override
	protected CXAbstractListComparator<String> getDefaultComparator() {
		return new CStringListComparator(false, true);
	}

	@Override
	protected CXAbstractListComparator<String> getDefaultComparator(
			boolean aSortAsc) {
		return new CStringListComparator(false, aSortAsc);
	}
}
