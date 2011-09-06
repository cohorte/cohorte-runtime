/**
 * File:   IApplicationDescr.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.base.conf;

import java.io.Serializable;
import java.util.Set;

/**
 * Description of a PSEM2M application
 * 
 * @author Thomas Calmant
 */
public interface IApplicationDescr extends Serializable {

    /**
     * Retrieves the application ID
     * 
     * @return The application ID
     */
    String getApplicationId();

    /**
     * Retrieves the description of the given isolate, null if not found
     * 
     * @param aId
     *            An isolate ID
     * @return The isolate description or null
     */
    IIsolateDescr getIsolate(String aId);

    /**
     * Retrieves the list of the found isolate IDs
     * 
     * @return The list of isolate IDs
     */
    Set<String> getIsolateIds();
}
