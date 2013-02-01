/**
 * File:   IAgentSignals.java
 * Author: Thomas Calmant
 * Date:   30 janv. 2013
 */
package org.cohorte.composer.api;

/**
 * Composer agents signals
 * 
 * @author Thomas Calmant
 */
public interface IAgentSignals {

    /**
     * A component has been successfully instantiated
     * 
     * Content: A component UID (String)
     */
    String COMPONENT_INSTANTIATED = IAgentSignals.PREFIX_EVENT_COMPONENT
            + "/instantiated";

    /**
     * A component has been invalidated
     * 
     * Content: A component UID (String)
     */
    String COMPONENT_INVALIDATED = IAgentSignals.PREFIX_EVENT_COMPONENT
            + "/invalidated";

    /**
     * A component has been lost
     * 
     * Content: A component UID (String)
     */
    String COMPONENT_LOST = IAgentSignals.PREFIX_EVENT_COMPONENT + "/lost";

    /**
     * A component has been validated
     * 
     * Content: A component UID (String)
     */
    String COMPONENT_VALIDATED = IAgentSignals.PREFIX_EVENT_COMPONENT
            + "/validated";

    /**
     * One or more factories have disappeared.
     * 
     * Content: An array of names (String[])
     */
    String FACTORY_LOST = IAgentSignals.PREFIX_EVENT_FACTORY + "/lost";

    /**
     * One or more factories are available.
     * 
     * Content: An array of names (String[])
     */
    String FACTORY_READY = IAgentSignals.PREFIX_EVENT_FACTORY + "/ready";

    /** Composer agent signals prefix */
    String PREFIX_COMMON = "/cohorte-composer-agent";

    /** Prefix for event signals */
    String PREFIX_EVENT = IAgentSignals.PREFIX_COMMON + "/event";

    /** Prefix for component signals */
    String PREFIX_EVENT_COMPONENT = IAgentSignals.PREFIX_EVENT + "/component";

    /** Prefix for factory signals */
    String PREFIX_EVENT_FACTORY = IAgentSignals.PREFIX_EVENT + "/factory";

    /** Prefix for request signals */
    String PREFIX_REQUEST = IAgentSignals.PREFIX_COMMON + "/request";

    /**
     * Tests if the agent receiving this signal can handle the given components
     * 
     * Content: Configuration of components (Map component UID (String) -&gt;
     * configuration (Object))
     * 
     * Response: The list of handled components UID (String[])
     */
    String REQUEST_CAN_HANDLE = IAgentSignals.PREFIX_REQUEST + "/can-handle";

    /**
     * Instantiates of the given components.
     * 
     * Content: Configuration of components (Map component UID -&gt;
     * configuration)
     * 
     * Response by events.
     */
    String REQUEST_INSTANTIATE = IAgentSignals.PREFIX_REQUEST + "/instantiate";

    /**
     * Stops (deletes) the given components.
     * 
     * Content: A list of components UIDs (String[])
     */
    String REQUEST_STOP = IAgentSignals.PREFIX_REQUEST + "/stop";
}
