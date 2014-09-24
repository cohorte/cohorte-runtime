/**
 * File:   Bundle.java
 * Author: Thomas Calmant
 * Date:   22 janv. 2013
 */
package org.psem2m.isolates.services.conf.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a simple bundle
 * 
 * @author Thomas Calmant
 */
public class BundleConf {

    /** Bundle file name */
    private String pFilename;

    /** Bundle name */
    private String pName;

    /** Bundle is optional */
    private boolean pOptional;

    /** Bundle properties */
    private final Map<String, Object> pProperties = new HashMap<String, Object>();

    /** Bundle version */
    private String pVersion;

    /**
     * Default bundle
     */
    public BundleConf() {

        // Do nothing
    }

    /**
     * Sets up the bundle
     * 
     * @param aName
     *            Bundle symbolic name
     * @param aVersion
     *            Bundle version (can be null)
     * @param aOptional
     *            Indicates if the bundle is optional
     */
    public BundleConf(final String aName, final String aVersion,
            final boolean aOptional) {

        pName = aName;
        pVersion = aVersion;
        pOptional = aOptional;
    }

    /**
     * @return the filename
     */
    public String getFilename() {

        return pFilename;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {

        return new HashMap<String, Object>(pProperties);
    }

    /**
     * @return the version
     */
    public String getVersion() {

        return pVersion;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {

        return pOptional;
    }

    /**
     * @param aFilename
     *            the filename to set
     */
    public void setFilename(final String aFilename) {

        pFilename = aFilename;
    }

    /**
     * @param aName
     *            the name to set
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * @param aOptional
     *            the optional to set
     */
    public void setOptional(final boolean aOptional) {

        pOptional = aOptional;
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
     * @param aVersion
     *            the version to set
     */
    public void setVersion(final String aVersion) {

        pVersion = aVersion;
    }

    /**
     * Converts the bean to a map
     * 
     * @return A map
     */
    public Map<String, Object> toMap() {

        final Map<String, Object> result = new HashMap<String, Object>();

        result.put("name", pName);
        result.put("filename", pFilename);
        result.put("properties", pProperties);
        result.put("version", pVersion);
        result.put("optional", pOptional);

        return result;
    }

    /**
     * Adds, updates or removes the given property. If aValue is null, the
     * property is removed. If aKey is null, the method does nothing.
     * 
     * @param aKey
     *            Property name
     * @param aValue
     *            Property value (or null)
     * @return The previous value of the property (or null)
     */
    public Object updateProperty(final String aKey, final Object aValue) {

        if (aKey == null) {
            // Refuse null keys
            return null;
        }

        final Object oldValue = pProperties.get(aKey);
        if (aValue != null) {
            // Add the property
            pProperties.put(aKey, aValue);

        } else {
            // Remove it
            pProperties.remove(aKey);
        }

        // Return the previous value
        return oldValue;
    }
}
