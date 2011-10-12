/**
 * File:   CacheChannelImpl.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICachedObject;

/**
 * Implementation of a cache channel
 * 
 * @author Thomas Calmant
 */
public class CacheChannelImpl<K, V> implements ICacheChannel<K, V> {

    /** The internal map */
    private final Map<K, CachedObject<V>> pCacheMap = new HashMap<K, CachedObject<V>>();

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#close()
     */
    @Override
    public void close() {

        pCacheMap.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#get(java.lang.Object)
     */
    @Override
    public ICachedObject<V> get(final K aKey) {

        return pCacheMap.get(aKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#getRandomObject()
     */
    @Override
    public ICachedObject<V> getRandomObject() {

        final int nbObjects = pCacheMap.size();
        if (nbObjects == 0) {
            // No objects, no random
            return null;
        }

        final int randomIndex = (int) Math.random() * nbObjects;

        // Get the ID at the selected index
        K randomObjectKey = null;
        final Iterator<K> iterator = pCacheMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {

            if (i == randomIndex) {
                // Found !
                randomObjectKey = iterator.next();
                break;
            }

            // Continue
            i++;
            iterator.next();
        }

        return pCacheMap.get(randomObjectKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#isEmpty()
     */
    @Override
    public boolean isEmpty() {

        return pCacheMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#put(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public ICachedObject<V> put(final K aKey, final V aValue) {

        if (aValue == null) {
            return pCacheMap.remove(aKey);

        } else {
            return pCacheMap.put(aKey, new CachedObject<V>(aValue));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#size()
     */
    @Override
    public long size() {

        return pCacheMap.size();
    }
}
