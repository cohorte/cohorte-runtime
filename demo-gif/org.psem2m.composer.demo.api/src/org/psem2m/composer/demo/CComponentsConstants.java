/**
 * File:   DemoComponentsConstants.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo;

/**
 * Internal constants of the PSEM2M Composer Demo
 * 
 * @author Thomas Calmant
 */
public interface CComponentsConstants {

    /** The cache queue poller */
    String COMPONENT_CACHE_QUEUE_POLLER = "cache-queue-poller";

    /** Queued carts stock handler */
    String COMPONENT_COMPUTE_QUEUED_CARTS = "compute-queued-carts";

    /** applyCart end point */
    String COMPONENT_ENDPOINT_APPLYCART = "server-endpoint-applyCart";

    /** getItem end point */
    String COMPONENT_ENDPOINT_GETITEM = "server-endpoint-getItem";

    /** getItems end point */
    String COMPONENT_ENDPOINT_GETITEMS = "server-endpoint-getItems";

    /** getItemsStock end point */
    String COMPONENT_ENDPOINT_GETITEMSSTOCK = "server-endpoint-getItemsStock";

    /** The ERP caller */
    String COMPONENT_ERP_CALLER = "erp-caller";

    /** applyCart normalizer */
    String COMPONENT_NORMALIZER_APPLYCART = "normalizer-applyCart";

    /** getItem normalizer */
    String COMPONENT_NORMALIZER_GETITEM = "normalizer-getItem";

    /** getItems normalizer */
    String COMPONENT_NORMALIZER_GETITEMS = "normalizer-getItems";

    /** getItemsStock normalizer */
    String COMPONENT_NORMALIZER_GETITEMSSTOCK = "normalizer-getItemsStock";

    /** Exported server component type */
    String COMPONENT_SERVER_EXPORTED = "server-exported";

    /** The cache queue poller */
    String COMPONENT_STORE_IN_CACHE_QUEUE = "store-cache-queue";

    /** iPOJO component instance name */
    String PROPERTY_INSTANCE_NAME = "instance.name";

    /** The name of the wire to the next component */
    String WIRE_NEXT = "next";

    /** The name of the wire to the result normalizer */
    String WIRE_NORMALIZER = "normalizer";
}
