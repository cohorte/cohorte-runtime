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
package org.psem2m.utilities.files;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * @author ogattaz
 * 
 */
public interface IXFilesContainer {

	public static final FileFilter NO_FILTER = null;

	public static final boolean WITH_DIRS = true;
	public static final boolean WITH_SUBDIRS = true;
	public static final boolean WITH_TEXTFILE = true;

	/**
	 * @param aFilter
	 * @param aWithDirs
	 * @param aInstanciateTxtFiles
	 * @return
	 * @throws Exception
	 */
	public ArrayList<File> getMyFiles(FileFilter aFilter, boolean aWithDirs,
			boolean aInstanciateTxtFiles) throws Exception;

	/**
	 * @param aList
	 * @param aFilter
	 * @param aSubDirs
	 * @param aInstanciateTxtFiles
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAll(CXSortListFiles aList, FileFilter aFilter, boolean aSubDirs,
			boolean aInstanciateTxtFiles) throws Exception;

	/**
	 * @param aList
	 * @param aFilter
	 * @param aSubDirs
	 * @param aInstanciateTxtFiles
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllDirs(CXSortListFiles aList, FileFilter aFilter, boolean aSubDirs,
			boolean aInstanciateTxtFiles) throws Exception;

	/**
	 * @param aList
	 * @param aFilter
	 * @param aSubDirs
	 * @param aInstanciateTxtFiles
	 * @return
	 * @throws Exception
	 */
	public CXSortListFiles scanAllFiles(CXSortListFiles aList, FileFilter aFilter,
			boolean aSubDirs, boolean aInstanciateTxtFiles) throws Exception;

}
