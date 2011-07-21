/**
 * File:   ISvcConfig.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.config;

import java.util.Date;
import java.util.List;

/**
 * Description of the PSEM2M Configuration reader service.
 * 
 * @author Thomas Calmant
 */
public interface ISvcConfig {

    /**
     * Retrieves the object value of the given parameter
     * 
     * @param aParamId
     *            The parameter ID
     * @return The object value
     */
    Object getParam(IParamId aParamId);

    /**
     * Retrieves the boolean value of the given parameter
     * 
     * @param aParamId
     *            The parameter ID
     * @return The boolean value
     */
    Boolean getParamBool(IParamId aParamId);

    /**
     * Retrieves the date value of the given parameter
     * 
     * @param aParamId
     *            The parameter ID
     * @return The date value
     */
    Date getParamDate(IParamId aParamId);

    /**
     * Retrieves the object value of the given parameter
     * 
     * @param aParamId
     *            The parameter ID
     * @return The object list (null if non existent)
     */
    List<Object> getParamList(IParamId aParamId);

    /**
     * Retrieves the long value of the given parameter
     * 
     * @param aParamId
     *            The parameter ID
     * @return The long value
     */
    Long getParamNum(IParamId aParamId);

    /**
     * Retrieves the string value of the given parameter
     * 
     * @param aParamId
     *            The parameter ID
     * @return The string value
     */
    String getParamStr(IParamId aParamId);

    /**
     * Tests if the given parameter exists in the configuration
     * 
     * @param aParamId
     *            The parameter to be tested
     * @return True if it exists, else false
     */
    boolean isParamExists(IParamId aParamId);
}
