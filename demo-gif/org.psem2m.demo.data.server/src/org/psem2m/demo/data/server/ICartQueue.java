/**
 * File:   ICartQueue.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.server;

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
}
