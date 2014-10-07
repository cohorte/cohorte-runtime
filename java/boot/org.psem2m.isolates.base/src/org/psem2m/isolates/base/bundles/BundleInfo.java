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

import java.io.Serializable;

import org.osgi.framework.Bundle;

/**
 * Represents a serializable bundle information structure
 *
 * @author Thomas Calmant
 */
public class BundleInfo implements Serializable {

    /** Serialization version */
    private static final long serialVersionUID = 1L;

    /** Bundle UID */
    private long pId = -1;

    /** Bundle location */
    private String pLocation = null;

    /** Bundle symbolic name */
    private String pName = null;

    /** Bundle state */
    private int pState = -1;

    /** Bundle version */
    private String pVersion = null;

    /**
     * Sets up a bundle information structure
     *
     * @param aBundle
     *            Bundle to read
     */
    public BundleInfo(final Bundle aBundle) {

        if (aBundle != null) {

            pId = aBundle.getBundleId();
            pName = aBundle.getSymbolicName();
            pState = aBundle.getState();
            pLocation = aBundle.getLocation();
            pVersion = aBundle.getVersion().toString();
        }
    }

    /**
     * Sets up a bundle information structure
     *
     * @param aId
     *            Bundle's ID
     * @param aName
     *            Bundle's symbolic name
     * @param aState
     *            Bundle's state
     * @param aLocation
     *            Bundle's location
     * @param aVersion
     *            Bundle's version
     */
    public BundleInfo(final long aId, final String aName, final int aState,
            final String aLocation, final String aVersion) {

        pId = aId;
        pName = aName;
        pState = aState;
        pLocation = aLocation;
        pVersion = aVersion;
    }

    /**
     * Retrieves the bundle's unique ID
     *
     * @return The bundle's UID
     */
    public long getId() {

        return pId;
    }

    /**
     * Retrieves the bundle's location identifier
     *
     * @return the bundle's location
     */
    public String getLocation() {

        return pLocation;
    }

    /**
     * Returns this bundle's current state
     *
     * @return The bundle's state
     */
    public int getState() {

        return pState;
    }

    /**
     * Returns the symbolic name of this bundle as specified by its
     * Bundle-SymbolicName manifest header.
     *
     * @return The bundle's symbolic name
     */
    public String getSymbolicName() {

        return pName;
    }

    /**
     * Returns the bundle's version string representation
     *
     * @return The bundle's version
     */
    public String getVersion() {

        return pVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        builder.append("BundleInfo(Id: ");
        builder.append(pId);
        builder.append(", Name: ");
        builder.append(pName);
        builder.append(", Version: ");
        builder.append(pVersion);
        builder.append(", State: ");
        builder.append(pState);
        builder.append(", Location: '");
        builder.append(pLocation);
        builder.append("')");

        return builder.toString();
    }
}
