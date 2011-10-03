/**
 * File:   FakeErpClient.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.client;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.erp.api.beans.ItemBean;
import org.psem2m.demo.erp.api.services.IErpDataProxy;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Fake implementation of the ERP proxy
 * 
 * @author Thomas Calmant
 */
@Component(name = "demo-erp-client-fake-factory", publicFactory = false, propagation = true)
@Provides(specifications = IErpDataProxy.class)
public class FakeErpClient extends CPojoBase implements IErpDataProxy {

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Default constructor
     */
    public FakeErpClient() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpDataProxy#getItems(java.lang.String)
     */
    @Override
    public ItemBean[] getItems(final String aCategory) {

        final ItemBean bean = new ItemBean();
        bean.setDescription("TOTO TOTO TOTO");
        bean.setId(aCategory + ".item.toto");
        bean.setPrice("100.00 EUR");

        return new ItemBean[] { bean };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpDataProxy#getItemsStock(java.lang
     * .String[])
     */
    @Override
    public int[] getItemsStock(final String[] aItemIds) {

        int result[] = new int[aItemIds.length];
        for (int i = 0; i < aItemIds.length; i++) {

            final String itemId = aItemIds[i];
            if (itemId.startsWith("titi")) {
                result[i] = 42;
            } else {
                result[i] = 128;
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Fake ERP Client Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Fake ERP Client Gone");
    }

}
