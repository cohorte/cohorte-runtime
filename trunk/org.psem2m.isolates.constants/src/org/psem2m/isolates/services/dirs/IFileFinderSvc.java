/**
 * File:   IFileFinderSvc.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.services.dirs;

import java.io.File;

/**
 * Describes a simple file finder
 * 
 * @author Thomas Calmant
 */
public interface IFileFinderSvc {

    /**
     * Tries to find the given file in the platform folders
     * 
     * @param aBaseFile
     *            Base file reference (aFileName could be relative to it)
     * @param aFileName
     *            The file to found (uses its absolute path then its name)
     * @return All found files with the given information, null if none found
     */
    File[] find(File aBaseFile, String aFileName);

    /**
     * Tries to find the given file name in the platform folders.
     * 
     * Tries in the home, then in the base and finally without prefix (for
     * complete paths). The file name must be a path from the root of a PSEM2M
     * base folder (home or base), a complete path or a path relative to the
     * working directory.
     * 
     * @param aFileName
     *            Name of the file to look for
     * @return All found files with the given name, null if none found
     */
    File[] find(String aFileName);
}
