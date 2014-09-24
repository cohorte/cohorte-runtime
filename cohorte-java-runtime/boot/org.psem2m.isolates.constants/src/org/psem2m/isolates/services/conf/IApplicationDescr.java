/**
 * File:   IApplicationDescr.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import java.io.Serializable;

/**
 * Description of a PSEM2M application
 * 
 * @author Thomas Calmant
 */
public interface IApplicationDescr extends Serializable {

    /**
     * The application ID : String (can't be null nor empty)
     */
    String APPLICATION_ID = "id";

    /**
     * The applications isolates : Map String (isolate ID) -&gt; Map String
     * (entry) -&gt; Object (value). Shouldn't be empty.
     */
    String APPLICATION_ISOLATES = "isolates";
}
