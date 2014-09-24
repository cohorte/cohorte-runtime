package org.psem2m.utilities.scripting;

import org.psem2m.utilities.scripting.CXJsSource.CXJsSourceLocalization;

/**
 * @author ogattaz
 * 
 */
public class CXJsScriptExcepCtx extends CXJsObjectBase {

	private final CXJsException pExcep;
	private final CXJsSourceMain pSrcMain;
	private final CXJsSourceLocalization pWhere;

	/**
	 * @param aExcep
	 * @param aSrcMain
	 * @param aWhere
	 */
	public CXJsScriptExcepCtx(CXJsException aExcep, CXJsSourceMain aSrcMain,
			CXJsSourceLocalization aWhere) {
		pWhere = aWhere;
		pSrcMain = aSrcMain;
		pExcep = aExcep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsObjectBase#addDescriptionInBuffer(
	 * java.lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddLine(aSB, "Error running script - Name[" + getMainSourceName() + "] - Language["
				+ getLanguage() + "]");
		StringBuilder wTmp = new StringBuilder(1024);
		String wMsg = pExcep.getMessage();
		descrAddLine(wTmp, "Message", wMsg);
		descrAddLine(wTmp, "Action", pExcep.getAction());
		if (getWhereSrc() != null) {
			descrAddLine(wTmp, "Error occured line[" + getWhereLineNum() + "] in "
					+ getWhereSourceName() + " module");
			descrAddLine(wTmp, "Code :");
			descrAddIndent(wTmp, getWhereSrc().getText(getWhereLineNum(), 4, "--> "));
		}
		if (pExcep.getCause() != null) {
			Throwable wCause = pExcep.getCause();
			while (wCause != null && wCause != pExcep) {
				if (wCause.getMessage() != null && wCause.getMessage().indexOf(wMsg) == -1) {
					// FDB - FIche 65417
					descrAddLine(wTmp, "Cause", wCause.getMessage());
				}
				wCause = wCause.getCause();
			}
		}
		descrAddIndent(aSB, wTmp);
		return aSB;
	}

	/**
	 * @return
	 */
	public CXJsException geExcep() {
		return pExcep;
	}

	/**
	 * @return
	 */
	public String getLanguage() {
		return pSrcMain == null ? "Unknown" : pSrcMain.getLanguage();
	}

	/**
	 * @return
	 */
	public CXJsSourceMain getMainSource() {
		return pSrcMain;
	}

	/**
	 * @return
	 */
	public String getMainSourceName() {
		return pSrcMain == null ? "Unknown" : pSrcMain.getSourceName();
	}

	/**
	 * @return
	 */
	public int getWhereLineNum() {
		return pWhere == null ? -1 : pWhere.getSourceLineNum();
	}

	/**
	 * @return
	 */
	public String getWhereSourceName() {
		return pWhere == null ? "Unknown" : pWhere.getSourceName();
	}

	/**
	 * @return
	 */
	public CXJsSource getWhereSrc() {
		return pWhere == null ? null : pWhere.getSrc();
	}
}
