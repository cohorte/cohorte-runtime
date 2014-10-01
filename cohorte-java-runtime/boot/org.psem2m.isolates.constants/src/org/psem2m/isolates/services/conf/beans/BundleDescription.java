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

package org.psem2m.isolates.services.conf.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.psem2m.isolates.services.conf.IBundleDescr;

/**
 * Description of a bundle
 *
 * @author Thomas Calmant
 */
public class BundleDescription implements Serializable {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** Bundle file name, if specified */
    private String pFile;

    /** True if the bundle is optional */
    private boolean pOptional;

    /** Bundle properties, if any */
    private Properties pProperties = null;

    /** Bundle symbolic name, mandatory */
    private String pSymbolicName;

    /** Bundle version, if specified */
    private String pVersion;

    /**
     * Default constructor
     */
    public BundleDescription() {

        // Does nothing
    }

    /**
     * Sets up the bean with the given map representation
     *
     * @param aMapRepresentation
     *            A map representation
     */
    public BundleDescription(final Map<String, Object> aMapRepresentation) {

        pFile = (String) aMapRepresentation.get(IBundleDescr.BUNDLE_FILE);
        pSymbolicName = (String) aMapRepresentation
                .get(IBundleDescr.BUNDLE_NAME);
        pVersion = (String) aMapRepresentation.get(IBundleDescr.BUNDLE_VERSION);

        if (aMapRepresentation.get(IBundleDescr.BUNDLE_OPTIONAL) instanceof Boolean) {
            pOptional = (Boolean) aMapRepresentation
                    .get(IBundleDescr.BUNDLE_OPTIONAL);

        } else {
            pOptional = false;
        }

        if (aMapRepresentation.get(IBundleDescr.BUNDLE_PROPERTIES) instanceof Map) {
            // Copy properties manually, to avoid exceptions
            pProperties = new Properties();

            final Map<?, ?> map = (Map<?, ?>) aMapRepresentation
                    .get(IBundleDescr.BUNDLE_PROPERTIES);
            for (final Entry<?, ?> entry : map.entrySet()) {

                // Only accept strings keys
                final String key = (String) entry.getKey();
                if (key == null) {
                    // Reject null keys
                    continue;
                }

                // Only accept strings values
                final String value = (String) entry.getValue();
                if (value == null) {
                    // Reject null values
                    continue;
                }

                pProperties.put(key, value);
            }
        }
    }

    /**
     * Sets up the bundle description
     *
     * @param aSymbolicName
     *            The bundle symbolic name (mandatory)
     */
    public BundleDescription(final String aSymbolicName) {

        this(aSymbolicName, null, null, false);
    }

    /**
     * Sets up the bundle description
     *
     * @param aSymbolicName
     *            The bundle symbolic name (mandatory)
     * @param aVersion
     *            Bundle version
     * @param aFile
     *            Bundle file
     * @param aOptional
     *            Bundle is optional
     */
    public BundleDescription(final String aSymbolicName, final String aVersion,
            final String aFile, final boolean aOptional) {

        pSymbolicName = aSymbolicName;
        pVersion = aVersion;
        pFile = aFile;
        pOptional = aOptional;
    }

    /**
     * Retrieves the path to the bundle
     *
     * @return the path to the bundle
     */
    public String getFile() {

        return pFile;
    }

    /**
     * Returns True if the bundle is optional in the configuration
     *
     * @return True if the bundle is optional
     */
    public boolean getOptional() {

        return pOptional;
    }

    /**
     * Retrieves the properties of the bundle
     *
     * @return the properties of the bundle
     */
    public Properties getProperties() {

        return pProperties;
    }

    /**
     * Retrieves the bundle symbolic name
     *
     * @return the bundle symbolic name
     */
    public String getSymbolicName() {

        return pSymbolicName;
    }

    /**
     * Retrieves the requested bundle version
     *
     * @return the requested bundle version
     */
    public String getVersion() {

        return pVersion;
    }

    /**
     * Tests if the bundle has properties
     *
     * @return true if the bundle has a set of properties
     */
    public boolean hasProperties() {

        return pProperties != null;
    }

    /**
     * Sets the bundle file (bean method)
     *
     * @param aFile
     *            Bundle file
     */
    public void setFile(final String aFile) {

        pFile = aFile;
    }

    /**
     * Set the bundle are optional or not (bean method)
     *
     * @param aOptional
     *            True if the bundle is optional
     */
    public void setOptional(final boolean aOptional) {

        pOptional = aOptional;
    }

    /**
     * Set the properties of the bundle
     *
     * @param aProperties
     *            the properties of the bundle
     */
    public void setProperties(final Properties aProperties) {

        pProperties = aProperties;
    }

    /**
     * Sets the bundle symbolic name (bean method)
     *
     * @param aSymbolicName
     *            The bundle symbolic name
     */
    public void setSymbolicName(final String aSymbolicName) {

        pSymbolicName = aSymbolicName;
    }

    /**
     * Sets the bundle version (bean method)
     *
     * @param aVersion
     *            the bundle version
     */
    public void setVersion(final String aVersion) {

        pVersion = aVersion;
    }

    /**
     * Converts this description into its map representation
     *
     * @return The map representation of this object
     */
    public Map<String, Object> toMap() {

        final Map<String, Object> map = new HashMap<String, Object>();

        map.put(IBundleDescr.BUNDLE_FILE, pFile);
        map.put(IBundleDescr.BUNDLE_NAME, pSymbolicName);
        map.put(IBundleDescr.BUNDLE_OPTIONAL, pOptional);
        map.put(IBundleDescr.BUNDLE_PROPERTIES, pProperties);
        map.put(IBundleDescr.BUNDLE_VERSION, pVersion);

        return map;
    }
}
