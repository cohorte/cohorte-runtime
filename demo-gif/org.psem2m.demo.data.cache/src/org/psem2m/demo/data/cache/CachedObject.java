/**
 * File:   CachedObject.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache;

/**
 * Stores a cached object information
 * 
 * @author Thomas Calmant
 */
public class CachedObject<T> {

    /** The cached object age */
    private long pCacheAge;

    /** The cached object */
    private T pCachedObject;

    /**
     * Sets up the members
     * 
     * @param aObject
     *            The cached object
     */
    public CachedObject(final T aObject) {

        pCachedObject = aObject;
        pCacheAge = System.currentTimeMillis();
    }

    /**
     * Retrieves the age of the cached object
     * 
     * @return the age of the cached object
     */
    public long getCacheAge() {

        return pCacheAge;
    }

    /**
     * Retrieves the cached object
     * 
     * @return the cached object
     */
    public T getObject() {

        return pCachedObject;
    }

    /**
     * Resets the cached object age
     */
    public void touch() {

        pCacheAge = System.currentTimeMillis();
    }
}
