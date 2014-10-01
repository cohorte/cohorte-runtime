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
