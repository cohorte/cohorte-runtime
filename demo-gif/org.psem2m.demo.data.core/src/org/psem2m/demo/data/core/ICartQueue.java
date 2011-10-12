/**
 * File:   ICartQueue.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.core;

import java.util.Map;

import org.psem2m.demo.data.core.impl.CartQueueItem;
import org.psem2m.demo.erp.api.beans.CCart;

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
     * @return The queue item associated to the cart, null on error
     */
    CartQueueItem enqueueCart(CCart aCart);

    /**
     * Retrieves the quantity reserved for each item in all known carts
     * 
     * @return All carts items reserved quantities
     */
    Map<String, Integer> getReservedQuantites();
}
