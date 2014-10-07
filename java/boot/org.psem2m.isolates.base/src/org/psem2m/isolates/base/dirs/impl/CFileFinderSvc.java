/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.base.dirs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Simple file finder : tries to find the given file in the platform main
 * directories.
 *
 * @author Thomas Calmant
 */
public class CFileFinderSvc implements IFileFinderSvc {

    /** Platform directories service */
    private final IPlatformDirsSvc pPlatformDirs;

    /**
     * Constructor without injection
     * 
     * @param aPlatformDirs
     *            Platform directory service instance
     */
    public CFileFinderSvc(final IPlatformDirsSvc aPlatformDirs) {

        pPlatformDirs = aPlatformDirs;
    }

    /**
     * Tries to extract a platform root path from the given. Non-null result
     * indicates that the given path is a root sub-path.
     * 
     * @param aPath
     *            Path to be transformed
     * @return The root-path if any, else null
     */
    protected String extractPlatformPath(final String aPath) {

        if (aPath == null || aPath.isEmpty()) {
            return null;
        }

        for (final File rootDir : pPlatformDirs.getPlatformRootDirs()) {
            // Test if the path starts with the root path
            if (aPath.startsWith(rootDir.getPath())) {
                return aPath.substring(rootDir.getPath().length());
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IFileFinderSvc#find(java.io.File,
     * java.lang.String)
     */
    @Override
    public File[] find(final File aBaseFile, final String aFileName) {

        // Use a set to avoid duplicates
        final Set<File> foundFiles = new LinkedHashSet<File>();

        if (aBaseFile != null) {
            // Try to be relative to the parent, if the base file is a file
            File baseDir = null;

            if (aBaseFile.isFile()) {
                // Base file is a file : get its parent directory
                baseDir = aBaseFile.getParentFile();

            } else if (aBaseFile.isDirectory()) {
                // Use the directory
                baseDir = aBaseFile;
            }

            if (baseDir != null) {
                // We have a valid base
                final File testRelFile = new File(baseDir, aFileName);
                if (testRelFile.exists()) {
                    foundFiles.add(testRelFile);
                }

                /*
                 * If the base file path begins with a platform root, remove it.
                 * Allows cross conf/ repo/ references.
                 */
                final String platformSubDir = extractPlatformPath(baseDir
                        .getPath());
                if (platformSubDir != null) {
                    foundFiles.addAll(internalFind(platformSubDir
                            + File.separator + aFileName));

                }

            } else {
                // Test the path directly in the platform dirs
                foundFiles.addAll(internalFind(aBaseFile.getPath()
                        + File.separator + aFileName));
            }
        }

        // In any case, try using only the file name
        foundFiles.addAll(internalFind(aFileName));

        return foundFiles.toArray(new File[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IFileFinderSvc#find(java.lang.String)
     */
    @Override
    public File[] find(final String aFileName) {

        final List<File> foundFiles = internalFind(aFileName);
        if (foundFiles.isEmpty()) {
            // Return null if no file was found
            return null;
        }

        return foundFiles.toArray(new File[0]);
    }

    /**
     * Tries to find the given file in the platform directories. Never returns
     * null.
     * 
     * @param aFileName
     *            Name of the file to search for
     * @return The list of the corresponding files (never null, can be empty)
     */
    protected List<File> internalFind(final String aFileName) {

        final List<File> foundFiles = new ArrayList<File>();

        // Test on each PSEM2M root directory
        for (final File rootDir : pPlatformDirs.getPlatformRootDirs()) {

            final File testFile = new File(rootDir, aFileName);
            if (testFile.exists()) {
                foundFiles.add(testFile);
            }
        }

        // Test as an absolute file path
        final File testFile = new File(aFileName);
        if (testFile.exists()) {
            foundFiles.add(testFile);
        }

        return foundFiles;
    }
}
