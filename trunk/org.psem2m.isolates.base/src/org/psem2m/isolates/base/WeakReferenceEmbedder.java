/**
 * File:   WeakReferenceEmbedder.java
 * Author: Thomas Calmant
 * Date:   14 déc. 2011
 */
package org.psem2m.isolates.base;

import java.lang.ref.WeakReference;

/**
 * Embeds an object weak reference
 * 
 * @author Thomas Calmant
 * 
 * @param <T>
 *            The embedded reference type
 */
public class WeakReferenceEmbedder<T> {

    /** The embedded reference */
    private WeakReference<T> pContent;

    /**
     * Retrieves the embedded object
     * 
     * @return the embedded object
     */
    public T get() {

        if (pContent == null) {
            return null;
        }

        return pContent.get();
    }

    /**
     * Resets the content to null
     */
    public void reset() {

        if (pContent != null) {
            pContent.clear();
        }

        pContent = null;
    }

    /**
     * Sets the embedded object
     * 
     * @param aContent
     *            The new embedded object
     */
    public void set(final T aContent) {

        if (aContent == null) {
            pContent = null;

        } else {
            pContent = new WeakReference<T>(aContent);
        }
    }
}
