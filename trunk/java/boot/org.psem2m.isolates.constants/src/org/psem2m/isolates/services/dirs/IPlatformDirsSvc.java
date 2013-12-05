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
     * Retrieves the log directory of the current platform base . Creates it
     * if needed.
     * 
     * @return the log directory of the current platform base
     */
	File getPlatformBaseLogDir();
		
    /**
     * Retrieves the specific log directory of the current isolate . Creates it
     * if needed.
     * 
     * @return the log directory of the current isolate
     */
    File getIsolateLogDir();

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
     * Retrieves the storage directory of the current isolate
     * 
     * @return The isolate storage directory
     */
    File getIsolateStorageDirectory();

    /**
     * Retrieves the current isolate UID
     * 
     * @return the UID of the current isolate
     */
    String getIsolateUID();

    /**
     * Retrieves the working directory of the current isolate
     * 
     * @return The isolate working directory
     */
    @Deprecated
    File getIsolateWorkingDirectory();

    /**
     * Retrieves the working directory of the current isolate (user.dir of the jvm)
     * 
     * @return The isolate working directory
     */
    File getIsolateUserDir();
    
    /**
     * Retrieves the PSEM2M_BASE value
     * 
     * @return the base directory of the platform
     */
    File getPlatformBase();

    /**
     * Retrieves the PSEM2M_HOME value
     * 
     * @return the home directory of the platform
     */
    File getPlatformHome();

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
