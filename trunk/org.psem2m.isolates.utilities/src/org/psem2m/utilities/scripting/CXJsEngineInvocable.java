package org.psem2m.utilities.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.psem2m.utilities.CXTimer;

public class CXJsEngineInvocable extends CXJsEngine {

	private boolean pEvaluated;
	private CXJsSourceMain pMainModule;

	// Constructor

	protected CXJsEngineInvocable(CXJsSourceMain aMainModule,
			ScriptEngine aEngine, CXJsScriptFactory afactory) {
		super(aEngine, afactory);
		pMainModule = aMainModule;
	}

	@Override
	public void destroy() {
		pMainModule = null;
	}

	public CXJsSourceMain getMainModule() {

		return pMainModule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adonix.scripting.CXJsEngine#eval(com.adonix.scripting.CXJsSourceMain)
	 */
	@Override
	public Object eval(CXJsSourceMain aMainModule) throws CXJsException {
		// 16w_111 - mise en evidence du pb de passage d'un contexte d'execution
		// a un script non compile
		// pour passer des attributs d'execution, utiliser la methode
		// "setAttrEngine"
		pEvaluated = true;
		return super.eval(aMainModule);
	}

	/**
	 * have the same effect as
	 * <code>getBindings(ScriptContext.ENGINE_SCOPE).put</code>.
	 * 
	 * @param aId
	 * @param aValue
	 */
	public void setAttrEngine(String aId, Object aValue) {
		getScriptEngine().put(aId, aValue);
	}

	// 16w_111 - mise en evidence du pb de passage d'un contexte d'execution a
	// un script non compile
	// suppression de la methode !
	//
	// public Object eval(CXJsSourceMain aMainModule, ScriptContext aCtx) throws
	// CXJsException
	// {
	// pEvaluated=true;
	// return super.eval(aMainModule, aCtx);
	// }

	// 16w_111 - mise en evidence du pb de passage d'un contexte d'execution a
	// un script non compile
	// suppression de la methode !
	//
	// public Object eval(CXJsSourceMain aMainModule, Bindings aBinding) throws
	// CXJsException
	// {
	// pEvaluated=true;
	// return super.eval(aMainModule, aBinding);
	// }

	public Object invokeFunction(String aFuntion, IXjsTracer tracer,
			Object... aArgs) throws CXJsException {
		String wAction = "invokeFunction(" + aFuntion + ")";
		checkEvaluated(wAction, tracer);
		Object wRes = null;
		if (isInvocable()) {
			boolean trace = tracer != null;
			CXTimer wT = trace ? new CXTimer("invokeFunction", true) : null;
			try {
				wRes = getInvocable().invokeFunction(aFuntion, aArgs);
			} catch (ScriptException e) {
				CXJsExcepRhino.throwMyScriptExcep(this, pMainModule, tracer, e,
						"invokeFunction(" + aFuntion + ")");
			} catch (Throwable e) {
				throwMyScriptExcep(pMainModule, tracer,
						"Error invoking function", e, "invokeFunction("
								+ aFuntion + ")");
			} finally {
				if (trace) {
					wT.stop();
					tracer.trace(wT.toDescription());
				}
			}
		} else
			throwMyScriptExcep(pMainModule, tracer,
					"JavaScript engine is not 'invocale' - Language["
							+ getLanguage() + "]", wAction);

		return wRes;
	}

	public Object invokeMethod(Object aJSObject, String aJSObjMethod,
			IXjsTracer tracer, Object... aArgs) throws CXJsException {
		String wAction = "invokeMethod(" + aJSObjMethod + ")";
		checkEvaluated(wAction, tracer);
		Object wRes = null;
		if (isInvocable()) {
			boolean trace = tracer != null;
			CXTimer wT = trace ? new CXTimer("invokeMethod", true) : null;
			try {
				wRes = getInvocable().invokeMethod(aJSObject, aJSObjMethod,
						aArgs);
			} catch (ScriptException e) {
				CXJsExcepRhino.throwMyScriptExcep(this, pMainModule, tracer, e,
						"invokeMethod(" + aJSObjMethod + ")");
			} catch (Throwable e) {
				throwMyScriptExcep(pMainModule, tracer,
						"Error invoking methos", e, "invokeMethod("
								+ aJSObjMethod + ")");
			} finally {
				if (trace) {
					wT.stop();
					tracer.trace(wT.toDescription());
				}
			}
		} else
			throwMyScriptExcep(pMainModule, tracer,
					"JavaScript engine is not 'invocale' - Language["
							+ getLanguage() + "]", wAction);
		return wRes;
	}

	private void checkEvaluated(String aJSObjMethod, IXjsTracer tracer)
			throws CXJsException {
		if (!pEvaluated)
			throwMyScriptExcep(
					pMainModule,
					tracer,
					"JavaScript engine must be evaluated before calling invoke method",
					"invokeMethod(" + aJSObjMethod + ")");
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddIndent(aSB, pMainModule.toDescription());
		return aSB;
	}
}
