package org.psem2m.utilities.scripting;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.psem2m.utilities.CXTimer;

public class CXJsCompiledScript extends CXJsObjectBase {

	private CXJsEngine pEngine;
	private CompiledScript pCompiledScript;
	private final boolean pCheckTimeStamp;
	private CXJsSourceMain pMainModule;

	protected CXJsCompiledScript(CXJsSourceMain aMainModule,
			CompiledScript aCompiledScript, CXJsEngine aEngine,
			boolean aCheckTimeStamp) {
		super();
		pCompiledScript = aCompiledScript;
		pEngine = aEngine;
		pCheckTimeStamp = aCheckTimeStamp;
		pMainModule = aMainModule;
	}

	public void destroy() {
		pEngine = null;
		pCompiledScript = null;
		pMainModule = null;
	}

	public CXJsSourceMain getMainModule() {

		return pMainModule;
	}

	public Object eval() throws CXJsException {
		return eval((IXjsTracer) null);
	}

	public Object eval(IXjsTracer tracer) throws CXJsException {
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("evalCompiled", true) : null;
		try {
			if (!checkTimeStamp(tracer))
				recompile(tracer);
			return pCompiledScript.eval();
		} catch (ScriptException e) {
			CXJsExcepRhino.throwMyScriptExcep(this, pMainModule, tracer, e,
					"evalCompiled");
		} catch (Exception e) {
			throwMyScriptExcep(tracer, "Error evaluating script", e,
					"evalCompiled");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return null;
	}

	public Object eval(Bindings bindings) throws CXJsException {
		return eval(bindings, null);
	}

	public Object eval(Bindings bindings, IXjsTracer tracer) throws CXJsException {
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("evalBindingCompiled", true) : null;
		try {
			if (!checkTimeStamp(tracer))
				recompile(tracer);
			return pCompiledScript.eval(bindings);
		} catch (ScriptException e) {
			// FDB - 64796
			CXJsExcepRhino.throwMyScriptExcep(this, pMainModule, tracer, e,
					"evalCompiledBinding");
		} catch (Exception e) {
			throwMyScriptExcep(tracer, "Error evaluating script", e,
					"evalCompiledBinding");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return null;
	}

	public Object eval(ScriptContext context) throws CXJsException {
		return eval(context, null);
	}

	// AJout tracer
	public Object eval(ScriptContext context, IXjsTracer tracer)
			throws CXJsException {
		Object wRes = null;
		// Test tracer!=null car on n'a pas de isTraceOn() dans ITracer
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("evalCompiledContext", true) : null;
		try {
			if (!checkTimeStamp(tracer))
				recompile(tracer);
			wRes = pCompiledScript.eval(context);
		} catch (ScriptException e) {
			// FDB - 64796
			CXJsExcepRhino.throwMyScriptExcep(this, pMainModule, tracer, e,
					"evalContextCompiled");
		} catch (Exception e) {
			throwMyScriptExcep(tracer, "Error evaluating script", e,
					"evalContextCompiled");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return wRes;
	}

	public boolean hasFilesDependencies() {
		return pMainModule.hasFilesDependencies();
	}

	public boolean isCheckTimeStamp() {
		return pCheckTimeStamp;
	}

	protected synchronized boolean checkTimeStamp(IXjsTracer tracer)
			throws CXJsException {
		boolean trace = tracer != null;
		if (!pCheckTimeStamp) {
			if (trace)
				tracer.trace("no checkTimeStamp");
			return true;
		}
		CXTimer wT = trace ? new CXTimer("checkTimeStamp", true) : null;
		try {
			return pMainModule.checkTimeStamp();
		} catch (Exception e) {
			throwMyScriptExcep(tracer, "Error checking timeStamp", e,
					"checkTimeStamp");
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
		return false;
	}

	protected void recompile(IXjsTracer tracer) throws CXJsException {
		pCompiledScript = pEngine.reCompile(pMainModule, tracer);
	}

	protected void throwMyScriptExcep(IXjsTracer tracer, String aErrMsg,
			String aAction) throws CXJsException {
		if (tracer != null)
			tracer.trace(aAction + "Error[" + aErrMsg + "]");
		throw new CXJsException(pMainModule, aErrMsg, null, aAction);
	}

	protected void throwMyScriptExcep(IXjsTracer tracer, String aErrMsg,
			Throwable e, String aAction) throws CXJsException {
		if (tracer != null)
			tracer.trace(this, aAction + "Error[" + aErrMsg + "]", e);
		throw new CXJsException(pMainModule, aErrMsg, e, aAction);
	}

	public CXJsEngine getEngine() {
		return pEngine;
	}

	public CompiledScript getCompiledScript() {
		return pCompiledScript;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddText(aSB, "CompiledScript - ");
		descrAddLine(aSB, pEngine.toDescription());
		descrAddIndent(aSB, pMainModule.toDescription());
		return aSB;
	}
}
