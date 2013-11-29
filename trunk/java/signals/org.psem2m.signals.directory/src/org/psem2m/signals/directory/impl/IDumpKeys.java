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

    /** Node UID (access) */
    String NODE_UID = "node_uid";

    /** Node UID -&gt; Host name map */
    String NODES_HOST = "nodes_host";

    /** Node name -&gt; set of node UIDs */
    String NODES_NAMES = "nodes_names";

    /** The isolate port (access) */
    String PORT = "port";
}
