/**
 * File:   DataServerSvc.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.server.impl;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.core.IQuarterback;
import org.psem2m.demo.erp.api.beans.CCart;
import org.psem2m.demo.erp.api.beans.CErpActionReport;
import org.psem2m.demo.erp.api.beans.CachedItemBean;
import org.psem2m.demo.erp.api.beans.CachedItemStockBean;
import org.psem2m.demo.erp.api.services.IErpData;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * DataServer service implementation, exported via Remote Services.
 * 
 * Remote Services export properties are indicated in the metadata file
 * 
 * @author Thomas Calmant
 */
@Component(name = "demo-dataserver-factory", publicFactory = false, propagation = true)
@Provides(specifications = IErpData.class)
public class DataServerSvc extends CPojoBase implements IErpData {

    /** The quarterback service iPOJO member ID */
    private static final String IPOJO_QUARTERBACK_ID = "quarterback";

    /** Write lock lock/unlock thread */
    private final ExecutorService pLockRunner;

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** ERP/Cache strategy handler */
    @Requires(id = IPOJO_QUARTERBACK_ID, optional = true)
    private IQuarterback pQuarterback;

    /** Quarterback presence flag */
    private boolean pQuarterbackPresent = false;

    /** The quarterback waiting lock */
    private final ReadWriteLock pQuarterbackWaitLock;

    /** The quarterback read lock */
    private final Lock pReadLock;

    /** Write lock locker */
    private final Runnable pRunWriteLocker = new Runnable() {

        @Override
        public void run() {

            internalBlockQuarterbackAccess();
        }
    };

    /** Write lock unlocker */
    private final Runnable pRunWriteUnlocker = new Runnable() {

        @Override
        public void run() {

            internalUnblockQuarterbackAccess();
        }
    };

    /** The quarterback write lock */
    private final Lock pWriteLock;

    /**
     * Default constructor
     */
    public DataServerSvc() {

        super();

        pQuarterbackWaitLock = new ReentrantReadWriteLock();
        pReadLock = pQuarterbackWaitLock.readLock();
        pWriteLock = pQuarterbackWaitLock.writeLock();

        pLockRunner = Executors.newFixedThreadPool(1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpData#applyCart(org.psem2m.demo.erp
     * .api.beans.CCartLine[])
     */
    @Override
    public CErpActionReport applyCart(final CCart aCart) {

        // Wait for the quarterback to become available
        if (!waitQuarterbackAccess()) {
            pLogger.logInfo(this, "applyCart",
                    "Quarterback is absent after unlock : abandon");
            return null;
        }

        final CErpActionReport result = pQuarterback.applyCart(aCart);

        pLogger.logInfo(this, "applyCart", aCart, " (",
                Arrays.toString(aCart.getCartLines()), ") =", result);

        return result;
    }

    /**
     * Called by iPOJO when the quarterback is bound
     * 
     * @param aQuarterback
     *            The bound service
     */
    @Bind(id = IPOJO_QUARTERBACK_ID)
    protected void bindQuarterback(final IQuarterback aQuarterback) {

        pLogger.logInfo(this, "bindQuarterback",
                "Quarterback bound - UNLOCK access");

        pQuarterbackPresent = true;

        // Release clients
        unblockQuarterbackAccess();

        pLogger.logInfo(this, "bindQuarterback",
                "Quarterback bound - UNLOCK access DONE");
    }

    /**
     * Blocks the access to the quarterback service from the locker thread
     */
    protected void blockQuarterbackAccess() {

        pLockRunner.execute(pRunWriteLocker);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.services.IErpData#getItem(java.lang.String)
     */
    @Override
    public CachedItemBean getItem(final String aItemId) {

        // Wait for the quarterback to become available
        if (!waitQuarterbackAccess()) {
            pLogger.logInfo(this, "getItem",
                    "Quarterback is absent after unlock : abandon");
            return null;
        }

        final CachedItemBean resultBean = pQuarterback.getItem(aItemId);

        pLogger.logInfo(this, "getItem", "\n\tgetItem(", aItemId, ") =",
                resultBean);

        return resultBean;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.services.IErpData#getItems(java.lang.String,
     * int, boolean, java.lang.String)
     */
    @Override
    public CachedItemBean[] getItems(final String aCategory,
            final int aItemsCount, final boolean aRandomize,
            final String aBaseId) {

        // Wait for the quarterback to become available
        if (!waitQuarterbackAccess()) {
            pLogger.logInfo(this, "getItems",
                    "Quarterback is absent after unlock : abandon");
            return null;
        }

        final CachedItemBean[] resultArray = pQuarterback.getItems(aCategory,
                aItemsCount, aRandomize, aBaseId);

        final StringBuilder resultLogBuilder = new StringBuilder();
        for (CachedItemBean bean : resultArray) {
            resultLogBuilder.append("\t\t").append(bean).append("\n");
        }

        pLogger.logInfo(this, "getItems", "\n\tgetItems(", aCategory, ", ",
                aItemsCount, ", ", aRandomize, ", ", aBaseId, ") =\n",
                resultLogBuilder, "\n");

        return resultArray;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpData#getItemsStock(java.lang.String
     * [])
     */
    @Override
    public CachedItemStockBean[] getItemsStock(final String[] aItemIds) {

        // Wait for the quarterback to become available
        if (!waitQuarterbackAccess()) {
            pLogger.logInfo(this, "getItemsStock",
                    "Quarterback is absent after unlock : abandon");
            return null;
        }

        final CachedItemStockBean[] resultArray = pQuarterback
                .getItemsStock(aItemIds);

        final StringBuilder resultLogBuilder = new StringBuilder();
        for (CachedItemStockBean bean : resultArray) {
            resultLogBuilder.append("\t\t").append(bean).append("\n");
        }

        pLogger.logInfo(this, "getItemsStock", "\n\tgetItemsStock(",
                Arrays.toString(aItemIds), ") =", Arrays.toString(resultArray));

        return resultArray;
    }

    /**
     * Blocks the access to the quarterback service
     */
    private void internalBlockQuarterbackAccess() {

        // Lock only if not already locked
        pWriteLock.tryLock();
    }

    /**
     * Unlocks the access to the quarterback service
     */
    private void internalUnblockQuarterbackAccess() {

        try {
            // Try to unlock in any case
            pWriteLock.unlock();

        } catch (IllegalMonitorStateException e) {
            /*
             * Ignore this exception at this level : it means that the lock
             * wasn't locked
             */

        } catch (Throwable th) {
            pLogger.logWarn(this, "unblockQuarterbackAccess",
                    "Error unlocking the write lock :", th);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Release the lock
        unblockQuarterbackAccess();

        pLogger.logInfo(this, "invalidatePojo",
                "Exported Data server service Gone");
    }

    /**
     * Called by iPOJO when the quarterback is bound
     * 
     * @param aQuarterback
     *            The bound service
     */
    @Unbind(id = IPOJO_QUARTERBACK_ID)
    protected void unbindQuarterback(final IQuarterback aQuarterback) {

        pLogger.logInfo(this, "unbindQuarterback",
                "Quarterback bound - BLOCK access");

        // Block clients until the service is bound again
        blockQuarterbackAccess();

        pQuarterbackPresent = false;

        pLogger.logInfo(this, "unbindQuarterback",
                "Quarterback bound - BLOCK access DONE");
    }

    /**
     * Unlocks the access to the quarterback service from the locker thread
     */
    protected void unblockQuarterbackAccess() {

        pLockRunner.execute(pRunWriteUnlocker);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Block clients until we get the service
        if (!pQuarterbackPresent) {
            pLogger.logInfo(this, "validatePojo",
                    "No quarterback service present");
            blockQuarterbackAccess();

        } else {
            pLogger.logInfo(this, "validatePojo", "Quarterback = "
                    + pQuarterback);
        }

        pLogger.logInfo(this, "validatePojo",
                "Exported Data server service Ready");
    }

    /**
     * Used by quarterback clients methods to be blocked until the service comes
     */
    protected boolean waitQuarterbackAccess() {

        pLogger.logInfo(this, "waitQuarterbackAccess",
                "Waiting for quarterback...");

        try {
            pReadLock.lock();

        } finally {
            // Just in case...
            pReadLock.unlock();
        }

        pLogger.logInfo(this, "waitQuarterbackAccess",
                "Waiting for quarterback DONE");

        return pQuarterbackPresent;
    }
}
