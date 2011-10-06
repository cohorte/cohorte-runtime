/**
 * File:   ErpClient.java
 * Author: Thomas Calmant
 * Date:   4 oct. 2011
 */
package org.psem2m.demo.erp.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.erp.api.beans.ItemBean;
import org.psem2m.demo.erp.api.services.IErpDataProxy;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.slave.agent.ISvcAgent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Implementation of the ERP proxy. Sends requests to the home-brewed Python ERP
 * 
 * @author Thomas Calmant
 */
@Component(name = "demo-erp-client-python-factory", publicFactory = false, propagation = true)
@Provides(specifications = IErpDataProxy.class)
public class ErpClient extends CPojoBase implements IErpDataProxy {

    /** getItem() method URI */
    public static final String GET_ITEM_URI = "/getItem";

    /** getItemsStock() method URI */
    public static final String GET_ITEMS_STOCK_URI = "/getItemsStock";

    /** getItems() method URI */
    public static final String GET_ITEMS_URI = "/getItems";

    /** /getState URI */
    public static final String GET_STATE_URI = "/getStateERP";

    /** The slave agent (to kill the isolate) */
    @Requires
    private ISvcAgent pAgentSvc;

    /** ERP Client configuration */
    private ErpClientConfig pConfig;

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Number of ERP access failures */
    private int pNbFailures = 0;

    /** PSEM2M Service export flag property */
    @ServiceProperty(name = "psem2m.service.export", value = "false")
    private boolean pPropertyExported = false;

    /**
     * Default constructor
     */
    public ErpClient() {

        super();
    }

    /**
     * Converts the given DOM document to an XML string
     * 
     * @param aDocument
     *            DOM Document to be converted
     * @return The XML string or null
     */
    protected String domToString(final Document aDocument) {

        if (aDocument == null) {
            return null;
        }

        // Prepare the transformer
        final Transformer transformer;
        try {
            final TransformerFactory transfromerFactory = TransformerFactory
                    .newInstance();

            transformer = transfromerFactory.newTransformer();

        } catch (TransformerConfigurationException e) {
            pLogger.logSevere(this, "domToString",
                    "Error creating the transformer", e);
            return null;
        }

        // Prepare the source
        final DOMSource xmlSource = new DOMSource(aDocument);

        // Prepare the String output
        final StringWriter stringWriter = new StringWriter();
        final StreamResult xmlResult = new StreamResult(stringWriter);

        try {
            // Transform
            transformer.transform(xmlSource, xmlResult);

        } catch (TransformerException e) {
            pLogger.logSevere(this, "domToString",
                    "Error transforming the Document into a String", e);
            return null;
        }

        // Retrieve the result string
        return stringWriter.toString();
    }

    /**
     * Prepares an access URL for the given URI in the ERP
     * 
     * @param aUri
     *            URI in the ERP ("/" if null)
     * @param aQuery
     *            Request parameters (can be null)
     * @return The forged URL
     * @throws MalformedURLException
     *             The generated URL is invalid
     */
    protected URL forgeUrl(final String aUri, final String aQuery)
            throws MalformedURLException {

        // Get the access port
        final int port = pConfig.getErpPort();

        // Prepare the URL
        final StringBuilder urlBuilder = new StringBuilder("http://localhost:");
        urlBuilder.append(port);

        // Append the URI
        if (aUri == null || !aUri.startsWith("/")) {
            urlBuilder.append("/");
        }

        if (aUri != null) {
            urlBuilder.append(aUri);
        }

        // Append the query, if any
        if (aQuery != null && !aQuery.isEmpty()) {

            if (!aQuery.startsWith("?")) {
                urlBuilder.append("?");
            }

            urlBuilder.append(aQuery);
        }

        return new URL(urlBuilder.toString());
    }

    /**
     * Retrieves the DOM document builder
     * 
     * @return A the DOM document builder, null on error
     */
    protected DocumentBuilder getDomDocumentBuilder() {

        // Prepare the DOM document creation
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory
                    .newInstance();
            return docFactory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            pLogger.logSevere(this, "getDomDocumentBuilder",
                    "Can't create a document builder.");
        }

        return null;
    }

    /**
     * Retrieves the content of the first child of given element with the given
     * tag name.
     * 
     * @param aElement
     *            A parent element
     * @param aTagName
     *            A child tag name
     * @return The child node value, null on error
     */
    protected String getElementChildValue(final Element aElement,
            final String aTagName) {

        if (aElement == null || aTagName == null) {
            return null;
        }

        final Node tagNode = aElement.getElementsByTagName(aTagName).item(0);
        if (tagNode == null) {
            return null;
        }

        return tagNode.getTextContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpDataProxy#getItem(java.lang.String)
     */
    @Override
    public ItemBean getItem(final String aItemId) {

        if (aItemId == null || aItemId.isEmpty()) {
            // No ID given
            return null;
        }

        // Prepare the query
        final String query = "?id=" + aItemId;

        try {
            // Prepare the URL
            final URL erpUrl = forgeUrl(GET_ITEM_URI, query);

            final String result = getUrlResult(erpUrl);
            if (result != null) {
                // Reset failure counter
                pNbFailures = 0;

                final ItemBean[] resultArray = xmlToItemBeans(result);
                if (result != null && resultArray.length > 0) {
                    // Return the first element found
                    return resultArray[0];

                } else {
                    // Invalid / empty result
                    pLogger.logInfo(this, "getItem",
                            "Bad result from ERP (null or empty)");
                    return null;
                }

            } else {
                // No answer
                pLogger.logInfo(this, "getItem",
                        "No answer from the ERP (null)");

                // Kill isolate in case of many errors
                handleFailure();

                return null;
            }

        } catch (MalformedURLException e) {
            pLogger.logSevere(this, "getItem", "Error generating the ERP URL",
                    e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpDataProxy#getItems(java.lang.String,
     * int, boolean, java.lang.String)
     */
    @Override
    public ItemBean[] getItems(final String aCategory, final int aItemsCount,
            final boolean aRandomize, final String aBaseId) {

        // Prepare the POST query
        final Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("category", aCategory);
        queryMap.put("count", String.valueOf(aItemsCount));
        queryMap.put("randomize", String.valueOf(aRandomize));
        queryMap.put("baseId", aBaseId);

        final String query = prepareSimplePostRequest("criteria", queryMap);
        if (query == null) {
            // Bad query content
            return null;
        }

        try {
            // Prepare the URL
            final URL erpUrl = forgeUrl(GET_ITEMS_URI, null);

            final String result = getUrlPOSTResult(erpUrl, query);
            if (result != null) {
                // Reset failure counter
                pNbFailures = 0;

                return xmlToItemBeans(result);

            } else {
                pLogger.logInfo(this, "getItem",
                        "No answer from the ERP (null)");

                // Kill isolate in case of many errors
                handleFailure();
            }

        } catch (MalformedURLException e) {
            pLogger.logSevere(this, "getItems", "Error generating the ERP URL",
                    e);
        }

        return null;
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

        if (aItemIds == null) {
            return null;
        }

        // Prepare the Query
        final List<Map<String, String>> queryList = new ArrayList<Map<String, String>>(
                aItemIds.length);

        // Prepare item nodes
        for (String itemId : aItemIds) {
            final Map<String, String> itemMap = new HashMap<String, String>();
            itemMap.put("id", itemId);
            queryList.add(itemMap);
        }

        // Generate the XML POST query
        final String xmlQuery = prepareListPostRequest("items", "item",
                queryList);

        try {
            // Prepare the URL
            final URL erpUrl = forgeUrl(GET_ITEMS_STOCK_URI, null);

            final String result = getUrlPOSTResult(erpUrl, xmlQuery);
            if (result != null) {
                // Reset failure counter
                pNbFailures = 0;

                // Analyze the XML result
                final Map<String, Integer> resultMap = xmlToItemStocks(result);
                if (resultMap == null) {
                    return null;
                }

                // Convert the map into an integer array
                final int[] resultArray = new int[aItemIds.length];
                for (int i = 0; i < aItemIds.length; i++) {
                    final Integer stock = resultMap.get(aItemIds[i]);
                    if (stock == null) {
                        resultArray[i] = -1;
                    } else {
                        resultArray[i] = stock.intValue();
                    }
                }

                // Return here
                return resultArray;

            } else {
                // Kill isolate in case of many errors
                handleFailure();
                return null;
            }

        } catch (MalformedURLException e) {
            pLogger.logSevere(this, "getItems", "Error generating the ERP URL",
                    e);
        }

        return null;
    }

    /**
     * Connects to the given URL and retrieves the POST response content on
     * success. Returns null on error or if the server response code is not 200.
     * 
     * @param aUrl
     *            URL to connect to
     * @return The server response on success, else null
     */
    protected String getUrlPOSTResult(final URL aUrl, final String aPostBody) {

        HttpURLConnection connection = null;
        try {
            // Connect to the URL
            connection = (HttpURLConnection) aUrl.openConnection();

            // Use a 2 seconds timeout
            connection.setReadTimeout(2000);

            // Prepare the POST request
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            final byte[] bodyContent = aPostBody.getBytes();
            connection.setRequestProperty("content-length",
                    Integer.toString(bodyContent.length));

            connection.getOutputStream().write(bodyContent);

            final int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                // Something happened
                pLogger.logInfo(this, "getUrlPOSTResult",
                        "Error during a POST request to ", aUrl, " - code=",
                        code);
                return null;
            }

            /*
             * Read the response content See here for more information :
             * http://weblogs
             * .java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
             */
            return new Scanner(connection.getInputStream()).useDelimiter("\\A")
                    .next();

        } catch (MalformedURLException e) {
            pLogger.logSevere(this, "getItems", "Error generating the ERP URL",
                    e);

        } catch (IOException e) {
            pLogger.logSevere(this, "getItems", "Error connecting the ERP", e);

        } finally {
            // Be nice, disconnect
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * Connects to the given URL and retrieves the response content on success.
     * Returns null on error or if the server response code is not 200.
     * 
     * @param aUrl
     *            URL to connect to
     * @return The server response on success, else null
     */
    protected String getUrlResult(final URL aUrl) {

        if (aUrl == null) {
            return null;
        }

        HttpURLConnection connection = null;
        try {
            // Connect to the URL
            connection = (HttpURLConnection) aUrl.openConnection();

            final int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                // Something happened
                pLogger.logInfo(this, "getUrlPOSTResult",
                        "Error during a POST request to ", aUrl, " - code=",
                        code);
                return null;
            }

            /*
             * Read the response content See here for more information :
             * http://weblogs
             * .java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
             */
            return new Scanner(connection.getInputStream()).useDelimiter("\\A")
                    .next();

        } catch (MalformedURLException e) {
            pLogger.logSevere(this, "getItems", "Error generating the ERP URL",
                    e);

        } catch (IOException e) {
            pLogger.logSevere(this, "getItems", "Error connecting the ERP", e);

        } finally {
            // Be nice, disconnect
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * Tests if the ERP failed to often, in which case the isolate is stopped.
     */
    protected void handleFailure() {

        pNbFailures++;

        if (pNbFailures >= 3) {
            // Log the failure
            pLogger.logSevere(this, "handleFailure",
                    "TOO MANY FAILS FROM ERP : ABANDON");

            // Kill isolate
            pAgentSvc.killIsolate();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

        pPropertyExported = false;
        pLogger.logInfo(this, "invalidatePojo", "Python ERP Client Gone");
    }

    /**
     * Returns the service export flag state
     * 
     * @return The service export flag state
     */
    public boolean isServiceExported() {

        return pPropertyExported;
    }

    /**
     * Converts the given item IDs array to an XML string
     * 
     * @param aItemIds
     *            An array of item IDs
     * @return The corresponding XML string
     */
    protected String itemsIdsToXml(final String[] aItemIds) {

        if (aItemIds == null) {
            // Invalid array
            return null;
        }

        // Get the document builder
        final DocumentBuilder docBuilder = getDomDocumentBuilder();
        if (docBuilder == null) {
            return null;
        }

        // Write the document
        final Document document = docBuilder.newDocument();

        // The root
        final Element rootNode = document.createElement("items");
        document.appendChild(rootNode);

        for (String itemId : aItemIds) {
            // The nodes
            final Element itemNode = document.createElement("item");
            rootNode.appendChild(itemNode);

            final Element itemIdNode = document.createElement("id");
            itemNode.appendChild(itemIdNode);

            final Text itemIdData = document.createTextNode(itemId);
            itemIdNode.appendChild(itemIdData);
        }

        return domToString(document);
    }

    /**
     * Parses the given XML data using the DOM parser.
     * 
     * @param aXmlData
     *            An XML String stream
     * @return The parsed DOM document, null on error.
     */
    protected Document parseXmlString(final String aXmlData) {

        // Get the DOM document builder
        final DocumentBuilder docBuilder = getDomDocumentBuilder();
        if (docBuilder == null) {
            return null;
        }

        // Parse the Data
        try {
            return docBuilder.parse(new ByteArrayInputStream(aXmlData
                    .getBytes()));

        } catch (SAXException e) {
            pLogger.logSevere(this, "parseXmlString",
                    "Error parsing the XML data", e);

        } catch (IOException e) {
            pLogger.logSevere(this, "parseXmlString",
                    "Error reading the XML data", e);
        }

        return null;
    }

    /**
     * Prepares an XML String data. The root node as the given tag name. Each
     * item corresponds to a root child with the given item tag name.
     * 
     * @param aRootTag
     *            XML Root tag name
     * @param aItemName
     *            Item node name
     * @param aItems
     *            Item children description
     * @return An XML string, null on error
     */
    protected String prepareListPostRequest(final String aRootTag,
            final String aItemName, final List<Map<String, String>> aItems) {

        if (aRootTag == null || aRootTag.isEmpty() || aItemName == null
                || aItemName.isEmpty() || aItems == null || aItems.isEmpty()) {
            // Invalid array
            return null;
        }

        // Get the document builder
        final DocumentBuilder docBuilder = getDomDocumentBuilder();
        if (docBuilder == null) {
            return null;
        }

        // Write the document
        final Document document = docBuilder.newDocument();

        // The root
        final Element rootNode = document.createElement(aRootTag);
        document.appendChild(rootNode);

        // The items
        for (Map<String, String> listItem : aItems) {

            if (listItem == null) {
                // Ignore null items
                continue;
            }

            // Prepare the item node, but don't add it yet
            final Element itemNode = document.createElement(aItemName);

            // Generate the item children
            for (Entry<String, String> mapEntry : listItem.entrySet()) {

                final String key = mapEntry.getKey();
                if (key == null) {
                    // Don't use null keys...
                    continue;
                }

                String value = mapEntry.getValue();
                if (value == null) {
                    // Consider null values as empty ones
                    value = "";
                }

                // The node
                final Element keyNode = document.createElement(key);
                itemNode.appendChild(keyNode);

                // The data
                keyNode.appendChild(document.createTextNode(value));
            }

            // Only add the item node if it useful
            if (itemNode.hasChildNodes()) {
                rootNode.appendChild(itemNode);
            }
        }

        // Convert to a string
        return domToString(document);
    }

    /**
     * Prepares an XML POST request. Root node gets the given root tag name. Map
     * keys are root node children tag names, values are Text nodes.
     * 
     * @param aRootTag
     *            The root node tag name
     * @param aRequestValues
     *            Children tag names -&gt; value
     * @return The corresponding XML data
     */
    protected String prepareSimplePostRequest(final String aRootTag,
            final Map<String, String> aRequestValues) {

        if (aRootTag == null || aRequestValues == null
                || aRequestValues.isEmpty()) {
            // Invalid map
            return null;
        }

        // Get the document builder
        final DocumentBuilder docBuilder = getDomDocumentBuilder();
        if (docBuilder == null) {
            return null;
        }

        // Write the document
        final Document document = docBuilder.newDocument();

        // The root
        final Element rootNode = document.createElement(aRootTag);
        document.appendChild(rootNode);

        for (Entry<String, String> mapEntry : aRequestValues.entrySet()) {

            final String key = mapEntry.getKey();
            if (key == null) {
                // Don't use null keys...
                continue;
            }

            String value = mapEntry.getValue();
            if (value == null) {
                // Consider null values as empty ones
                value = "";
            }

            // The node
            final Element keyNode = document.createElement(key);
            rootNode.appendChild(keyNode);

            // The data
            keyNode.appendChild(document.createTextNode(value));
        }

        // Convert to a string
        return domToString(document);
    }

    /**
     * Tries to access the ERP. Considers an HTTP 200 code on /getStateErp as a
     * success.
     * 
     * @return True on success, False on error.
     */
    protected boolean testErpConnection() {

        try {
            final URL stateUrl = forgeUrl(GET_STATE_URI, null);

            // Connect to the URL
            HttpURLConnection connection = (HttpURLConnection) stateUrl
                    .openConnection();

            final int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                // Something happened
                pLogger.logInfo(this, "testErpConnection",
                        "ERP answered with code=", code);
                return false;
            }

            // OK
            return true;

        } catch (MalformedURLException e) {
            pLogger.logSevere(this, "testErpConnection",
                    "Can't forge the ERP URL");

        } catch (Exception e) {
            pLogger.logSevere(this, "testErpConnection", "Can't access the ERP");
        }

        return false;
    }

    /**
     * Validates the presence of the ERP. Kills the isolate after 3 failures.
     */
    protected void validateErpPresence() {

        pLogger.logInfo(this, "validateErpPresence", "Test ERP presence...");

        // Test the connection to the ERP, kill the isolate on failure
        boolean erpAccessible = false;
        for (int nbTries = 0; nbTries < 3; nbTries++) {

            if (testErpConnection()) {
                erpAccessible = true;
                break;
            }

            // Give some more time...
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // We may be killed in another way
                return;
            }
        }

        pLogger.logInfo(this, "validateErpPresence", "ERP Present =",
                erpAccessible);

        if (erpAccessible) {
            // Client ready to roll
            pPropertyExported = true;

        } else {
            // Can't access the ERP, suicide
            pAgentSvc.killIsolate();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Reset the failure counter
        pNbFailures = 0;

        // Read the ERP client configuration
        pConfig = new ErpClientConfig();
        try {
            pConfig.init();

        } catch (Exception e) {
            pLogger.logSevere(this, "validatePojo",
                    "Error reading ERP Client configuration.", e);

            throw new BundleException("Error reading ERP Client configuration",
                    e);
        }

        // Validate the ERP presence in a new thread
        new Thread(new Runnable() {

            @Override
            public void run() {

                validateErpPresence();
            }
        }).start();

        pLogger.logInfo(this, "validatePojo", "Python ERP Client Ready");
    }

    /**
     * Converts the given XML data to an item beans array
     * 
     * @param aXmlData
     *            XML data received from the ERP
     * @return An array of item beans
     */
    protected ItemBean[] xmlToItemBeans(final String aXmlData) {

        // Parse the document
        final Document document = parseXmlString(aXmlData);
        if (document == null) {
            return null;
        }

        // Prepare the result list
        final List<ItemBean> resultList = new ArrayList<ItemBean>();

        // Get the root node
        final Element rootNode = document.getDocumentElement();

        // Grab all items
        final NodeList itemsList = rootNode.getElementsByTagName("item");
        for (int i = 0; i < itemsList.getLength(); i++) {

            final Element itemElement = (Element) itemsList.item(i);

            final String itemId = getElementChildValue(itemElement, "id");
            final String itemName = getElementChildValue(itemElement, "lib");
            final String itemDesc = getElementChildValue(itemElement, "text");
            final String itemPrice = getElementChildValue(itemElement, "price");

            if (itemId != null && itemName != null && itemDesc != null
                    && itemPrice != null) {

                // Construct the bean
                final ItemBean itemBean = new ItemBean();
                itemBean.setId(itemId);
                itemBean.setName(itemName);
                itemBean.setDescription(itemDesc);
                itemBean.setPrice(itemPrice);

                resultList.add(itemBean);
            }
        }

        return resultList.toArray(new ItemBean[resultList.size()]);
    }

    /**
     * Converts the stock nodes in the given XML data to an map
     * 
     * @param aXmlData
     *            XML data received from the ERP
     * @return A itemId - stock map
     */
    protected Map<String, Integer> xmlToItemStocks(final String aXmlData) {

        // Parse the document
        final Document document = parseXmlString(aXmlData);
        if (document == null) {
            return null;
        }

        // Result preparation
        final Map<String, Integer> resultMap = new HashMap<String, Integer>();

        // Get the root node
        final Element rootNode = document.getDocumentElement();

        // Grab all items
        final NodeList itemsList = rootNode.getElementsByTagName("item");
        for (int i = 0; i < itemsList.getLength(); i++) {

            try {
                final Element itemElement = (Element) itemsList.item(i);

                final String itemId = getElementChildValue(itemElement, "id");
                final String itemStock = getElementChildValue(itemElement,
                        "stock");

                if (itemId != null && itemStock != null) {
                    resultMap.put(itemId, Integer.valueOf(itemStock));
                }

            } catch (Exception e) {
                // Ignore exception...
                pLogger.logWarn(this, "xmlToItemsStock",
                        "Error reading a node value : ", e);
            }
        }

        return resultMap;
    }
}
