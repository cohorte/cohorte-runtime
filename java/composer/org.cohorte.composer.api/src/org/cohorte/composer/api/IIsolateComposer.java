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

package org.cohorte.composer.api;

import java.util.Set;

/**
 * Specification of an Isolate Composer service
 *
 * @author Thomas Calmant
 */
public interface IIsolateComposer {

    /**
     * Returns a bean that describes this isolate
     *
     * @return A bean that describes this isolate
     */
    Isolate get_isolate_info();

    /**
     * Returns the UID of the isolate hosting this composer
     *
     * @return An isolate UID
     */
    String get_isolate_uid();

    /**
     * Instantiates the given components
     *
     * @param aComponents
     *            A set of RawComponent beans
     */
    void instantiate(Set<RawComponent> aComponents);

    /**
     * Kills the components with the given names
     *
     * @param aNames
     *            Names of the components to kill
     */
    void kill(Set<String> aNames);
}
