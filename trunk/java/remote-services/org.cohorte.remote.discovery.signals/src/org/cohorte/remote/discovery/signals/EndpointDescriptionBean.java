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
package org.cohorte.remote.discovery.signals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cohorte.remote.ExportEndpoint;
import org.cohorte.remote.ImportEndpoint;

/**
 * Represents an endpoint in signals events
 * 
 * @author Thomas Calmant
 */
public class EndpointDescriptionBean {

    /** Export configurations */
    private String[] pConfigurations;

    /** Framework UID */
    private String pFrameworkUid;

    /** Endpoint name */
    private String pName;

    /** Exported properties */
    private final Map<String, Object> pProperties = new LinkedHashMap<String, Object>();

    /** Exported specifications */
    private String[] pSpecifications;

    /** Endpoint UID */
    private String pUid;

    /**
     * Default constructor, for bean marshaling
     */
    public EndpointDescriptionBean() {

    }

    /**
     * Prepares the bean using an existing endpoint
     * 
     * @param aEndpoint
     *            An export endpoint
     */
    public EndpointDescriptionBean(final ExportEndpoint aEndpoint) {

        // Simple copy
        pUid = aEndpoint.getUid();
        pFrameworkUid = aEndpoint.getFrameworkUid();
        pName = aEndpoint.getName();

        // Array copy
        final String[] configs = aEndpoint.getConfigurations();
        pConfigurations = new String[configs.length];
        System.arraycopy(configs, 0, pConfigurations, 0, configs.length);

        final String[] specs = aEndpoint.getExportedSpecs();
        pSpecifications = new String[specs.length];
        System.arraycopy(specs, 0, pSpecifications, 0, specs.length);

        // Store import properties
        pProperties.putAll(aEndpoint.makeImportProperties());
    }

    /**
     * @return the configurations
     */
    public String[] getConfigurations() {

        return pConfigurations;
    }

    /**
     * @return the frameworkUid
     */
    public String getFrameworkUid() {

        return pFrameworkUid;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * Returns a copy of the endpoint properties, with import properties and no
     * export property
     * 
     * @return the endpoint properties, without export ones
     */
    public Map<String, Object> getProperties() {

        return new LinkedHashMap<String, Object>(pProperties);
    }

    /**
     * @return the specifications
     */
    public String[] getSpecifications() {

        return pSpecifications;
    }

    /**
     * @return the uid
     */
    public String getUid() {

        return pUid;
    }

    /**
     * @param aConfigurations
     *            the configurations to set
     */
    public void setConfigurations(final String[] aConfigurations) {

        pConfigurations = aConfigurations;
    }

    /**
     * @param aFrameworkUid
     *            the frameworkUid to set
     */
    public void setFrameworkUid(final String aFrameworkUid) {

        pFrameworkUid = aFrameworkUid;
    }

    /**
     * @param aName
     *            the name to set
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * Sets the endpoint properties. There must be no export property in the
     * given map
     * 
     * @param aProperties
     *            the properties to set
     */
    public void setProperties(final Map<String, Object> aProperties) {

        pProperties.clear();
        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * @param aSpecifications
     *            the specifications to set
     */
    public void setSpecifications(final String[] aSpecifications) {

        pSpecifications = aSpecifications;
    }

    /**
     * @param aUid
     *            the uid to set
     */
    public void setUid(final String aUid) {

        pUid = aUid;
    }

    /**
     * Converts this endpoint description into an import endpoint
     * 
     * @return An ImportEndpoint object
     */
    public ImportEndpoint toImportEndpoint() {

        return new ImportEndpoint(pUid, pFrameworkUid, pConfigurations, pName,
                pSpecifications, pProperties);
    }
}
