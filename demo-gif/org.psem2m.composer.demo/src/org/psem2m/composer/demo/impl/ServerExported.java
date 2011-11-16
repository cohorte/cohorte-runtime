/**
 * File:   ServerExported.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.psem2m.composer.test.api.ComponentContextBean;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.composer.test.api.IComponentContext;
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

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.demo.IErpData#applyCart(java.util.Map)
     */
    @Override
    public Map<String, Object> applyCart(final Map<String, Object> aCart) {

        try {
            // Prepare the component context
            final ComponentContextBean context = new ComponentContextBean();
            context.setRequest(aCart);

            return pChainApplyCart.computeResult(context).getResults().get(0);

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

        ComponentContextBean context = null;
        try {

            // Prepare the key map
            final Map<String, Object> wKeyMap = new HashMap<String, Object>();
            wKeyMap.put("itemId", aItemId);

            // Prepare the treatment map
            final Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put(IComponentContext.REQUEST_KEY, wKeyMap);

            // Prepare the component context
            context = new ComponentContextBean();
            context.setRequest(requestMap);

            return pChainGetItem.computeResult(context).getResults().get(0);

        } catch (final Exception ex) {

            // Log the error
            pLogger.logSevere(this, "getItem",
                    "Error treating an getItem request.", ex);

            pLogger.logSevere(this, "getItem-Errors", context.getErrors());

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
            // Prepare the criteria map
            final Map<String, Object> wCriteriaMap = new HashMap<String, Object>();
            wCriteriaMap.put("category", aCategory);
            wCriteriaMap.put("itemsCount", aItemsCount);
            wCriteriaMap.put("randomize", aRandomize);
            wCriteriaMap.put("baseId", aBaseId);

            // Prepare the treatment map
            final Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put(IComponentContext.REQUEST_CRITERIA, wCriteriaMap);

            // Prepare the component context
            final ComponentContextBean context = new ComponentContextBean();
            context.setRequest(requestMap);

            return pChainGetItems.computeResult(context).getResults().get(0);

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
            // Prepare the key list of key maps
            final List<Map<String, Object>> wKeyList = new ArrayList<Map<String, Object>>();

            // Prepare the key maps
            Map<String, Object> wKeyMap;
            for (String wItemId : aItemIds) {
                wKeyMap = new HashMap<String, Object>();
                wKeyMap.put("itemId", wItemId);
                wKeyList.add(wKeyMap);
            }

            // Prepare the treatment map
            final Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put(IComponentContext.REQUEST_KEYS, wKeyList);

            // Prepare the component context
            final ComponentContextBean context = new ComponentContextBean();
            context.setRequest(requestMap);

            return pChainGetItemsStock.computeResult(context).getResults()
                    .get(0);

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

        pLogger.logInfo(this, "invalidatePojo", "Component", pName, "Gone");
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

        pLogger.logInfo(this, "validatePojo", "Component", pName, "Ready");
    }
}
