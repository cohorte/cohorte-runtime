/**
 * File:   CLogEntry.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;

/**
 * Implementation of an OSGi log entry
 * 
 * @author Thomas Calmant
 */
public class CLogEntry implements LogEntry {

    /** Logging bundle */
    private Bundle pBundle;

    /** Log level */
    private final int pLevel;

    /** Message content */
    private final String pMessage;

    /** Logger service reference */
    private final ServiceReference<?> pServiceReference;

    /** Associated exception */
    private final Throwable pThrowable;

    /** Message time stamp */
    private final long pTime;

    /**
     * Sets up the log entry
     * 
     * @param aServiceReference
     *            Source service reference
     * @param aDefaultBundle
     *            Bundle to be used if service reference is null (Must not be
     *            null).
     * @param aLevel
     *            OSGi log level
     * @param aMessage
     *            Log message
     * @param aThrowable
     *            Associated exception
     */
    public CLogEntry(final ServiceReference<?> aServiceReference,
            final Bundle aDefaultBundle, final int aLevel,
            final String aMessage, final Throwable aThrowable) {

        pTime = System.currentTimeMillis();
        pServiceReference = aServiceReference;
        pLevel = aLevel;
        pMessage = aMessage;
        pThrowable = aThrowable;

        if (pServiceReference != null) {
            pBundle = pServiceReference.getBundle();
        }

        if (pBundle == null) {
            pBundle = aDefaultBundle;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogEntry#getBundle()
     */
    @Override
    public Bundle getBundle() {

        return pBundle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogEntry#getException()
     */
    @Override
    public Throwable getException() {

        return pThrowable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogEntry#getLevel()
     */
    @Override
    public int getLevel() {

        return pLevel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogEntry#getMessage()
     */
    @Override
    public String getMessage() {

        return pMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogEntry#getServiceReference()
     */
    @Override
    public ServiceReference<?> getServiceReference() {

        return pServiceReference;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogEntry#getTime()
     */
    @Override
    public long getTime() {

        return pTime;
    }
}
