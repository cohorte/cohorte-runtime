package org.psem2m.utilities.scripting;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.psem2m.utilities.CXTimer;

public class CXJsEngine extends CXJsObjectBase {

	private ScriptEngine pEngine;
	private CXJsScriptFactory pFactory;

	protected CXJsEngine(ScriptEngine aEngine, CXJsScriptFactory afactory) {
		super();
		pEngine = aEngine;
		pFactory = afactory;
	}

	public void destroy() {
		pEngine = null;
		pFactory = null;
	}

	// Pour eval - Ajout dans ENGINE_SCOPE
	public void evalPut(String key, Object value) {
		pEngine.put(key, value);
	}

	public Object eval(CXJsSourceMain aMainModule) throws CXJsException {
		return eval(aMainModule, (IXjsTracer) null);
	}

	public Object eval(CXJsSourceMain aMainModule, IXjsTracer tracer)
			throws CXJsException {
		Object wRes = null;
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("eval", true) : null;
		try {
			wRes = pEngine.eval(aMainModule.getMergedCode());
		} catch (ScriptException e) {
			CXJsExcepRhino.throwMyScriptExcep(this, aMainModule, tracer, e,
					"eval");
		} catch (Exception e) {
			throwMyScriptExcep(aMainModule, tracer, "Error evaluating script",
					e, "eval");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return wRes;
	}

	public Object eval(CXJsSourceMain aMainModule, ScriptContext aCtx)
			throws CXJsException {
		return eval(aMainModule, aCtx, (IXjsTracer) null);
	}

	public Object eval(CXJsSourceMain aMainModule, ScriptContext aCtx,
			IXjsTracer tracer) throws CXJsException {
		Object wRes = null;
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("evalCtx", true) : null;
		try {
			wRes = pEngine.eval(aMainModule.getMergedCode(), aCtx);
		} catch (ScriptException e) {
			CXJsExcepRhino.throwMyScriptExcep(this, aMainModule, tracer, e,
					"evalCtx");
		} catch (Exception e) {
			throwMyScriptExcep(aMainModule, tracer, "Error evaluating script",
					e, "evalCtx");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return wRes;
	}

	public Object eval(CXJsSourceMain aMainModule, Bindings aBinding)
			throws CXJsException {
		return eval(aMainModule, aBinding, (IXjsTracer) null);
	}

	public Object eval(CXJsSourceMain aMainModule, Bindings aBinding,
			IXjsTracer tracer) throws CXJsException {
		Object wRes = null;
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("evalBinding", true) : null;
		try {
			wRes = pEngine.eval(aMainModule.getMergedCode(), aBinding);
		} catch (ScriptException e) {
			CXJsExcepRhino.throwMyScriptExcep(this, aMainModule, tracer, e,
					"evalBinding");
		} catch (Exception e) {
			throwMyScriptExcep(aMainModule, tracer, "Error evaluating script",
					e, "evalBinding");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return wRes;
	}

	/**
	 * @param aMainModule
	 * @param aCheckTimeStamp
	 * @return
	 * @throws CXJsException
	 */
	public CXJsCompiledScript compile(CXJsSourceMain aMainModule,
			boolean aCheckTimeStamp) throws CXJsException {
		return compile(aMainModule, aCheckTimeStamp, CXjsTracerNull.getInstance());
	}

	public CXJsCompiledScript compile(CXJsSourceMain aMainModule,
			boolean aCheckTimeStamp, IXjsTracer tracer) throws CXJsException {
		if (tracer != null)
			tracer.trace("Compile");
		return new CXJsCompiledScript(aMainModule, doCompile(aMainModule,
				"compile", tracer), this, aCheckTimeStamp);
	}

	/**
	 * 16j_107 - ogat - 2010/06/11 - pour compatibilite ascendante
	 * 
	 * @param aMainModule
	 * @return
	 * @throws CXJsException
	 */
	public CompiledScript reCompile(CXJsSourceMain aMainModule)
			throws CXJsException {
		return reCompile(aMainModule, CXjsTracerNull.getInstance());
	}

	public CompiledScript reCompile(CXJsSourceMain aMainModule, IXjsTracer tracer)
			throws CXJsException {
		if (tracer != null)
			tracer.trace("reCompile");
		if (aMainModule == null)
			throwMyScriptExcep(aMainModule, tracer,
					"Can't recompile script - Script has never been compiled - Language["
							+ getLanguage() + "]", "reCompile");
		aMainModule.reload(tracer);
		return doCompile(aMainModule, "reCompile", tracer);
	}

	private CompiledScript doCompile(CXJsSourceMain aMainModule,
			String aAction, IXjsTracer tracer) throws CXJsException {
		if (isCompilable()) {
			boolean trace = tracer != null;
			CXTimer wT = trace ? new CXTimer("compile", true) : null;
			try {
				return ((Compilable) pEngine).compile(aMainModule
						.getMergedCode());
			} catch (ScriptException e) {
				CXJsExcepRhino.throwMyScriptExcep(this, aMainModule, tracer, e,
						"doCompile");
			} catch (Exception e) {
				throwMyScriptExcep(aMainModule, tracer,
						"Error compiling script", e, "doCompile");
			} finally {
				if (trace) {
					wT.stop();
					tracer.trace(wT.toDescription());
				}
			}
		} else
			throwMyScriptExcep(aMainModule, tracer,
					"JavaScript engine is not 'compilable' - Language["
							+ getLanguage() + "]", aAction);
		return null;
	}

	public Object getJSObject(String akey) {
		return pEngine.get(akey);
	}

	protected void throwMyScriptExcep(CXJsSourceMain aMainModule,
			IXjsTracer tracer, String aErrMsg, String aAction)
			throws CXJsException {
		if (tracer != null)
			tracer.trace(aAction + "Error[" + aErrMsg + "]");
		throw new CXJsException(aMainModule, aErrMsg, null, aAction);
	}

	protected void throwMyScriptExcep(CXJsSourceMain aMainModule,
			IXjsTracer tracer, String aErrMsg, Throwable e, String aAction)
			throws CXJsException {
		if (tracer != null)
			tracer.trace(this, aAction + "Error[" + aErrMsg + "]", e);
		throw new CXJsException(aMainModule, aErrMsg, e, aAction);
	}

	public ScriptEngine getScriptEngine() {
		return pEngine;
	}

	public CXJsScriptFactory getFactory() {
		return pFactory;
	}

	public String getLanguage() {
		return pFactory.getCallName();
	}

	public boolean isInvocable() {
		return pEngine instanceof Invocable;
	}

	public boolean isCompilable() {
		return pEngine instanceof Compilable;
	}

	protected Invocable getInvocable() {
		return (Invocable) pEngine;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "ScriptEngine - Language", getLanguage());
		return aSB;
	}
}
