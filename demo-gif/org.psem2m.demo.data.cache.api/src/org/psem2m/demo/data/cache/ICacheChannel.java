/**
 * File:   ICacheChannel.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache;

import java.io.Serializable;

/**
 * Describes a cache channel
 * 
 * @author Thomas Calmant
 * 
 * @param <K>
 *            The type of keys in the channel (must be serializable)
 * @param <V>
 *            The type of values in the channel (must be serializable)
 */
public interface ICacheChannel<K extends Serializable, V extends Serializable>
        extends Serializable {

    /**
     * Clears the channel content
     */
    void clear();

    /**
     * Retrieves the cached object associated to the given key
     * 
     * @param aKey
     *            A reference key
     * @return The associated object, null if none
     */
    ICachedObject<V> get(K aKey);

    /**
     * Retrieves a random object from the channel, null if the channel is empty
     * 
     * @return A random object, or null
     */
    ICachedObject<V> getRandomObject();

    /**
     * Tests if the channel is empty
     * 
     * @return True if the channel is empty
     */
    boolean isEmpty();

    /**
     * Puts or replace the given value at the given key
     * 
     * @param aKey
     *            A reference key
     * @param aValue
     *            The cached value
     * @return The previous cached value, null if none
     */
    ICachedObject<V> put(K aKey, V aValue);

    /**
     * Retrieves the number of cached objects
     * 
     * @return the number of cached objects
     */
    int size();
}
