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

package org.psem2m.status.storage;

/**
 * Defines a status storage creator service
 *
 * @author Thomas Calmant
 */
public interface IStatusStorageCreator {

    /**
     * Instantiates a new status storage
     *
     * @return A new status storage
     */
    <S extends State, T> IStatusStorage<S, T> createStorage();

    /**
     * Clears the given status storage
     *
     * @param aStorage
     *            A status storage
     */
    void deleteStorage(IStatusStorage<?, ?> aStorage);
}
