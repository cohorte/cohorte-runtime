/**
 * File:   CLogReaderServiceFactory.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;

/**
 * LogReader service factory
 * 
 * @author Thomas Calmant
 */
public class CLogReaderServiceFactory implements
        ServiceFactory<LogReaderService> {

    /** The internal log handler */
    private final CLogInternal pLogger;

    /**
     * Prepares the log service factory
     * 
     * @param aLogger
     *            The internal log handler
     */
    public CLogReaderServiceFactory(final CLogInternal aLogger) {

        pLogger = aLogger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle,
     * org.osgi.framework.ServiceRegistration)
     */
    @Override
    public LogReaderService getService(final Bundle aBundle,
            final ServiceRegistration<LogReaderService> aServiceRegistration) {

        final CLogReaderServiceImpl reader = new CLogReaderServiceImpl(pLogger);
        pLogger.addLogReader(reader);
        return reader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle,
     * org.osgi.framework.ServiceRegistration, java.lang.Object)
     */
    @Override
    public void ungetService(final Bundle aBundle,
            final ServiceRegistration<LogReaderService> aServiceRegistration,
            final LogReaderService aServiceInstance) {

        if (aServiceInstance instanceof CLogReaderServiceImpl) {

            final CLogReaderServiceImpl reader = (CLogReaderServiceImpl) aServiceInstance;

            // Remove the reader from the internal log handler
            pLogger.removeLogReader(reader);

            // Clear listeners
            reader.close();
        }
    }
}
