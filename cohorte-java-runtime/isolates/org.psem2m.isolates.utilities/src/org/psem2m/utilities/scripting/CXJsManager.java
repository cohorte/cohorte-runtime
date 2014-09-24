package org.psem2m.utilities.scripting;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.psem2m.utilities.rsrc.CXRsrcProvider;
import org.psem2m.utilities.rsrc.CXRsrcUriPath;

/**
 * @author ogattaz
 * 
 */
public class CXJsManager extends CXJsObjectBase {

	private final static ScriptEngineManager sEngineManager = new ScriptEngineManager();

	private CXJsScriptFactory pScriptEngineFactory;

	/**
	 * @param aScriptLanguage
	 * @throws CXJsExcepUnknownLanguage
	 */
	public CXJsManager(String aScriptLanguage) throws CXJsExcepUnknownLanguage {
		for (ScriptEngineFactory xFact : sEngineManager.getEngineFactories()) {
			if (CXJsScriptFactory.checkName(xFact, aScriptLanguage)) {
				pScriptEngineFactory = CXJsScriptFactory.newInstance(xFact, aScriptLanguage);
			}
		}
		if (pScriptEngineFactory == null) {
			throw new CXJsExcepUnknownLanguage(aScriptLanguage);
		}
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
		StringBuilder wSB = new StringBuilder();
		descrAddLine(wSB, "Sources - Root directory");
		descrAddLine(wSB, "Current ScriptEngineFactory");
		if (pScriptEngineFactory != null) {
			descrAddIndent(wSB, pScriptEngineFactory.toDescription());
		} else {
			descrAddIndent(wSB, "ScriptEngineFactory", "NULL");
		}
		descrAddLine(wSB, "Nb languages available", sEngineManager.getEngineFactories().size());
		descrAddIndent(wSB, getAvailableLanguages());
		descrAddLine(aSB, "Script engine manager");
		return descrAddIndent(aSB, wSB);
	}

	/**
	 * @param aName
	 * @return
	 */
	public boolean checkName(String aName) {
		return pScriptEngineFactory == null ? false : pScriptEngineFactory.checkName(aName);
	}

	/**
	 * Compile
	 * 
	 * @param aRootSrc
	 * @param aRelativePath
	 * @param aCheckTimeStamp
	 * @param tracer
	 * @return
	 * @throws CXJsException
	 */
	public CXJsCompiledScript compileFromFile(CXRsrcProvider aRootSrc, CXRsrcUriPath aRelativePath,
			boolean aCheckTimeStamp, IXjsTracer tracer) throws CXJsException {
		CXJsEngine wEngine = pScriptEngineFactory.getScriptEngine();
		return wEngine.compile(getMainSource(aRootSrc, aRelativePath, tracer), aCheckTimeStamp,
				tracer);
	}

	/**
	 * @return
	 */
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

	/**
	 * @return
	 */
	public String getLanguage() {
		return pScriptEngineFactory == null ? "Null" : pScriptEngineFactory.getCallName();
	}

	/**
	 * @param aRootSrc
	 * @param aRelativePath
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(CXRsrcProvider aRootSrc, CXRsrcUriPath aRelativePath)
			throws CXJsException {
		return CXJsSourceMain.newInstanceFromFile(aRootSrc, aRelativePath, getLanguage(),
				CXjsTracerNull.getInstance());
	}

	/**
	 * @param aRootSrc
	 * @param aRelativePath
	 * @param tracer
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(CXRsrcProvider aRootSrc, CXRsrcUriPath aRelativePath,
			IXjsTracer tracer) throws CXJsException {
		return CXJsSourceMain.newInstanceFromFile(aRootSrc, aRelativePath, getLanguage(), tracer);
	}

	/**
	 * @param aSource
	 * @param aRootSrc
	 * @param tracer
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(String aSource, CXRsrcProvider aRootSrc, IXjsTracer tracer)
			throws CXJsException {
		return CXJsSourceMain.newInstanceFromSource(aRootSrc, null, aSource, getLanguage(), tracer);
	}

	/**
	 * @param aSource
	 * @param aRootSrc
	 * @param aMairSrcRelPath
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(String aSource, CXRsrcProvider aRootSrc,
			String aMairSrcRelPath) throws CXJsException {
		return CXJsSourceMain.newInstanceFromSource(aRootSrc, aMairSrcRelPath, aSource,
				getLanguage(), CXjsTracerNull.getInstance());
	}

	/**
	 * @param aSource
	 * @param aRootSrc
	 * @param aMairSrcRelPath
	 * @param tracer
	 * @return
	 * @throws CXJsException
	 */
	public CXJsSourceMain getMainSource(String aSource, CXRsrcProvider aRootSrc,
			String aMairSrcRelPath, IXjsTracer tracer) throws CXJsException {
		return CXJsSourceMain.newInstanceFromSource(aRootSrc, aMairSrcRelPath, aSource,
				getLanguage(), tracer);
	}

	/**
	 * @return
	 */
	public CXJsEngine getScriptEngine() {
		return pScriptEngineFactory.getScriptEngine();
	}

	/**
	 * @return
	 */
	public CXJsScriptFactory getScriptEngineFactory() {
		return pScriptEngineFactory;
	}

	/**
	 * @param aMainModule
	 * @return
	 */
	public CXJsEngineInvocable getScriptEngineInvocable(CXJsSourceMain aMainModule) {
		return pScriptEngineFactory.getScriptEngineInvocable(aMainModule);
	}

	/**
	 * @return
	 */
	public boolean isMultiThreaded() {
		return pScriptEngineFactory.isMultiThreaded();
	}
}
