package org.psem2m.utilities.rsrc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.scripting.CXJsObjectBase;


// Classe - Path d'un URL qui pointe vers un directctory
// -IMMUTABLE -

public class CXRsrcUriDir extends CXJsObjectBase implements Cloneable {

	// Constantes

	public final static String SEPARATORS = "/\\";
	public final static char SEPARATOR = '/';
	public final static String SEPARATOR_STR = "/";
	public final static char BAD_SEPARATOR = '\\';

	// Privates

	private String pPath = null;
	private String[] pPathArray = null;
	private boolean pStartWithSep = true;
	private boolean pEndWithSep = true;

	// GConstructeurs

	public CXRsrcUriDir() {
		this((String) null, true, true);
	}

	public CXRsrcUriDir(String aPath) {
		this(aPath, true, true);
	}

	public CXRsrcUriDir(String aPath, boolean aStartWithSep, boolean aEndWithSep) {
		pStartWithSep = aStartWithSep;
		pEndWithSep = aEndWithSep;
		initPath(aPath);
	}

	public CXRsrcUriDir(CXRsrcUriDir aPath) {
		super();
		if (aPath != null && aPath.isValid()) {
			pPath = aPath.pPath;
			pPathArray = Arrays.copyOf(aPath.pPathArray,
					aPath.pPathArray.length);
			pStartWithSep = aPath.pStartWithSep;
			pEndWithSep = aPath.pEndWithSep;
		} else
			pPath = EMPTY_STR;
	}

	@Override
	public CXRsrcUriDir clone() {
		return new CXRsrcUriDir(this);
	}

	private void initPath(String aPath) {
		ArrayList<String> wArray = new ArrayList<String>();
		String wResult;
		if (aPath == null)
			wResult = EMPTY_STR;
		else
			wResult = aPath.trim();
		if (wResult.length() != 0) {
			StringBuilder wTmp = new StringBuilder();
			if (pStartWithSep)
				wTmp.append(SEPARATOR);
			boolean wAddSep = false;
			StringTokenizer wTok = new StringTokenizer(wResult, SEPARATORS,
					false);
			while (wTok.hasMoreTokens()) {
				String wElmt = wTok.nextToken();
				if (wElmt.length() != 0) {
					if (wAddSep)
						wTmp.append(SEPARATOR);
					else
						wAddSep = true;
					wTmp.append(wElmt);
					wArray.add(wElmt);
				}
			}
			if (pEndWithSep) {
				int wLen = wTmp.length();
				if (wLen == 0
						|| (wLen != 0 && wTmp.charAt(wLen - 1) != SEPARATOR))
					wTmp.append(SEPARATOR);
			}
			wResult = wTmp.toString();
		} else if (pEndWithSep || pStartWithSep)
			wResult = SEPARATOR_STR;
		pPath = wResult;
		pPathArray = wArray.toArray(new String[wArray.size()]);
	}

	// Get and set

	public boolean isValid() {
		return pPath != null;
	}

	public boolean isEmpty() {
		return pPath != null
				&& (pPath.length() == 0 || (pPath.length() == 1 && pPath
						.charAt(0) == getSepChar()));
	}

	public boolean isSubDirOf(CXRsrcUriDir aDir, boolean aIgnoreCase) {
		return isSubDirOf(aDir, 0, aIgnoreCase);
	}

	public boolean isSubDirOf(CXRsrcUriDir aDir, int aStartFrom,
			boolean aIgnoreCase) {
		if (aDir == null || aDir.isEmpty()
				|| aDir.pPathArray.length >= this.pPathArray.length
				|| aStartFrom < 0)
			return false;
		for (int i = 0; i < aDir.pPathArray.length; i++) {
			if (!(aIgnoreCase ? aDir.pPathArray[i]
					.equalsIgnoreCase(this.pPathArray[aStartFrom + i])
					: aDir.pPathArray[i]
							.equals(this.pPathArray[aStartFrom + i])))
				return false;
		}
		return true;
	}

	public boolean startsWith(CXRsrcUriDir aDir, boolean aIgnoreCase) {
		if (aDir == null || !aDir.isValid()
				|| aDir.pPathArray.length > this.pPathArray.length)
			return false;
		int wStartIdx = 0;
		for (int i = 0; i < aDir.pPathArray.length; i++) {
			wStartIdx = this
					.indexOf(aDir.pPathArray[i], wStartIdx, aIgnoreCase);
			if (wStartIdx == -1)
				return false;
			wStartIdx++;
		}
		return true;
	}

	public int indexOf(String aDirName, boolean aIgnoreCase) {
		return indexOf(aDirName, 0, aIgnoreCase);
	}

	public int indexOf(String aDirName, int aFromIdx, boolean aIgnoreCase) {
		if (aDirName == null || this.pPathArray.length == 0 || aFromIdx < 0
				|| aFromIdx >= pPathArray.length)
			return -1;
		for (int i = aFromIdx; i < pPathArray.length; i++) {
			if (aIgnoreCase ? pPathArray[i].equalsIgnoreCase(aDirName)
					: pPathArray[i].equals(aDirName))
				return i;
		}
		return -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || pPath == null)
			return false;
		if (obj instanceof String)
			return pPath.equals(obj);
		if (obj instanceof CXRsrcUriDir)
			return pPath.equals(((CXRsrcUriDir) obj).pPath);
		return false;
	}

	public boolean equalsIgnoreCase(Object obj) {
		if (obj == null || pPath == null)
			return false;
		if (obj instanceof String)
			return pPath.equalsIgnoreCase((String) obj);
		if (obj instanceof CXRsrcUriDir)
			return pPath.equalsIgnoreCase(((CXRsrcUriDir) obj).pPath);
		return false;
	}

	// Ajoute this e la ressource aToAppend
	public CXRsrcUriPath concat(CXRsrcUriPath aToAppend) {
		return new CXRsrcUriPath(this, aToAppend);
	}

	// Renvoie une nouvelle instance de CXtdRsrcUrlDir - this + aToAppend
	// Copie car immutable
	public CXRsrcUriDir concat(String aToAppend) {
		if (aToAppend != null && aToAppend.length() != 0)
			return concat(newRsrcUriDir(aToAppend));
		else
			return this.clone();
	}

	// Renvoie une nouvelle instance de CXtdRsrcUrlDir - this + aToAppend
	// Copie car immutable
	public CXRsrcUriDir concat(CXRsrcUriDir aToAppend) {
		if (aToAppend != null && !aToAppend.isEmpty())
			return newRsrcUriDir(concatToAsString(aToAppend));
		else
			return this.clone();
	}

	public CXRsrcUriDir concat(CXFileDir aToAppend) {
		if (aToAppend != null)
			return concat(aToAppend.getAbsolutePath());
		else
			return this.clone();
	}

	// Renvoie le path de this + aToAppend
	private String concatToAsString(CXRsrcUriDir aToAppend) {
		if (aToAppend == null || aToAppend.isEmpty() || !aToAppend.isValid())
			return getPath();
		else if (this.isEmpty() || !this.isValid())
			return aToAppend.getPath();
		else {
			StringBuilder wTmp = new StringBuilder(pPath == null ? EMPTY_STR
					: pPath);
			if (this.pEndWithSep) {
				if (aToAppend.pStartWithSep)
					wTmp.append(aToAppend.getPath().substring(1));
				else
					wTmp.append(aToAppend.getPath());
			} else {
				if (aToAppend.pStartWithSep)
					wTmp.append(aToAppend.getPath());
				else
					wTmp.append(SEPARATOR).append(aToAppend.getPath());
			}
			return wTmp.toString();
		}
	}

	private CXRsrcUriDir newRsrcUriDir(String aPath) {
		return new CXRsrcUriDir(aPath, pStartWithSep, pEndWithSep);
	}

	// renvoie le path tel qu'il est specifie par startWithSeparator et
	// endWithSeparator
	public String getPath() {
		return pPath;
	}

	// renvoie le path tel qu'il est specifie par startWithSeparator et
	// endWithSeparator
	public String getPath(boolean aForceStartSep, boolean aForceEndSep) {
		if (isEmpty()) {
			return aForceStartSep || aForceEndSep ? SEPARATOR_STR : "";
		}
		StringBuilder wSB = new StringBuilder();
		if (aForceStartSep) {
			if (!pStartWithSep)
				wSB.append(SEPARATOR);
			wSB.append(pPath);
		} else if (pPath != null && !pPath.isEmpty()) {
			if (pStartWithSep)
				wSB.append(pPath.substring(1));
			else
				wSB.append(pPath);
		}
		if (aForceEndSep) {
			if (!pEndWithSep)
				wSB.append(SEPARATOR);
		} else if (wSB.length() > 0) {
			if (pEndWithSep)
				return wSB.substring(0, wSB.length() - 1);
		}
		return wSB.toString();
	}

	// Renvoie le nieme element du path
	public String getPathPart(int aIdx) {
		return aIdx >= pPathArray.length || aIdx < 0 ? null : pPathArray[aIdx];
	}

	// Renvoie le path e partir de la aIdx partie inclue
	public CXRsrcUriDir getPathFromPart(int aIdx) {
		if (aIdx <= 0)
			return this.clone();
		if (aIdx > pPathArray.length)
			return newRsrcUriDir(null);
		StringBuilder wSB = new StringBuilder();
		for (int i = aIdx; i < pPathArray.length; i++) {
			wSB.append(SEPARATOR).append(pPathArray[i]);
		}
		return newRsrcUriDir(wSB.toString());
	}

	// Renvoie l'index de aUrlPart /xtend/data/remote -> renvoie 2 pour remote
	public int getFirstPartIdx(String aUrlPart) {
		if (aUrlPart == null || aUrlPart.isEmpty())
			return -1;
		for (int i = 0; i < pPathArray.length; i++) {
			if (aUrlPart.equals(pPathArray[i]))
				return i;
		}
		return -1;
	}

	// Renvoie le nombre d'elements du path
	public int getNbPart() {
		return pPathArray.length;
	}

	// Revoie getPath avec un '/' en debut et un '/' en fin sauf si vide '/'
	public String getUrlPath() {
		if (isValid()) {
			if (isEmpty())
				return SEPARATOR_STR;
			else if (pStartWithSep && pEndWithSep)
				return pPath;
			else {
				StringBuilder wTmp = new StringBuilder();
				if (!pStartWithSep)
					wTmp.append(SEPARATOR);
				wTmp.append(pPath);
				if (!pEndWithSep)
					wTmp.append(SEPARATOR);
				return wTmp.toString();
			}
		} else
			return "";
	}

	public CXRsrcUriDir getParent() {
		// FDB - Fiche 65417
		if (!isValid() || isEmpty() || getNbPart() <= 1)
			return newRsrcUriDir(null);
		StringBuilder wSB = new StringBuilder();
		for (int i = 0; i < pPathArray.length - 1; i++) {
			wSB.append(SEPARATOR).append(pPathArray[i]);
		}
		return newRsrcUriDir(wSB.toString());
	}

	// Si a/b/c -> Renvoie c - null sinon
	public String getParentDirName() {
		return getNbPart() > 1 ? getPathPart(getNbPart() - 1) : null;
	}

	// Revoie getPath sans '/' en debut et un '/' en fin
	public String getUrlRelativePath() {
		if (isValid()) {
			if (isEmpty())
				return EMPTY_STR;
			else {
				StringBuilder wTmp = new StringBuilder();
				if (pStartWithSep)
					wTmp.append(pPath.substring(1));
				else
					wTmp.append(pPath);
				if (!pEndWithSep)
					wTmp.append(SEPARATOR);
				return wTmp.toString();
			}
		} else
			return EMPTY_STR;
	}

	// Revoie le path de la ressource aResourceId Dir/SubDir/resource.xml
	public String getUrlRelativePath(String aResourceId) {
		return new StringBuilder(getUrlRelativePath()).append(
				aResourceId == null ? "" : aResourceId).toString();
	}

	// Revoie le path de la ressource aResourceId /Dir/SubDir/resource.xml (avec
	// ou sans parametres &param=xx)
	public String getUrlPath(String aResourceId) {
		return new StringBuilder(getUrlPath()).append(
				aResourceId == null ? "" : aResourceId).toString();
	}

	public char getSepChar() {
		return SEPARATOR;
	}

	public char getBadSepChar() {
		return BAD_SEPARATOR;
	}

	public String[] getPartsArray() {
		return getPartsArray(true);
	}

	// aCopie=true pour que l'objet reste immutable - False pour besoins
	// specifiques
	protected String[] getPartsArray(boolean aCopie) {
		return pPathArray == null ? null : aCopie ? Arrays.copyOf(pPathArray,
				pPathArray.length) : pPathArray;
	}

	// Interface IXtdDescriber

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddLine(aSB, "Path", pPath == null ? DESCR_NONE : pPath);
		return aSB;
	}
}
