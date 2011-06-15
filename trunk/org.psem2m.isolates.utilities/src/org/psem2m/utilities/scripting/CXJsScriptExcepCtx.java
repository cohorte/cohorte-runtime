package org.psem2m.utilities.scripting;

import org.psem2m.utilities.scripting.CXJsSource.CXJsSourceLocalization;

public class CXJsScriptExcepCtx extends CXJsObjectBase {

	private final CXJsSourceLocalization pWhere;
	private final CXJsSourceMain pSrcMain;
	private final CXJsException pExcep;

	public CXJsScriptExcepCtx(CXJsException aExcep, CXJsSourceMain aSrcMain,
			CXJsSourceLocalization aWhere) {
		pWhere = aWhere;
		pSrcMain = aSrcMain;
		pExcep = aExcep;
	}

	public CXJsException geExcep() {
		return pExcep;
	}

	public int getWhereLineNum() {
		return pWhere == null ? -1 : pWhere.getSourceLineNum();
	}

	public CXJsSource getWhereSrc() {
		return pWhere == null ? null : pWhere.getSrc();
	}

	public String getWhereSourceName() {
		return pWhere == null ? "Unknown" : pWhere.getSourceName();
	}

	public String getLanguage() {
		return pSrcMain == null ? "Unknown" : pSrcMain.getLanguage();
	}

	public String getMainSourceName() {
		return pSrcMain == null ? "Unknown" : pSrcMain.getSourceName();
	}

	public CXJsSourceMain getMainSource() {
		return pSrcMain;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddLine(aSB, "Error running script - Name[" + getMainSourceName()
				+ "] - Language[" + getLanguage() + "]");
		StringBuilder wTmp = new StringBuilder(1024);
		String wMsg = pExcep.getMessage();
		descrAddLine(wTmp, "Message", wMsg);
		descrAddLine(wTmp, "Action", pExcep.getAction());
		if (getWhereSrc() != null) {
			descrAddLine(wTmp, "Error occured line[" + getWhereLineNum()
					+ "] in " + getWhereSourceName() + " module");
			descrAddLine(wTmp, "Code :");
			descrAddIndent(wTmp,
					getWhereSrc().getText(getWhereLineNum(), 4, "--> "));
		}
		if (pExcep.getCause() != null) {
			Throwable wCause = pExcep.getCause();
			while (wCause != null && wCause != pExcep) {
				if (wCause.getMessage() != null
						&& wCause.getMessage().indexOf(wMsg) == -1) {
					// FDB - FIche 65417
					descrAddLine(wTmp, "Cause", wCause.getMessage());
				}
				wCause = wCause.getCause();
			}
		}
		descrAddIndent(aSB, wTmp);
		return aSB;
	}
}
