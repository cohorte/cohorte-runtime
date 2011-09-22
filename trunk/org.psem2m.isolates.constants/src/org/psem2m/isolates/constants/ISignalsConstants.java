/**
 * File:   ISignalsConstants.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.isolates.constants;

/**
 * PSEM2M Signals system constants
 * 
 * @author Thomas Calmant
 */
public interface ISignalsConstants {

    /** Remote service broadcaster signals names prefix */
    String BROADCASTER_SIGNAL_NAME_PREFIX = "/remote-service-broadcaster";

    /**
     * Broadcaster Remote Event notification signal.
     * 
     * Associated data : an instance of
     * {@link org.psem2m.isolates.services.remote.beans.RemoteServiceEvent}
     */
    String BROADCASTER_SIGNAL_REMOTE_EVENT = BROADCASTER_SIGNAL_NAME_PREFIX
            + "/remote-event";

    /**
     * Broadcaster end points update request signal.
     * 
     * Associated data : none
     */
    String BROADCASTER_SIGNAL_REQUEST_ENDPOINTS = BROADCASTER_SIGNAL_NAME_PREFIX
            + "/request-endpoints";

    /** Match all sub signals */
    String MATCH_ALL = "/*";
}
