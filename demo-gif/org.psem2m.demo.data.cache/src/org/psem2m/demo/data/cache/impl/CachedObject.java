/**
 * File:   CachedObject.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.io.Serializable;

import org.psem2m.demo.data.cache.ICachedObject;

/**
 * Stores a cached object information
 * 
 * @author Thomas Calmant
 */
public class CachedObject<T extends Serializable> implements ICachedObject<T>,
        Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (pCachedObject != null) {
            return pCachedObject.equals(aObj);
        }

        return super.equals(aObj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICachedObject#getCacheAge()
     */
    @Override
    public long getCacheAge() {

        return pCacheAge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICachedObject#getObject()
     */
    @Override
    public T getObject() {

        return pCachedObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICachedObject#touch()
     */
    @Override
    public void touch() {

        pCacheAge = System.currentTimeMillis();
    }
}
