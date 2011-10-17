/**
 * File:   ICachedObject.java
 * Author: Thomas Calmant
 * Date:   12 oct. 2011
 */
package org.psem2m.demo.data.cache;

/**
 * Describes a cached object
 * 
 * @author Thomas Calmant
 * 
 * @param <T>
 *            The type if the cached object
 */
public interface ICachedObject<T> {

    /**
     * Retrieves the age of the cached object
     * 
     * @return the age of the cached object
     */
    long getCacheAge();

    /**
     * Retrieves the cached object
     * 
     * @return the cached object
     */
    T getObject();

    /**
     * Resets the cached object age
     */
    void touch();
}
