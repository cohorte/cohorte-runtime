/**
 * File:   DataServerSvc.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.server.impl;

import java.util.Arrays;
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
import org.psem2m.demo.data.server.IQuarterback;
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

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** ERP/Cache strategy handler */
    @Requires(id = IPOJO_QUARTERBACK_ID, optional = true)
    private IQuarterback pQuarterback;

    /** The quarterback waiting lock */
    private ReadWriteLock pQuarterbackWaitLock;

    /** The quarterback read lock */
    private Lock pReadLock;

    /** The quarterback write lock */
    private Lock pWriteLock;

    /**
     * Default constructor
     */
    public DataServerSvc() {

        super();
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
        waitQuarterbackAccess();

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

        // Release clients
        unblockQuarterbackAccess();
    }

    /**
     * Blocks the access to the quarterback service
     */
    protected void blockQuarterbackAccess() {

        pWriteLock.lock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.services.IErpData#getItem(java.lang.String)
     */
    @Override
    public CachedItemBean getItem(final String aItemId) {

        // Wait for the quarterback to become available
        waitQuarterbackAccess();

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
        waitQuarterbackAccess();

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
        waitQuarterbackAccess();

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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pReadLock.unlock();
        pWriteLock.unlock();

        pReadLock = null;
        pWriteLock = null;
        pQuarterbackWaitLock = null;

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

        // Block clients until the service is bound again
        blockQuarterbackAccess();
    }

    /**
     * Unlocks the access to the quarterback service
     */
    protected void unblockQuarterbackAccess() {

        pWriteLock.unlock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pQuarterbackWaitLock = new ReentrantReadWriteLock();
        pReadLock = pQuarterbackWaitLock.readLock();
        pWriteLock = pQuarterbackWaitLock.writeLock();

        // Block clients until we get the service
        blockQuarterbackAccess();

        pLogger.logInfo(this, "validatePojo",
                "Exported Data server service Ready");
    }

    /**
     * Used by quarterback clients methods to be blocked until the service comes
     */
    protected void waitQuarterbackAccess() {

        pReadLock.lock();
        pReadLock.unlock();
    }
}
