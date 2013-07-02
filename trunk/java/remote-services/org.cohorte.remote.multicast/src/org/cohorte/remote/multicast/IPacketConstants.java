/**
 * File:   IPacketConstants.java
 * Author: Thomas Calmant
 * Date:   28 juin 2013
 */
package org.cohorte.remote.multicast;

/**
 * Defines the constant keys and values used in Pelix remote services discovery
 * 
 * @author Thomas Calmant
 */
public interface IPacketConstants {

    /** A remote service have been added */
    String EVENT_ADD = "add";

    /** Reply to a discovery packet */
    String EVENT_DISCOVERED = "discovered";

    /** Request for all isolates to signal themselves */
    String EVENT_DISCOVERY = "discovery";

    /** A remote service have been removed */
    String EVENT_REMOVE = "remove";

    /** A remote service have been updated */
    String EVENT_UPDATE = "update";

    /** The access to the storage servlet */
    String KEY_ACCESS = "access";

    /** Path to the servlet */
    String KEY_ACCESS_PATH = "path";

    /** Port to the servlet HTTP server */
    String KEY_ACCESS_PORT = "port";

    /** New properties of an updated service */
    String KEY_ENDPOINT_NEW_PROPERTIES = "new_properties";

    /** The UID of an end point */
    String KEY_ENDPOINT_UID = "uid";

    /** The kind of event */
    String KEY_EVENT = "event";

    /** The UID of the sender of the event */
    String KEY_SENDER = "sender";
}
