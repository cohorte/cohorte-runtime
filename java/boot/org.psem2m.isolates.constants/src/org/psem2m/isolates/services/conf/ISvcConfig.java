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
import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * PSEM2M Configuration service
 *
 * @author Thomas Calmant
 */
public interface ISvcConfig {

    /**
     * Retrieves the description of current application
     *
     * @return the description of current application
     */
    ApplicationDescription getApplication();

    /**
     * Retrieves the description of the current isolate
     *
     * @return the description of the current isolate
     */
    IsolateDescription getCurrentIsolate();

    /**
     * Retrieves the description of the isolate described in the given string
     *
     * @param aConfigurationString
     *            A configuration string
     * @return The parsed description, or null
     */
    IsolateDescription parseIsolate(String aConfigurationString);

    /**
     * Reloads configuration
     *
     * @return True on success, else false
     */
    boolean refresh();

    /**
     * Sets the currently used isolate description. If the parameter is null,
     * then the next call to {@link #getCurrentIsolate()} will return
     * getApplication().getIsolate(<i>currentIsolate</i>).
     *
     * @param aIsolateDescription
     *            An isolate description
     */
    void setCurrentIsolate(IsolateDescription aIsolateDescription);
}
