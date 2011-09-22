/**
 * File:   OsgiObjectInputStream.java
 * Author: Thomas Calmant
 * Date:   22 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.Utilities;

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

        } catch (Exception e) {
            // Ignore errors at this level
        }

        // Try with bundles
        if (pBundleContext != null) {
            final Class<?> clazz = Utilities.findClassInBundles(
                    pBundleContext.getBundles(), aDesc.getName());

            if (clazz != null) {
                return clazz;
            }
        }

        // Use the parent if needed
        return super.resolveClass(aDesc);
    }
}
