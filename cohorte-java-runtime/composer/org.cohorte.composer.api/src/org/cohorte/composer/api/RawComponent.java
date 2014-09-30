/**
 * File:   RawComponent.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2013
 */
package org.cohorte.composer.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a component, as described in the configuration file. This bean is
 * meant to be transmitted by remote services.
 *
 * @author Thomas Calmant
 */
public class RawComponent {

    /** JSON class name, for Cohorte in Python */
    public static final String JSON_CLASS = "cohorte.composer.beans.RawComponent";

    /** Name of the bundle providing the component factory */
    private String pBundleName;

    /** Bundle version string */
    private String pBundleVersion;

    /** Factory name */
    private String pFactory;

    /** Field name -&gt; LDAP filter */
    private final Map<String, String> pFilters = new LinkedHashMap<String, String>();

    /** Name of the isolate that will host the component */
    private String pIsolate;

    /** Implementation language */
    private String pLanguage;

    /** Instance name */
    private String pName;

    /** Name of the node that will host the component */
    private String pNode;

    /** Properties of the components */
    private final Map<String, Object> pProperties = new LinkedHashMap<String, Object>();

    /** Field name -&gt; Component name */
    private final Map<String, String> pWires = new LinkedHashMap<String, String>();

    /**
     * Default constructor
     */
    public RawComponent() {

        // Do nothing
    }

    /**
     * Parameterized constructor
     *
     * @param aFactory
     *            Name of the component factory
     * @param aName
     *            Name of the component instance
     */
    public RawComponent(final String aFactory, final String aName) {

        pFactory = aFactory;
        pName = aName;
    }

    /**
     * Returns the JSON class for Python
     *
     * @return
     */
    public Object[] get__jsonclass__() {

        return new Object[] { JSON_CLASS, new Object[] {} };
    }

    /**
     * @return the bundleName
     */
    public String getBundle_name() {

        return pBundleName;
    }

    /**
     * @return the bundleVersion
     */
    public String getBundle_version() {

        return pBundleVersion;
    }

    /**
     * @return the factory
     */
    public String getFactory() {

        return pFactory;
    }

    /**
     * @return the filters
     */
    public Map<String, String> getFilters() {

        return pFilters;
    }

    /**
     * @return the isolate
     */
    public String getIsolate() {

        return pIsolate;
    }

    /**
     * @return the language
     */
    public String getLanguage() {

        return pLanguage;
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

        return pProperties;
    }

    /**
     * @return the wires
     */
    public Map<String, String> getWires() {

        return pWires;
    }

    /**
     * Compatibility method
     */
    public void set__jsonclass__(final Object[] aValue) {

        // Do nothing
    }

    /**
     * @param aBundleName
     *            the bundleName to set
     */
    public void setBundle_name(final String aBundleName) {

        pBundleName = aBundleName;
    }

    /**
     * @param aBundleVersion
     *            the bundleVersion to set
     */
    public void setBundle_version(final String aBundleVersion) {

        pBundleVersion = aBundleVersion;
    }

    /**
     * @param aFactory
     *            the factory to set
     */
    public void setFactory(final String aFactory) {

        pFactory = aFactory;
    }

    /**
     * @param aFilters
     *            the filters to set
     */
    public void setFilters(final Map<String, String> aFilters) {

        if (aFilters == null) {
            // Clean up
            pFilters.clear();

        } else {
            pFilters.putAll(aFilters);
        }
    }

    /**
     * @param aIsolate
     *            the isolate to set
     */
    public void setIsolate(final String aIsolate) {

        pIsolate = aIsolate;
    }

    /**
     * @param aLanguage
     *            the language to set
     */
    public void setLanguage(final String aLanguage) {

        pLanguage = aLanguage;
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

        if (aProperties == null) {
            // Clean up
            pProperties.clear();

        } else {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * @param aWires
     *            the wires to set
     */
    public void setWires(final Map<String, String> aWires) {

        if (aWires == null) {
            // Clean up
            pWires.clear();

        } else {
            pWires.putAll(aWires);
        }
    }
}
