package org.psem2m.utilities;



/**
 * class abstraite permettant de lister un ensemble d'object de declarant les methodes pour retrouver un object selon un id
 * first element correpond to the id type and second element correspond to the Element type
 */
public abstract class CXSortedListSearch<E> extends CXSortList<E>{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * default  constructor  
	 */
	public CXSortedListSearch(){
		super();
	}
	
	public CXSortedListSearch(CXAbstractListComparator<E> aComp){
		super();
		setComparator(aComp);
	}
	

	/**
	 * search a element follow dichotomic algorythme
	 * return object that correspond to element Id
	 * @param an Element of this sorted list :
	 * @return
	 */
	public E findDich(Object aId){
		E wResult = null;
		Object[] aTreeSet = this.getTreeSet().toArray();
		
		// verifie si le premier correspond a l'element que l'on cherche 
		int begin = 0; 
		int end 	= this.size()-1;
		wResult= searchDich(aTreeSet,begin,end,aId);

		return wResult;
	}

	/**
	 * return true if the first element of sorted list is the element that we're looking for
	 * else false
	 * @param aId
	 * @return
	 */
	protected boolean isFistElem(Object[] aTreeSet,Object aId){
		// on exclude le premier element pour chercher dans un tableau [1..n]
		if(compareElem(aTreeSet[0],aId) == 0 ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * do a Dichotomous research in Sorted list
	 * @param begin
	 * @param end
	 * @param
	 */	
	@SuppressWarnings("unchecked")
	protected E searchDich(Object[] aTable,int begin,int end,Object search){
		int middle = 0;

		middle = (begin+end)/2;
		E wMiddle = (E)aTable[middle];
		
		if(  aTable[middle] == null ){
			return null;
		}else if( compareElem(wMiddle,search)==0 ){
			// find
			return wMiddle;
		}else if(  begin >= end ){ // not find searched element 
			return null;
		}else if( compareElem(wMiddle,search) > 0 ){
			// object recherche est plus bas dans la liste
			return searchDich(aTable,begin,middle-1,search);
		}else  if( compareElem(wMiddle,search) < 0 ){
			// object recherche est plus haut dans la liste
			return searchDich(aTable,middle+1,end,search);
		}else{
			return null;
		}
	}
	
	/**
	 * return -2 if error occured else -1,0,1
	 * @param Elem : correspond to a Elem of the list
	 * @param Elem2: correspond to a Elem of the list
	 * 
	 * @return
	 */
	protected abstract int compareElem(Object Elem, Object Elem2);

}
