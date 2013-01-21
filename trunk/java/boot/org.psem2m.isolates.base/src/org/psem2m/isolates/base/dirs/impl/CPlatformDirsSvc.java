/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.base.dirs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CPlatformDirsSvc implements IPlatformDirsSvc {

    /** Isolate storage directory parent */
    private static final String ISOLATE_STORAGE_DIR_BASE = CPlatformDirsSvc.VAR_DIRECTORY
            + File.separator + "storage" + File.separator;

    /** Isolate working directory parent */
    private static final String ISOLATE_WORKING_DIR_BASE = CPlatformDirsSvc.VAR_DIRECTORY
            + File.separator + "work" + File.separator;

    /** Platform instance log directory */
    private static final String LOGGING_DIR_BASE = CPlatformDirsSvc.VAR_DIRECTORY
            + File.separator + "log" + File.separator;

    /** Platform base and home repository directory name */
    private static final String REPOSITORY_NAME = "repo";

    /** Base working directory */
    private static final String VAR_DIRECTORY = "var";

    /** The bundle context */
    private final BundleContext pContext;

    /**
     * Sets up the platform informations service
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public CPlatformDirsSvc(final BundleContext aBundleContext) {

        pContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir()
     */
    @Override
    public File getIsolateLogDir() throws Exception {

        return getIsolateLogDir(getIsolateUID());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir(java.lang.String)
     */
    @Override
    public File getIsolateLogDir(final String aIsolateId) throws Exception {

        final File wLogDir = new File(getPlatformLogDir(), aIsolateId);
        if (!wLogDir.exists()) {
            wLogDir.mkdirs();
        }
        return wLogDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateName()
     */
    @Override
    public String getIsolateName() {

        return pContext.getProperty(IPlatformProperties.PROP_ISOLATE_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateNode()
     */
    @Override
    public String getIsolateNode() {

        return pContext.getProperty(IPlatformProperties.PROP_ISOLATE_NODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateStorageDir
     * (java.lang.String)
     */
    @Override
    public File getIsolateStorageDir(final String aIsolateId) {

        final File dir = new File(getPlatformBaseDir(),
                ISOLATE_STORAGE_DIR_BASE + aIsolateId);
        if (!dir.exists()) {
            // Create directories if needed
            dir.mkdirs();
        }

        return dir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateUID()
     */
    @Override
    public String getIsolateUID() {

        return pContext.getProperty(IPlatformProperties.PROP_ISOLATE_UID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.IPlatformDirsSvc#getIsolateWorkingDir(java.lang
     * .String)
     */
    @Override
    public File getIsolateWorkingDir(final String aIsolateId) {

        return new File(getPlatformBaseDir(), ISOLATE_WORKING_DIR_BASE
                + aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformBaseDir()
     */
    @Override
    public File getPlatformBaseDir() {

        return new File(
                pContext.getProperty(IPlatformProperties.PROP_PLATFORM_BASE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IPlatformDirsSvc#getPlatformHomeDir()
     */
    @Override
    public File getPlatformHomeDir() {

        return new File(
                pContext.getProperty(IPlatformProperties.PROP_PLATFORM_HOME));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformLogDir()
     */
    @Override
    public File getPlatformLogDir() throws Exception {

        final File wLogDir = new File(getPlatformBaseDir(), LOGGING_DIR_BASE
                + "psem2m");
        if (!wLogDir.exists()) {
            wLogDir.mkdirs();
        }
        return wLogDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IPlatformDirsSvc#getPlatformRootDirs()
     */
    @Override
    public File[] getPlatformRootDirs() {

        return new File[] { getPlatformBaseDir(), getPlatformHomeDir(),
                new File(System.getProperty("user.dir")) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IPlatformDirsSvc#getRepositories()
     */
    @Override
    public File[] getRepositories() {

        final List<File> repositories = new ArrayList<File>();

        // Current instance repository
        final File baseRepo = new File(getPlatformBaseDir(), REPOSITORY_NAME);
        if (baseRepo.exists()) {
            repositories.add(baseRepo);
        }

        // Home repository
        final File homeRepo = new File(getPlatformHomeDir(), REPOSITORY_NAME);
        if (!homeRepo.equals(baseRepo) && homeRepo.exists()) {
            repositories.add(homeRepo);
        }

        // Add other repositories here, from higher to lower priority

        return repositories.toArray(new File[0]);
    }
}
