/**
 * File:   BrokerException.java
 * Author: Thomas Calmant
 * Date:   8 août 2012
 */
package org.psem2m.isolates.slave.agent.core;

import java.text.MessageFormat;

/**
 * Configuration Broker client exception
 * 
 * @author Thomas Calmant
 */
public class BrokerException extends Exception {

    /**
     * Serialization version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The response code associated to the exception
     */
    private final int pResponseCode;

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            Message associated to the exception
     */
    public BrokerException(final String aMessage) {

        this(aMessage, -1);
    }

    /**
     * Sets up the exception
     * 
     * @param aMessage
     *            Message associated to the exception
     * @param aReponseCode
     *            Response code associated to the exception
     */
    public BrokerException(final String aMessage, final int aReponseCode) {

        super(MessageFormat.format(aMessage, aReponseCode));
        pResponseCode = aReponseCode;
    }

    /**
     * Retrieves the response code associated to the exception, or -1
     * 
     * @return the response code associated to the exception
     */
    public int getResponseCode() {

        return pResponseCode;
    }
}
