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

package org.psem2m.isolates.base.activators;

import org.osgi.framework.BundleException;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public interface IPojoBase {

    /**
     * @return the id of the bundle
     */
    String getPojoId();

    /**
     * Called when the POJO has been invalidated by iPOJO (dependency gone, ...)
     *
     * @throws BundleException
     *             An error occurred while stopping the POJO
     */
    void invalidatePojo() throws BundleException;

    /**
     * Called when iPOJO starts the POJO.
     *
     * @throws BundleException
     *             An error occurred while starting the POJO
     */
    void validatePojo() throws BundleException;
}
