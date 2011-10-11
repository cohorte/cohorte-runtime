/**
 * File:   ICartQueue.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.server;

import java.util.Map;
import java.util.concurrent.Semaphore;

import org.psem2m.demo.erp.api.beans.CCart;
import org.psem2m.demo.erp.api.beans.CErpActionReport;

/**
 * Represents a cart queue handler
 * 
 * @author Thomas Calmant
 */
public interface ICartQueue {

    /**
     * Adds the given cart to the agent queue and returns the semaphore
     * associated to it
     * 
     * @param aCart
     *            A cart
     * @return The semaphore associated to the cart, null on error
     */
    Semaphore enqueueCart(CCart aCart);

    /**
     * Retrieves and removes the ERP action report from the agent result map
     * 
     * @param aCart
     *            A cart
     * @return The associated report, null on error or if the cart has not yet
     *         been treated.
     */
    CErpActionReport getActionReport(CCart aCart);

    /**
     * Retrieves the quantity reserved in all known carts for the given item
     * 
     * @param aItemId
     *            An item ID
     * @return The quantity reserved for this item (0 if not available)
     */
    int getItemReservedQuantity(String aItemId);

    /**
     * Retrieves the quantity reserved for each item in all known carts
     * 
     * @return All carts items reserved quantities
     */
    Map<String, Integer> getReservedQuantites();
}
