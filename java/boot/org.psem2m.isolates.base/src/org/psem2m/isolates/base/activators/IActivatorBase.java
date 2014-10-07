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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public interface IActivatorBase {

    /**
     * Retrieves all available service references
     *
     * @return an array containing all the available service references.
     */
    ServiceReference<?>[] getAllServiceReferences();

    /**
     * Retrieves the bundle ID
     *
     * @return The bundle ID
     */
    String getBundleId();

    /**
     * Retrieves the bundle context of the activator
     *
     * @return The bundle context
     */
    BundleContext getContext();

    /**
     * Retrieves the service reference of a service using the unique service id
     *
     * @param aServiceId
     *            the unique service id
     * @return an instance of ServiceReference
     *
     * @throws Exception
     *             Something went wrong
     */
    ServiceReference<?> getServiceReference(Long aServiceId) throws Exception;
}
