/**
 * File:   ComponentDescription.java
 * Author: Thomas Calmant
 * Date:   28 mars 2013
 */
package org.cohorte.composer.agent;

import java.util.Properties;

/**
 * Minimalist component instance description triplet
 * 
 * @author Thomas Calmant
 */
public class ComponentDescription {

    /** Component factory */
    private final String pFactory;

    /** Instance properties */
    private final Properties pInstanceProperties;

    /** Instance name */
    private final String pName;

    /** Service properties */
    private final Properties pServiceProperties;

    /**
     * Sets up the component description
     * 
     * @param aName
     *            Instance name
     * @param aFactory
     *            Component factory
     * @param aInstanceProperties
     *            Component instance properties
     * @param aServiceProperties
     *            Provided services properties
     */
    public ComponentDescription(final String aName, final String aFactory,
            final Properties aInstanceProperties,
            final Properties aServiceProperties) {

        pName = aName;
        pFactory = aFactory;
        pInstanceProperties = aInstanceProperties;
        pServiceProperties = aServiceProperties;
    }

    /**
     * Retrieves the component factory
     * 
     * @return The component factory
     */
    public String getFactory() {

        return pFactory;
    }

    /**
     * Retrieves the component instance properties
     * 
     * @return The instance properties
     */
    public Properties getInstanceProperties() {

        return pInstanceProperties;
    }

    /**
     * Retrieves the instance name
     * 
     * @return The instance name
     */
    public String getName() {

        return pName;
    }

    /**
     * Retrieves the provided services properties
     * 
     * @return The services properties
     */
    public Properties getServiceProperties() {

        return pServiceProperties;
    }
}
