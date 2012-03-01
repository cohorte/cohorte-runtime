/**
 * File:   CacheChannelImpl.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.psem2m.demo.data.cache.CachedObject;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICachedObject;

/**
 * Implementation of a cache channel
 * 
 * @author Thomas Calmant
 */
public class CacheChannelImpl<K extends Serializable, V extends Serializable>
        implements ICacheChannel<K, V> {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The internal map */
    private final Map<K, CachedObject<V>> pCacheMap = new HashMap<K, CachedObject<V>>();

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#clear()
     */
    @Override
    public void clear() {

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

        final int randomIndex = (int) (Math.random() * nbObjects);

        // Get the ID at the selected index
        final Iterator<CachedObject<V>> iterator = pCacheMap.values()
                .iterator();
        int i = 0;
        while (iterator.hasNext()) {

            if (i == randomIndex) {
                // Found !
                return iterator.next();
            }

            // Continue
            i++;
            iterator.next();
        }

        // Shouldn't be there...
        return null;
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

        final ICachedObject<V> result;

        if (aValue == null) {
            result = pCacheMap.remove(aKey);

        } else {
            result = pCacheMap.put(aKey, new CachedObject<V>(aValue));
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#size()
     */
    @Override
    public int size() {

        return pCacheMap.size();
    }
}
