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
import java.net.URI;

/**
 * Describes a bundle reference
 *
 * @author Thomas Calmant
 */
public class BundleRef {

    /** The bundle file */
    private File pBundleFile;

    /** The bundle name */
    private final String pBundleName;

    /** The bundle URI */
    private final URI pBundleUri;

    /**
     * File constructor
     *
     * @param aBundleName
     *            The bundle name
     * @param aBundleFile
     *            The bundle file (can't be null)
     */
    public BundleRef(final String aBundleName, final File aBundleFile) {

        pBundleFile = aBundleFile;
        pBundleName = aBundleName;
        pBundleUri = aBundleFile.toURI();
    }

    /**
     * URI Constructor
     *
     * @param aBundleName
     *            The bundle name
     * @param aBundleUri
     *            The bundle URI (can't be null)
     */
    public BundleRef(final String aBundleName, final URI aBundleUri) {

        pBundleName = aBundleName;
        pBundleUri = aBundleUri;
    }

    /**
     * Retrieves the bundle File (if any)
     *
     * @return The bundle File, or null
     */
    public File getFile() {

        return pBundleFile;
    }

    /**
     * Retrieves the bundle name
     *
     * @return The bundle name
     */
    public String getName() {

        return pBundleName;
    }

    /**
     * Retrieves the bundle URI
     *
     * @return The bundle URI
     */
    public URI getUri() {

        return pBundleUri;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return pBundleUri.toString();
    }
}
