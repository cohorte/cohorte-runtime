/**
 * File:   ICachedBean.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.api.beans;

/**
 * Describes a cached bean
 * 
 * @author Thomas Calmant
 */
public interface ICachedBean {

    /**
     * Retrieves the quality level of cached information (see
     * {@link IQualityLevels})
     * 
     * @return the information quality level
     */
    int getQualityLevel();

    /**
     * Sets the quality level of cached information (see {@link IQualityLevels})
     * 
     * 
     * @param aQualityLevel
     *            the information quality level
     */
    void setQualityLevel(final int aQualityLevel);
}
