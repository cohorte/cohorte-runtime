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
import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Retrieves the configuration that was used to start this isolate
 *
 * @author Thomas Calmant
 */
public interface IStartConfiguration {

    /**
     * Retrieves the configuration of the application
     *
     * @return the configuration of the application
     */
    ApplicationDescription getApplication();

    /**
     * Retrieves the configuration used to start the isolate
     *
     * @return The boot configuration
     */
    IsolateConf getConfiguration();
}
