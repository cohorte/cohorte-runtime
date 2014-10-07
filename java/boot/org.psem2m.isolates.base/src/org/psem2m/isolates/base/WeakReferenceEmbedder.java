/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
