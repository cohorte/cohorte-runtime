/**
 * File:   ComposerAgentSignals.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.agent;

/**
 * Defines composer agent signals constants
 * 
 * @author Thomas Calmant
 */
public interface ComposerAgentSignals {

    /** The name of the component that changed */
    String COMPONENT_CHANGED_KEY_NAME = "name";

    /** The new state of the component that changed */
    String COMPONENT_CHANGED_KEY_STATE = "state";

    /** Filter that matches all request signals */
    String FILTER_ALL_REQUESTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/*";

    /** Filter that matches all request signals */
    String FILTER_ALL_RESPONSES = ComposerAgentSignals.SIGNAL_RESPONSE_PREFIX
            + "/*";

    /**
     * Composite name key in a component instantiation result
     * 
     * Value type : String
     */
    String RESULT_KEY_COMPOSITE = "composite";

    /**
     * Failed components names key in a component instantiation result
     * 
     * Value type : String[]
     */
    String RESULT_KEY_FAILED = "failed";

    /**
     * Instantiated components names key in a component instantiation result
     * 
     * Value type : String[]
     */
    String RESULT_KEY_INSTANTIATED = "instantiated";

    /**
     * Successfully stopped components names key
     * 
     * Value type : String[]
     */
    String RESULT_KEY_STOPPED = "stopped";

    /**
     * Unknown components names key : can't handle these components because
     * they're unknown.
     * 
     * Value type : String[]
     */
    String RESULT_KEY_UNKNOWN = "unknown";

    /**
     * Asks the agent to reply with a signal containing a sub-set of the given
     * components array that can be executed in the agent isolate.
     * 
     * Associated data : An array of components
     */
    String SIGNAL_CAN_HANDLE_COMPONENTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/can-handle-components";

    /**
     * Asks the agent to stop the given components.
     * 
     * Associated data : A Map <ul> <li>name (String) : The component name</li>
     * <li>state (Integer) : The new component state</li> </ul>
     */
    String SIGNAL_COMPONENT_CHANGED = ComposerAgentSignals.SIGNAL_PREFIX
            + "/component-changed";

    /** Prefix to factory state change signals */
    String SIGNAL_FACTORY_PREFIX = ComposerAgentSignals.SIGNAL_PREFIX
            + "/factory-state";

    /**
     * Asks the agent to start the given components.
     * 
     * Associated data : An array of components
     */
    String SIGNAL_INSTANTIATE_COMPONENTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/instantiate-components";

    /**
     * Signal sent to notify monitors that an isolate can now handle the given
     * factories.
     * 
     * Associated data : an array of factory names (string array)
     */
    String SIGNAL_ISOLATE_ADD_FACTORY = ComposerAgentSignals.SIGNAL_FACTORY_PREFIX
            + "/added";

    /**
     * Signal sent to notify monitors that an isolate agent is gone and to
     * forget all its factories
     * 
     * Associated data : none
     */
    String SIGNAL_ISOLATE_FACTORIES_GONE = ComposerAgentSignals.SIGNAL_FACTORY_PREFIX
            + "/all-gone";

    /**
     * Signal sent to notify monitors that an isolate can't handle the given
     * factories anymore.
     * 
     * Associated data : an array of factory names (string array)
     */
    String SIGNAL_ISOLATE_REMOVE_FACTORY = ComposerAgentSignals.SIGNAL_FACTORY_PREFIX
            + "/removed";

    /** Composer agent signals prefix */
    String SIGNAL_PREFIX = "/psem2m-composer-agent";

    /** Requests prefix */
    String SIGNAL_REQUEST_PREFIX = ComposerAgentSignals.SIGNAL_PREFIX
            + "/request";

    /**
     * Answers a {@link #SIGNAL_CAN_HANDLE_COMPONENTS} from the composer core
     * with the components that can be started by this isolate.
     * 
     * Associated data : An array of components
     */
    String SIGNAL_RESPONSE_HANDLES_COMPONENTS = ComposerAgentSignals.SIGNAL_RESPONSE_PREFIX
            + "/can-handle-components";

    /**
     * Answers a {@link #SIGNAL_INSTANTIATE_COMPONENTS} from the composer core
     * with the components that couldn't be instantiated.
     * 
     * Associated data : An array of components
     */
    String SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS = ComposerAgentSignals.SIGNAL_RESPONSE_PREFIX
            + "/instantiate-components";

    /** Response prefix */
    String SIGNAL_RESPONSE_PREFIX = ComposerAgentSignals.SIGNAL_PREFIX
            + "/response";

    /**
     * Asks the agent to stop the given components.
     * 
     * Associated data : An array of components
     */
    String SIGNAL_STOP_COMPONENTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/stop-components";
}
