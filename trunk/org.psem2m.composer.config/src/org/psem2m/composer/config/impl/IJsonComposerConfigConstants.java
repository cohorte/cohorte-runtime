/**
 * File:   IJsonComposerConfigConstants.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.config.impl;

/**
 * PSEM2M Composer JSON Configuration file constants
 * 
 * @author Thomas Calmant
 */
public interface IJsonComposerConfigConstants {

    /** Component fields filters */
    String COMPONENT_FIELDS_FILTERS = "filters";

    /** Host isolate of a component */
    String COMPONENT_ISOLATE = "isolate";

    /** Name of a component */
    String COMPONENT_NAME = "name";

    /** Properties of a component */
    String COMPONENT_PROPERTIES = "properties";

    /** Type of a component */
    String COMPONENT_TYPE = "type";

    /** Wires of the component */
    String COMPONENT_WIRES = "wires";

    /** Components set content */
    String COMPOSET_COMPONENTS = "components";

    /** Components set sub-sets */
    String COMPOSET_COMPOSETS = "composets";

    /** Array defining the content of a components set */
    String COMPOSET_CONTENT = "composet";

    /** Content of a components set */
    String COMPOSET_CONTENT_TYPE = "content";

    /** The components set contains only components */
    String COMPOSET_CONTENT_TYPE_COMPONENTS = "components";

    /** The components set contains only components sets */
    String COMPOSET_CONTENT_TYPE_SETS = "sets";

    /** Name of a components set */
    String COMPOSET_NAME = "name";
}
