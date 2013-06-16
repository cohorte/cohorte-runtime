package org.psem2m.utilities.scripting;

import java.util.LinkedList;

import org.psem2m.utilities.rsrc.CXRsrcText;
import org.psem2m.utilities.rsrc.CXRsrcUriDir;
import org.psem2m.utilities.rsrc.CXRsrcUriPath;

/**
 * Represente un source JS
 * 
 * @author ogattaz
 * 
 */
public class CXJsModule extends CXJsSource {

	private final static String DIR_CURRENT = "./";
	private final static String DIR_PARENT = "../";

	protected static CXJsModule loadNewModule(CXJsSource aParent, String aRelPath, String aInclude)
			throws Exception {
		if (aRelPath == null || aRelPath.isEmpty()) {
			throw new Exception("Bad include syntax[" + aInclude + "] - ParentDir["
					+ aParent.getSrcRootDir().getPath() + "]");
		}
		String wRelPath = aRelPath.replace(CXRsrcUriDir.BAD_SEPARATOR, CXRsrcUriDir.SEPARATOR);
		CXRsrcUriDir wSrcRootDir = aParent.getSrcRootDir();
		if (wRelPath.startsWith(DIR_PARENT)) {
			do {
				if (wSrcRootDir != null) {
					if (!wSrcRootDir.isEmpty()) {
						wRelPath = wRelPath.substring(DIR_PARENT.length());
						wSrcRootDir = wSrcRootDir.getParent();
					} else {
						wSrcRootDir = null;
					}
				}
			} while (wSrcRootDir != null && wRelPath.startsWith(DIR_PARENT));
			if (wSrcRootDir == null) {
				throw new Exception("Bad include[" + aInclude + " - ParentDir["
						+ aParent.getSrcRootDir().getPath() + "]");
			}
		} else {
			if (wRelPath.startsWith(DIR_CURRENT)) {
				wRelPath = wRelPath.substring(DIR_CURRENT.length());
			}
		}
		return new CXJsModule(aParent, wSrcRootDir, wRelPath, aInclude);
	}

	// Contenu de l'instruction include - POur message erreur
	private final String pInclude;
	// Path relatif du module par rapport au root des scripts
	private final CXRsrcUriPath pPath;
	// Ressource
	private CXRsrcText pRsrc;

	/**
	 * @param aParent
	 * @param aParentDir
	 * @param aRelPath
	 * @param aInclude
	 */
	private CXJsModule(CXJsSource aParent, CXRsrcUriDir aParentDir, String aRelPath, String aInclude) {
		super(aParent);
		pPath = new CXRsrcUriPath(aParentDir, aRelPath);
		setSrcRootDir(pPath.getParent());
		pInclude = aInclude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsSource#addDescriptionInBuffer(java
	 * .lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = aSB == null ? new StringBuilder(1024) : aSB;
		descrAddProp(aSB, "Path", getPath());
		return super.addDescriptionInBuffer(aSB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CXJsModule)) {
			return false;
		}
		CXJsModule oMod = (CXJsModule) o;
		return oMod.pPath.equals(pPath);
	}

	/**
	 * @param o
	 * @return
	 */
	public boolean equalsIgnoreCase(Object o) {
		if (o == null || !(o instanceof CXJsModule)) {
			return false;
		}
		CXJsModule oMod = (CXJsModule) o;
		return oMod.pPath.equalsIgnoreCase(pPath);
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return pPath.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.CXJsSource#getNextSibling()
	 */
	@Override
	protected CXJsModule getNextSibling() {
		CXJsSource wParent = getParent();
		if (wParent == null) {
			return null;
		}
		return wParent.getNext(this);
	}

	/**
	 * @return
	 */
	public String getPath() {
		return pPath.getFullPath();
	}

	/**
	 * @return
	 */
	public CXRsrcText getRsrc() {
		return pRsrc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.CXJsSource#getSourceName()
	 */
	@Override
	public String getSourceName() {
		return getFileName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.CXJsSource#isMain()
	 */
	@Override
	public boolean isMain() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsSource#isMe(org.psem2m.utilities.scripting
	 * .CXJsModule)
	 */
	@Override
	public boolean isMe(CXJsModule aModule) {
		return aModule != null && aModule.equals(this);
	}

	/*
	 * Renvoie le nombre total de lignes
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.CXJsSource#load()
	 */
	@Override
	public void load() throws CXJsExcepLoad {
		try {
			if (pPath.hasName()) {
				pRsrc = getRoot().getRsrcProvider().rsrcReadTxt(pPath);
				super.setSource(pRsrc.getContent());
				super.load();
			}
		} catch (CXJsExcepLoad e) {
			throw e;
		} catch (Exception e) {
			loadThrowExcep(getRoot(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsSource#loadThrowExcep(org.psem2m.utilities
	 * .scripting.CXJsSourceMain, java.lang.Throwable)
	 */
	@Override
	protected void loadThrowExcep(CXJsSourceMain aMain, Throwable e) throws CXJsExcepLoad {
		throw new CXJsExcepLoad(aMain, e, "Error reading module " + getFileName() + " - Include["
				+ pInclude + "]");
	}

	/**
	 * @param aOrderedList
	 * @return
	 */
	private String orderedList2Str(LinkedList<CXJsModule> aOrderedList) {
		StringBuilder wSB = new StringBuilder();
		int wIdx = 0;
		for (CXJsModule xMod : aOrderedList) {
			wSB.append("[").append(wIdx++).append("]").append(xMod.getFileName()).append('\n');
		}
		return wSB.toString();
	}

	/**
	 * UNiquement sir load si !pMergeIncludes
	 * 
	 * Empty If Statement finds instances where a condition is checked but
	 * nothing is done about it.
	 * 
	 * @param aOrderedList
	 * @param aParentIdx
	 * @throws CXJsExcepLoad
	 */
	protected void orderIncludes(LinkedList<CXJsModule> aOrderedList, int aParentIdx)
			throws CXJsExcepLoad {
		int wMyIdx = aOrderedList.indexOf(this);
		if (aParentIdx < 0) {
			if (wMyIdx == -1) {
				aOrderedList.addLast(this);
				wMyIdx = aOrderedList.indexOf(this);
			}
		} else {
			if (wMyIdx == -1) {
				aOrderedList.add(aParentIdx, this);
				wMyIdx = aOrderedList.indexOf(this);
				aParentIdx++;
			} else if (wMyIdx >= aParentIdx) {
				// Erreur dans les dependances entre modules
				throw new CXJsExcepLoad(getRoot(), "Include error in module["
						+ getParent().getSourceName() + "] - Circular reference - Include["
						+ pInclude + "] - Stack[" + getIncludeStack() + "]", "resolvingIncludes");
			}
			// si wMyIdx < aParentIdx alors Ordre Ok
		}
		if (traceDebugOn()) {
			System.out.println("Current : " + getSourceName() + " idx[" + wMyIdx + "]");
			System.out.println("  Parent : " + getParent().getSourceName() + " idx[" + aParentIdx
					+ "]");
		}
		CXJsModule wModule = getFirstChild();
		if (wModule != null) {
			wModule.orderIncludes(aOrderedList, wMyIdx);
		}
		wModule = getNextSibling();
		if (wModule != null) {
			wModule.orderIncludes(aOrderedList, aParentIdx);
		}
		if (traceDebugOn()) {
			System.out.println(orderedList2Str(aOrderedList));
		}
	}
}
