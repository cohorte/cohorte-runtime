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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simple isolate representation
 *
 * @author Thomas Calmant
 */
public class IsolateConf {

    /** Isolate boot arguments */
    private final List<String> pBootArguments = new LinkedList<String>();

    /** Isolate bundles */
    private final List<BundleConf> pBundles = new LinkedList<BundleConf>();

    /** Isolate base composition */
    private final List<ComponentConf> pComponents = new LinkedList<ComponentConf>();

    /** Isolate environment variables */
    private final Map<String, String> pEnvironment = new HashMap<String, String>();

    /** Kind of isolate */
    private String pKind;

    /** Configuration level */
    private String pLevel;

    /** Name of the isolate */
    private String pName;

    /** Node hosting the isolate */
    private String pNode;

    /** Isolate properties */
    private final Map<String, Object> pProperties = new HashMap<String, Object>();

    /** Configuration category */
    private String pSublevel;

    /** UID of the isolate */
    private String pUID;

    /**
     * Default constructor
     */
    public IsolateConf() {

        // Do nothing
    }

    /**
     * Sets up the isolate
     *
     * @param aUID
     *            Isolate UID
     * @param aName
     *            Isolate name
     * @param aNode
     *            Isolate node
     * @param aKind
     *            Isolate kind
     * @param aLevel
     *            Configuration level
     * @param aSublevel
     *            Configuration category
     */
    public IsolateConf(final String aUID, final String aName,
            final String aNode, final String aKind, final String aLevel,
            final String aSublevel) {

        pUID = aUID;
        pName = aName;
        pKind = aKind;
        pLevel = aLevel;
        pSublevel = aSublevel;
        pNode = aNode;
    }

    /**
     * @return the bootArguments
     */
    public List<String> getBootArguments() {

        return pBootArguments;
    }

    /**
     * @return the bundles
     */
    public List<BundleConf> getBundles() {

        return new ArrayList<BundleConf>(pBundles);
    }

    /**
     * @return the components
     */
    public List<ComponentConf> getComponents() {

        return new ArrayList<ComponentConf>(pComponents);
    }

    /**
     * @return the environment
     */
    public Map<String, String> getEnvironment() {

        return new HashMap<String, String>(pEnvironment);
    }

    /**
     * @return the kind
     */
    public String getKind() {

        return pKind;
    }

    /**
     * @return the level
     */
    public String getLevel() {

        return pLevel;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * @return the node
     */
    public String getNode() {

        return pNode;
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {

        return new HashMap<String, Object>(pProperties);
    }

    /**
     * @return the sub level
     */
    public String getSublevel() {

        return pSublevel;
    }

    /**
     * @return the uID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * @param aBootArguments
     *            the bootArguments to set
     */
    public void setBootArguments(final Collection<String> aBootArguments) {

        if (aBootArguments != null) {
            pBootArguments.addAll(aBootArguments);
        }
    }

    /**
     * @param aBundles
     *            the bundles to set
     */
    public void setBundles(final Collection<BundleConf> aBundles) {

        if (aBundles != null) {
            pBundles.addAll(aBundles);
        }
    }

    /**
     * @param aComponents
     *            the components to set
     */
    public void setComponents(final Collection<ComponentConf> aComponents) {

        if (aComponents != null) {
            pComponents.addAll(aComponents);
        }
    }

    /**
     * @param aEnvironment
     *            the environment to set
     */
    public void setEnvironment(final Map<String, String> aEnvironment) {

        if (aEnvironment != null) {
            pEnvironment.putAll(aEnvironment);
        }
    }

    /**
     * @param aKind
     *            the kind to set
     */
    public void setKind(final String aKind) {

        pKind = aKind;
    }

    /**
     * @param aLevel
     *            the level to set
     */
    public void setLevel(final String aLevel) {

        pLevel = aLevel;
    }

    /**
     * @param aName
     *            the name to set
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * @param aNode
     *            the node to set
     */
    public void setNode(final String aNode) {

        pNode = aNode;
    }

    /**
     * @param aProperties
     *            the properties to set
     */
    public void setProperties(final Map<String, Object> aProperties) {

        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * @param aSublevel
     *            the sub level to set
     */
    public void setSublevel(final String aSublevel) {

        pSublevel = aSublevel;
    }

    /**
     * @param aUID
     *            the uID to set
     */
    public void setUID(final String aUID) {

        pUID = aUID;
    }
}
