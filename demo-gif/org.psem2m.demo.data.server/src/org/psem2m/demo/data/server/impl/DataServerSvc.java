/**
 * File:   DataServerSvc.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.server.impl;

import java.util.Arrays;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.handler.temporal.Temporal;
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
@Component(name = "psem2m-demo-dataserver-factory", publicFactory = true, propagation = true)
@Provides(specifications = IErpData.class)
public class DataServerSvc extends CPojoBase implements IErpData {

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * ERP/Cache strategy handler
     * 
     * <ul> <li>Timeout = 30 seconds (in milliseconds)</li> <li>onTimeout =
     * nullable : use a mock after the timeout</li> </ul>
     */
    @Temporal(timeout = 30000, onTimeout = "nullable")
    private IQuarterback pQuarterback;

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

        final CErpActionReport result = pQuarterback.applyCart(aCart);

        pLogger.logInfo(this, "applyCart", aCart, " (",
                Arrays.toString(aCart.getCartLines()), ") =", result);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.services.IErpData#getItem(java.lang.String)
     */
    @Override
    public CachedItemBean getItem(final String aItemId) {

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

        pLogger.logInfo(this, "invalidatePojo",
                "Exported Data server service Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo",
                "Exported Data server service Ready");
    }
}
