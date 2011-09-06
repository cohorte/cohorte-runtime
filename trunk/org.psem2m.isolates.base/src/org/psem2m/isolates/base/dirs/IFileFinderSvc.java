/**
 * File:   IFileFinderSvc.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.base.dirs;

import java.io.File;

/**
 * Describes a simple file finder
 * 
 * @author Thomas Calmant
 */
public interface IFileFinderSvc {

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
