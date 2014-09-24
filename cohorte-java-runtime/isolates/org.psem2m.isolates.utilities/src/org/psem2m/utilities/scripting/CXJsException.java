package org.psem2m.utilities.scripting;

import org.psem2m.utilities.scripting.CXJsSource.CXJsSourceLocalization;

/**
 * @author ogattaz
 * 
 */
public class CXJsException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String pAction;

	// initialise a -1
	private int pLineNumber = -1;

	private final CXJsSourceMain pMainSrc;

	/**
	 * Exception non generee par le ScriptEngine
	 * 
	 * @param aMainSrc
	 * @param aMessage
	 * @param aAction
	 */
	public CXJsException(CXJsSourceMain aMainSrc, String aMessage, String aAction) {
		this(aMainSrc, aMessage, null, aAction);
	}

	/**
	 * Exception non generee par le ScriptEngine
	 * 
	 * @param aMainSrc
	 * @param aMessage
	 * @param aExcep
	 * @param aAction
	 */
	public CXJsException(CXJsSourceMain aMainSrc, String aMessage, Throwable aExcep, String aAction) {
		super(aMessage, aExcep);
		pMainSrc = aMainSrc;
		pAction = aAction;
	}

	/**
	 * @param aMainSrc
	 * @param aExcep
	 * @param aAction
	 * @param aLineNum
	 */
	public CXJsException(CXJsSourceMain aMainSrc, Throwable aExcep, String aAction, int aLineNum) {
		this(aMainSrc, aExcep.getMessage(), aExcep, aAction);
		pLineNumber = aLineNum;
	}

	/**
	 * @return
	 */
	public String getAction() {
		return pAction;
	}

	/**
	 * @return
	 */
	public CXJsScriptExcepCtx getExcepCtx() {
		CXJsSourceLocalization wWhere = pMainSrc == null ? null : pMainSrc
				.findSource(getLineNumber());
		return new CXJsScriptExcepCtx(this, pMainSrc, wWhere);
	}

	/**
	 * @return
	 */
	public int getLineNumber() {
		return pLineNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		String wMsg = super.getMessage();
		return wMsg == null ? "No error message" : wMsg;
	}

	/**
	 * Peut etre null si erreur hors evaluation de source
	 * 
	 * @return
	 */
	public CXJsSourceMain getModuleMain() {
		return pMainSrc;
	}
}
