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
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
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
    class CServiceInfos {
        /** the service **/
        Object pService;
        /** the name of the service **/
        String pServiceName;

        /** the registration info of the service **/
        ServiceRegistration pServiceRegistration;

        CServiceInfos(final ServiceRegistration aServiceRegistration,
                final String aServiceName, final Object aService) {

            pServiceRegistration = aServiceRegistration;
            pServiceName = aServiceName;
            pService = aService;
        }

        /**
         * @return
         */
        public Object getService() {

            return pService;
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
        public ServiceRegistration getServiceRegistration() {

            return pServiceRegistration;
        }
    }

    /** Maximum log files for the LogService */
    public static final int LOG_FILES_COUNT = 5;

    /** Maximum log file size (100 Mo) */
    public static final int LOG_FILES_SIZE = 100 * 1024 * 1024;

    /** Log service underlying logger */
    private IActivityLogger pActivityLogger;

    /** Bundle finder service */
    private CBundleFinderSvc pBundleFinderSvc;

    /** File finder service */
    private CFileFinderSvc pFileFinderSvc;

    /** Log service */
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
     * Prepares the activity logger
     * 
     * @return The activity logger
     * @throws Exception
     *             An error occurred while preparing the logger
     */
    protected IActivityLogger getLogger() throws Exception {

        // if no logger already created
        if (pActivityLogger == null) {

            // Be sure we have a valid platform service instance
            final IPlatformDirsSvc wPlatformDirsSvc = getPlatformDirs();

            // the name of the logger
            final String wLoggerName = "psem2m.isolate."
                    + wPlatformDirsSvc.getIsolateId();

            // the FilePathPattern of the logger
            final StringBuilder wFilePathPattern = new StringBuilder();
            wFilePathPattern.append(wPlatformDirsSvc.getIsolateLogDir()
                    .getAbsolutePath());
            wFilePathPattern.append(File.separator);
            wFilePathPattern.append("LogService-%g.txt");

            pActivityLogger = new CIsolateLoggerChannel(wLoggerName,
                    wFilePathPattern.toString(), IActivityLoggerBase.ALL,
                    LOG_FILES_SIZE, LOG_FILES_COUNT);

            // add the java context
            pActivityLogger.logInfo(this, "getLogger",
                    CXJvmUtils.getJavaContext());

            // add the environment context
            pActivityLogger.logInfo(this, "getLogger",
                    CXOSUtils.getEnvContext());

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
     * @return A platform directories registry instance
     */
    public IPlatformDirsSvc getPlatformDirs() {

        // if no PlatformDirsSvc already created
        if (pPlatformDirsSvc == null) {
            pPlatformDirsSvc = new CPlatformDirsSvc();
        }

        return pPlatformDirsSvc;
    }

    /**
     * @param aServiceInterface
     * @param aService
     * @param aState
     */
    private void logServiceManipulation(final String aServiceName,
            final Object aService, final String aState) {

        try {

            getLogger().logInfo(this, "logServiceRegistering", "Service=",
                    aServiceName, aState + '=', true);

        } catch (final Exception e) {
            System.err.println("Can't log registration");
            e.printStackTrace();
        }
    }

    /**
     * log the registration of a service in the logger of the isolate
     * 
     * @param aName
     * @param aService
     * @throws Exception
     */
    private void logServiceRegistration(final String aServiceName,
            final Object aService) {

        logServiceManipulation(aServiceName, aService, "Registered");
    }

    /**
     * log the unregistration of a service in the logger of the isolate
     * 
     * @param aName
     * @param aService
     * @throws Exception
     */
    private void logServiceUnregistration(final String aServiceName,
            final Object aService) {

        logServiceManipulation(aServiceName, aService, "Unregistered");
    }

    /**
     * @param aServiceInterface
     * @param aService
     */
    private void registerOneService(final BundleContext aBundleContext,
            final String aServiceName, final Object aService) {

        try {
            final ServiceRegistration registration = aBundleContext
                    .registerService(aServiceName, aService, null);
            pRegisteredServicesInfos.add(new CServiceInfos(registration,
                    aServiceName, aService));
            logServiceRegistration(aServiceName, aService);

        } catch (final Exception e) {
            System.err.format("Can't register the service [%s].", aServiceName);
            e.printStackTrace();
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

        try {
            getLogger().logInfo(this, "start", "START", toDescription());
        } catch (final Exception e) {
            System.err.println("Can't log the begining of the start method");
            e.printStackTrace();
        }

        // Register platform directories service
        registerOneService(aBundleContext, IPlatformDirsSvc.class.getName(),
                getPlatformDirs());

        try {
            // LogService interface
            registerOneService(aBundleContext, LogService.class.getName(),
                    getLogServiceFactory());
        } catch (final Exception e) {
            System.err.println("Can't get the LogServiceFactory");
            e.printStackTrace();
        }
        try {
            // LogReader service interface
            registerOneService(aBundleContext,
                    LogReaderService.class.getName(),
                    getLogReaderServiceFactory());
        } catch (final Exception e) {
            System.err.println("Can't get the LogReaderServiceFactory");
            e.printStackTrace();
        }
        try {
            // IsolateLogger service
            registerOneService(aBundleContext,
                    IIsolateLoggerSvc.class.getName(), getIsolateLoggerSvc());
        } catch (final Exception e) {
            System.err.println("Can't get the IsolateLoggerSvc");
            e.printStackTrace();

        }

        // Register the file finder
        registerOneService(aBundleContext, IFileFinderSvc.class.getName(),
                getFileFinder());

        // Register the bundle finder
        registerOneService(aBundleContext, IBundleFinderSvc.class.getName(),
                getBundleFinder());

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

        // Unregister all services
        for (final CServiceInfos wServiceInfos : pRegisteredServicesInfos) {
            wServiceInfos.getServiceRegistration().unregister();
            logServiceUnregistration(wServiceInfos.getServiceName(),
                    wServiceInfos.getService());
        }

        pRegisteredServicesInfos.clear();

        try {
            getLogger().logInfo(this, "stop", "STOP ENDED");
            getLogger().close();
        } catch (final Exception e) {
            System.err.println("Can't log the begining of the stop method");
            e.printStackTrace();
        }
    }
}
