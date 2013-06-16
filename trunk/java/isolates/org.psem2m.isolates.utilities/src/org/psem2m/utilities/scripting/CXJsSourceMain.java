package org.psem2m.utilities.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.psem2m.utilities.CXTimer;
import org.psem2m.utilities.rsrc.CXRsrcProvider;
import org.psem2m.utilities.rsrc.CXRsrcText;
import org.psem2m.utilities.rsrc.CXRsrcUriDir;
import org.psem2m.utilities.rsrc.CXRsrcUriPath;

public class CXJsSourceMain extends CXJsSource {

	public static CXJsSourceMain newInstanceFromFile(
			CXRsrcProvider aSrcProvider, CXRsrcUriPath aRelPath,
			String aLanguage, IXjsTracer tracer) throws CXJsException {
		CXJsSourceMain wResult = new CXJsSourceMain(aSrcProvider, aLanguage);
		wResult.loadFromFile(aRelPath, tracer);
		return wResult;
	}

	// Acces au JS qui pointe sur le repertoires des script
	// aMairSrcRelPath : Path relatif par rapport a aSrcProvider pour les
	// includes de aMainSrc
	// -> Root de aSrcProvider si null
	// aLanguage : language du scripting
	public static CXJsSourceMain newInstanceFromSource(
			CXRsrcProvider aSrcProvider, String aMainSrcRelPath,
			String aMainSrc, String aLanguage, IXjsTracer tracer)
			throws CXJsException {
		CXJsSourceMain wResult = new CXJsSourceMain(aSrcProvider, aLanguage);
		wResult.loadFromSource(aMainSrc, new CXRsrcUriDir(aMainSrcRelPath),
				tracer);
		return wResult;
	}

	/**
	 * LowCase permet plus de souplesse dasn les includes
	 * 
	 * @param aRelPath
	 * @return
	 */
	public static String pathToHashMapKey(String aRelPath) {
		return aRelPath == null ? null : aRelPath.toLowerCase();
	}

	private CXRsrcUriPath pFilePath;
	private CXRsrcText pFileRsrc;
	private final String pLanguage;
	// Modules classes par path en lowCase
	private HashMap<String, CXJsModule> pListModules = new HashMap<String, CXJsModule>();
	private String pMergedCode;

	private final LinkedList<CXJsModule> pOrderedIncludes = new LinkedList<CXJsModule>();

	private CXRsrcText[] pResources;

	private final CXRsrcProvider pRootRsrc;

	private CXJsSourceMain(CXRsrcProvider aSrcProvider, String aLanguage) {
		super();
		pRootRsrc = aSrcProvider;
		pLanguage = aLanguage;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = aSB == null ? new StringBuilder() : aSB;
		if (isLoadedFromFile()) {
			descrAddLine(aSB, "Loaded from file", pFilePath.getFullPath());
		} else
			descrAddLine(aSB, "Loaded from source");
		descrAddLine(aSB, "Root directory");
		descrAddIndent(aSB, pRootRsrc.toDescription());
		if (pResources != null) {
			descrAddLine(aSB, "Ressources");
			StringBuilder wTmp = new StringBuilder();
			for (CXRsrcText xRsrc : pResources)
				descrAddLine(wTmp, xRsrc.toDescription());
			descrAddIndent(aSB, wTmp);
		}
		descrAddLine(aSB, "Includes tree");
		descrAddIndent(aSB, treeToString());
		descrAddLine(aSB, "Source");
		aSB = super.addDescriptionInBuffer(aSB);
		return aSB;
	}

	public boolean checkTimeStamp() throws CXJsException {
		try {
			if (pResources == null)
				return true;
			for (CXRsrcText xRsrc : pResources)
				if (!pRootRsrc.checkTimeStamp(xRsrc))
					return false;
			return true;
		} catch (Exception e) {
			throw new CXJsException(this, "Error checking timeStamp", e,
					"checkTimeStamp");
		}
	}

	public CXRsrcText[] checkTimeStamps() throws CXJsException {
		try {
			if (pResources == null)
				return null;
			ArrayList<CXRsrcText> wArray = new ArrayList<CXRsrcText>(
					pResources.length);
			for (CXRsrcText xRsrc : pResources)
				if (!pRootRsrc.checkTimeStamp(xRsrc))
					wArray.add(xRsrc);
			return wArray.size() == 0 ? null : wArray
					.toArray(new CXRsrcText[wArray.size()]);
		} catch (Exception e) {
			throw new CXJsException(this, "Error checking timeStamp", e,
					"checkTimeStamp");
		}
	}

	@Override
	public CXJsSourceLocalization findSource(int aMergeLineNumber) {
		// FDB - Fiche 64829 - pLineNumber>0
		if (aMergeLineNumber < 0)
			return null;
		for (CXJsModule xMod : pOrderedIncludes) {
			CXJsSourceLocalization wRes = xMod.findSource(aMergeLineNumber);
			if (wRes != null) {
				return wRes;
			}
		}
		return super.findSource(aMergeLineNumber);
	}

	public Object getErrReport(int aLineNumber, int aColumnNumber) {
		return null;
	}

	public String getLanguage() {
		return pLanguage;
	}

	public String getMergedCode() {
		return isLoaded() ? pMergedCode : "";
	}

	@Override
	protected CXJsModule getNextSibling() {
		return null;
	}

	public CXRsrcText[] getResources() {
		return pResources;
	}

	public CXRsrcProvider getRsrcProvider() {
		return pRootRsrc;
	}

	@Override
	public String getSourceName() {
		return pFilePath != null ? pFilePath.getName() : "Main";
	}

	public boolean hasFilesDependencies() {
		return isLoadedFromFile()
				|| (pResources != null && pResources.length != 0);
	}

	public boolean isLoadedFromFile() {
		return pFilePath != null;
	}

	public boolean isLoadedFromSource() {
		return pFilePath == null;
	}

	@Override
	public boolean isMain() {
		return true;
	}

	protected void load(String aSource, CXRsrcUriDir aSrcRootDir)
			throws CXJsExcepLoad {
		load(aSource, aSrcRootDir, (IXjsTracer) null);
	}

	protected void load(String aSource, CXRsrcUriDir aSrcRootDir,
			IXjsTracer tracer) throws CXJsExcepLoad {
		boolean trace = tracer != null;
		CXTimer wT = trace ? new CXTimer("loadMainScript", true) : null;
		try {
			setSource(aSource);
			setSrcRootDir(aSrcRootDir);
			StringBuilder wSB = loadDoBefore();
			super.load();
			loadDoAfter(wSB);
			if (trace)
				tracer.trace("loadOk - code["
						+ (pMergedCode == null ? 0 : pMergedCode.length())
						+ "] chars");
		} catch (Exception e) {
			if (trace)
				tracer.trace("loadMainScriptError", e);
			loadThrowExcep(this, e);
		} finally {
			if (trace) {
				wT.stop();
				tracer.trace(wT.toDescription());
			}
		}
	}

	protected void loadDoAfter(StringBuilder aSB) throws CXJsExcepLoad {
		int wRsrcSize = pListModules.size();
		if (pFileRsrc != null)
			wRsrcSize += 1;
		if (wRsrcSize > 0) {
			pResources = new CXRsrcText[wRsrcSize];
			int i = 0;
			if (pFileRsrc != null)
				pResources[i++] = pFileRsrc;
			for (CXJsModule xMod : pListModules.values())
				pResources[i++] = xMod.getRsrc();
		}
		StringBuilder wSB = new StringBuilder();
		processIncludes();
		int wStartLine = 1;
		for (CXJsModule xMod : pOrderedIncludes) {
			wStartLine = xMod.merge(wSB, wStartLine);
		}
		merge(wSB, wStartLine);
		pMergedCode = wSB.toString();
		if (traceDebugOn()) {
			System.out
					.println("------------------ descrToString ------------------");
			System.out.println(toDescription());
			System.out
					.println("------------------ MergedCode ------------------");
			System.out.println(pMergedCode);
		}
	}

	protected StringBuilder loadDoBefore() throws CXJsExcepLoad {
		pListModules = new HashMap<String, CXJsModule>();
		return new StringBuilder(10240);
	}

	// Renvoie le nombre total de lignes
	public void loadFromFile(CXRsrcUriPath aRelpath, IXjsTracer tracer)
			throws CXJsExcepLoad {
		try {
			pFilePath = aRelpath;
			pFileRsrc = pRootRsrc.rsrcReadTxt(aRelpath);
			load(pFileRsrc.getContent(), pFilePath.getParent(), tracer);
		} catch (CXJsExcepLoad e) {
			if (tracer != null)
				tracer.trace("loadMainFleError", e);
			throw (e);
		} catch (Exception e) {
			if (tracer != null)
				tracer.trace("loadMainFleError", e);
			loadThrowExcep(this, e);
		}
	}

	// Renvoie le nombre total de lignes
	public void loadFromSource(String aSource, CXRsrcUriDir aDir,
			IXjsTracer tracer) throws CXJsExcepLoad {
		load(aSource, aDir, tracer);
	}

	// Acc�s au code

	protected void loadModuleAdd(CXJsModule aModule) {
		pListModules.put(pathToHashMapKey(aModule.getPath()), aModule);
	}

	protected boolean loadModuleExists(String aRelPath) {
		return pListModules.containsKey(pathToHashMapKey(aRelPath));
	}

	@Override
	protected void loadThrowExcep(CXJsSourceMain aMain, Throwable e)
			throws CXJsExcepLoad {
		if (isLoadedFromFile())
			throw new CXJsExcepLoad(aMain, e, "Error main module "
					+ pFilePath.getName());
		else
			super.loadThrowExcep(aMain, e);
	}

	// UNiquement sir load si !pMergeIncludes
	protected LinkedList<CXJsModule> processIncludes() throws CXJsExcepLoad {
		if (hasModules()) {
			for (CXJsModule xMod : getModules()) {
				xMod.orderIncludes(pOrderedIncludes, -1);
			}
		}
		return pOrderedIncludes;
	}

	public void reload(IXjsTracer tracer) throws CXJsExcepLoad {
		if (isLoaded()) {
			pOrderedIncludes.clear();
			pListModules.clear();
			pMergedCode = null;
			pResources = null;
			if (isLoadedFromFile()) {
				super.initMainReload(true);
				loadFromFile(pFilePath, tracer);
			} else {
				super.initMainReload(false);
				loadFromSource(getSources(), getSrcRootDir(), tracer);
			}
		} else
			throw new CXJsExcepLoad(this, "Can't reload script["
					+ getSourceName() + "] - Script is not loaded");
	}
}
