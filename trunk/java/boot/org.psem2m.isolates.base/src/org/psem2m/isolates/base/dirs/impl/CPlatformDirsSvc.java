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

    /** Platform base and home repository directory name */
    private static final String REPOSITORY_NAME = "repo";

    /** The bundle context */
    private final BundleContext pContext;

    /** The initial working directory */
    private final File pInitialWorkingDirectory;

    /**
     * Sets up the platform informations service. Stores the working directory
     * at instantiation time.
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public CPlatformDirsSvc(final BundleContext aBundleContext) {

        pContext = aBundleContext;

        // Store the working directory
        pInitialWorkingDirectory = new File(System.getProperty("user.dir"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir()
     */
    @Override
    public File getIsolateLogDir() {

        // Valid log directory
        return tryCreateDirectory(new File(pInitialWorkingDirectory, "log"),
                pInitialWorkingDirectory);
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
     * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateStorageDirectory
     * ()
     */
    @Override
    public File getIsolateStorageDirectory() {

        return tryCreateDirectory(
                new File(pInitialWorkingDirectory, "storage"),
                pInitialWorkingDirectory);
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
     * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateWorkingDirectory
     * ()
     */
    @Override
    public File getIsolateWorkingDirectory() {

        return pInitialWorkingDirectory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformBaseDir()
     */
    @Override
    public File getPlatformBase() {

        return new File(
                pContext.getProperty(IPlatformProperties.PROP_PLATFORM_BASE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IPlatformDirsSvc#getPlatformHomeDir()
     */
    @Override
    public File getPlatformHome() {

        return new File(
                pContext.getProperty(IPlatformProperties.PROP_PLATFORM_HOME));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IPlatformDirsSvc#getPlatformRootDirs()
     */
    @Override
    public File[] getPlatformRootDirs() {

        return new File[] { getPlatformBase(), getPlatformHome(),
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
        final File baseRepo = new File(getPlatformBase(), REPOSITORY_NAME);
        if (baseRepo.exists()) {
            repositories.add(baseRepo);
        }

        // Home repository
        final File homeRepo = new File(getPlatformHome(), REPOSITORY_NAME);
        if (!homeRepo.equals(baseRepo) && homeRepo.exists()) {
            repositories.add(homeRepo);
        }

        // Add other repositories here, from higher to lower priority

        return repositories.toArray(new File[0]);
    }

    /**
     * Tries to create the given directory, return the default one on failure
     * 
     * @param aDirectory
     *            Directory to create via {@link File#mkdirs()}
     * @param aDefault
     *            Directory to return on failure
     * @return The created directory or the default one
     */
    private File tryCreateDirectory(final File aDirectory, final File aDefault) {

        if (aDirectory == null) {
            return aDefault;
        }

        if (!aDirectory.exists()) {
            try {
                if (!aDirectory.mkdirs()) {
                    // Error creating directories
                    return aDefault;
                }

            } catch (final SecurityException ex) {
                // Security error
                return aDefault;
            }
        }

        // Valid log directory
        return aDirectory;
    }
}
