/**
 * File:   ISignalsEventsConstants.java
 * Author: Thomas Calmant
 * Date:   15 déc. 2011
 */
package org.psem2m.isolates.constants;

/**
 * Constants used by the EventAdmin bridge
 * 
 * @author Thomas Calmant
 */
public interface ISignalsEventsConstants {

    /**
     * Defines event targets
     * 
     * Value : A EBaseGroup string value
     */
    String EXPORT_GROUP = "org.psem2m.event.export.group";

    /**
     * Defines event targets
     * 
     * Value: A collection or an array of strings
     */
    String EXPORT_ISOLATES = "org.psem2m.event.export.isolates";

    /**
     * Property to indicate that the event must be exported
     * 
     * Value : boolean
     */
    String EXPORTED = "org.psem2m.event.exported";

    /**
     * Property to indicate that the event has been imported
     * 
     * Value : boolean
     */
    String IMPORTED = "org.psem2m.event.imported";

    /**
     * Name of the signal, prefixing the event topic.
     * 
     * Trailing slash is needed, as topic name will be appended and it must not
     * start with a slash.
     */
    String SIGNAL_PREFIX = "/psem2m-event-admin-bridge/";
}
