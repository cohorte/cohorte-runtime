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
     * <p>
     * Value type : String
     * </p>
     */
    String RESULT_KEY_COMPOSITE = "composite";

    /**
     * Failed components names key in a component instantiation result
     * 
     * <p>
     * Value type : String[]
     * </p>
     */
    String RESULT_KEY_FAILED = "failed";

    /**
     * Instantiated components names key in a component instantiation result
     * 
     * <p>
     * Value type : String[]
     * </p>
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
     * <p>
     * Value type : String[]
     * </p>
     */
    String RESULT_KEY_UNKNOWN = "unknown";

    /**
     * Asks the agent to reply with a signal containing a sub-set of the given
     * components array that can be executed in the agent isolate.
     * 
     * <p>
     * Associated data : An array of components
     * </p>
     */
    String SIGNAL_CAN_HANDLE_COMPONENTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/can-handle-components";

    /**
     * Asks the agent to stop the given components.
     * 
     * <p>
     * Associated data : A Map
     * </p>
     * <ul>
     * <li>name (String) : The component name</li>
     * <li>state (ECompositionEvent) : The new component state</li>
     * </ul>
     */
    String SIGNAL_COMPONENT_CHANGED = ComposerAgentSignals.SIGNAL_PREFIX
            + "/component-changed";

    /** Prefix to factory state change signals */
    String SIGNAL_FACTORY_PREFIX = ComposerAgentSignals.SIGNAL_PREFIX
            + "/factory-state";

    /**
     * Asks the agent to start the given components.
     * 
     * <p>
     * Associated data : An array of components
     * </p>
     */
    String SIGNAL_INSTANTIATE_COMPONENTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/instantiate-components";

    /**
     * Signal sent to notify monitors that an isolate can now handle the given
     * factories.
     * 
     * <p>
     * Associated data : an array of factory names (string array)
     * </p>
     */
    String SIGNAL_ISOLATE_ADD_FACTORY = ComposerAgentSignals.SIGNAL_FACTORY_PREFIX
            + "/added";

    /**
     * Signal sent by the composer core to an agent. The agent must return all
     * of its factories.
     * 
     * <p>
     * Associated data : none
     * </p>
     */
    String SIGNAL_ISOLATE_FACTORIES_DUMP = ComposerAgentSignals.SIGNAL_FACTORY_PREFIX
            + "/dump";

    /**
     * Signal sent to notify monitors that an isolate agent is gone and to
     * forget all its factories
     * 
     * <p>
     * Associated data : none
     * </p>
     */
    String SIGNAL_ISOLATE_FACTORIES_GONE = ComposerAgentSignals.SIGNAL_FACTORY_PREFIX
            + "/all-gone";

    /**
     * Signal sent to notify monitors that an isolate can't handle the given
     * factories anymore.
     * 
     * <p>
     * Associated data : an array of factory names (string array)
     * </p>
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
     * <p>
     * Associated data : An array of components
     * </p>
     */
    String SIGNAL_RESPONSE_HANDLES_COMPONENTS = ComposerAgentSignals.SIGNAL_RESPONSE_PREFIX
            + "/can-handle-components";

    /**
     * Answers a {@link #SIGNAL_INSTANTIATE_COMPONENTS} from the composer core
     * with the components that couldn't be instantiated.
     * 
     * <p>
     * Associated data : A map :
     * </p>
     * <ul>
     * <li>{@link #RESULT_KEY_COMPOSITE} : Name of the instantiating components
     * set (String)</li>
     * <li>
     * {@link #RESULT_KEY_INSTANTIATED} : Names of successfully instantiated
     * components (String[])</li>
     * <li>{@link #RESULT_KEY_FAILED} : Names of failed components (String[])</li>
     * </ul>
     */
    String SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS = ComposerAgentSignals.SIGNAL_RESPONSE_PREFIX
            + "/instantiate-components";

    /** Response prefix */
    String SIGNAL_RESPONSE_PREFIX = ComposerAgentSignals.SIGNAL_PREFIX
            + "/response";

    /**
     * Asks the agent to stop the given components.
     * 
     * <p>
     * Associated data : An array of components
     * </p>
     */
    String SIGNAL_STOP_COMPONENTS = ComposerAgentSignals.SIGNAL_REQUEST_PREFIX
            + "/stop-components";
}
