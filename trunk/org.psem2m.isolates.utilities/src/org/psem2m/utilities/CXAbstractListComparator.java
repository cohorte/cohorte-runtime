package org.psem2m.utilities;
//
import java.util.Comparator;
//
/**
* Classe de base des comparateurs utilis�s dans la classe CAdminListSorted
* 
* M�thodes � r�impl�menter
* 	abstract protected int compareObjects(Object a, Object b);
* 	protected boolean equalsObjects(Object a, Object b); -->  a==b par d�faut
*/
public abstract class CXAbstractListComparator<E> implements Comparator<E>
{
	// Trie ascendant par d�faut
	private boolean pAsc = true;
	
	// CONSTRUCTEURS
	
	public CXAbstractListComparator()
	{
//16w_104 - Fiche 44010 - Eclatement History
		this(true);
	}
	
	public CXAbstractListComparator(boolean aSortAsc)
	{
		super();
		pAsc = aSortAsc;
	}
	
	// METHODES DE L'INTERFACE COMPARE
	
	@Override
	public int compare(E a, E b)
	{
		int wRes = 0;
		if (!equalsObjects(a, b))
		{
			wRes = compareObjects(a, b);
			// !!!! Le TreeSet consid�re les �l�ments �gaux comme identique et ne les ajoute pas (Ordre total)
			// --> on force la diff�rence 
			// --> Ca ralentit l'insertion mais ca n'a d'impact que sur l'ordre de classement des �l�ments �gaux
			if (wRes == 0)
				wRes = 1;
			// On inverse le tri si ordre descendant
			if (isDesc())
				wRes = wRes * -1;
		} // Else renvoie 0 (b �crase a) car un m�me objet ne peut �tre ajout� 2 fois dans la liste
		return wRes;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj != null && obj instanceof Comparator<?>;
	}
	
	// METHODES ABSTRAITES ET A REIMPLEMENTER
	
	// A r�impl�menter pour le crit�re de tri
	// Idem m�thode compare
	abstract protected int compareObjects(Object a, Object b);
	
	// A r�impl�menter pour le crit�re de tri
	// Renvoie true si les objets a et b sont identiques (au sens du pointeur)
	// Ex : Liste de fichiers tri�e par taille
	// --> a et b sont identique si les path sont identiques
	// -----> equalsObjects compare les path de a et b
	// --> Si a et b ont la m�me taille ils ne sont pas consid�r�s comme identiques
	// -----> equalsObjects renvoie false
	protected boolean equalsObjects(Object a, Object b)
	{
		return a==b;
	}
	
	// IMPLEMENTATION
	
	public boolean isAsc()
	{
		return pAsc;
	}
	
	public boolean isDesc()
	{
		return !pAsc;
	}
	
	public CXAbstractListComparator<E> reverseSort()
	{
		pAsc = !pAsc;
		return this;
	}
}