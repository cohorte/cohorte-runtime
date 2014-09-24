package org.psem2m.utilities.scripting;

/**
 * @author ogattaz
 * 
 */
public class CXJsExcepLoad extends CXJsException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param aSrcMain
	 * @param aMessage
	 */
	public CXJsExcepLoad(CXJsSourceMain aSrcMain, String aMessage) {
		this(aSrcMain, (Exception) null, aMessage);
	}

	/**
	 * @param aSrcMain
	 * @param aMessage
	 * @param aAction
	 */
	public CXJsExcepLoad(CXJsSourceMain aSrcMain, String aMessage, String aAction) {
		super(aSrcMain, aMessage, aAction);
	}

	/**
	 * @param aSrcMain
	 * @param e
	 * @param aMessage
	 */
	public CXJsExcepLoad(CXJsSourceMain aSrcMain, Throwable e, String aMessage) {
		super(aSrcMain, aMessage, e, "loadingSource");
	}
}
