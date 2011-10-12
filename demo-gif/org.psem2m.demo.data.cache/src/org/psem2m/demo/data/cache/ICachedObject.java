/**
 * File:   ICachedObject.java
 * Author: "Thomas Calmant"
 * Date:   12 oct. 2011
 */
package org.psem2m.demo.data.cache;

/**
 * @author "Thomas Calmant"
 *
 * @param <T>
 */
public interface ICachedObject<T> {

    /**
     * Retrieves the age of the cached object
     * 
     * @return the age of the cached object
     */
    public abstract long getCacheAge();

    /**
     * Retrieves the cached object
     * 
     * @return the cached object
     */
    public abstract T getObject();

    /**
     * Resets the cached object age
     */
    public abstract void touch();

}
