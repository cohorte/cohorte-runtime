/**
 * File:   DataServerSvc.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.server.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.server.IQuarterback;
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

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** ERP/Cache strategy handler */
    @Requires
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
     * @see org.psem2m.demo.erp.api.services.IErpData#getItems(java.lang.String)
     */
    @Override
    public CachedItemBean[] getItems(final String aCategory) {

        return pQuarterback.getItems(aCategory);
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

        return pQuarterback.getItemsStock(aItemIds);
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
