/**
 * File:   ErpClientConfig.java
 * Author: Thomas Calmant
 * Date:   4 oct. 2011
 */
package org.psem2m.demo.erp.client;

/**
 * ERP proxy configuration
 * 
 * @author Thomas Calmant
 */
public class ErpClientConfig {

    /** ERP HTTP port */
    public static final String ERP_PORT = "org.psem2m.erp.port";

    /** ERP access port */
    private int pErpPort;

    /**
     * Retrieves the ERP access port
     * 
     * @return The ERP access port
     */
    public int getErpPort() {

        return pErpPort;
    }

    /**
     * Prepares the ERP configuration
     * 
     * @throws Exception
     *             An error occurred while reading the configuration
     */
    public void init() throws Exception {

        final String erpPort = System.getProperty(ERP_PORT);
        pErpPort = Integer.parseInt(erpPort);
    }
}
