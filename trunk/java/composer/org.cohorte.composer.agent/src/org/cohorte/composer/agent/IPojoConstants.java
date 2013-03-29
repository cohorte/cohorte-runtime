/**
 * File:   IpojoConstants.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.cohorte.composer.agent;

/**
 * iPOJO constant properties
 * 
 * @author Thomas Calmant
 */
public interface IPojoConstants {

    /** Provided service handler name */
    String HANDLER_PROVIDED_SERVICE = "org.apache.felix.ipojo:provides";

    /** The instance name of a component */
    String INSTANCE_NAME = "instance.name";

    /** iPOJO Requires handler metadata element name */
    String REQUIRES_ELEMENT_NAME = "Requires";

    /** iPOJO Requires handler field name attribute */
    String REQUIRES_FIELD = "field";

    /** Requires handler : filter attribute */
    String REQUIRES_FILTERS = "requires.filters";

    /** Requires handler : from attribute */
    String REQUIRES_FROM = "requires.from";

    /** iPOJO Requires handler field ID attribute */
    String REQUIRES_ID = "id";

    /** iPOJO temporal handler metadata element name (with namespace) */
    String TEMPORAL_ELEMENT_NAME = "org.apache.felix.ipojo.handler.temporal:temporal";

    /** iPOJO Requires handler field name attribute (same as Requires one) */
    String TEMPORAL_FIELD = REQUIRES_FIELD;

    /** Temporal handler : filter attribute */
    String TEMPORAL_FILTERS = "temporal.filters";

    /** iPOJO Requires handler Id name attribute (same as Requires one) */
    String TEMPORAL_ID = REQUIRES_ID;
}
