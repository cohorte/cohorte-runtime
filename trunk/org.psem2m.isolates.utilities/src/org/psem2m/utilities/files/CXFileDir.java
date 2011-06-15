package org.psem2m.utilities.files;

//
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * Classe de gestion de r�pertoies !! MonoThread
 */
public class CXFileDir extends CXFileBase implements IXFilesContainer {
	private static final long serialVersionUID = 3257562910606570550L;

	// 16j_103
	private static CXFileDir sTempDir = new CXFileDir(getTempAbsolutePath());

	// 16j_102 - mise en place de la classe CXFileFilterName
	private static CXFileDir sUserDir = new CXFileDir(
			System.getProperty("user.dir"));

	/**
	 * 16w_104 - suppression des ajout syst�matique d'un "separatorChar" en fin
	 * de path d'un Dir !
	 * 
	 * @param aPath
	 * @return
	 */
	public static String checkPath(String aPath) {
		if (aPath == null)
			return null;
		if (aPath.length() == 0)
			return aPath;
		aPath = checkSeparator(aPath);
		int wIdxLastChar = aPath.length() - 1;

		if (aPath.charAt(wIdxLastChar) == separatorChar)
			aPath = aPath.substring(0, wIdxLastChar);

		return aPath;

		// String wTemp = checkSeparator(aPath);
		// if (!wTemp.endsWith(separator))
		// return wTemp + separatorChar;
		// else
		// return wTemp;
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListExt
	 * @return
	 */
	public static FileFilter getExcluderExtension(String aListExt) {
		return getExcluderExtension(aListExt, null);
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListExt
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderExtension(String aListExt,
			FileFilter aSubFileFilter) {
		return new CXFileFilterExtension(aListExt, aSubFileFilter,
				!CXFileFilter.INCLUDE);
	}

	// 16w_104 - suppression des ajout syst�matique d'un "separatorChar" en fin
	// de path d'un Dir !
	// /**
	// * Ajout d'un s�parateur en fin du getPath()
	// * Le getPath() de File ne renvoie pas de sp�rateur en fin
	// */
	// public String getPath()
	// {
	// String wTemp = super.getAbsolutePath();
	// if (!wTemp.endsWith(separator))
	// return wTemp + separatorChar;
	// else
	// return wTemp;
	// }

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListRegExp
	 * @return
	 */
	public static FileFilter getExcluderName(String aListRegExp) {
		return getExcluderName(aListRegExp, null);
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListRegExp
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderName(String aListRegExp,
			FileFilter aSubFileFilter) {
		return new CXFileFilterName(aListRegExp, aSubFileFilter,
				!CXFileFilter.INCLUDE);
	}

	/**
	 * 16w_104 - Fiche 50792 - Admin - Erreur de detection du fonctionnement de
	 * tomcat
	 * 
	 * @param aListSubPath
	 * @return
	 */
	public static FileFilter getExcluderSubPath(String aListSubPath) {
		return getExcluderSubPath(aListSubPath, null);
	}

	/**
	 * 16w_104 - Fiche 50792 - Admin - Erreur de detection du fonctionnement de
	 * tomcat
	 * 
	 * @param aListSubPath
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getExcluderSubPath(String aListSubPath,
			FileFilter aSubFileFilter) {
		return new CXFileFilterSubPath(aListSubPath, aSubFileFilter,
				!CXFileFilter.INCLUDE);
	}

	/**
	 * 16j_102
	 * 
	 * @return
	 */
	public static FileFilter getExcluderSvn() {
		return getExcluderSvn(null);
	}

	public static FileFilter getExcluderSvn(FileFilter aSubFileFilter) {
		return new CXFileFilterSvn(aSubFileFilter, !CXFileFilter.INCLUDE);
	}

	/**
	 * @param aListExt
	 * @return
	 */
	public static FileFilter getFilterExtension(String aListExt) {
		return getFilterExtension(aListExt, null);
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListExt
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterExtension(String aListExt,
			FileFilter aSubFileFilter) {
		return new CXFileFilterExtension(aListExt, aSubFileFilter,
				CXFileFilter.INCLUDE);
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListRegExp
	 * @return
	 */
	public static FileFilter getFilterName(String aListRegExp) {
		return getFilterName(aListRegExp, null);
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @param aListRegExp
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterName(String aListRegExp,
			FileFilter aSubFileFilter) {
		return new CXFileFilterName(aListRegExp, aSubFileFilter,
				CXFileFilter.INCLUDE);
	}

	/**
	 * 16w_104 - Fiche 50792 - Admin - Erreur de detection du fonctionnement de
	 * tomcat
	 * 
	 * @param aListSubPath
	 * @return
	 */
	public static FileFilter getFilterSubPath(String aListSubPath) {
		return getFilterSubPath(aListSubPath, null);
	}

	/**
	 * 16w_104 - Fiche 50792 - Admin - Erreur de detection du fonctionnement de
	 * tomcat
	 * 
	 * @param aListSubPath
	 * @param aSubFileFilter
	 * @return
	 */
	public static FileFilter getFilterSubPath(String aListSubPath,
			FileFilter aSubFileFilter) {
		return new CXFileFilterSubPath(aListSubPath, aSubFileFilter,
				CXFileFilter.INCLUDE);
	}

	public static FileFilter getFilterSvn() {
		return getFilterSvn(null);
	}

	public static FileFilter getFilterSvn(FileFilter aSubFileFilter) {
		return new CXFileFilterSvn(aSubFileFilter, CXFileFilter.INCLUDE);
	}

	// 16j_103
	private static String getTempAbsolutePath() {
		String wPath = null;
		try {
			File wTempFile = File.createTempFile("tmp", "txt");
			wPath = wTempFile.getParent();
			wTempFile.delete();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return wPath;
	}

	// 16j_103
	public static CXFileDir getTempDir() {
		return sTempDir;
	}

	/**
	 * 16j_102 - mise en place de la classe CXFileFilterName
	 * 
	 * @return
	 */
	public static CXFileDir getUserDir() {
		return sUserDir;
	}

	// FDB - Fiche 50430
	public static CXFileDir newInstance(CXFileDir aRoot, Object... aArgs) {
		aArgs = aArgs == null || aArgs.length == 0 ? null : aArgs;
		StringBuilder wSB = new StringBuilder();
		if (aArgs != null && aArgs.length != 0) {
			for (Object xObj : aArgs) {
				String wPath = CXStringUtils.strFullTrim(xObj != null ? xObj
						.toString() : null);
				if (wPath != null && !wPath.isEmpty()) {
					char wFirst = wPath.charAt(0);
					if (wFirst != '/' && wFirst != '\\')
						wSB.append(separatorChar);
					wSB.append(wPath);
				}
			}
		}
		return aRoot == null ? new CXFileDir(wSB.toString()) : new CXFileDir(
				aRoot, wSB.toString());
	}

	// FDB - Fiche 50430
	public static CXFileDir newInstance(Object... aArgs) {
		return newInstance(null, aArgs);
	}

	/**
	 * JAVA EXCEPTION : Impossible de creer le repertoire
	 * 
	 * Waits for NFS to propagate a file creation, imposing a timeout.
	 * 
	 * @param file
	 *            The file
	 * @param seconds
	 *            The maximum time in seconds to wait.
	 * @return True if file exists.
	 * 
	 *         Needs a clearer javadoc to see its real purpose for someone
	 *         without NFS-knowledge.
	 */
	private static boolean waitFor(File file, int seconds) {
		int timeout = 0;
		int tick = 0;
		while (!file.exists()) {
			if (tick++ >= 10) {
				tick = 0;
				if (timeout++ > seconds) {
					return false;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
			} catch (Exception ex) {
				break;
			}
		}
		return true;
	}

	private IXDirScanListener pDirScanListener = null;

	private int pScanLevel = 0;

	/**
	 * @param aParentDir
	 * @param aSubDir
	 */
	public CXFileDir(CXFileDir aParentDir, String aSubDir) {
		super(aParentDir, checkPath(aSubDir));
	}

	/**
	 * @param aFullPath
	 */
	public CXFileDir(String aFullPath) {
		super(checkPath(aFullPath));
	}

	/**
	 * @param aParentDir
	 * @param aSubDir
	 */
	public CXFileDir(String aParentDir, String aSubDir) {
		super(checkPath(aParentDir), checkPath(aSubDir));
	}

	/**
	 * 
	 * Supprime seulement le contenu du repertoire - Recursif
	 * 
	 * @return
	 * @throws Exception
	 */
	public int clean() throws Exception {
		return clean(null);
	}

	/**
	 * 
	 * 
	 * @param aFileFilter
	 * @return
	 * @throws Exception
	 */
	public int clean(FileFilter aFileFilter) throws Exception {
		return remove(false, aFileFilter);
	}

	/**
	 * Copie tous le contenu du r�pertoire courant --> aDstDir - Recursif
	 * 
	 * @param aDstDir
	 *            le dossier de destination : le cr�e s'il n'existe pas, le
	 *            supprime s'il existe
	 * @return
	 * @throws Exception
	 */
	public int copyTo(CXFileDir aDstDir) throws Exception {
		aDstDir.remove();
		return copyTo(aDstDir, null, true, true);
	}

	/**
	 * 16j_000 - Int�gration interpr�teur deamon
	 * 
	 * Copie le contenu du r�pertoire courant --> aDstDir - Recursif
	 * 
	 * @param aDstDir
	 *            le dossier de destination : le cr�e s'il n'existe pas,
	 * @param aDeleteExinstingContent
	 *            si vrai, supprime le dossier de destination s'il existe
	 * @return
	 * @throws Exception
	 */
	public int copyTo(CXFileDir aDstDir, boolean aDeleteExinstingContent)
			throws Exception {
		if (aDeleteExinstingContent)
			aDstDir.remove();
		return copyTo(aDstDir, null, true, true);
	}

	/**
	 * Copie le contenu du r�pertoire courant --> aDstDir - Recursif
	 * 
	 * @param aDstDir
	 *            le dossier de destination : le cr�e s'il n'existe pas, le
	 *            supprime s'il existe
	 * 
	 * @param aFilter
	 *            --> Filtre sur les fichiers � copier - "ext,ext,..."
	 * @return
	 * @throws Exception
	 */
	public int copyTo(CXFileDir aDstDir, FileFilter aFilter) throws Exception {
		aDstDir.remove();
		return copyTo(aDstDir, aFilter, true, true);
	}

	/**
	 * Copie du contenu du r�pertoire courant --> aDstDir - Recursif Ne cr�e pas
	 * les r�pertoires vides
	 * 
	 * @param aFilter
	 *            --> Filtre sur les fichiers � copier - "ext,ext,..."
	 * @param aFilter
	 *            =null --> Aucun filtre sur les fichiers � copier
	 * @param aDeleteExinstingFiles
	 *            =true --> Ecrase les fichier existants ou pas
	 * @param aCreateEmptyDir
	 *            =true --> Cr�e les r�pertoires qui ne contiennent aucun
	 *            fichier
	 * @param aCreateEmptyDir
	 *            =false --> Ne cr�e que les r�pertoires qui contienet des
	 *            fichiers
	 * @return le nombre de fichiers copi�s et r�pertoires cr��s
	 */
	public int copyTo(CXFileDir aDstDir, FileFilter aFilter,
			boolean aDeleteExinstingFiles, boolean aCreateEmptyDir)
			throws Exception {
		if (!exists())
			return 0;

		if (!aDstDir.exists())
			aDstDir.mkdirs();

		int wRes = 0;
		Iterator<File> wIt = getMyFiles(aFilter, true).iterator();
		while (wIt.hasNext()) {
			File wFile = wIt.next();
			if (wFile.isFile()) {
				((CXFile) wFile).copyTo(aDstDir, wFile.getName(),
						aDeleteExinstingFiles);
				wRes++;
			} else if (wFile.isDirectory())
				wRes += ((CXFileDir) wFile).copyTo(
						new CXFileDir(aDstDir.getAbsolutePath(), wFile
								.getName()), aFilter, aDeleteExinstingFiles,
						aCreateEmptyDir);
		}
		if (wRes == 0 && !aCreateEmptyDir)
			aDstDir.delete();

		return wRes;
	}

	/**
	 * 16w_103 - Fiche 46397 - JAVA EXCEPTION : Impossible de cr�er le
	 * r�pertoire
	 * 
	 * Cr��e le r�pertoire et son arborescence
	 * 
	 * @return true si un r�pertoire a �t� cr�� false si il existait d�j� ou non
	 *         cr��
	 */
	public boolean createHierarchy() throws Exception {
		if (exists())
			return false;
		boolean wCreated = this.mkdirs();
		// comme NTFS met un peu de temps pour propager les maj dees table
		// d'allocation...
		if (!wCreated)
			wCreated = waitFor(this, 1);
		return wCreated;
	}

	/**
                 */
	public void failIfNotExist() throws Exception {
		if (!exists())
			throw new IOException("Directory not found [" + getAbsolutePath()
					+ "]");
	}

	/**
	 * 14w_009 - Bug 31297 - Mise � jour d'un serveur web utilisation de la
	 * constante WITH_TEXTFILE
	 */
	public ArrayList<File> getMyFiles(FileFilter aFilter, boolean aWithDirs)
			throws Exception {
		return getMyFiles(aFilter, aWithDirs, !WITH_TEXTFILE);
	}

	/**
	 * Renvoie la liste des Fichier et dossiers - Non r�cursif
	 * 
	 * @param aFilter
	 *            --> Filtre sur les fichiers
	 * @param aWithDirs
	 *            --> False --> ignore pas les sous-r�pertoires
	 */
	@Override
	public ArrayList<File> getMyFiles(FileFilter aFilter, boolean aWithDirs,
			boolean aInstanciateTxtFiles) throws Exception {
		failIfNotExist();

		File[] wList;
		if (aFilter == null)
			wList = this.listFiles();
		else
			wList = this.listFiles(aFilter);
		ArrayList<File> wResult = new ArrayList<File>(wList.length);

		for (int wI = 0; wI < wList.length; wI++) {
			File wFile = wList[wI];
			// 16w_104 - suppression du fitre sur les fichiers et dossiers
			// cach�s !
			if (wFile.isFile()) {
				CXFileBase wNewFile = newFile(wFile.getAbsolutePath(),
						aInstanciateTxtFiles);
				// 16j_101
				if (hasScanListener())
					getScanListener().listenOneFile(getScanLevel(),
							wResult.size(), wNewFile);
				wResult.add(wNewFile);
			} else if (aWithDirs && wFile.isDirectory()) {
				CXFileBase wNewFileDir = newFileDir(wFile.getAbsolutePath());
				// 16j_101
				if (hasScanListener())
					getScanListener().listenOneFile(getScanLevel(),
							wResult.size(), wNewFileDir);
				wResult.add(wNewFileDir);
			}

		}
		// System.out.print("getMyFiles : " + this.getAbsolutePath() +
		// " - Count=" + wResult.size()+ "\n");
		return wResult;
	}

	/**
	 * 16j_107 - Fiche 65574 - Ordre de chargement des extends sous Linux renvoi
	 * la liste des fichiers dans l'orde aphabetique
	 */
	public ArrayList<File> getMySortedFiles(FileFilter aFilter,
			boolean aWithDirs) throws Exception {
		ArrayList<File> wList = getMySortedFiles(aFilter, aWithDirs,
				!WITH_TEXTFILE);

		return wList;
	}

	/**
	 * 16j_107 - Fiche 65574 - Ordre de chargement des extends sous Linux
	 * 
	 * Renvoie la liste des Fichier et dossiers dans l'ordre alphabetique- Non
	 * r�cursif
	 * 
	 * @param aFilter
	 *            --> Filtre sur les fichiers
	 * @param aWithDirs
	 *            --> False --> ignore pas les sous-r�pertoires
	 */
	public ArrayList<File> getMySortedFiles(FileFilter aFilter,
			boolean aWithDirs, boolean aInstanciateTxtFiles) throws Exception {
		failIfNotExist();

		File[] wListSorted;

		if (aFilter == null)
			wListSorted = this.listFiles();
		else
			wListSorted = this.listFiles(aFilter);

		Arrays.sort(wListSorted);

		ArrayList<File> wResult = new ArrayList<File>(wListSorted.length);

		for (int wI = 0; wI < wListSorted.length; wI++) {
			File wFile = wListSorted[wI];
			// 16w_104 - suppression du fitre sur les fichiers et dossiers
			// cach�s !
			if (wFile.isFile()) {
				CXFileBase wNewFile = newFile(wFile.getAbsolutePath(),
						aInstanciateTxtFiles);
				// 16j_101
				if (hasScanListener())
					getScanListener().listenOneFile(getScanLevel(),
							wResult.size(), wNewFile);
				wResult.add(wNewFile);
			} else if (aWithDirs && wFile.isDirectory()) {
				CXFileBase wNewFileDir = newFileDir(wFile.getAbsolutePath());
				// 16j_101
				if (hasScanListener())
					getScanListener().listenOneFile(getScanLevel(),
							wResult.size(), wNewFileDir);
				wResult.add(wNewFileDir);
			}

		}
		// System.out.print("getMyFiles : " + this.getAbsolutePath() +
		// " - Count=" + wResult.size()+ "\n");
		return wResult;
	}

	/**
                 */
	public CXFileDir getParentDirectory() throws Exception {
		return new CXFileDir(getParent());
	}

	// 16w_104 - suppression des ajout syst�matique d'un "separatorChar" en fin
	// de path d'un Dir !
	// /**
	// */
	// public static String calcFullPath(String aParentDir, String aSubDir)
	// {
	// String wParent = checkPath(aParentDir);
	// if (aSubDir == null)
	// return aParentDir;
	// else
	// {
	// String wSubDir = checkPath(aSubDir);
	// if (wSubDir.startsWith(separator))
	// return wParent + wSubDir.substring(1, wSubDir.length());
	// else
	// return wParent + wSubDir;
	// }
	// }
	/**
	 * @param aEndWithSep
	 *            Ajout ou non d'un s�parateur en fin du getPath()
	 */
	public String getPath(boolean aEndWithSep) {
		String wTemp = super.getAbsolutePath();
		if (wTemp.endsWith(separator)) {
			if (aEndWithSep)
				return wTemp;
			else
				return wTemp.substring(0, wTemp.length() - 1);
		} else {
			if (aEndWithSep)
				return wTemp + separatorChar;
			else
				return wTemp;
		}
	}

	// 16j_101
	private int getScanLevel() {
		return pScanLevel;
	}

	// 16j_101
	private IXDirScanListener getScanListener() {
		return pDirScanListener;
	}

	// 16j_101
	private boolean hasScanListener() {
		return pDirScanListener != null;
	}

	/**
	 * True si le r�pertoie est vide
	 */
	public boolean isEmpty() {
		return this.listFiles().length == 0;
	}

	public boolean isMetaInf() {
		return this.getName().equals(META_INF);
	}

	// 16j_101
	private boolean isScanLevelZero() {
		return getScanLevel() == 0;
	}

	/**
	 * Supprime aDstDir D�place le r�pertoire courant --> aDstDir - Recursif
	 */
	public int moveTo(CXFileDir aDstDir) throws Exception {
		aDstDir.remove();
		return moveTo(aDstDir, null, true, true);
	}

	/**
	 * Supprime aDstDir D�place le r�pertoire courant --> aDstDir - Recursif
	 * 
	 * @param aFilter
	 *            --> Filtre sur les fichiers � copier - "ext,ext,..."
	 */
	public int moveTo(CXFileDir aDstDir, FileFilter aFilter) throws Exception {
		aDstDir.remove();
		return moveTo(aDstDir, aFilter, true, true);
	}

	/**
	 * D�placement du r�pertoire courant --> aDstDir - Recursif Ne cr�e pas les
	 * r�pertoires vides
	 * 
	 * @param aFilter
	 *            --> Filtre sur les fichiers � copier - "ext,ext,..."
	 * @param aFilter
	 *            =null --> Aucun filtre sur les fichiers � copier
	 * @param aDeleteExinstingFiles
	 *            =true --> Ecrase les fichier existants ou pas
	 * @param aCreateEmptyDir
	 *            =true --> Cr�e les r�pertoires qui ne contiennent aucun
	 *            fichier
	 * @param aCreateEmptyDir
	 *            =false --> Ne cr�e que les r�pertoires qui contienet des
	 *            fichiers
	 * @return le nombre de fichiers copi�s et r�pertoires cr��s
	 */
	public int moveTo(CXFileDir aDstDir, FileFilter aFilter,
			boolean aDeleteExinstingFiles, boolean aCreateEmptyDir)
			throws Exception {
		if (!exists())
			return 0;

		if (!aDstDir.exists())
			aDstDir.mkdirs();

		int wRes = 0;
		ArrayList<File> wMyFiles = getMyFiles(aFilter, true);
		Iterator<File> wIt = wMyFiles.iterator();
		while (wIt.hasNext()) {
			File wFile = wIt.next();
			if (wFile.isFile()) {
				((CXFile) wFile).moveTo(aDstDir, wFile.getName(),
						aDeleteExinstingFiles);
				wRes++;
			} else if (wFile.isDirectory())
				wRes += ((CXFileDir) wFile).moveTo(
						new CXFileDir(aDstDir.getAbsolutePath(), wFile
								.getName()), aFilter, aDeleteExinstingFiles,
						aCreateEmptyDir);
		}
		// Suppression si dossier source si vide
		if (wRes == 0 && !aCreateEmptyDir)
			aDstDir.delete();
		// Suppression si aucun fichier et sous-dossier - Sinon on conserve le
		// dossier -> Cas d'un filtre no null
		if (this.isEmpty())
			this.delete();

		return wRes;
	}

	/**
	 * Instancie un objet fichier - CAdminFile et classes d�riv�es
	 */
	protected CXFile newFile(String aPath, boolean aInstanciateTxtFiles) {
		if (aInstanciateTxtFiles)
			return new CXFileText(aPath);
		else
			return new CXFile(aPath);
	}

	/**
	 * Instancie un objet r�pertoire - CAdminFileDir et classes d�riv�es
	 * 
	 * @param aPath
	 * @return
	 */
	protected CXFileDir newFileDir(String aPath) {
		return new CXFileDir(aPath);
	}

	/**
	 * 16w_104
	 * 
	 * @param aXFileProcessor
	 * @return
	 * @throws Exception
	 */
	public boolean processAllFiles(IXFileProcessor aXFileProcessor)
			throws Exception {
		return processAllFiles(aXFileProcessor, WITH_DIRS, NO_FILTER);
	}

	/**
	 * 16w_104
	 * 
	 * @param aList
	 * @param aFilter
	 * @param aWithSubDirs
	 * @param aXFileProcessor
	 * @return
	 * @throws Exception
	 */
	protected boolean processAllFiles(IXFileProcessor aXFileProcessor,
			boolean aWithSubDirs, FileFilter aFilter) throws Exception {
		boolean wContinue = true;

		Iterator<File> wIt = getMyFiles(aFilter, WITH_DIRS, !WITH_TEXTFILE)
				.iterator();
		while (wContinue && wIt.hasNext()) {
			File wFile = wIt.next();

			wContinue = aXFileProcessor.processFile(wFile);
			if (wFile.isDirectory() && aWithSubDirs)
				((CXFileDir) wFile).processAllFiles(aXFileProcessor,
						aWithSubDirs, aFilter);
		}
		return wContinue;
	}

	/**
	 * 14w_009 - Bug 31297 - Mise � jour d'un serveur web
	 * 
	 * Supprime le r�pertoire (et son contenu) - Recursif
	 * 
	 * @return
	 * @throws Exception
	 */
	public int remove() throws Exception {
		return remove(true, NO_FILTER);
	}

	/**
	 * 16w_104
	 * 
	 * @param aRemoveMe
	 * @return
	 * @throws Exception
	 */
	private int remove(boolean aRemoveMe, FileFilter aFileFilter)
			throws Exception {
		int wNbFile = 0;
		if (exists()) {
			Iterator<File> wIt = getMyFiles(aFileFilter, WITH_DIRS).iterator();
			while (wIt.hasNext()) {
				File wFile = wIt.next();
				if (wFile.isFile()) {
					if (aFileFilter == null || aFileFilter.accept(wFile)) {
						((CXFile) wFile).delete();
						wNbFile++;
					}
				} else if (wFile.isDirectory()) {
					wNbFile += ((CXFileDir) wFile).remove(true, aFileFilter);
				}
			}
			if (aRemoveMe) {
				boolean wDeleted = this.delete();
				if (!wDeleted)
					throw new CXException(
							CXException.WHY_UNSPECIFIED,
							"Unable to delete [%s]. canWrite=[%b]. isEmpty=[%b]",
							getAbsolutePath(), canWrite(), isEmpty());
				wNbFile++;
			}
		}
		return wNbFile;
	}

	/**
	 * 16w_104 - Cr�ation de l'outil de cr�ation des fiches "history" � partir
	 * du fichier xml extrait de SUIVIPRD
	 * 
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAll() throws Exception {
		return scanAll(null, null, true, false);
	}

	/**
	 * 16w_104 - Cr�ation de l'outil de cr�ation des fiches "history" � partir
	 * du fichier xml extrait de SUIVIPRD
	 * 
	 * @param aList
	 * @param aFilter
	 * @param aSubDirs
	 * @param aInstanciateTxtFiles
	 * @return
	 * @throws Exception
	 */
	@Override
	public CXSortListFiles scanAll(CXSortListFiles aList, FileFilter aFilter,
			boolean aSubDirs, boolean aInstanciateTxtFiles) throws Exception {
		if (aList == null)
			aList = new CXSortListFiles();
		Iterator<File> wIt = getMyFiles(aFilter, true, aInstanciateTxtFiles)
				.iterator();
		while (wIt.hasNext()) {
			File wFile = wIt.next();

			aList.add(wFile);
			if (wFile.isDirectory() && aSubDirs)
				((CXFileDir) wFile).scanAll(aList, aFilter, aSubDirs,
						aInstanciateTxtFiles);

		}
		return aList;
	}

	/**
	 * 16w_104 - Cr�ation de l'outil de cr�ation des fiches "history" � partir
	 * du fichier xml extrait de SUIVIPRD
	 * 
	 * @param aFilter
	 * @param aSubDirs
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAll(FileFilter aFilter, boolean aSubDirs)
			throws Exception {
		return scanAll(null, aFilter, aSubDirs, false);
	}

	/**
	 * 16w_104 - Fiche 44010 - Eclatement History
	 * 
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllDirs() throws Exception {
		return scanAllDirs(null, null, true, true);
	}

	/**
	 * 16w_104 - Fiche 44010 - Eclatement History
	 * 
	 * @param aSubDirs
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllDirs(boolean aSubDirs) throws Exception {
		return scanAllDirs(null, null, aSubDirs, true);
	}

	/**
	 * 16w_104 - Fiche 44010 - Eclatement History
	 * 
	 * @param aList
	 * @param aSubDirs
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllDirs(CXSortListFiles aList, boolean aSubDirs)
			throws Exception {
		return scanAllDirs(aList, null, aSubDirs, true);
	}

	/**
	 * 16w_104 - Fiche 44010 - Eclatement History
	 * 
	 * @param aList
	 * @param aFilter
	 * @param aSubDirs
	 * @param aInstanciateTxtFiles
	 * @return
	 * @throws Exception
	 */
	@Override
	public CXSortListFiles scanAllDirs(CXSortListFiles aList,
			FileFilter aFilter, boolean aSubDirs, boolean aInstanciateTxtFiles)
			throws Exception {
		if (aList == null)
			aList = new CXSortListFiles();
		Iterator<File> wIt = getMyFiles(aFilter, true, aInstanciateTxtFiles)
				.iterator();
		while (wIt.hasNext()) {
			File wFile = wIt.next();
			if (wFile.isDirectory()) {
				aList.add(wFile);
				if (aSubDirs)
					((CXFileDir) wFile).scanAllDirs(aList, aFilter, aSubDirs,
							aInstanciateTxtFiles);
			}
		}
		return aList;
	}

	/**
	 * 16w_104 - Cr�ation de l'outil de cr�ation des fiches "history" � partir
	 * du fichier xml extrait de SUIVIPRD
	 * 
	 * @param aFilter
	 * @param aSubDirs
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllDirs(FileFilter aFilter, boolean aSubDirs)
			throws Exception {
		return scanAllDirs(null, aFilter, aSubDirs, true);
	}

	/**
	 * Renvoie la liste de tous les Fichiers (CAdminFile) et dossiers - Recursif
	 * Utilis� si pas de lecture/ecriture sur les fichiers
	 */
	public CXSortListFiles scanAllFiles() throws Exception {
		return scanAllFiles(null, null, true, false);
	}

	/**
	 * 16j_100
	 * 
	 * @param aWithSubDirs
	 *            si False --> Ne prend pas en compte les sous-r�pertoires
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllFiles(boolean aWithSubDirs) throws Exception {
		return scanAllFiles(null, null, aWithSubDirs, false);
	}

	/**
	 * Renvoie la liste des Fichier et dossiers - Recursif aFilter --> Filtre
	 * sur les fichiers - null authoris� aSubDirs --> False --> Ne prend pas en
	 * compte les sous-r�pertoires aInstanciateTxtFiles --> True instancie un
	 * CAdminTextFile (avec m�thode de lecture/ecriture)
	 */
	@Override
	public CXSortListFiles scanAllFiles(CXSortListFiles aList,
			FileFilter aFilter, boolean aSubDirs, boolean aInstanciateTxtFiles)
			throws Exception {
		if (aList == null)
			aList = new CXSortListFiles();

		Iterator<File> wIt = getMyFiles(aFilter, true, aInstanciateTxtFiles)
				.iterator();
		while (wIt.hasNext()) {
			File wFile = wIt.next();
			if (wFile.isFile())
				aList.add(wFile);
			else if (wFile.isDirectory() && aSubDirs) {
				// 16j_101
				if (hasScanListener())
					((CXFileDir) wFile).setScanListner(getScanListener());
				// 16j_101
				((CXFileDir) wFile).setScanLevel(getScanLevel() + 1);
				((CXFileDir) wFile).scanAllFiles(aList, aFilter, aSubDirs,
						aInstanciateTxtFiles);
			}
		}
		// 16j_101
		if (hasScanListener() && isScanLevelZero())
			getScanListener().listenEndScan();
		return aList;
	}

	/**
	 * Renvoie la liste des Fichier (CAdminFile) et dossiers - Recursif Utilis�
	 * si pas de lecture/ecriture sur les fichiers aFilter --> Filtre sur les
	 * fichiers - null authoris� aSubDirs --> False --> Ne prend pas en compte
	 * les sous-r�pertoires
	 */
	public CXSortListFiles scanAllFiles(FileFilter aFilter, boolean aSubDirs)
			throws Exception {
		return scanAllFiles(null, aFilter, aSubDirs, false);
	}

	/**
	 * Renvoie la liste des Fichier (CAdminFile) et dossiers - Recursif Utilis�
	 * si pas de lecture/ecriture sur les fichiers aListeExtensions --> Liste
	 * des extension s�par�es par ';' - null authoris� aSubDirs --> False --> Ne
	 * prend pas en compte les sous-r�pertoires
	 */
	public CXSortListFiles scanAllFiles(String aListeExtensions,
			boolean aSubDirs) throws Exception {
		if (aListeExtensions != null)
			return scanAllFiles(getFilterExtension(aListeExtensions), aSubDirs);
		else
			return scanAllFiles((FileFilter) null, aSubDirs);
	}

	/**
	 * Renvoie la liste de tous les Fichiers (CAdminFileText) et dossiers -
	 * Recursif Utilis� si lecture/ecriture sur les fichiers
	 */
	public CXSortListFiles scanAllTextFiles() throws Exception {
		return scanAllFiles(null, null, true, true);
	}

	/**
	 * Renvoie la liste des Fichier (CAdminFileText) et dossiers - Recursif
	 * Utilis� si lecture/ecriture sur les fichiers aFilter --> Filtre sur les
	 * fichiers - null authoris� aSubDirs --> False --> Ne prend pas en compte
	 * les sous-r�pertoires
	 */
	public CXSortListFiles scanAllTextFiles(FileFilter aFilter, boolean aSubDirs)
			throws Exception {
		return scanAllFiles((CXSortListFiles) null, aFilter, aSubDirs, true);
	}

	/**
	 * Renvoie la liste des Fichier (CAdminFileText) et dossiers - Recursif
	 * Utilis� si pas de lecture/ecriture sur les fichiers aListeExtensions -->
	 * Liste des extension s�par�es par ';' - null authoris� aSubDirs --> False
	 * --> Ne prend pas en compte les sous-r�pertoires
	 */
	public CXSortListFiles scanAllTextFiles(String aListeExtensions,
			boolean aSubDirs) throws Exception {
		if (aListeExtensions != null)
			return scanAllTextFiles(getFilterExtension(aListeExtensions),
					aSubDirs);
		else
			return scanAllTextFiles((FileFilter) null, aSubDirs);
	}

	// 16j_101
	private void setScanLevel(int aScanLevel) {
		pScanLevel = aScanLevel;
	}

	// 16j_101
	public void setScanListner(IXDirScanListener aDirScanListener) {
		pDirScanListener = aDirScanListener;
	}
}
