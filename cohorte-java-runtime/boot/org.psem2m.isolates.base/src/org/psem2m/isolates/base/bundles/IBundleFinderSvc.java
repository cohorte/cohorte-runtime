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

package org.psem2m.isolates.base.bundles;

import java.io.File;

/**
 * Represents a bundle file finder service
 *
 * @author Thomas Calmant
 */
public interface IBundleFinderSvc {

    /** The bootstrap main class */
    String BOOTSTRAP_MAIN_CLASS = "org.psem2m.utilities.bootstrap.Main";

    /** The bootstrap symbolic name */
    String BOOTSTRAP_SYMBOLIC_NAME = "org.psem2m.utilities.bootstrap";

    /**
     * Searches for a bundle according to the given possible names. It looks in
     * platform repositories, then in the local directory if needed.
     *
     * @param aBundlePossibleNames
     *            Possible file names for the bundle
     * @return A file reference to the bundle, null if not found
     */
    BundleRef findBundle(String... aBundlePossibleNames);

    /**
     * Finds the bootstrap JAR file using its symbolic name (internal constant).
     *
     * @return The bootstrap JAR file, null if not found.
     */
    File getBootstrap();
}
