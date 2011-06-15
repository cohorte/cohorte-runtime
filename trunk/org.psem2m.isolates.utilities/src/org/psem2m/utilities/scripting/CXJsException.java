package org.psem2m.utilities.scripting;

import org.psem2m.utilities.scripting.CXJsSource.CXJsSourceLocalization;

public class CXJsException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String pAction;

	// initialise a -1
	private int pLineNumber = -1;

	private final CXJsSourceMain pMainSrc;

	public CXJsException(CXJsSourceMain aMainSrc, Throwable aExcep,
			String aAction, int aLineNum) {
		this(aMainSrc, aExcep.getMessage(), aExcep, aAction);
		pLineNumber = aLineNum;
	}

	// Exceptio non generee par le ScriptEngine
	public CXJsException(CXJsSourceMain aMainSrc, String aMessage,
			String aAction) {
		this(aMainSrc, aMessage, null, aAction);
	}

	// Exceptio non generee par le ScriptEngine
	public CXJsException(CXJsSourceMain aMainSrc, String aMessage,
			Throwable aExcep, String aAction) {
		super(aMessage, aExcep);
		pMainSrc = aMainSrc;
		pAction = aAction;
	}

	@Override
	public String getMessage() {
		String wMsg = super.getMessage();
		return wMsg == null ? "No error message" : wMsg;
	}

	// Peut etre null si erreur hors evaluation de source
	public CXJsSourceMain getModuleMain() {
		return pMainSrc;
	}

	public String getAction() {
		return pAction;
	}

	public int getLineNumber() {
		return pLineNumber;
	}

	public CXJsScriptExcepCtx getExcepCtx() {
		CXJsSourceLocalization wWhere = pMainSrc == null ? null : pMainSrc
				.findSource(getLineNumber());
		return new CXJsScriptExcepCtx(this, pMainSrc, wWhere);
	}
}
