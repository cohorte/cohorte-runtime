/**
 * File:   IDumpKeys.java
 * Author: Thomas Calmant
 * Date:   21 janv. 2013
 */
package org.psem2m.signals.directory.impl;

/**
 * Defines the keys to use in a dump packet
 * 
 * @author Thomas Calmant
 */
public interface IDumpKeys {

    /** UID -&gt; Access map */
    String ACCESSES = "accesses";

    /** Isolate name (access) */
    String NAME = "name";

    /** The isolate node (access) */
    String NODE = "node";

    /** Node -&gt; Host name map */
    String NODES_HOST = "nodes_host";

    /** The isolate port (access) */
    String PORT = "port";
}
