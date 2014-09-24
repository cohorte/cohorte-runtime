/**
 * File:   CLogServiceFactory.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * Log Service factory : associate a log service with each bundle
 * 
 * @author Thomas Calmant
 */
public class CLogServiceFactory implements ServiceFactory<LogService> {

    /** The internal log handler */
    private final CLogInternal pLogInternal;

    /**
     * Prepares the log service factory
     * 
     * @param aLogger
     *            The internal log handler
     */
    public CLogServiceFactory(final CLogInternal aLogInternal) {

        pLogInternal = aLogInternal;
        
        pLogInternal.addEntry(new CLogEntry(null,
                null, LogService.LOG_INFO, String.format("CLogServiceFactory.<init>: instanciated - LogInternal=[%s]",pLogInternal), null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle,
     * org.osgi.framework.ServiceRegistration)
     */
    @Override
    public LogService getService(final Bundle aBundle,
            final ServiceRegistration<LogService> aServiceRegistration) {

        return new CLogServiceImpl(pLogInternal, aBundle);
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
            final ServiceRegistration<LogService> aServiceRegistration,
            final LogService aServiceInstance) {

        if (aServiceInstance instanceof IActivityLogger) {
            ((IActivityLogger) aServiceInstance).close();
        }
    }
}
