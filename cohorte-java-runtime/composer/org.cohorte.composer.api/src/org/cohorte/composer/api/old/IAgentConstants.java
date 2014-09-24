/**
 * File:   IAgentConstants.java
 * Author: Thomas Calmant
 * Date:   28 mars 2013
 */
package org.cohorte.composer.api.old;

/**
 * Common instances constants
 * 
 * @author Thomas Calmant
 */
public interface IAgentConstants {

    /**
     * Component instance name
     */
    String PROP_COMPONENT_NAME = "cohorte.composer.component.name";

    /**
     * Component instance UID
     */
    String PROP_COMPONENT_UID = "cohorte.composer.component.uid";

    /**
     * Component parent composite name
     */
    String PROP_COMPOSITE_NAME = "cohorte.composer.composite.name";

    /**
     * UID of the isolate hosting the component
     */
    String PROP_HOST_ISOLATE = "cohorte.composer.isolate";
}
