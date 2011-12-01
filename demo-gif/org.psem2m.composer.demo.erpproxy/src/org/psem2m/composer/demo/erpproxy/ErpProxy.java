/**
 * File:   ErpProxy.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.erpproxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Controller;
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
import org.psem2m.composer.demo.CComponentsConstants;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
import org.psem2m.composer.demo.IErpData;
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

    /** The component life cycle controller */
    @Controller
    private boolean pController;

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

    /** The instance name */
    @Property(name = CComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The ping thread */
    private Thread pPingThread;

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

    /**
     * Calls the ERP
     * 
     * @param aContext
     *            The chain component context
     */
    protected void applyCart(final IComponentContext aContext) {

        // Call the ERP
        final Map<String, Object> result = pProxy.applyCart(aContext
                .getRequest());

        // Store the result
        aContext.setResult(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.test.api.IComponent#computeResult(org.psem2m.composer
     * .test.api.IComponentContext)
     */
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        try {

            // The ERP method to call
            final String method = (String) aContext.getMetadata().get(
                    "erp-method");
            if (method == null || method.isEmpty()) {
                aContext.addError(pName, "No ERP method specified.");
                return aContext;
            }

            // Call the corresponding method
            if (method.equals("applyCart")) {
                applyCart(aContext);

            } else if (method.equals("getItem")) {
                getItem(aContext);

            } else if (method.equals("getItems")) {
                getItems(aContext);

            } else if (method.equals("getItemsStock")) {
                getItemsStock(aContext);

            } else {
                aContext.addError(pName, "Method '" + method
                        + "' not implemented");
            }

            return aContext;

        } catch (final Exception ex) {

            // Stop the component
            pController = false;

            // Re-throw the exception
            throw ex;
        }
    }

    /**
     * Calls the ERP
     * 
     * @param aContext
     *            The chain component context
     */
    protected void getItem(final IComponentContext aContext) {

        // Get the key map
        @SuppressWarnings("unchecked")
        final Map<String, Object> keyMap = (Map<String, Object>) aContext
                .getRequest().get(IComponentContext.REQUEST_KEY);

        // Get the itemId
        final String itemId = (String) keyMap.get("itemId");

        // Call the ERP
        final Map<String, Object> result = pProxy.getItem(itemId);

        // Store the result
        aContext.setResult(result);
    }

    /**
     * Calls the ERP
     * 
     * @param aContext
     *            The chain component context
     */
    protected void getItems(final IComponentContext aContext) {

        // Get the request map
        @SuppressWarnings("unchecked")
        final Map<String, Object> wCriteriaMap = (Map<String, Object>) aContext
                .getRequest().get(IComponentContext.REQUEST_CRITERIA);

        // Get the parameters
        final String category = (String) wCriteriaMap.get("category");
        final Integer itemsCountObj = (Integer) wCriteriaMap.get("itemsCount");
        final Boolean randomizeObj = (Boolean) wCriteriaMap.get("randomize");
        final String baseId = (String) wCriteriaMap.get("baseId");

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
        final List<Map<String, Object>> result = pProxy.getItems(category,
                itemsCount, randomize, baseId);

        // Store the result
        aContext.getResults().clear();
        aContext.getResults().addAll(result);
    }

    /**
     * Calls the ERP
     * 
     * @param aContext
     *            The chain component context
     */
    protected void getItemsStock(final IComponentContext aContext) {

        // Get the request map
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> wKeyList = (List<Map<String, Object>>) aContext
                .getRequest().get(IComponentContext.REQUEST_KEYS);

        final List<String> wItemIds = new ArrayList<String>();
        for (final Map<String, Object> wKeyMap : wKeyList) {
            wItemIds.add((String) wKeyMap.get("itemId"));
        }

        final String[] itemIds = wItemIds.toArray(new String[0]);

        // Call the ERP
        final List<Map<String, Object>> result = pProxy.getItemsStock(itemIds);

        // Store the result
        aContext.getResults().clear();
        aContext.getResults().addAll(result);
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

        // Stop all thread if necessary
        if (pPingThread != null) {
            pPingThread.interrupt();
        }

        // Prepare the ping thread
        pPingThread = new Thread() {

            @Override
            public void run() {

                while (!isInterrupted() && !pController) {

                    ping();

                    if (!pController) {
                        try {
                            Thread.sleep(500);
                        } catch (final InterruptedException e) {
                            // Interrupted = stopped
                            return;
                        }
                    }
                }
            }
        };

        // Start the ping thread
        pPingThread.setDaemon(true);
        pPingThread.start();

        pLogger.logInfo(this, "invalidatePojo", "ERP Proxy gone");
    }

    /**
     * Sends a getItem("?") request to the ERP to test if it is alive
     */
    private void ping() {

        try {
            prepareClient();
        } catch (final URISyntaxException e) {

            pLogger.logSevere(this, "ping", "Can't ping", e);
            return;
        }

        // Try to call the ERP
        boolean newState;
        try {
            pProxy.getItem("?");
            newState = true;

        } catch (final Throwable th) {

            // Stay stopped...
            newState = false;
        }

        pClient.closeProxy(pProxy);

        pController = newState;
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

        // Stop the ping thread, if any
        if (pPingThread != null) {
            pPingThread.interrupt();
            pPingThread = null;
        }

        try {
            prepareClient();
            pLogger.logInfo(this, "validatePojo", "ERP Proxy ready");

        } catch (final URISyntaxException e) {

            pLogger.logSevere(this, "validatePojo",
                    "Error preparing the ERP Proxy :", e);
        }
    }
}
