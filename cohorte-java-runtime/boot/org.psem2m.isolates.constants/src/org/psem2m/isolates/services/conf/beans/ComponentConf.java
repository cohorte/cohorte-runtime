/**
 * File:   Component.java
 * Author: Thomas Calmant
 * Date:   22 janv. 2013
 */
package org.psem2m.isolates.services.conf.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a basic component
 * 
 * @author Thomas Calmant
 */
public class ComponentConf {

    /** Component factory name */
    private String pFactory;

    /** Component instance name */
    private String pName;

    /** Component properties */
    private final Map<String, Object> pProperties = new HashMap<String, Object>();

    /**
     * Default constructor
     */
    public ComponentConf() {

        // Do nothing
    }

    /**
     * Sets up the component
     * 
     * @param aFactory
     *            Component factory name
     * @param aName
     *            Component instance name
     */
    public ComponentConf(final String aFactory, final String aName) {

        pFactory = aFactory;
        pName = aName;
    }

    /**
     * Sets up the component
     * 
     * @param aFactory
     *            Component factory name
     * @param aName
     *            Component instance name
     * @param aProperties
     *            Component properties (copied)
     */
    public ComponentConf(final String aFactory, final String aName,
            final Map<String, Object> aProperties) {

        this(aFactory, aName);
        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * @return the factory
     */
    public String getFactory() {

        return pFactory;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * Retrieves a copy of the component properties
     * 
     * @return the properties
     */
    public Map<String, Object> getProperties() {

        return new HashMap<String, Object>(pProperties);
    }

    /**
     * @param aFactory
     *            the factory to set
     */
    public void setFactory(final String aFactory) {

        pFactory = aFactory;
    }

    /**
     * @param aName
     *            the name to set
     */
    public void setName(final String aName) {

        pName = aName;
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
     * Converts the bean to a map
     * 
     * @return A map
     */
    public Map<String, Object> toMap() {

        final Map<String, Object> result = new HashMap<String, Object>();

        result.put("factory", pFactory);
        result.put("name", pName);
        result.put("properties", pProperties);

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
