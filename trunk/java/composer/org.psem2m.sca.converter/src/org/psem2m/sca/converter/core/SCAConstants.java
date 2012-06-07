/**
 * File:   SCAConstants.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.core;

/**
 * Constants for the SCA model parser
 * 
 * @author Thomas Calmant
 */
public interface SCAConstants {

    /** Component XML node name */
    String COMPONENT_ELEMENT = "component";

    /** Composite name attribute */
    String COMPOSITE_ATTR_NAME = "name";

    /** Composite target name space attribute */
    String COMPOSITE_ATTR_NAMESPACE = "targetNamespace";

    /** Composite XML node name */
    String COMPOSITE_ELEMENT = "composite";

    /** Composite implementation tag name */
    String IMPLEMENTATION_COMPOSITE = "implementation.composite";

    /** PSEM2M implementation tag name */
    String IMPLEMENTATION_PSEM2M = "implementation.psem2m";

    /** Property XML node name */
    String PROPERTY_ELEMENT = "property";

    /** PSEM2M extensions name space */
    String PSEM2M_NS = "http://www.psem2m.org/ns/psem2m-sca";

    /** Reference XML node name */
    String REFERENCE_ELEMENT = "reference";

    /** SCA XML name space */
    String SCA_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";

    /** Service XML node name */
    String SERVICE_ELEMENT = "service";

    /** SCA Type : Binding */
    String TYPE_BINDING = "Binding";

    /** SCA Type : Implementation */
    String TYPE_IMPLEMENTATION = "Implementation";

    /** SCA Type : Interface */
    String TYPE_INTERFACE = "Interface";

    /** Wire XML node name */
    String WIRE_ELEMENT = "wire";
}
