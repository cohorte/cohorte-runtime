/**
 * File:   IMulticastConstants.java
 * Author: Thomas Calmant
 * Date:   14 juin 2012
 */
package org.psem2m.signals.multicast;

/**
 * Constants for the multicast agent
 * 
 * @author Thomas Calmant
 */
public interface IMulticastConstants {

    /** Isolate registration packet */
    byte PACKET_REGISTER = 1;

    /**
     * The PSEM2M multicast port
     * 
     * FIXME: It should be read from the configuration
     */
    int PSEM2M_MULTICAST_PORT = 42000;

    /**
     * Signal received to confirm a registration
     */
    String SIGNAL_CONFIRM_BEAT = IMulticastConstants.SIGNAL_PREFIX + "/confirm";

    /**
     * The multicast agent signals filter
     */
    String SIGNAL_MATCH_ALL = IMulticastConstants.SIGNAL_PREFIX + "/*";

    /**
     * The multicast agent signals prefix
     */
    String SIGNAL_PREFIX = "/psem2m-multicast-agent";
}
