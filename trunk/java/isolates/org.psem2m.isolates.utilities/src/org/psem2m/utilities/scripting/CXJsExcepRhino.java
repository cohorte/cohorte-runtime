package org.psem2m.utilities.scripting;

import java.lang.reflect.Method;

import javax.script.ScriptException;

public class CXJsExcepRhino extends CXJsException {
	private final static String MSG_RHINO_START_1 = "javax.";
	private final static String MSG_RHINO_START_2 = "sun.";
	private final static String MSG_RHINO_START_3 = "error";
	private final static String MSG_RHINO_START_4 = "wrapped ";
	private final static String MSG_RHINO_START_NUM = "#";
	private final static String MSG_RHINO_STOP = "(<unknown source>";

	private static final long serialVersionUID = 1L;

	//
	/**
	 * Dans certains cas (eval s'un script compile) on n'a pas le Numero de
	 * ligne // via getLineNumber main il est indique dans le texte du message
	 * 
	 * @param aExcep
	 * @return
	 */
	private static int checkLineNum(ScriptException aExcep) {
		if (aExcep == null) {
			return -1;
		}
		// LineNumber renvoye par ScriptException
		if (aExcep.getLineNumber() >= 0) {
			return aExcep.getLineNumber();
		}
		// LineNumber non renvoye par ScriptException
		// -1- Check de la cause si c'est une instance de
		// sun.org.mozilla.javascript.internal.RhinoException/WrappedException
		try {
			// --> L'engine Rhino fournit le Numero de ligne qui, dans certains
			// cas,
			// n'est pas propage dans ScriptException
			// --> Le package sun.org.mozilla.* n'est pas visible dans le
			// Serveur bridge OSGI
			// --> On check la presence de la methode cause.lineNumber
			Throwable obj = aExcep.getCause();
			if (obj != null) {
				Method meth = obj.getClass().getMethod("lineNumber");
				if (meth != null && meth.getReturnType().equals(int.class)) {
					return ((Integer) meth.invoke(aExcep.getCause())).intValue();
				}
			}
		} catch (Exception e) {
		}
		// -2- Check du message qui peut contenir le Numero de ligne
		// Ex : sun.org.mozilla.javascript.internal.ecmaerror: referenceerror:
		// "ddde" n'est pas defini (<unknown source>#2)
		String wMsg = aExcep.getMessage().toLowerCase();
		int wPosStart = wMsg.lastIndexOf(MSG_RHINO_START_NUM);
		if (wPosStart == -1) {
			return aExcep.getLineNumber();
		}
		wPosStart += MSG_RHINO_START_NUM.length();
		int wPosStop = wMsg.indexOf(')', wPosStart);
		if (wPosStop == -1) {
			return aExcep.getLineNumber();
		}
		wMsg = wMsg.substring(wPosStart, wPosStop).trim();
		try {
			return Integer.parseInt(wMsg);
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * @param obj
	 * @param main
	 * @param tracer
	 * @param e
	 * @param aAction
	 * @throws CXJsException
	 */
	protected static void throwMyScriptExcep(Object obj, CXJsSourceMain main, IXjsTracer tracer,
			ScriptException e, String aAction) throws CXJsException {
		if (tracer != null) {
			tracer.trace(obj, aAction + "Error", e);
		}
		throw new CXJsExcepRhino(main, e, aAction);
	}

	/**
	 * 
	 */
	private String pCustomMessage = null;

	/**
	 * @param aSrcMain
	 * @param aExcep
	 * @param aAction
	 */
	public CXJsExcepRhino(CXJsSourceMain aSrcMain, ScriptException aExcep, String aAction) {
		super(aSrcMain, aExcep, aAction, checkLineNum(aExcep));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.CXJsException#getMessage()
	 */
	@Override
	public String getMessage() {
		if (pCustomMessage != null) {
			return pCustomMessage;
		}
		pCustomMessage = super.getMessage();
		String wMsgLC = pCustomMessage.toLowerCase();
		int wPosStart = -1;
		String[] searchStrs = { "exception:", "ecmaerror:" };
		int wTmpStart = 0;
		while (wTmpStart != -1) {
			int wTmpStop = -1;
			String searchStr = null;
			for (int i = 0; i < searchStrs.length && wTmpStop == -1; i++) {
				searchStr = searchStrs[i];
				wTmpStop = wMsgLC.indexOf(searchStr, wTmpStart);
			}
			boolean wFound = searchStr != null && wTmpStop != -1;
			if (wFound) {
				String wTmp = wMsgLC.substring(wTmpStart, wTmpStop);
				wFound = wTmp.indexOf(MSG_RHINO_START_1) != -1
						|| wTmp.indexOf(MSG_RHINO_START_2) != -1
						|| wTmp.indexOf(MSG_RHINO_START_3) != -1
						|| wTmp.indexOf(MSG_RHINO_START_4) != -1;
			}
			if (wFound) {
				wTmpStart = wPosStart = wTmpStop + searchStr.length();
			} else {
				wTmpStart = -1;
			}
		}

		if (wPosStart == -1) {
			wPosStart = 0;
		}
		int wPosStop = wMsgLC.indexOf(MSG_RHINO_STOP);
		if (wPosStop == -1) {
			wPosStop = pCustomMessage.length();
		}
		if (wPosStop != pCustomMessage.length() || wPosStart != 0) {
			pCustomMessage = pCustomMessage.substring(wPosStart, wPosStop).trim();
			if (pCustomMessage.startsWith("[") && pCustomMessage.endsWith("]")) {
				pCustomMessage = pCustomMessage.substring(1, pCustomMessage.length() - 1).trim();
			}
		}
		pCustomMessage = pCustomMessage == null || pCustomMessage.trim().length() < 1 ? super
				.getMessage() : pCustomMessage;
		if (pCustomMessage == null || "object error".equals(pCustomMessage.toLowerCase())) {
			// Type Error javascript - On essaie de determiner la valeur
			try {
				// --> ScriptException ne fournit pas de methode directe pour
				// determiner le contenu de l'erreur
				// --> Le package sun.org.mozilla.* n'est pas visible dans le
				// Serveur bridge OSGI
				// --> On check la presence de la methode cause.value
				Throwable obj = super.getCause() == null ? null : super.getCause().getCause();
				// Obj est une
				// sun.org.mozilla.javascript.internal.RhinoException qui
				// permettrait de recuperer l'objet lie a l'exception
				if (obj != null) {
					Method meth = obj.getClass().getMethod("getValue");
					if (meth != null && meth.getReturnType().equals(Object.class)) {
						pCustomMessage = meth.invoke(obj).toString();
					}
				}
			} catch (Exception e) {
				// pas réussi récupérer la cause...
			}
		}
		return pCustomMessage;
	}
}
