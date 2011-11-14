/**
 * File:   ErpProxy.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.erpproxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jabsorb.client.Client;
import org.jabsorb.client.HTTPSession;
import org.jabsorb.client.Session;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.IErpData;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.isolates.base.BundlesClassLoader;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Creates a JSON-RPC proxy to the ERP
 * 
 * @author Thomas Calmant
 */
@Component(name = "erp-proxy-json-rpc")
@Provides(specifications = IComponent.class)
public class ErpProxy extends CPojoBase implements IComponent {

    /** The bundle context */
    private BundleContext pBundleContext;

    /** The Jabsorb client */
    private Client pClient;

    /** The ERP host name */
    @Property(name = "host")
    private String pErpHost;

    /** The ERP host port */
    @Property(name = "port")
    private int pErpPort;

    /** The URI of the JSON-RPC servlet, if any */
    @Property(name = "uri")
    private String pErpUri;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The ERP proxy */
    private IErpData pProxy;

    /**
     * Called by iPOJO...
     * 
     * @param aContext
     *            The bundle context
     */
    public ErpProxy(final BundleContext aContext) {

        super();
        pBundleContext = aContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public Map<String, Object> computeResult(final Map<String, Object> aData)
            throws Exception {

        // The ERP method to call
        final String method = (String) aData.get("erp-method");
        if (method == null || method.isEmpty()) {
            aData.put(KEY_ERROR, "No ERP method specified.");
            return aData;
        }

        // Call the corresponding method
        if (method.equals("getItem")) {
            getItem(aData);

        } else if (method.equals("getItems")) {
            getItems(aData);

        } else if (method.equals("getItemsStock")) {
            getItemsStock(aData);

        } else {
            aData.put(KEY_ERROR, "Method '" + method + "' not implemented");
        }

        return aData;
    }

    /**
     * Calls the ERP
     * 
     * @param aData
     *            The chain component parameter
     */
    @SuppressWarnings("unchecked")
    protected void getItem(final Map<String, Object> aData) {

        // Get the request map
        final Map<String, Object> requestMap = (Map<String, Object>) aData
                .get(KEY_REQUEST);
        final String itemId = (String) requestMap.get("itemId");

        // Call the ERP
        final Map<String, Object> result = pProxy.getItem(itemId);

        // Store the result
        aData.put(KEY_RESULT, result);
    }

    /**
     * Calls the ERP
     * 
     * @param aData
     *            The chain component parameter
     */
    @SuppressWarnings("unchecked")
    protected void getItems(final Map<String, Object> aData) {

        // Get the request map
        final Map<String, Object> requestMap = (Map<String, Object>) aData
                .get(KEY_REQUEST);

        // Get the parameters
        final String category = (String) requestMap.get("category");
        final Integer itemsCountObj = (Integer) requestMap.get("itemsCount");
        final Boolean randomizeObj = (Boolean) requestMap.get("randomize");
        final String baseId = (String) requestMap.get("baseId");

        // Convert objects to primitives, taking care of null values
        int itemsCount = 0;
        if (itemsCountObj != null) {
            itemsCount = itemsCountObj.intValue();
        }

        boolean randomize = false;
        if (randomizeObj != null) {
            randomize = randomizeObj.booleanValue();
        }

        // Call the ERP
        final Map<String, Object> result = pProxy.getItems(category,
                itemsCount, randomize, baseId);

        // Store the result
        aData.put(KEY_RESULT, result);
    }

    /**
     * Calls the ERP
     * 
     * @param aData
     *            The chain component parameter
     */
    @SuppressWarnings("unchecked")
    protected void getItemsStock(final Map<String, Object> aData) {

        // Get the request map
        final Map<String, Object> requestMap = (Map<String, Object>) aData
                .get(KEY_REQUEST);
        final String[] itemIds = (String[]) requestMap.get("itemIds");

        // Call the ERP
        final Map<String, Object> result = pProxy.getItemsStock(itemIds);

        // Store the result
        aData.put(KEY_RESULT, result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pClient.closeProxy(pProxy);
        pProxy = null;

        pLogger.logInfo(this, "invalidatePojo", "ERP Proxy gone");
    }

    /**
     * Prepares the Jabsorb client
     * 
     * @throws URISyntaxException
     *             Error computing the ERP URI
     */
    protected void prepareClient() throws URISyntaxException {

        // Using HTTP...
        final StringBuilder builder = new StringBuilder("http://");

        // Host
        if (pErpHost == null || pErpHost.isEmpty()) {
            builder.append("localhost");
        } else {
            builder.append(pErpHost);
        }

        // Port
        if (pErpPort > 0) {
            builder.append(":").append(pErpPort);
        }

        // Sub-URI
        if (pErpUri != null && !pErpUri.isEmpty()) {

            if (!pErpUri.startsWith("/")) {
                builder.append("/");
            }
            builder.append(pErpUri);

        } else {
            builder.append("/");
        }

        // Prepare the Jabsorb client
        final Session session = new HTTPSession(new URI(builder.toString()));

        pClient = new Client(session, new BundlesClassLoader(pBundleContext));
        pProxy = (IErpData) pClient.openProxy(null, IErpData.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        try {
            prepareClient();
            pLogger.logInfo(this, "validatePojo", "ERP Proxy ready");

        } catch (final URISyntaxException e) {

            pLogger.logSevere(this, "validatePojo",
                    "Error preparing the ERP Proxy :", e);
        }
    }
}
