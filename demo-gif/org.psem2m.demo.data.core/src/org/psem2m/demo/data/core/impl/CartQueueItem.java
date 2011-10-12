/**
 * File:   CartQueueItem.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.core.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Semaphore;

import org.psem2m.demo.erp.api.beans.CCart;
import org.psem2m.demo.erp.api.beans.CErpActionReport;

/**
 * Represents the item used in the cart agent queue
 * 
 * @author Thomas Calmant
 */
public class CartQueueItem implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The cart */
    private CCart pCart;

    /** The action report */
    private CErpActionReport pReport;

    /** The result semaphore */
    private Semaphore pSemaphore;

    /**
     * Sets up the cart queue item
     * 
     * @param aCart
     *            A cart
     */
    public CartQueueItem(final CCart aCart) {

        pCart = aCart;
        pSemaphore = new Semaphore(0);
    }

    /**
     * Retrieves the cart
     * 
     * @return the cart
     */
    public CCart getCart() {

        return pCart;
    }

    /**
     * Retrieves the ERP action report
     * 
     * @return the ERP action report
     */
    public synchronized CErpActionReport getReport() {

        return pReport;
    }

    /**
     * Retrieves the semaphore that will be released when the ERP will respond
     * 
     * @return the semaphore
     */
    public Semaphore getSemaphore() {

        return pSemaphore;
    }

    /**
     * Custom de-serialization
     * 
     * @param aObjectInputStream
     *            An object input stream
     * @throws IOException
     *             An error occurred while reading the object
     * @throws ClassNotFoundException
     *             A required class wasn't found
     */
    private void readObject(final ObjectInputStream aObjectInputStream)
            throws IOException, ClassNotFoundException {

        // The cart
        pCart = (CCart) aObjectInputStream.readObject();

        // The ERP action report
        pReport = (CErpActionReport) aObjectInputStream.readObject();
    }

    /**
     * Sets the ERP action report
     * 
     * @param aReport
     *            the ERP action report
     */
    public synchronized void setReport(final CErpActionReport aReport) {

        pReport = aReport;
    }

    /**
     * Custom serialization
     * 
     * @param aObjectOutputStream
     *            The object output stream
     * @throws IOException
     *             An error occurred while writing objects
     */
    private void writeObject(final ObjectOutputStream aObjectOutputStream)
            throws IOException {

        // The cart
        aObjectOutputStream.writeObject(pCart);

        // The ERP action report
        aObjectOutputStream.writeObject(pReport);
    }
}
