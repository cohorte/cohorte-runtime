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

/**
 * @author ogattaz
 * 
 */
public class CXJsScriptContext implements ScriptContext {

	public final static String ACT_EVAL = "eval";
	public final static String ACT_EVAL_COMPILED = "evalCompiled";
	public final static String ACT_INVOKE_FUNC = "invokeFunc";
	public final static String ACT_INVOKE_METH = "invokeMeth";

	private static List<Integer> scopes;

	static {
		scopes = new ArrayList<Integer>(2);
		scopes.add(new Integer(ENGINE_SCOPE));
		scopes.add(new Integer(GLOBAL_SCOPE));
		scopes = Collections.unmodifiableList(scopes);
	}

	protected Bindings engineScope;
	protected Writer errorWriter;
	protected Bindings globalScope;
	private String pAction;
	private StringWriter pBuffer;
	private StringWriter pErrBuffer;
	private final int pInitSize;
	private final CXTimer pTimer = new CXTimer();
	protected Reader reader;
	protected Writer writer;

	/**
	 * @param aInitSize
	 */
	public CXJsScriptContext(int aInitSize) {
		globalScope = null;
		pInitSize = aInitSize > 0 ? aInitSize : 8192;
		engineScope = new SimpleBindings();
		resetWriter();
	}

	/**
	 * @return
	 */
	public String descrToString() {
		StringBuilder wSB = new StringBuilder(1024);
		CXJsObjectBase.descrAddLine(wSB, "Output buffer - Size", pBuffer.getBuffer().length());
		CXJsObjectBase.descrAddIndent(wSB, pBuffer.getBuffer().toString());
		if (pErrBuffer != null) {
			CXJsObjectBase
					.descrAddLine(wSB, "Error buffer - Size", pErrBuffer.getBuffer().length());
			CXJsObjectBase.descrAddIndent(wSB, pErrBuffer.getBuffer().toString());
		}
		return wSB.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		if (engineScope.containsKey(name)) {
			return getAttribute(name, ENGINE_SCOPE);
		} else if (globalScope != null && globalScope.containsKey(name)) {
			return getAttribute(name, GLOBAL_SCOPE);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getAttribute(java.lang.String, int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getAttributesScope(java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getBindings(int)
	 */
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

	/**
	 * @return
	 */
	public StringWriter getBuffer() {
		return pBuffer;
	}

	/**
	 * @return
	 */
	public long getDurationNs() {
		return pTimer.getDurationNs();
	}

	/**
	 * @return
	 */
	public String getDurationStrMs() {
		return new StringBuilder(pAction).append(" - ").append(pTimer.getDurationStrMilliSec())
				.toString();
	}

	/**
	 * @return
	 */
	public StringWriter getErrBuffer() {
		if (pErrBuffer == null) {
			pErrBuffer = new StringWriter();
		}
		return pErrBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getErrorWriter()
	 */
	@Override
	public Writer getErrorWriter() {
		// Uniquement si besoin
		if (errorWriter == null) {
			errorWriter = new PrintWriter(getErrBuffer());
		}
		return errorWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getReader()
	 */
	@Override
	public Reader getReader() {
		// Uniquement si besoin
		if (reader == null) {
			reader = new InputStreamReader(System.in);
		}
		return reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getScopes()
	 */
	@Override
	public List<Integer> getScopes() {
		return scopes;
	}

	/**
	 * @return
	 */
	public CXTimer getTimer() {
		return pTimer;
	}

	/**
	 * @return
	 */
	public String getTimerInfo() {
		return new StringBuilder().append(pAction).append(" - StartAt[")
				.append(pTimer.getStartAtSecStr()).append("] - StopAt[")
				.append(pTimer.getStopAtSecStr()).append("] - Duration[")
				.append(pTimer.getDurationStrMilliSec()).append("]").toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#getWriter()
	 */
	@Override
	public Writer getWriter() {
		return writer;
	}

	/**
	 * @return
	 */
	public boolean isRunning() {
		return pTimer.isCounting();
	}

	/**
	 * @param name
	 */
	public void removeAttrEngine(String name) {
		removeAttribute(name, ENGINE_SCOPE);
	}

	/**
	 * @param name
	 */
	public void removeAttrGlobal(String name) {
		removeAttribute(name, GLOBAL_SCOPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#removeAttribute(java.lang.String, int)
	 */
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

	/**
	 * 
	 */
	public void resetWriter() {
		pBuffer = new StringWriter(pInitSize);
		writer = new PrintWriter(pBuffer);
		reader = null;
		errorWriter = null;
		pErrBuffer = null;
	}

	/**
	 * @param name
	 * @param value
	 */
	public void setAttrEngine(String name, Object value) {
		setAttribute(name, value, ENGINE_SCOPE);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void setAttrGlobal(String name, Object value) {
		setAttribute(name, value, GLOBAL_SCOPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#setAttribute(java.lang.String,
	 * java.lang.Object, int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#setBindings(javax.script.Bindings, int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#setErrorWriter(java.io.Writer)
	 */
	@Override
	public void setErrorWriter(Writer writer) {
		this.errorWriter = writer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#setReader(java.io.Reader)
	 */
	@Override
	public void setReader(Reader reader) {
		this.reader = reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.script.ScriptContext#setWriter(java.io.Writer)
	 */
	@Override
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * @param aAction
	 * @return
	 */
	public CXJsScriptContext start(String aAction) {
		return start(aAction, 0);
	}

	/**
	 * @param aAction
	 * @param aTimeRef
	 * @return
	 */
	public CXJsScriptContext start(String aAction, long aTimeRef) {
		pAction = aAction;
		pTimer.start(aTimeRef);
		return this;
	}

	/**
	 * @return
	 */
	public CXJsScriptContext stop() {
		pTimer.stop();
		return this;
	}
}
