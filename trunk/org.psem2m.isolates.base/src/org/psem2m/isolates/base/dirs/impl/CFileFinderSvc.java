/**
 * File:   CFileFinderSvc.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.base.dirs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.psem2m.isolates.base.dirs.IFileFinderSvc;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.files.CXFileDir;

/**
 * Simple file finder : tries to find the given file in the platform main
 * directories.
 * 
 * @author Thomas Calmant
 */
public class CFileFinderSvc implements IFileFinderSvc {

    /** Platform directories service */
    private IPlatformDirsSvc pPlatformDirs;

    /**
     * Default constructor (for iPOJO)
     */
    public CFileFinderSvc() {
	super();
    }

    /**
     * Constructor without injection
     * 
     * @param aPlatformDirs
     *            Platform directory service instance
     */
    public CFileFinderSvc(final IPlatformDirsSvc aPlatformDirs) {
	super();
	pPlatformDirs = aPlatformDirs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IFileFinderSvc#find(java.lang.String)
     */
    @Override
    public File[] find(final String aFileName) {

	final List<File> foundFiles = new ArrayList<File>();

	// Test on each PSEM2M root directory
	for (CXFileDir rootDir : pPlatformDirs.getPlatformRootDirs()) {

	    final File testFile = new File(rootDir, aFileName);
	    if (testFile.exists()) {
		foundFiles.add(testFile);
	    }
	}

	if (foundFiles.isEmpty()) {
	    // Return null if no file was found
	    return null;
	}

	return foundFiles.toArray(new File[0]);
    }
}
