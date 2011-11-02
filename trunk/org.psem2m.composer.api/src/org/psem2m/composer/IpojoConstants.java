/**
 * File:   IpojoConstants.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer;

/**
 * iPOJO constant properties
 * 
 * @author Thomas Calmant
 */
public interface IpojoConstants {

    /** Provided service handler name */
    String HANDLER_PROVIDED_SERVICE = "org.apache.felix.ipojo:provides";

    /** The instance name of a component */
    String INSTANCE_NAME = "instance.name";

    /** Requires handler : filter attribute */
    String REQUIRES_FILTERS = "requires.filters";

    /** Requires handler : from attribute */
    String REQUIRES_FROM = "requires.from";
}
