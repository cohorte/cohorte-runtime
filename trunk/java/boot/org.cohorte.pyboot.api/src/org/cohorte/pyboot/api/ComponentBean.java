/**
 * File:   ComponentBean.java
 * Author: Thomas Calmant
 * Date:   17 janv. 2013
 */
package org.cohorte.pyboot.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a basic component
 * 
 * @author Thomas Calmant
 */
public class ComponentBean {

    /** ComponentBean factory name (type) */
    private final String pFactory;

    /** Instance name */
    private final String pName;

    /** Instance properties */
    private final Map<String, Object> pProperties = new HashMap<String, Object>();

    /**
     * Setup the component bean
     * 
     * @param aFactory
     *            ComponentBean type
     * @param aName
     *            Instance name
     * @param aProperties
     *            Instance properties
     */
    public ComponentBean(final String aFactory, final String aName,
            final Map<String, Object> aProperties) {

        pFactory = aFactory;
        pName = aName;
        if (aProperties != null) {
            // Copy given properties
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
     * @return the properties
     */
    public Map<String, Object> getProperties() {

        return new HashMap<String, Object>(pProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Component(name='" + pName + "', factory='" + pFactory + "')";
    }
}
