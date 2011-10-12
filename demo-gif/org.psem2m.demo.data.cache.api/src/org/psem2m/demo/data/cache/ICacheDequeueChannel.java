/**
 * File:   ICacheDequeueChannel.java
 * Author: Thomas Calmant
 * Date:   12 oct. 2011
 */
package org.psem2m.demo.data.cache;

import java.util.concurrent.BlockingDeque;

/**
 * Defines a queued cache channel
 * 
 * @author Thomas Calmant
 */
public interface ICacheDequeueChannel<K, V> extends ICacheChannel<K, V>,
        BlockingDeque<ICachedObject<V>> {
}
