/**
 * File:   ComponentBean.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * PSEM2M Composer component description bean
 * 
 * @author Thomas Calmant
 */
public class ComponentBean implements Serializable {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** Forced "@Requires" field filters */
    private final Map<String, String> pFieldFilters = new HashMap<String, String>();

    /** The host isolate ID */
    private String pIsolate;

    /** The component name */
    private String pName;

    /** The name of the parent composite */
    private String pParentName;

    /** Component properties */
    private final Map<String, String> pProperties = new HashMap<String, String>();

    /** The component type */
    private String pType;

    /** Component wires */
    private final Map<String, String> pWires = new HashMap<String, String>();

    /**
     * Default constructor
     */
    public ComponentBean() {

        // Does nothing...
    }

    /**
     * Retrieves the forced fields filters map
     * 
     * @return the forced fields filters map
     */
    public Map<String, String> getFieldsFilters() {

        return pFieldFilters;
    }

    /**
     * Retrieves the component host isolate
     * 
     * @return the host isolate
     */
    public String getIsolate() {

        return pIsolate;
    }

    /**
     * Retrieves the component instance name
     * 
     * @return the component instance name
     */
    public String getName() {

        return pName;
    }

    /**
     * Retrieves the name of the parent container
     * 
     * @return the name of the parent container
     */
    public String getParentName() {

        return pParentName;
    }

    /**
     * Retrieves the component properties map
     * 
     * @return the component properties map
     */
    public Map<String, String> getProperties() {

        return pProperties;
    }

    /**
     * Retrieves the component type name
     * 
     * @return the component type name
     */
    public String getType() {

        return pType;
    }

    /**
     * Retrieves the component wires
     * 
     * @return the component wires
     */
    public Map<String, String> getWires() {

        return pWires;
    }

    /**
     * Sets the selection filter for the given field
     * 
     * @param aField
     *            A field name
     * @param aFilter
     *            A LDAP OSGi service filter (can be null)
     */
    public void setFieldFilter(final String aField, final String aFilter) {

        if (aField == null || aField.trim().isEmpty()) {
            // Ignore invalid field names
            return;
        }

        pFieldFilters.put(aField.trim(), aFilter);
    }

    /**
     * Sets the forced fields filters map
     * 
     * @param aFilters
     *            the forced fields filters map
     */
    public void setFieldsFilters(final Map<String, String> aFilters) {

        pFieldFilters.clear();

        if (aFilters != null) {
            pFieldFilters.putAll(aFilters);
        }
    }

    /**
     * Sets the host isolate
     * 
     * @param aIsolate
     *            the host isolate
     */
    public void setIsolate(final String aIsolate) {

        pIsolate = aIsolate;
    }

    /**
     * Sets the component instance name
     * 
     * @param aName
     *            the instance name
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * Sets the parent container name
     * 
     * @param aParentName
     *            the name of the parent container
     */
    public void setParentName(final String aParentName) {

        pParentName = aParentName;
    }

    /**
     * Sets the new component properties
     * 
     * @param aProperties
     *            New component properties
     */
    public void setProperties(final Map<String, String> aProperties) {

        pProperties.clear();

        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * Sets the component type name
     * 
     * @param aType
     *            the component type name
     */
    public void setType(final String aType) {

        pType = aType;
    }

    /**
     * Sets the component wires
     * 
     * @param aWires
     *            the component wires
     */
    public void setWires(final Map<String, String> aWires) {

        pWires.clear();

        if (aWires != null) {
            pWires.putAll(aWires);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("Component(");
        builder.append("Name=").append(pName);
        builder.append(", Type=").append(pType);
        builder.append(", Isolate=").append(pIsolate);
        builder.append(", Parent=").append(pParentName);
        builder.append(")");

        return builder.toString();
    }
}
