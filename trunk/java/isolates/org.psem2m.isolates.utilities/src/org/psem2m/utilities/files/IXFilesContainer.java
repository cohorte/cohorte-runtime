package org.psem2m.utilities.files;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;



public interface IXFilesContainer {

	// 16w_104 - Fiche 44010 - Eclatement History
	public final static boolean WITH_SUBDIRS = true;

	//14w_009 - Bug 31297 - Mise � jour d'un serveur web
	public final static boolean WITH_DIRS = true;
	
	//14w_009 - Bug 31297 - Mise � jour d'un serveur web
	public final static boolean WITH_TEXTFILE = true;
	
	//14w_009 - Bug 31297 - Mise � jour d'un serveur web
	public final static FileFilter NO_FILTER = null;
	
	

	public ArrayList<File> getMyFiles(FileFilter aFilter, boolean aWithDirs, boolean aInstanciateTxtFiles)	throws Exception ;

	public CXSortListFiles scanAll(CXSortListFiles aList, FileFilter aFilter, boolean aSubDirs,boolean aInstanciateTxtFiles) throws Exception ;

	public CXSortListFiles scanAllFiles(CXSortListFiles aList, FileFilter aFilter, boolean aSubDirs,boolean aInstanciateTxtFiles) throws Exception;
	
	public CXSortListFiles scanAllDirs(CXSortListFiles aList, FileFilter aFilter, boolean aSubDirs, boolean aInstanciateTxtFiles) throws Exception ;
			
}
