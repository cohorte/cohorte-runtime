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

package org.psem2m.isolates.services.conf;

import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.BundleDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * Defines a PSEM2M configuration reader
 *
 * @author Thomas Calmant
 */
public interface IConfigurationReader {

    /**
     * Retrieves the description of the application corresponding to the given
     * ID
     *
     * @param aApplicationId
     *            An available application ID
     * @return A description of an application
     */
    ApplicationDescription getApplication(String aApplicationId);

    /**
     * Retrieves all available application IDs
     *
     * @return Available application IDs
     */
    String[] getApplicationIds();

    /**
     * Loads the given configuration file
     *
     * @param aConfigurationFile
     *            A PSEM2M configuration file
     *
     * @return True if the file was successfully read
     */
    boolean load(String aConfigurationFile);

    /**
     * Retrieves the description of the bundle described in the given string
     *
     * @param aBundleConfiguration
     *            A configuration string
     * @return The parsed description, or null
     */
    BundleDescription parseBundle(String aBundleConfiguration);

    /**
     * Retrieves the description of the isolate described in the given string
     *
     * @param aIsolateConfiguration
     *            A configuration string
     * @return The parsed description, or null
     */
    IsolateDescription parseIsolate(String aIsolateConfiguration);
}
