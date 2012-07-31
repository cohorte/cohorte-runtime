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
    String BROADCASTER_SIGNAL_NAME_PREFIX = "/psem2m/remote-service-broadcaster";

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

    /** Signal result code: internal error */
    int CODE_INTERNAL_ERROR = 501;

    /** Signal result code: no listener found */
    int CODE_NO_LISTENER = 404;

    /** Signal result code: no error */
    int CODE_OK = 200;

    /**
     * Lost contact with isolate
     * 
     * Associated data : the Isolate ID (CharSequence)
     */
    String ISOLATE_LOST_SIGNAL = "/psem2m/isolate/lost";

    /**
     * IsolateStatus signal
     * 
     * Associated data : an IsolateStatus object
     */
    String ISOLATE_STATUS_SIGNAL = "/psem2m/isolate/status";

    /**
     * Stop isolate signal
     * 
     * Associated data : none
     */
    String ISOLATE_STOP_SIGNAL = "/psem2m/isolate/stop";

    /** Match all sub signals */
    String MATCH_ALL = "/*";

    /**
     * Platform stop signal
     * 
     * Associated data : none
     */
    String MONITOR_SIGNAL_STOP_PLATFORM = "/psem2m/platform/stop";
}
