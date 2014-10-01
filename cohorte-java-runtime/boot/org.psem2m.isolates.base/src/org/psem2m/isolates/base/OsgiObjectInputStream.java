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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * A class loader aware ObjectInputStream.
 *
 * @author Thomas Calmant
 */
public class OsgiObjectInputStream extends ObjectInputStream {

    /** Bundle context */
    private final BundleContext pBundleContext;

    /**
     * Prepares the ObjectInputStream
     *
     * @param aBundleContext
     *            A valid bundle context
     * @param aInputStream
     *            An input stream
     *
     * @throws IOException
     *             The {@link ObjectInputStream} constructor failed
     *
     * @see ObjectInputStream#ObjectInputStream(InputStream)
     */
    public OsgiObjectInputStream(final BundleContext aBundleContext,
            final InputStream aInputStream) throws IOException {

        super(aInputStream);
        pBundleContext = aBundleContext;
    }

    /**
     * Tries to load the given class by looking into all available bundles.
     *
     * @param aBundles
     *            An array containing all bundles to search into
     * @param aClassName
     *            Name of the class to load
     * @param aAllowResolvedBundles
     *            Allows to look into bundles in RESOLVED state
     * @return The searched class, null if not found
     */
    private Class<?> findClassInBundles(final Bundle[] aBundles,
            final String aClassName, final boolean aAllowResolvedBundles) {

        if (aBundles == null) {
            // No bundles to look into
            return null;
        }

        // Prepare the state mask
        int stateMask = Bundle.ACTIVE;
        if (aAllowResolvedBundles) {
            stateMask |= Bundle.RESOLVED;
        }

        for (final Bundle bundle : aBundles) {
            // Check if the bundle state passes the mask
            final int bundleState = bundle.getState();
            if ((bundleState | stateMask) != 0) {
                try {
                    return bundle.loadClass(aClassName);

                } catch (final ClassNotFoundException e) {
                    // Class not found, try next bundle...
                }
            }
        }

        return null;
    }

    /**
     * Resolves the given class using the Thread class loader, then by calling
     * all active bundles.
     *
     * Based on code from <a href=
     * "http://tech-tauk.blogspot.com/2010/05/thread-context-classlaoder-in.html"
     * >Tech Talk</a>
     *
     * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
     */
    @Override
    protected Class<?> resolveClass(final ObjectStreamClass aDesc)
            throws IOException, ClassNotFoundException {

        try {
            // Try with current thread class loader
            final ClassLoader currentTccl = Thread.currentThread()
                    .getContextClassLoader();
            return currentTccl.loadClass(aDesc.getName());

        } catch (final Exception e) {
            // Ignore errors at this level
        }

        // Try with bundles
        if (pBundleContext != null) {
            final Class<?> clazz = findClassInBundles(
                    pBundleContext.getBundles(), aDesc.getName(), true);
            if (clazz != null) {
                return clazz;
            }
        }

        // Use the parent if needed
        return super.resolveClass(aDesc);
    }
}
