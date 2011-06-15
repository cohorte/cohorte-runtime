package org.psem2m.utilities.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class CXJsScriptFactoryRhino extends CXJsScriptFactory {

	public CXJsScriptFactoryRhino(ScriptEngineFactory aFactory, String aCallName) {
		super(aFactory, aCallName);
	}

	@Override
	protected CXJsEngine newScriptEngine(ScriptEngine aScriptEngine) {
		return new CXJsEngine(aScriptEngine, this);
	}
}
