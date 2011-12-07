/**
 * File:   ComposerAgentConstants.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.agent;

/**
 * Defines composer agent constants
 * 
 * @author Thomas Calmant
 */
public interface ComposerAgentConstants {

    /** The composite name component property */
    String COMPOSITE_NAME = "org.psem2m.composer.composite.name";

    /** The ID of the isolate hosting the component instance */
    String HOST_ISOLATE = "org.psem2m.composer.isolate";

    /** iPOJO Requires handler metadata element name */
    String REQUIRES_ELEMENT_NAME = "Requires";

    /** iPOJO Requires handler field name attribute */
    String REQUIRES_FIELD = "field";

    /** iPOJO Requires handler field ID attribute */
    String REQUIRES_ID = "id";

    /** iPOJO temporal handler metadata element name (with namespace) */
    String TEMPORAL_ELEMENT_NAME = "org.apache.felix.ipojo.handler.temporal:temporal";

    /** iPOJO Requires handler field name attribute (same as Requires one) */
    String TEMPORAL_FIELD = REQUIRES_FIELD;

    /** iPOJO Requires handler Id name attribute (same as Requires one) */
    String TEMPORAL_ID = REQUIRES_ID;
}
