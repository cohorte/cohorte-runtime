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
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.bundles.impl.CBundleFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CFileFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CPlatformDirsSvc;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * @author Thomas Calmant
 */
public class CBundleBaseActivator implements BundleActivator {

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

    /** Internal log handler */
    private CLogInternal pLogInternal;

    /** Log reader service factory */
    private CLogReaderServiceFactory pLogReaderServiceFactory;

    /** Log service factory */
    private CLogServiceFactory pLogServiceFactory;

    /** Platform directories service */
    private CPlatformDirsSvc pPlatformDirsSvc;

    /** OSGi services registration */
    private final List<ServiceRegistration> pRegisteredServices = new ArrayList<ServiceRegistration>();

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
     * Prepares the activity logger
     * 
     * @return The activity logger
     * @throws Exception
     *             An error occurred while preparing the logger
     */
    protected IActivityLogger getLogger() throws Exception {

	// Be sure we have a valid platform service instance
	if (pPlatformDirsSvc == null) {
	    getPlatformDirs();
	}

	if (pActivityLogger == null) {

	    final StringBuilder pathBuilder = new StringBuilder(
		    pPlatformDirsSvc.getIsolateLogDir().getAbsolutePath());
	    pathBuilder.append(File.separator);
	    pathBuilder.append("LogService-%g.txt");

	    pActivityLogger = CActivityLoggerBasic.newLogger(
		    pPlatformDirsSvc.getIsolateId() + "-LogService",
		    pathBuilder.toString(), IActivityLoggerBase.ALL,
		    LOG_FILES_SIZE, LOG_FILES_COUNT);

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

	if (pPlatformDirsSvc == null) {
	    pPlatformDirsSvc = new CPlatformDirsSvc();
	}

	return pPlatformDirsSvc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public synchronized void start(final BundleContext aBundleContext) {

	ServiceRegistration registration;

	// Register platform directories
	registration = aBundleContext.registerService(
		IPlatformDirsSvc.class.getName(), getPlatformDirs(), null);
	pRegisteredServices.add(registration);

	try {
	    // LogService interface
	    registration = aBundleContext.registerService(
		    LogService.class.getName(), getLogServiceFactory(), null);
	    pRegisteredServices.add(registration);

	    // LogReader service interface
	    registration = aBundleContext.registerService(
		    LogReaderService.class.getName(),
		    getLogReaderServiceFactory(), null);
	    pRegisteredServices.add(registration);

	} catch (Exception e) {
	    System.err.println("Can't create the log service factory");
	    e.printStackTrace();
	}

	// Register the file finder
	registration = aBundleContext.registerService(
		IFileFinderSvc.class.getName(), getFileFinder(), null);
	pRegisteredServices.add(registration);

	// Register the bundle finder
	registration = aBundleContext.registerService(
		IBundleFinderSvc.class.getName(), getBundleFinder(), null);
	pRegisteredServices.add(registration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public synchronized void stop(final BundleContext aBundleContext) {

	// Unregister all services
	for (ServiceRegistration registration : pRegisteredServices) {
	    registration.unregister();
	}

	pRegisteredServices.clear();
    }
}
