package org.psem2m.utilities.scripting;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;

import org.psem2m.utilities.CXTimer;

public class CXJsScriptContext implements ScriptContext {

	public final static String ACT_EVAL = "eval";
	public final static String ACT_EVAL_COMPILED = "evalCompiled";
	public final static String ACT_INVOKE_FUNC = "invokeFunc";
	public final static String ACT_INVOKE_METH = "invokeMeth";

	protected Writer writer;
	protected Writer errorWriter;
	protected Reader reader;
	protected Bindings engineScope;
	protected Bindings globalScope;
	private StringWriter pBuffer;
	private StringWriter pErrBuffer;
	private final CXTimer pTimer = new CXTimer();
	private String pAction;
	private final int pInitSize;

	public CXJsScriptContext(int aInitSize) {
		globalScope = null;
		pInitSize = aInitSize > 0 ? aInitSize : 8192;
		engineScope = new SimpleBindings();
		resetWriter();
	}

	public void resetWriter() {
		pBuffer = new StringWriter(pInitSize);
		writer = new PrintWriter(pBuffer);
		reader = null;
		errorWriter = null;
		pErrBuffer = null;
	}

	public CXTimer getTimer() {
		return pTimer;
	}

	public boolean isRunning() {
		return pTimer.isCounting();
	}

	public CXJsScriptContext start(String aAction) {
		return start(aAction, 0);
	}

	public CXJsScriptContext start(String aAction, long aTimeRef) {
		pAction = aAction;
		pTimer.start(aTimeRef);
		return this;
	}

	public CXJsScriptContext stop() {
		pTimer.stop();
		return this;
	}

	public String getDurationStrMs() {
		return new StringBuilder(pAction).append(" - ")
				.append(pTimer.getDurationStrMilliSec()).toString();
	}

	public long getDurationNs() {
		return pTimer.getDurationNs();
	}

	public String getTimerInfo() {
		return new StringBuilder().append(pAction).append(" - StartAt[")
				.append(pTimer.getStartAtSecStr()).append("] - StopAt[")
				.append(pTimer.getStopAtSecStr()).append("] - Duration[")
				.append(pTimer.getDurationStrMilliSec()).append("]").toString();
	}

	public String descrToString() {
		StringBuilder wSB = new StringBuilder(1024);
		CXJsObjectBase.descrAddLine(wSB, "Output buffer - Size", pBuffer
				.getBuffer().length());
		CXJsObjectBase.descrAddIndent(wSB, pBuffer.getBuffer().toString());
		if (pErrBuffer != null) {
			CXJsObjectBase.descrAddLine(wSB, "Error buffer - Size", pErrBuffer
					.getBuffer().length());
			CXJsObjectBase.descrAddIndent(wSB, pErrBuffer.getBuffer().toString());
		}
		return wSB.toString();
	}

	public StringWriter getBuffer() {
		return pBuffer;
	}

	public StringWriter getErrBuffer() {
		if (pErrBuffer == null)
			pErrBuffer = new StringWriter();
		return pErrBuffer;
	}

	// Interface ScriptContext

	@Override
	public void setBindings(Bindings bindings, int scope) {
		switch (scope) {
		case ENGINE_SCOPE:
			if (bindings == null) {
				throw new NullPointerException("Engine scope cannot be null.");
			}
			engineScope = bindings;
			break;
		case GLOBAL_SCOPE:
			globalScope = bindings;
			break;
		default:
			throw new IllegalArgumentException("Invalid scope value.");
		}
	}

	@Override
	public Object getAttribute(String name) {
		if (engineScope.containsKey(name)) {
			return getAttribute(name, ENGINE_SCOPE);
		} else if (globalScope != null && globalScope.containsKey(name)) {
			return getAttribute(name, GLOBAL_SCOPE);
		}
		return null;
	}

	@Override
	public Object getAttribute(String name, int scope) {
		// System.out.println(String.format("getAttribute scope[%d] [%s]",scope,name));
		switch (scope) {
		case ENGINE_SCOPE:
			return engineScope.get(name);
		case GLOBAL_SCOPE:
			if (globalScope != null) {
				return globalScope.get(name);
			}
			return null;
		default:
			throw new IllegalArgumentException("Illegal scope value.");
		}
	}

	@Override
	public Object removeAttribute(String name, int scope) {
		switch (scope) {
		case ENGINE_SCOPE:
			if (getBindings(ENGINE_SCOPE) != null) {
				return getBindings(ENGINE_SCOPE).remove(name);
			}
			return null;
		case GLOBAL_SCOPE:
			if (getBindings(GLOBAL_SCOPE) != null) {
				return getBindings(GLOBAL_SCOPE).remove(name);
			}
			return null;
		default:
			throw new IllegalArgumentException("Illegal scope value.");
		}
	}

	public void removeAttrEngine(String name) {
		removeAttribute(name, ENGINE_SCOPE);
	}

	public void removeAttrGlobal(String name) {
		removeAttribute(name, GLOBAL_SCOPE);
	}

	public void setAttrEngine(String name, Object value) {
		setAttribute(name, value, ENGINE_SCOPE);
	}

	public void setAttrGlobal(String name, Object value) {
		setAttribute(name, value, GLOBAL_SCOPE);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		// System.out.println(String.format("setAttribute scope[%d] [%s|%s] ]",scope,name,value.toString()));

		switch (scope) {
		case ENGINE_SCOPE:
			engineScope.put(name, value);
			return;
		case GLOBAL_SCOPE:
			if (globalScope != null) {
				globalScope.put(name, value);
			}
			return;
		default:
			throw new IllegalArgumentException("Illegal scope value.");
		}
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public Reader getReader() {
		// Uniquement si besoin
		if (reader == null)
			reader = new InputStreamReader(System.in);
		return reader;
	}

	@Override
	public void setReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	@Override
	public Writer getErrorWriter() {
		// Uniquement si besoin
		if (errorWriter == null)
			errorWriter = new PrintWriter(getErrBuffer());
		return errorWriter;
	}

	@Override
	public void setErrorWriter(Writer writer) {
		this.errorWriter = writer;
	}

	@Override
	public int getAttributesScope(String name) {
		if (engineScope.containsKey(name)) {
			return ENGINE_SCOPE;
		} else if (globalScope != null && globalScope.containsKey(name)) {
			return GLOBAL_SCOPE;
		} else {
			return -1;
		}
	}

	@Override
	public Bindings getBindings(int scope) {
		// System.out.println(String.format("getBindings scope[%d] ",scope));

		if (scope == ENGINE_SCOPE) {
			return engineScope;
		} else if (scope == GLOBAL_SCOPE) {
			return globalScope;
		} else {
			throw new IllegalArgumentException("Illegal scope value.");
		}
	}

	@Override
	public List<Integer> getScopes() {
		return scopes;
	}

	private static List<Integer> scopes;
	static {
		scopes = new ArrayList<Integer>(2);
		scopes.add(new Integer(ENGINE_SCOPE));
		scopes.add(new Integer(GLOBAL_SCOPE));
		scopes = Collections.unmodifiableList(scopes);
	}
}
