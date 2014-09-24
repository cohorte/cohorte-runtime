package org.psem2m.utilities.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * @author ogattaz
 * 
 */
public class CXJsScriptFactoryRhino extends CXJsScriptFactory {

	/**
	 * @param aFactory
	 * @param aCallName
	 */
	public CXJsScriptFactoryRhino(ScriptEngineFactory aFactory, String aCallName) {
		super(aFactory, aCallName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsScriptFactory#newScriptEngine(javax
	 * .script.ScriptEngine)
	 */
	@Override
	protected CXJsEngine newScriptEngine(ScriptEngine aScriptEngine) {
		return new CXJsEngine(aScriptEngine, this);
	}
}
