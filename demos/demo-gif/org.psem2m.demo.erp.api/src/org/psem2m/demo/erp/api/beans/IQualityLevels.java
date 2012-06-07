/**
 * File:   IQualityLevels.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.api.beans;

/**
 * Cache quality levels definition
 * 
 * @author Thomas Calmant
 */
public interface IQualityLevels {

    /** Item has been taken from cache, stored less than 5 minutes ago */
    int CACHE_LEVEL_ACCEPTABLE = 2;

    /** Item has been taken from cache, stored more than 15 minutes ago */
    int CACHE_LEVEL_CRITICAL = 4;

    /** Item has been taken from cache, stored less than 1 minute ago */
    int CACHE_LEVEL_FRESH = 1;

    /** Item is from the ERP (synchronized) */
    int CACHE_LEVEL_SYNC = 0;

    /** Item has been taken from cache, stored less than 15 minutes ago */
    int CACHE_LEVEL_WARNING = 3;
}
