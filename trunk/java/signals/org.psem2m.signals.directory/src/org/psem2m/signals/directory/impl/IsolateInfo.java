/**
 * File:   IsolateInfo.java
 * Author: Thomas Calmant
 * Date:   31 juil. 2012
 */
package org.psem2m.signals.directory.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * A small information storage
 * 
 * @author Thomas Calmant
 */
public class IsolateInfo {

    /** Isolate groups */
    private final List<String> pGroups = new ArrayList<String>();

    /** Isolate ID */
    private String pId;

    /** Isolate node */
    private String pNode;

    /** Signals access port */
    private int pPort;

    /**
     * Sets up the bean
     * 
     * @param aId
     *            Isolate ID
     * @param aNode
     *            Isolate node
     * @param aPort
     *            Signals access port
     */
    public IsolateInfo(final String aId, final String aNode, final int aPort) {

        pId = aId;
        pNode = aNode;
        pPort = aPort;
    }

    /**
     * Retrieves a direct access to the isolate groups list
     * 
     * @return the isolate groups list
     */
    public List<String> getGroups() {

        return pGroups;
    }

    /**
     * Retrieves the Isolate ID
     * 
     * @return the Isolate ID
     */
    public String getId() {

        return pId;
    }

    /**
     * Retrieves the Isolate node
     * 
     * @return the Isolate node
     */
    public String getNode() {

        return pNode;
    }

    /**
     * Retrieves the signals access port
     * 
     * @return the signals access port
     */
    public int getPort() {

        return pPort;
    }

    /**
     * Sets the Isolate ID
     * 
     * @param aId
     *            the Isolate ID
     */
    public void setId(final String aId) {

        pId = aId;
    }

    /**
     * Sets the Isolate node
     * 
     * @param aNode
     *            the Isolate node
     */
    public void setNode(final String aNode) {

        pNode = aNode;
    }

    /**
     * Sets the signals access port
     * 
     * @param aPort
     *            the signals access port
     */
    public void setPort(final int aPort) {

        pPort = aPort;
    }

}
