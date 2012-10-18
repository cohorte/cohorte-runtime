/**
 * File:   IStatusStorageCreator.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.status.storage;

/**
 * Defines a status storage creator service
 * 
 * @author Thomas Calmant
 */
public interface IStatusStorageCreator {

    /**
     * Clears the given status storage
     * 
     * @param aStorage
     *            A status storage
     */
    void deleteStorage(IStatusStorage<?, ?> aStorage);

    /**
     * Instantiates a new status storage
     * 
     * @return A new status storage
     */
    <S extends State, T> IStatusStorage<S, T> createStorage();
}
