/**
 * File:   IRegistrationKeys.java
 * Author: Thomas Calmant
 * Date:   21 janv. 2013
 */
package org.psem2m.signals.directory.impl;

/**
 * Defines the keys to use in a registration packet
 * 
 * @author Thomas Calmant
 */
public interface IRegistrationKeys {

    /** The isolate address */
    String ADDRESS = "address";

    /** The isolate name */
    String NAME = "name";

    /** The isolate node */
    String NODE = "node";

    /** The isolate port */
    String PORT = "port";

    /** The registration propagation flag */
    String PROPAGATE = "propagate";

    /** The isolate UID */
    String UID = "uid";
}
