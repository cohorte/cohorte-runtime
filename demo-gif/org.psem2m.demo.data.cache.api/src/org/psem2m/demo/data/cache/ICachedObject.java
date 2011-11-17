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
     * Retrieves the date of insertion of the cached object, i.e. the
     * {@link System#currentTimeMillis()} value at the time of its insertion in
     * the cache (in milliseconds).
     * 
     * @return the age of the cached object (in milliseconds)
     */
    long getCacheAge();

    /**
     * Retrieves the cached object
     * 
     * @return the cached object
     */
    T getObject();

    /**
     * Tests if this object has an acceptable age, according to the given
     * maximum age.
     * 
     * Comparison is done with (System.currentTimeMillis() - getCacheAge()) &lt;
     * aMaxAge.
     * 
     * @param aMaxAge
     *            A maximum cache age (in milliseconds)
     * @return True if the object is still acceptable
     */
    boolean isAcceptable(long aMaxAge);

    /**
     * Resets the cached object age
     */
    void touch();
}
