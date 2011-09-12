/**
 * File:   CLogServiceFactory.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * Log Service factory : associate a log service with each bundle
 * 
 * @author Thomas Calmant
 */
public class CLogServiceFactory implements ServiceFactory {

    /** The internal log handler */
    private CLogInternal pLogger;

    /**
     * Prepares the log service factory
     * 
     * @param aLogger
     *            The internal log handler
     */
    public CLogServiceFactory(final CLogInternal aLogger) {
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
    public Object getService(final Bundle aBundle,
	    final ServiceRegistration aServiceRegistration) {

	return new CLogServiceImpl(pLogger, aBundle);
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
	    final ServiceRegistration aServiceRegistration,
	    final Object aServiceInstance) {

	if (aServiceInstance instanceof IActivityLogger) {
	    ((IActivityLogger) aServiceInstance).close();
	}
    }
}
