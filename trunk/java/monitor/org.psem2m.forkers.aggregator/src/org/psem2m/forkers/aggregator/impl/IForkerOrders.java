/**
 * File:   IForkerOrders.java
 * Author: Thomas Calmant
 * Date:   31 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

/**
 * Definition of the constants used by the forker aggregator to communicate with
 * forkers
 * 
 * @author Thomas Calmant
 */
public interface IForkerOrders {

    /** The order ID request key */
    String CMD_ID = "requestToken";

    /** The order result key */
    String RESULT_CODE = "result";

    /** The forker heart beat */
    String SIGNAL_HEART_BEAT = IForkerOrders.SIGNAL_PREFIX + "heart-beat";

    /** The signal match string */
    String SIGNAL_MATCH_ALL = IForkerOrders.SIGNAL_PREFIX + "*";

    /** The ping isolate signal */
    String SIGNAL_PING_ISOLATE = IForkerOrders.SIGNAL_PREFIX + "ping";

    /** The platform stopping signal */
    String SIGNAL_PLATFORM_STOPPING = IForkerOrders.SIGNAL_PREFIX
            + "platform-stopping";

    /** The signals prefix */
    String SIGNAL_PREFIX = "/psem2m/internals/forkers/";

    /** The response signal */
    String SIGNAL_RESPONSE = IForkerOrders.SIGNAL_PREFIX + "response";

    /** The start isolate signal */
    String SIGNAL_START_ISOLATE = IForkerOrders.SIGNAL_PREFIX + "start";

    /** The stop isolate signal */
    String SIGNAL_STOP_ISOLATE = IForkerOrders.SIGNAL_PREFIX + "stop";
}
