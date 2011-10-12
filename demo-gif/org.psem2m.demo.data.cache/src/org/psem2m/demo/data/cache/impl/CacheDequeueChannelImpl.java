/**
 * File:   CacheDequeueChannelImpl.java
 * Author: Thomas Calmant
 * Date:   12 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICachedObject;

/**
 * Implementation of a queued cache channel
 * 
 * @author Thomas Calmant
 */
public class CacheDequeueChannelImpl<K extends Serializable, V extends Serializable>
        extends LinkedBlockingDeque<ICachedObject<V>> implements
        ICacheDequeueChannel<K, V> {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The internal association map */
    private final Map<K, ICachedObject<V>> pAssociationMap = new HashMap<K, ICachedObject<V>>();

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#close()
     */
    @Override
    public void close() {

        clear();
        pAssociationMap.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#get(java.lang.Object)
     */
    @Override
    public ICachedObject<V> get(final K aKey) {

        return pAssociationMap.get(aKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheChannel#getRandomObject()
     */
    @Override
    public ICachedObject<V> getRandomObject() {

        final int nbObjects = size();
        if (nbObjects == 0) {
            // No objects, no random
            return null;
        }

        final int randomIndex = (int) Math.random() * nbObjects;

        // Get the ID at the selected index
        final Iterator<ICachedObject<V>> iterator = iterator();
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
     * @see org.psem2m.demo.data.cache.ICacheChannel#put(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public ICachedObject<V> put(final K aKey, final V aValue) {

        final ICachedObject<V> cachedObject = new CachedObject<V>(aValue);

        if (!contains(cachedObject)) {
            // Add in the queue, if needed
            offer(cachedObject);
        }

        return pAssociationMap.put(aKey, cachedObject);
    }
}
