package org.psem2m.utilities.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.psem2m.utilities.rsrc.CXRsrcUriDir;

/**
 * Represente un source JS
 * 
 * @author ogattaz
 * 
 */
public abstract class CXJsSource extends CXJsObjectBase {

	/**
	 * @author ogattaz
	 * 
	 */
	public static class CXJsSourceLocalization {

		private final int pSourceLineNum;
		private final CXJsSource pSrc;

		/**
		 * @param aSourceLineNum
		 * @param aSrc
		 */
		public CXJsSourceLocalization(int aSourceLineNum, CXJsSource aSrc) {
			pSourceLineNum = aSourceLineNum;
			pSrc = aSrc;
		}

		/**
		 * @return
		 */
		public int getSourceLineNum() {
			return pSourceLineNum;
		}

		/**
		 * @return
		 */
		public String getSourceName() {
			return pSrc == null ? "Unknown" : pSrc.getSourceName();
		}

		/**
		 * @return
		 */
		public CXJsSource getSrc() {
			return pSrc;
		}
	}

	// Sources originaux avec les includes
	private final static String INCLUDE_START = "#include";
	// Sources avec includes commentes
	private boolean pLoaded = false;
	private int pMergeStartLine;
	private ArrayList<CXJsModule> pModules;
	private CXJsSource pParent;
	private final CXJsSourceMain pRoot;
	private String pSources;
	// Source parent
	private int pSourcesNbLines;
	// Ordre important pour calcul du source
	private String pSourcesNoInclude;

	private CXRsrcUriDir pSrcRootDir;

	/**
	 * 
	 */
	protected CXJsSource() {
		this(null);
	}

	/**
	 * @param aParent
	 */
	public CXJsSource(CXJsSource aParent) {
		pParent = aParent;
		pRoot = aParent == null ? (CXJsSourceMain) this : aParent.pRoot;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddLine(aSB, "SourceName", getSourceName());
		descrAddProp(aSB, "SourcesNbLines", pSourcesNbLines);
		descrAddProp(aSB, "MergeStartLine", pMergeStartLine);
		descrAddLine(aSB, "NbModules", pModules == null ? 0 : pModules.size());
		if (pModules != null) {
			StringBuilder wTmp = new StringBuilder(1024);
			for (int i = 0; i < pModules.size(); i++) {
				descrAddLine(wTmp, "Module[" + i + "]");
				if (i != 0) {
					descrAddLine(wTmp);
				}
				descrAddLine(wTmp, pModules.get(i).toDescription());
			}
			descrAddIndent(aSB, wTmp);
		}
		return aSB;
	}

	/**
	 * @param aLine
	 * @return
	 */
	private String extractPath(String aLine) {
		int wPosStart, wPosEnd;
		if ((wPosStart = aLine.indexOf('\"')) == -1) {
			wPosStart = aLine.indexOf('\'');
		}
		if (wPosStart == -1) {
			return null;
		}
		wPosStart++;
		if ((wPosEnd = aLine.indexOf('\"', wPosStart)) == -1) {
			wPosEnd = aLine.indexOf('\'', wPosStart);
		}
		if (wPosEnd == -1) {
			return null;
		}
		return aLine.substring(wPosStart, wPosEnd).trim();
	}

	/**
	 * @param aMergeLineNumber
	 * @return
	 */
	public CXJsSourceLocalization findSource(int aMergeLineNumber) {
		if (aMergeLineNumber >= pMergeStartLine
				&& aMergeLineNumber <= (pMergeStartLine + pSourcesNbLines)) {
			return new CXJsSourceLocalization(aMergeLineNumber - pMergeStartLine + 1, this);
		}
		return null;
	}

	/**
	 * @return
	 */
	protected CXJsModule getFirstChild() {
		return pModules == null || pModules.isEmpty() ? null : pModules.get(0);
	}

	/**
	 * @param aModule
	 * @return
	 */
	public int getIdx(CXJsModule aModule) {
		if (pModules == null || aModule == null) {
			return -1;
		}
		for (int i = 0; i < pModules.size(); i++) {
			if (pModules.get(i).equals(aModule)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return
	 */
	public String getIncludeStack() {
		StringBuilder wSB = new StringBuilder();
		if (!isRoot()) {
			wSB.append(getParent().getIncludeStack()).append('/');
		}
		wSB.append(getSourceName());
		return wSB.toString();
	}

	/**
	 * @return
	 */
	public CXJsModule[] getModules() {
		return hasModules() ? pModules.toArray(new CXJsModule[pModules.size()]) : null;
	}

	/**
	 * @param aModule
	 * @return
	 */
	public CXJsModule getNext(CXJsModule aModule) {
		if (pModules == null || aModule == null) {
			return null;
		}
		int wIdx = getIdx(aModule);
		return wIdx < 0 || wIdx > (pModules.size() - 2) ? null : pModules.get(wIdx + 1);
	}

	/**
	 * @return
	 */
	protected abstract CXJsModule getNextSibling();

	/**
	 * @return
	 */
	protected CXJsSource getParent() {
		return pParent;
	}

	/**
	 * @return
	 */
	protected CXJsSourceMain getRoot() {
		return pRoot;
	}

	public abstract String getSourceName();

	/**
	 * Sources originaux avec les includes
	 * 
	 * @return
	 */
	public String getSources() {
		return pSources;
	}

	/**
	 * Sources originaux avec les includes commentes
	 * 
	 * @return
	 */
	public String getSourcesNoInclude() {
		return pSourcesNoInclude;
	}

	/**
	 * @return
	 */
	protected CXRsrcUriDir getSrcRootDir() {
		return pSrcRootDir;
	}

	/**
	 * @param aLineNum
	 * @param aNbLines
	 * @return
	 */
	public String getText(int aLineNum, int aNbLines) {
		return this.getText(aLineNum, aNbLines, null);
	}

	/**
	 * @param aLineNum
	 * @param aNbLines
	 * @param aMark
	 * @return
	 */
	public String getText(int aLineNum, int aNbLines, String aMark) {
		StringBuilder wRes = new StringBuilder(1024);
		try {
			BufferedReader wReader = new BufferedReader(new StringReader(pSources));
			if (wReader.ready()) {
				int wStart = aLineNum - (aNbLines / 2);
				int wStop = aLineNum + (aNbLines / 2);
				int wCpt = 0;
				String wLine;
				while ((wLine = wReader.readLine()) != null) {
					wCpt++;
					if (wCpt >= wStart && wCpt <= wStop) {
						if (wRes.length() != 0) {
							wRes.append('\n');
						}
						if (aMark != null && wCpt == aLineNum) {
							wRes.append(aMark);
						}
						wRes.append(wLine);
					}
				}
			}
		} catch (IOException e) {
			wRes.append("Source[" + getSourceName() + "] - Error reading line[" + aLineNum
					+ "] - Msg[" + e.getMessage() + "]");
		}
		return wRes.toString();
	}

	/**
	 * @param aModule
	 * @return
	 */
	public boolean hasModule(CXJsModule aModule) {
		return getIdx(aModule) != -1;
	}

	/**
	 * @return
	 */
	public boolean hasModules() {
		return pModules != null && !pModules.isEmpty();
	}

	/**
	 * @param aClearSrc
	 */
	protected void initMainReload(boolean aClearSrc) {
		pSourcesNbLines = 0;
		pMergeStartLine = 0;
		pLoaded = false;
		pParent = null;
		// Suppression des liaison pour les modules lies au main
		if (pModules != null) {
			for (CXJsModule xModule : pModules) {
				xModule.initMainReload(true);
			}
			pModules.clear();
			pModules = null;
		}
		if (aClearSrc) {
			pSources = null;
			pSourcesNoInclude = null;
		}
		// Element conserves
		// -> pRoot
		// -> pParentDir
	}

	/**
	 * @return
	 */
	public boolean isLoaded() {
		return pLoaded;
	}

	/**
	 * @return
	 */
	public boolean isMain() {
		return isRoot();
	}

	/**
	 * @param aModule
	 * @return
	 */
	public boolean isMe(CXJsModule aModule) {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isRoot() {
		return pParent == null;
	}

	/**
	 * Renvoie le nombre total de lignes - Repertoire qui contient le soure ->
	 * Null si root ou source non lu dans un fichier
	 * 
	 * @throws CXJsExcepLoad
	 */
	public void load() throws CXJsExcepLoad {
		boolean wHasInclude = false;
		StringBuilder wSB = new StringBuilder();
		if (pModules != null) {
			// Pour reload du main
			pModules.clear();
			pModules = null;
			pLoaded = false;
		}
		pSourcesNbLines = 0;
		BufferedReader wReader = new BufferedReader(new StringReader(pSources));
		try {
			if (wReader.ready()) {
				String wLine;
				while ((wLine = wReader.readLine()) != null) {
					pSourcesNbLines++;
					boolean wInclude = false;
					if (wLine.length() > INCLUDE_START.length()) {
						int i = 0;
						while (i < wLine.length()) {
							char wChar = wLine.charAt(i);
							// Prend en compte #include ou #include
							if (wChar != ' ' && wChar != '\t' && wChar != '/') {
								break;
							} else {
								i++;
							}
						}
						String wTrim = i == 0 ? wLine : wLine.substring(i);
						wInclude = wTrim.startsWith(INCLUDE_START);
					}
					if (wInclude) {
						if (!wHasInclude) {
							wHasInclude = true;
						}
						// Si pas de merge on commente l'include
						boolean wCommentInclude = true;
						// Module a charger
						// CXJsModule wModule=loadNewModule(extractPath(wLine),
						// aParentDir);
						CXJsModule wModule = CXJsModule.loadNewModule(this, extractPath(wLine),
								wLine);
						if (wModule.getPath() != null) {
							// Le module est identifie par sont pathpar rapport
							// au route des scripts
							if (!pRoot.loadModuleExists(wModule.getPath())) {
								if (!hasModule(wModule) && !isMe(wModule)) {
									if (traceDebugOn()) {
										System.out.println(getSourceName() + " - Add ["
												+ wModule.getSourceName() + "]");
									}
									// Ajout en 1er pour recursivite
									pRoot.loadModuleAdd(wModule);
									wModule.load();
								} else {
									wCommentInclude = true;
								}
							} else {
								wCommentInclude = true;
							}
							if (pModules == null) {
								pModules = new ArrayList<CXJsModule>();
							}
							if (!isMe(wModule) && getIdx(wModule) == -1) {
								pModules.add(wModule);
							}
						} else {
							wCommentInclude = true;
						}
						if (wCommentInclude) {
							wSB.append("//").append(wLine).append('\n');
						}
					} else {
						wSB.append(wLine).append('\n');
					}
				}
				pLoaded = true;
			}
		} catch (CXJsExcepLoad e) {
			throw (e);
		} catch (Exception e) {
			loadThrowExcep(getRoot(), e);
		}
		if (wHasInclude) {
			pSourcesNoInclude = wSB.toString();
		} else {
			pSourcesNoInclude = pSources;
		}
	}

	/**
	 * @param aMain
	 * @param e
	 * @throws CXJsExcepLoad
	 */
	protected void loadThrowExcep(CXJsSourceMain aMain, Throwable e) throws CXJsExcepLoad {
		throw new CXJsExcepLoad(aMain, e, "Error reading source '" + getSourceName() + "'");
	}

	/**
	 * @param aSb
	 * @param aStartLine
	 * @return
	 */
	protected int merge(StringBuilder aSb, int aStartLine) {
		pMergeStartLine = aStartLine;
		if (aSb.length() != 0 && aSb.charAt(aSb.length() - 1) != '\n') {
			aSb.append('\n');
		}
		aSb.append(getSourcesNoInclude());
		if (traceDebugOn()) {
			System.out.println(getSourceName() + " - Start " + pMergeStartLine + " - Stop "
					+ (pMergeStartLine + pSourcesNbLines));
		}
		return pMergeStartLine + pSourcesNbLines;
	}

	/**
	 * @param aSources
	 */
	protected void setSource(String aSources) {
		pSources = aSources;
	}

	/**
	 * @param aSrcRootDir
	 */
	protected void setSrcRootDir(CXRsrcUriDir aSrcRootDir) {
		pSrcRootDir = aSrcRootDir;
	}

	/**
	 * @return
	 */
	protected boolean traceDebugOn() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsObjectBase#addDescriptionInBuffer(
	 * java.lang.Appendable)
	 */
	/**
	 * @param aSB
	 * @param aLevel
	 * @return
	 */
	public StringBuilder treeToBuilder(StringBuilder aSB, int aLevel) {
		for (int i = 0; i < aLevel; i++) {
			aSB.append('+');
		}
		if (aLevel != 0) {
			aSB.append(' ');
		}
		aSB.append(getSourceName()).append('\n');
		CXJsModule wMod = getFirstChild();
		if (wMod != null) {
			wMod.treeToBuilder(aSB, aLevel + 1);
		}
		wMod = getNextSibling();
		if (wMod != null) {
			wMod.treeToBuilder(aSB, aLevel);
		}
		return aSB;
	}

	/**
	 * @return
	 */
	public String treeToString() {
		return treeToBuilder(new StringBuilder(), 0).toString();
	}
}
