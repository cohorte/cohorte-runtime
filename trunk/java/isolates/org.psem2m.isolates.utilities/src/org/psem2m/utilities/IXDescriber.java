package org.psem2m.utilities;

public interface IXDescriber {

	/**
	 * ajoute la description a un StringBuilder
	 * 
	 * @param aSB
	 * @return
	 */
	public Appendable addDescriptionInBuffer(Appendable aBuffer);

	/**
	 * retourne la description dans une chaine
	 * 
	 * @return
	 */
	public String toDescription();
}
