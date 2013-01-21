/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *    Thomas Calmant (isandlaTech) - Pure OSGi convertion
 *******************************************************************************/
package org.psem2m.isolates.base.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.bundles.impl.CBundleFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CFileFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CPlatformDirsSvc;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.logging.CActivityLoggerBasicConsole;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * @author Thomas Calmant
 */
public class CBundleBaseActivator extends CXObjectBase implements
        BundleActivator {

    /**
     * Service Infos bean
     * 
     * @author ogattaz
     * 
     */
    private class CServiceInfos {

        /** the name of the service **/
        private final String pServiceName;

        /** the registration info of the service **/
        private final ServiceRegistration<?> pServiceRegistration;

        public CServiceInfos(final ServiceRegistration<?> aServiceRegistration,
                final String aServiceName) {

            pServiceRegistration = aServiceRegistration;
            pServiceName = aServiceName;
        }

        /**
         * @return
         */
        public String getServiceName() {

            return pServiceName;
        }

        /**
         * @return
         */
        public ServiceRegistration<?> getServiceRegistration() {

            return pServiceRegistration;
        }
    }

    /** Maximum log files for the LogService */
    public static final int LOG_FILES_COUNT = 5;

    /** Maximum log file size (100 Mo) */
    public static final int LOG_FILES_SIZE = 100 * 1024 * 1024;

    /** Log instance underlying logger */
    private IActivityLogger pActivityLogger;

    /** The bundle context */
    private BundleContext pBundleContext;

    /** Bundle finder service */
    private CBundleFinderSvc pBundleFinderSvc;

    /** File finder service */
    private CFileFinderSvc pFileFinderSvc;

    /** Log service available for all the bundles of the isolate */
    private CIsolateLoggerSvc pIsolateLoggerSvc;

    /** Internal log handler */
    private CLogInternal pLogInternal;

    /** Log reader service factory */
    private CLogReaderServiceFactory pLogReaderServiceFactory;

    /** Log service factory */
    private CLogServiceFactory pLogServiceFactory;

    /** Platform directories service */
    private CPlatformDirsSvc pPlatformDirsSvc;

    /** OSGi services registration */
    private final List<CServiceInfos> pRegisteredServicesInfos = new ArrayList<CServiceInfos>();

    /** The service listener */
    private ServiceListener pRegistrationListener = null;

    /**
	 * 
	 */
    private void destroyLogger() {

        getLogger().close();
        pActivityLogger = null;
    }

    /**
     * Creates or retrieves an instance of the bundle finder
     * 
     * @return A bundle finder instance
     */
    public IBundleFinderSvc getBundleFinder() {

        if (pBundleFinderSvc == null) {
            pBundleFinderSvc = new CBundleFinderSvc(getPlatformDirs());
        }

        return pBundleFinderSvc;
    }

    /**
     * Creates or retrieves an instance of the file finder
     * 
     * @return A file finder instance
     */
    public IFileFinderSvc getFileFinder() {

        if (pFileFinderSvc == null) {
            pFileFinderSvc = new CFileFinderSvc(getPlatformDirs());
        }

        return pFileFinderSvc;
    }

    /**
     * Retrieves the log service instance, creates it if needed
     * 
     * @return The log service instance
     * @throws Exception
     *             An error occurred while preparing the logger
     */
    public CIsolateLoggerSvc getIsolateLoggerSvc() throws Exception {

        if (pIsolateLoggerSvc == null) {
            pIsolateLoggerSvc = new CIsolateLoggerSvc(getLogger());
        }

        return pIsolateLoggerSvc;
    }

    /**
     * return the activity logger instance.
     * 
     * @return The activity logger
     * @throws Exception
     *             An error occurred while preparing the logger
     */
    protected IActivityLogger getLogger() {

        // if no logger already created
        if (pActivityLogger == null) {
            initLogger();
        }

        return pActivityLogger;
    }

    /**
     * Creates or retrieves an instance of the internal log handler
     * 
     * @return the internal log handler
     * @throws Exception
     *             An error occurred while preparing the underlying logger
     */
    public CLogInternal getLogInternal() throws Exception {

        if (pLogInternal == null) {
            pLogInternal = new CLogInternal(getLogger());
        }

        return pLogInternal;
    }

    /**
     * Creates or retrieves an instance of the log reader service factory
     * 
     * @return the log reader service factory
     * @throws Exception
     *             An error occurred while preparing the underlying logger
     */
    public CLogReaderServiceFactory getLogReaderServiceFactory()
            throws Exception {

        if (pLogReaderServiceFactory == null) {
            pLogReaderServiceFactory = new CLogReaderServiceFactory(
                    getLogInternal());
        }

        return pLogReaderServiceFactory;
    }

    /**
     * Creates or retrieves an instance of the log service factory
     * 
     * @return A log service factory instance
     * @throws Exception
     *             An error occurred while preparing the underlying logger
     */
    public CLogServiceFactory getLogServiceFactory() throws Exception {

        // if no LogServiceFactory already created
        if (pLogServiceFactory == null) {
            pLogServiceFactory = new CLogServiceFactory(getLogInternal());
        }

        return pLogServiceFactory;
    }

    /**
     * Creates or retrieves an instance of the platform directories registry
     * 
     * @param aContext
     *            The bundle context
     * @return A platform directories registry instance
     */
    public IPlatformDirsSvc getPlatformDirs() {

        // if no PlatformDirsSvc already created
        if (pPlatformDirsSvc == null) {
            pPlatformDirsSvc = new CPlatformDirsSvc(pBundleContext);
        }

        return pPlatformDirsSvc;
    }

    /**
     * @return true if the IsolateLoggerSvc is available
     */
    public boolean hasIsolateLoggerSvc() {

        return pIsolateLoggerSvc != null;
    }

    /**
	 * 
	 */
    private void initLogger() {

        try {
            // Be sure we have a valid platform service instance
            final IPlatformDirsSvc wPlatformDirsSvc = getPlatformDirs();

            // the name of the logger
            final String wLoggerName = "psem2m.isolate."
                    + wPlatformDirsSvc.getIsolateUID();

            // the FilePathPattern of the logger
            final StringBuilder wFilePathPattern = new StringBuilder();
            wFilePathPattern.append(wPlatformDirsSvc.getIsolateLogDir()
                    .getAbsolutePath());
            wFilePathPattern.append(File.separator);
            wFilePathPattern.append("LogService-%g.txt");

            pActivityLogger = new CIsolateLoggerChannel(wLoggerName,
                    wFilePathPattern.toString(), IActivityLoggerBase.ALL,
                    LOG_FILES_SIZE, LOG_FILES_COUNT);
        } catch (final Exception e) {
            pActivityLogger = CActivityLoggerBasicConsole.getInstance();
            pActivityLogger.logSevere(this, "initLogger",
                    "Can't instanciate a CIsolateLoggerChannel", e);
        }

        // add the java context
        pActivityLogger
                .logInfo(this, "initLogger", CXJvmUtils.getJavaContext());

        // add the environment context
        pActivityLogger.logInfo(this, "initLogger", CXOSUtils.getEnvContext());

    }

    /**
     * @param aServiceInterface
     * @param aService
     * @param aState
     */
    private void logServiceManipulation(final String aServiceName,
            final String aState) {

        getLogger().logDebug(this, "logServiceRegistering", "Service=",
                aServiceName, aState + '=', true);
    }

    /**
     * log the registration of a service in the logger of the isolate
     * 
     * @param aName
     * @param aService
     * @throws Exception
     */
    private void logServiceRegistration(final String aServiceName) {

        logServiceManipulation(aServiceName, "Registered");
    }

    /**
     * log the unregistration of a service in the logger of the isolate
     * 
     * @param aName
     * @param aService
     * @throws Exception
     */
    private void logServiceUnregistration(final String aServiceName) {

        logServiceManipulation(aServiceName, "Unregistered");
    }

    /**
	 * 
	 */
    private void putInPlaceRegistrationListener(
            final BundleContext aBundleContext) {

        pRegistrationListener = new ServiceListener() {

            @Override
            public void serviceChanged(final ServiceEvent aServiceEvent) {

                final ServiceReference<?> wServiceReference = aServiceEvent
                        .getServiceReference();

                final String[] types = (String[]) wServiceReference
                        .getProperty(Constants.OBJECTCLASS);

                final String wServiceClass = types != null ? Arrays
                        .toString(types) : "<null>";

                // FIXME class loader dead lock
                // see:
                // http://underlap.blogspot.fr/2006/11/experimental-fix-for-sunbug-4670071.html
                // // Get the service with OUR bundle context
                // final Object wService = aBundleContext
                // .getService(wServiceReference);
                //
                // // Get the class name
                // final String wServiceClass = wService.getClass().getName();
                //
                // // Release the service
                // aBundleContext.ungetService(wServiceReference);

                switch (aServiceEvent.getType()) {
                case ServiceEvent.REGISTERED:
                    logServiceRegistration(wServiceClass);
                    break;

                case ServiceEvent.UNREGISTERING:
                    logServiceUnregistration(wServiceClass);
                    break;

                default:
                    break;
                }
            }
        };

        try {
            aBundleContext.addServiceListener(pRegistrationListener, null);

        } catch (final InvalidSyntaxException e) {
            getLogger().logSevere(this, "putInPlaceRegistrationListener",
                    "Can't register the service listener.", e);
        }
    }

    /**
     * @param aServiceInterface
     * @param aService
     */
    private <S> void registerOneService(final BundleContext aBundleContext,
            final Class<S> aServiceInterface, final S aService) {

        try {
            final ServiceRegistration<S> registration = aBundleContext
                    .registerService(aServiceInterface, aService, null);
            pRegisteredServicesInfos.add(new CServiceInfos(registration,
                    aServiceInterface.getName()));
            logServiceRegistration(aServiceInterface.getName());

        } catch (final Exception e) {

            getLogger().logSevere(this, "registerOneService",
                    "Can't register the service [%s]. %s.", aServiceInterface,
                    e);
        }
    }

    /**
     * @param aServiceInterface
     * @param aService
     */
    private <S> void registerOneServiceFactory(
            final BundleContext aBundleContext,
            final Class<S> aServiceInterface,
            final ServiceFactory<S> aServiceFactory) {

        try {
            final ServiceRegistration<?> registration = aBundleContext
                    .registerService(aServiceInterface.getName(),
                            aServiceFactory, null);
            pRegisteredServicesInfos.add(new CServiceInfos(registration,
                    aServiceInterface.getName()));
            logServiceRegistration(aServiceInterface.getName());

        } catch (final Exception e) {

            getLogger().logSevere(this, "registerOneService",
                    "Can't register the service [%s]. %s.", aServiceInterface,
                    e);
        }
    }

    /**
     * @param aBundleContext
     */
    private void removeRegistrationListener(final BundleContext aBundleContext) {

        if (pRegistrationListener != null) {
            try {
                aBundleContext.removeServiceListener(pRegistrationListener);
            } catch (final Exception e) {
                getLogger()
                        .logSevere(
                                this,
                                "removeServiceLogger",
                                "Can't remove the listener of all the service registering and unregistering",
                                e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext aBundleContext) {

        // Store the bundle context
        pBundleContext = aBundleContext;

        getLogger().logInfo(this, "start", "START", toDescription());

        // Register platform directories service
        registerOneService(aBundleContext, IPlatformDirsSvc.class,
                getPlatformDirs());

        try {
            // LogService interface
            registerOneServiceFactory(aBundleContext, LogService.class,
                    getLogServiceFactory());
        } catch (final Exception e) {
            getLogger().logSevere(this, "start",
                    "Can't get the LogServiceFactory and register it", e);
        }
        try {
            // LogReader service interface
            registerOneServiceFactory(aBundleContext, LogReaderService.class,
                    getLogReaderServiceFactory());
        } catch (final Exception e) {
            getLogger().logSevere(this, "start",
                    "Can't get the LogReaderServiceFactory and register it", e);
        }
        try {
            // IsolateLogger service
            registerOneService(aBundleContext, IIsolateLoggerSvc.class,
                    getIsolateLoggerSvc());
        } catch (final Exception e) {
            getLogger().logSevere(this, "start",
                    "Can't get the IsolateLoggerSvc and register it", e);
        }

        // Register the file finder
        registerOneService(aBundleContext, IFileFinderSvc.class,
                getFileFinder());

        // Register the bundle finder
        registerOneService(aBundleContext, IBundleFinderSvc.class,
                getBundleFinder());

        // put in place a listner witch logs each service registration and
        // unregistration
        putInPlaceRegistrationListener(aBundleContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext aBundleContext) {

        try {
            getLogger().logInfo(this, "stop", "STOP", toDescription());
        } catch (final Exception e) {
            System.err.println("Can't log the begining of the stop method");
            e.printStackTrace();
        }
        // remove the listner witch logs each service registration and
        // unregistration
        removeRegistrationListener(aBundleContext);

        // Unregister all services
        for (final CServiceInfos wServiceInfos : pRegisteredServicesInfos) {
            wServiceInfos.getServiceRegistration().unregister();
            logServiceUnregistration(wServiceInfos.getServiceName());
        }

        pRegisteredServicesInfos.clear();

        getLogger().logInfo(this, "stop", "STOP ENDED");

        // The end of the isolate
        destroyLogger();

        pBundleContext = null;
    }
}
