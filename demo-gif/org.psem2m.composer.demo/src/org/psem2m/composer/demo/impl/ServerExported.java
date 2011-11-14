/**
 * File:   ServerExported.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.demo.IErpData;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Exported data server service
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_SERVER_EXPORTED)
@Provides(specifications = IErpData.class)
public class ServerExported extends CPojoBase implements IErpData {

    /** applyCart treatment chain */
    @Requires(id = "applyCart")
    private IComponent pChainApplyCart;

    /** getItem treatment chain */
    @Requires(id = "getItem")
    private IComponent pChainGetItem;

    /** getItems treatment chain */
    @Requires(id = "getItems")
    private IComponent pChainGetItems;

    /** getItemsStock treatment chain */
    @Requires(id = "getItemsStock")
    private IComponent pChainGetItemsStock;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pInstanceName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.demo.IErpData#applyCart(java.util.Map)
     */
    @Override
    public Map<String, Object> applyCart(final Map<String, Object> aCart) {

        try {
            // Prepare the treatment map
            final Map<String, Object> treatmentMap = new HashMap<String, Object>();
            treatmentMap.put(IComponent.KEY_REQUEST, aCart);

            return pChainApplyCart.computeResult(treatmentMap);

        } catch (final Exception ex) {

            // Log the error
            pLogger.logSevere(this, "applyCart",
                    "Error treating an applyCart request.", ex);

            // Return an error map
            return makeErrorMap(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.demo.IErpData#getItem(java.lang.String)
     */
    @Override
    public Map<String, Object> getItem(final String aItemId) {

        try {
            // Prepare the treatment map
            final Map<String, Object> treatmentMap = new HashMap<String, Object>();
            final Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("itemId", aItemId);

            treatmentMap.put(IComponent.KEY_REQUEST, requestMap);

            return pChainGetItem.computeResult(treatmentMap);

        } catch (final Exception ex) {

            // Log the error
            pLogger.logSevere(this, "getItem",
                    "Error treating an getItem request.", ex);

            // Return an error map
            return makeErrorMap(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.demo.IErpData#getItems(java.lang.String, int,
     * boolean, java.lang.String)
     */
    @Override
    public Map<String, Object> getItems(final String aCategory,
            final int aItemsCount, final boolean aRandomize,
            final String aBaseId) {

        try {
            // Prepare the treatment map
            final Map<String, Object> treatmentMap = new HashMap<String, Object>();

            final Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("category", aCategory);
            requestMap.put("itemsCount", aItemsCount);
            requestMap.put("randomize", aRandomize);
            requestMap.put("baseId", aBaseId);

            treatmentMap.put(IComponent.KEY_REQUEST, requestMap);

            return pChainGetItems.computeResult(treatmentMap);

        } catch (final Exception ex) {

            // Log the error
            pLogger.logSevere(this, "getItems",
                    "Error treating an getItems request.", ex);

            // Return an error map
            return makeErrorMap(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.demo.IErpData#getItemsStock(java.lang.String[])
     */
    @Override
    public Map<String, Object> getItemsStock(final String[] aItemIds) {

        try {
            // Prepare the treatment map
            final Map<String, Object> treatmentMap = new HashMap<String, Object>();

            final Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("itemIds", aItemIds);

            treatmentMap.put(IComponent.KEY_REQUEST, requestMap);

            return pChainGetItemsStock.computeResult(treatmentMap);

        } catch (final Exception ex) {

            // Log the error
            pLogger.logSevere(this, "getItemsStock",
                    "Error treating an getItemsStock request.", ex);

            // Return an error map
            return makeErrorMap(ex);
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

        pLogger.logInfo(this, "invalidatePojo", "Component", pInstanceName,
                "Gone");
    }

    /**
     * Creates a map containing the given throwable
     * 
     * @param aThrowable
     *            A throwable
     * @return A map containing the given throwable
     */
    protected Map<String, Object> makeErrorMap(final Throwable aThrowable) {

        final Map<String, Object> resultMap = new HashMap<String, Object>();

        if (aThrowable != null) {
            resultMap.put(IComponent.KEY_ERROR, aThrowable);

        } else {
            resultMap.put(IComponent.KEY_ERROR, "No error given.");
        }

        return resultMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component", pInstanceName,
                "Ready");
    }
}
