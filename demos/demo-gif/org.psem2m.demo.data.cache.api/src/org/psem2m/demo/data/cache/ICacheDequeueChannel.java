/**
 * File:   ICacheDequeueChannel.java
 * Author: Thomas Calmant
 * Date:   12 oct. 2011
 */
package org.psem2m.demo.data.cache;

import java.io.Serializable;
import java.util.concurrent.BlockingDeque;

/**
 * Defines a queued cache channel
 * 
 * @author Thomas Calmant
 * 
 * @param <K>
 *            The type of keys in the channel (must be serializable)
 * @param <V>
 *            The type of values in the channel (must be serializable)
 */
public interface ICacheDequeueChannel<K extends Serializable, V extends Serializable>
        extends ICacheChannel<K, V>, BlockingDeque<ICachedObject<V>>,
        Serializable {

    /**
     * Adds the given value to the channel (creates the cached object)
     * 
     * @param aValue
     *            The value to be cached
     * @return True on success
     */
    boolean add(V aValue);
}
