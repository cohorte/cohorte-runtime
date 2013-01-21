/**
 * File:   IsolateInfo.java
 * Author: Thomas Calmant
 * Date:   31 juil. 2012
 */
package org.psem2m.signals.directory.impl;

/**
 * A small information storage
 * 
 * @author Thomas Calmant
 */
public class IsolateInfo {

    /** Isolate name */
    private String pName;

    /** Isolate node */
    private String pNode;

    /** Signals access port */
    private int pPort;

    /** Isolate UID */
    private String pUID;

    /**
     * Sets up the bean
     * 
     * @param aUID
     *            Isolate ID
     * @param aName
     *            Isolate name
     * @param aNode
     *            Isolate node
     * @param aPort
     *            Signals access port
     */
    public IsolateInfo(final String aUID, final String aName,
            final String aNode, final int aPort) {

        pUID = aUID;
        pName = aName;
        pNode = aNode;
        pPort = aPort;
    }

    /**
     * Retrieves the name of the isolate
     * 
     * @return the name of the isolate
     */
    public String getName() {

        return pName;
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
     * Retrieves the Isolate UID
     * 
     * @return the Isolate UID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * Sets the name of the isolate
     * 
     * @param aName
     *            the name of the isolate
     */
    public void setName(final String aName) {

        pName = aName;
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

    /**
     * Sets the Isolate ID
     * 
     * @param aUID
     *            the Isolate ID
     */
    public void setUID(final String aUID) {

        pUID = aUID;
    }

}
