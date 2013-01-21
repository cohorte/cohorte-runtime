/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.services.dirs;

import java.io.File;

import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface IPlatformDirsSvc extends IPlatformProperties {

    /**
     * Retrieves the specific log directory of the current isolate . Creates it
     * if needed.
     * 
     * @return the log directory of the current isolate
     * @throws Exception
     *             An error occurred while preparing the log directory
     */
    File getIsolateLogDir() throws Exception;

    /**
     * Retrieves the specific log directory of the given isolate. Creates it if
     * needed.
     * 
     * @param aIsolateId
     *            the id of an isolate
     * @return the log directory of the isolate
     * @throws Exception
     *             if the hierarchy doesn't exist and can't be created
     */
    File getIsolateLogDir(final String aIsolateId) throws Exception;

    /**
     * Retrieves the name of the current isolate
     * 
     * @return the name of the current isolate
     */
    String getIsolateName();

    /**
     * Retrieves the node of the current isolate
     * 
     * @return the node of the current isolate
     */
    String getIsolateNode();

    /**
     * @param aIsolateId
     *            the id of an isolate
     * @return The isolate storage directory
     */
    File getIsolateStorageDir(String aIsolateId);

    /**
     * Retrieves the current isolate UID
     * 
     * @return the UID of the current isolate
     */
    String getIsolateUID();

    /**
     * @param aIsolateId
     *            the id of an isolate
     * @return The isolate working directory
     */
    File getIsolateWorkingDir(String aIsolateId);

    /**
     * Retrieves the PSEM2M_BASE value
     * 
     * <pre>
     * -Dorg.psem2m.platform.base=${workspace_loc}/psem2m/platforms/felix.user
     * .dir
     * </pre>
     * 
     * @return the base directory of the platform
     */
    File getPlatformBaseDir();

    /**
     * Retrieves the PSEM2M_HOME value
     * 
     * <pre>
     * -Dorg.psem2m.platform.home=/usr/share/psem2m
     * </pre>
     * 
     * @return the home directory of the platform
     */
    File getPlatformHomeDir();

    /**
     * Retrieves the log directory file object. Creates the directory if needed.
     * 
     * @return the log directory of the platform
     * @throws Exception
     *             An error occurred while preparing the log directory
     */
    File getPlatformLogDir() throws Exception;

    /**
     * Retrieves the platform root directories : base, home and working
     * directory
     * 
     * @return The platform root directories
     */
    File[] getPlatformRootDirs();

    /**
     * Retrieves all known repositories, in order of priority.
     * 
     * The first element is the base repository, then the home repository, then
     * the system repository.
     * 
     * @return An array with at least one element
     */
    File[] getRepositories();
}
