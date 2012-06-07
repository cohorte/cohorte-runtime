package org.psem2m.utilities;

/**
 * generique interface that define a methode getId()
 * 
 * @param <E>
 */
public interface IXIdentifiable<E extends Comparable<E>> extends
		Comparable<IXIdentifiable<?>> {

	public static String LIB_ID = IXIdentifier.LIB_ID;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	int compareTo(IXIdentifiable<?> aIdentifiable);

	/**
	 * @return
	 */
	public E getIdentifier();

}
