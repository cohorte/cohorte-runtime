package org.psem2m.utilities.scripting;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.psem2m.utilities.rsrc.CXRsrcProvider;
import org.psem2m.utilities.rsrc.CXRsrcUriPath;


public class CXJsManager extends CXJsObjectBase {

	private final static ScriptEngineManager sEngineManager = new ScriptEngineManager();

	private CXJsScriptFactory pScriptEngineFactory;

	public CXJsManager(String aScriptLanguage) throws CXJsExcepUnknownLanguage {
		for (ScriptEngineFactory xFact : sEngineManager.getEngineFactories()) {
			if (CXJsScriptFactory.checkName(xFact, aScriptLanguage)) {
				pScriptEngineFactory = CXJsScriptFactory.newInstance(xFact,
						aScriptLanguage);
			}
		}
		if (pScriptEngineFactory == null)
			throw new CXJsExcepUnknownLanguage(aScriptLanguage);
	}

	/**
	 * @param aRootSrc
	 * @param aRelativePath
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(CXRsrcProvider aRootSrc,
			CXRsrcUriPath aRelativePath) throws CXJsException {
		return CXJsSourceMain.newInstanceFromFile(aRootSrc, aRelativePath,
				getLanguage(), CXjsTracerNull.getInstance());
	}

	public CXJsSourceMain getMainSource(String aSource,
			CXRsrcProvider aRootSrc, IXjsTracer tracer) throws CXJsException {
		return CXJsSourceMain.newInstanceFromSource(aRootSrc, null, aSource,
				getLanguage(), tracer);
	}

	/**
	 * @param aSource
	 * @param aRootSrc
	 * @param aMairSrcRelPath
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(String aSource,
			CXRsrcProvider aRootSrc, String aMairSrcRelPath)
			throws CXJsException {
		return CXJsSourceMain.newInstanceFromSource(aRootSrc, aMairSrcRelPath,
				aSource, getLanguage(), CXjsTracerNull.getInstance());
	}

	public CXJsSourceMain getMainSource(String aSource,
			CXRsrcProvider aRootSrc, String aMairSrcRelPath, IXjsTracer tracer)
			throws CXJsException {
		return CXJsSourceMain.newInstanceFromSource(aRootSrc, aMairSrcRelPath,
				aSource, getLanguage(), tracer);
	}

	public CXJsSourceMain getMainSource(CXRsrcProvider aRootSrc,
			CXRsrcUriPath aRelativePath, IXjsTracer tracer) throws CXJsException {
		return CXJsSourceMain.newInstanceFromFile(aRootSrc, aRelativePath,
				getLanguage(), tracer);
	}

	public CXJsEngine getScriptEngine() {
		return pScriptEngineFactory.getScriptEngine();
	}

	public CXJsEngineInvocable getScriptEngineInvocable(
			CXJsSourceMain aMainModule) {
		return pScriptEngineFactory.getScriptEngineInvocable(aMainModule);
	}

	// Compile
	public CXJsCompiledScript compileFromFile(CXRsrcProvider aRootSrc,
			CXRsrcUriPath aRelativePath, boolean aCheckTimeStamp, IXjsTracer tracer)
			throws CXJsException {
		CXJsEngine wEngine = pScriptEngineFactory.getScriptEngine();
		return wEngine.compile(getMainSource(aRootSrc, aRelativePath, tracer),
				aCheckTimeStamp, tracer);
	}

	public boolean checkName(String aName) {
		return pScriptEngineFactory == null ? false : pScriptEngineFactory
				.checkName(aName);
	}

	public boolean isMultiThreaded() {
		return pScriptEngineFactory.isMultiThreaded();
	}

	public CXJsScriptFactory getScriptEngineFactory() {
		return pScriptEngineFactory;
	}

	public String getLanguage() {
		return pScriptEngineFactory == null ? "Null" : pScriptEngineFactory
				.getCallName();
	}

	public String getAvailableLanguages() {
		StringBuilder wSB = new StringBuilder();
		int wIdx = 0;
		for (ScriptEngineFactory xFact : sEngineManager.getEngineFactories()) {
			descrAddProp(wSB, "", wIdx++);
			descrAddLine(wSB, xFact.getLanguageName());
			descrAddIndent(wSB, new CXJsScriptFactory(xFact).toDescription());
		}
		return wSB.toString();
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		StringBuilder wSB = new StringBuilder();
		descrAddLine(wSB, "Sources - Root directory");
		descrAddLine(wSB, "Current ScriptEngineFactory");
		if (pScriptEngineFactory != null)
			descrAddIndent(wSB, pScriptEngineFactory.toDescription());
		else
			descrAddIndent(wSB, "ScriptEngineFactory", "NULL");
		descrAddLine(wSB, "Nb languages available", sEngineManager
				.getEngineFactories().size());
		descrAddIndent(wSB, getAvailableLanguages());
		descrAddLine(aSB, "Script engine manager");
		return descrAddIndent(aSB, wSB);
	}
}
