/**
 * File:   ISvcConfig.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * PSEM2M Configuration service
 * 
 * @author Thomas Calmant
 */
public interface ISvcConfig {

    /**
     * Retrieves the description of current application
     * 
     * @return the description of current application
     */
    ApplicationDescription getApplication();

    /**
     * Retrieves the description of the current isolate
     * 
     * @return the description of the current isolate
     */
    IsolateDescription getCurrentIsolate();

    /**
     * Retrieves the description of the isolate described in the given string
     * 
     * @param aConfigurationString
     *            A configuration string
     * @return The parsed description, or null
     */
    IsolateDescription parseIsolate(String aConfigurationString);

    /**
     * Reloads configuration
     * 
     * @return True on success, else false
     */
    boolean refresh();

    /**
     * Sets the currently used isolate description. If the parameter is null,
     * then the next call to {@link #getCurrentIsolate()} will return
     * getApplication().getIsolate(<i>currentIsolate</i>).
     * 
     * @param aIsolateDescription
     *            An isolate description
     */
    void setCurrentIsolate(IsolateDescription aIsolateDescription);
}
