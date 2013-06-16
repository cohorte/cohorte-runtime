package org.psem2m.utilities.scripting;

/**
 * @author ogattaz
 * 
 */
public class CXJsExcepUnknownLanguage extends CXJsException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param aLanguage
	 */
	public CXJsExcepUnknownLanguage(String aLanguage) {
		super(null, "Scripting language [" + aLanguage + "] is not registered in the current JVM",
				"scriptManager");
	}
}
