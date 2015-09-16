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

package org.psem2m.isolates.services.dirs;

import java.io.File;

import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.utilities.IXDescriber;

/**
 * MOD_OG_20150415, MOD_OG_20150625 Enhancement of the comments
 *
 *
 * ATTENTION :
 *
 * the "user.dir" system property must contain the isolateDir.
 *
 * <pre>
 * .../BASE/var/ISOLATE_NAME/ISOLATE_UUID/)
 * </pre>
 *
 * @see look at cohorte forker !
 *
 *
 * @author isandlatech (www.isandlatech.com) - ogattaz
 */
public interface IPlatformDirsSvc extends IPlatformProperties, IXDescriber {

	/**
	 * MOD_OG_20150625 standards sub dirs
	 */
	String DIRNAME_CONF = "conf";
	String DIRNAME_DATA = "data"; // MOD_BD_20150916
	String DIRNAME_LOG = "log";
	String DIRNAME_REPOSITORY = "repo";
	String DIRNAME_STORAGE = "storage";
	String DIRNAME_VAR = "var";

	/**
	 * Returns the root directory of the current isolate
	 *
	 * <pre>
	 * ...base/var/[ISOLATE_NAME]/[ISOLATE_UUID]/
	 * </pre>
	 *
	 * @return The isolate working directory
	 */
	File getIsolateDir();

	/**
	 * Retrieves the specific log directory of the current isolate . Creates it
	 * if needed.
	 *
	 * <pre>
	 * ...base/var/[ISOLATE_NAME]/[ISOLATE_UUID]/log
	 * </pre>
	 *
	 * @return the log directory of the current isolate
	 */
	File getIsolateLogDir();

	/**
	 * Retrieves the name of the current isolate
	 *
	 * <pre>
	 * -Dcohorte.isolate.name=Agregator
	 * </pre>
	 *
	 * @return the name of the current isolate
	 */
	String getIsolateName();

	/**
	 * Returns the storage directory of the current isolate
	 *
	 * <pre>
	 * $BASE / var / $ISOLATE_NAME / $ISOLATE_UUID / storage
	 * </pre>
	 *
	 * @return The isolate storage directory
	 */
	File getIsolateStorageDir();

	/**
	 * Retrieves the current isolate UID
	 *
	 * The UUID is calculated by the forker, it is prefixed by the index of the
	 * instance of the isolate.
	 *
	 * <pre>
	 * -Dcohorte.isolate.uid=000-KITS-OE5F-KJS8-M568
	 * </pre>
	 *
	 * @return the UID of the current isolate
	 */
	String getIsolateUID();

	/**
	 * Retrieves the working directory of the current isolate ( "user.dir" of
	 * the jvm)
	 *
	 * So this method returns the "IsolateDir" like the forker has set it to be
	 * the "user dir" of the jvm.
	 *
	 * @return The isolate working directory.
	 *
	 */
	File getIsolateUserDir();

	/**
	 * Retrieves the directory where components could write persistent data.
	 *
	 * <pre>
	 * -Dcohorte.node.data.dir=/opt/isandlaTech/product/base
	 * </pre>
	 *
	 * @return the data directory
	 */
	File getNodeDataDir();

	/**
	 * Retrieves the node name of the current isolate
	 *
	 * <pre>
	 * -Dcohorte.node.name=ArmCortexAgregator
	 * </pre>
	 *
	 * @return the node of the current isolate
	 */
	String getNodeName();

	/**
	 * Retrieves the nodeUUID of the node of the current isolate
	 *
	 * <pre>
	 * -Dcohorte.node.uid=1HJU-45KI-OIUT-P36S
	 * </pre>
	 *
	 * @return the node UUID of the node of the current isolate
	 */
	String getNodeUID();

	/**
	 * Retrieves the cohorte base dir path
	 *
	 * <pre>
	 * -Dcohorte.base=/opt/isandlaTech/product/base
	 * </pre>
	 *
	 * @return the base directory of the platform
	 */
	File getPlatformBase();

	/**
	 * Retrieves the cohorte home dir path
	 *
	 * <pre>
	 * -Dcohorte.home=/opt/isandlaTech/product/home
	 * </pre>
	 *
	 * @return the home directory of the platform
	 */
	File getPlatformHome();

	/**
	 * Returns the platform root directories : base, home and working directory
	 *
	 * @return an array of directories
	 */
	File[] getPlatformRootDirs();

	/**
	 * Returns an array of all known repositories, in order of priority.
	 *
	 * The first element is the base repository, then the home repository, then
	 * the system repository.
	 *
	 * @return An array with at least one element
	 */
	File[] getRepositories();
}
