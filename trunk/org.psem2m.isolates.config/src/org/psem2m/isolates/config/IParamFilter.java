/**
 * File:   IParamFilter.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.config;

/**
 * Describes a configuration parameter filter
 * 
 * @author Thomas Calmant
 */
public interface IParamFilter {

    /**
     * Tests if the given parameter matches this filter
     * 
     * @param aParam
     *            Parameter to be tested
     * @return True if the parameter matches this filter
     */
    boolean match(IParamId aParam);
}
