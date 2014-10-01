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
