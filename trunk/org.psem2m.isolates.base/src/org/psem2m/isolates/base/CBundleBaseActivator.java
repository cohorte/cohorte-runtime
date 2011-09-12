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
package org.psem2m.isolates.base;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.bundles.impl.CBundleFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CFileFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CPlatformDirsSvc;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * @author Thomas Calmant
 */
public class CBundleBaseActivator implements BundleActivator {

    /** Bundle finder service */
    private CBundleFinderSvc pBundleFinderSvc;

    /** File finder service */
    private CFileFinderSvc pFileFinderSvc;

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
