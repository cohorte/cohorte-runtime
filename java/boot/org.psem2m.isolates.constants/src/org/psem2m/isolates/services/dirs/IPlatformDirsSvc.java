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

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public interface IPlatformDirsSvc extends IPlatformProperties {

    /**
     * Returns the root directory of the current isolate
     *
     * @return The isolate working directory
     */
    File getIsolateDir();

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
     * @return The isolate storage directory
     */
    File getIsolateStorageDir();

    /**
     * Retrieves the current isolate UID
     *
     * <pre>
     * -Dcohorte.isolate.uid=KITS-OE5F-KJS8-M568
     * </pre>
     *
     * @return the UID of the current isolate
     */
    String getIsolateUID();

    /**
     * Retrieves the working directory of the current isolate (user.dir of the
     * jvm)
     *
     * @return The isolate working directory
     */
    File getIsolateUserDir();

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
     * Retrieves the nodeUUID of the current isolate
     *
     * <pre>
     * -Dcohorte.node.uid=1HJU-45KI-OIUT-P36S
     * </pre>
     *
     * @return the node UUID of the current isolate
     */
    String getNodeUID();

    /**
     * Retrieves the cohorte base dir path
     *
     * <pre>
     * -Dcohorte-Dcohorte.base=${workspace_loc:platform}/BASE
     * </pre>
     *
     * @return the base directory of the platform
     */
    File getPlatformBase();

    /**
     * Retrieves the cohorte home dir path
     *
     * <pre>
     * -Dcohorte.home=${workspace_loc:platform}/HOME
     * </pre>
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
