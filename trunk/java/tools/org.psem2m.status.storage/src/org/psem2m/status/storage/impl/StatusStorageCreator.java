/**
 * File:   StatusStorageCreator.java
 * Author: Thomas Calmant
 * Date:   27 août 2012
 */
package org.psem2m.status.storage.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.status.storage.IStatusStorage;
import org.psem2m.status.storage.IStatusStorageCreator;
import org.psem2m.status.storage.State;

/**
 * Implementation of the status storage creator
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-status-storage-creator-factory", publicFactory = false)
@Provides(specifications = IStatusStorageCreator.class)
@Instantiate(name = "psem2m-status-storage-creator")
public class StatusStorageCreator implements IStatusStorageCreator {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.status.storage.IStatusStorageCreator#clearStorage(org.psem2m
     * .status.storage.IStatusStorage)
     */
    @Override
    public void deleteStorage(final IStatusStorage<?, ?> aStorage) {

        if (aStorage != null) {
            aStorage.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.status.storage.IStatusStorageCreator#createStorage()
     */
    @Override
    public <S extends State, T> IStatusStorage<S, T> createStorage() {

        return new StatusStorage<S, T>();
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        pLogger.logInfo(this, "invalidate", "Status Storage Creator gone");
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        pLogger.logInfo(this, "validate", "Status Storage Creator ready");
    }
}
