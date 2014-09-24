/**
 * File:   SignalData.java
 * Author: Thomas Calmant
 * Date:   12 juin 2012
 */
package org.psem2m.signals;

/**
 * Basic implementation of a signal data bean
 * 
 * @author Thomas Calmant
 */
public class SignalData implements ISignalData {

    /** The source isolate name */
    private String pIsolateName;

    /** The source isolate UID */
    private String pIsolateUID;

    /** The name of the node hosting the source isolate */
    private String pNodeName;

    /** The UID of the node hosting the source isolate */
    private String pNodeUID;

    /** The sender host/address */
    private String pSender;

    /** The data associated to the signal */
    private Object pSignalData;

    /** Signal time stamp */
    private long pTimestamp;

    /**
     * Default constructor (sets up the time stamp to now)
     */
    public SignalData() {

        pTimestamp = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalData#getSenderAddress()
     */
    @Override
    public String getSenderAddress() {

        return pSender;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalData#getSenderName()
     */
    @Override
    public String getSenderName() {

        return pIsolateName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalData#getSenderNodeName()
     */
    @Override
    public String getSenderNodeName() {

        return pNodeName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalData#getSenderNodeUID()
     */
    @Override
    public String getSenderNodeUID() {

        return pNodeUID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalData#getSenderUID()
     */
    @Override
    public String getSenderUID() {

        return pIsolateUID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalData#getSignalContent()
     */
    @Override
    public Object getSignalContent() {

        return pSignalData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalData#getTimestamp()
     */
    @Override
    public long getTimestamp() {

        return pTimestamp;
    }

    /**
     * Sets up the sender address
     * 
     * @param aSender
     *            the sender address
     */
    public void setSenderAddress(final String aSender) {

        pSender = aSender;
    }

    /**
     * Sets up the sending name
     * 
     * @param aNode
     *            An isolate name
     */
    public void setSenderName(final String aName) {

        pIsolateName = aName;
    }

    /**
     * Sets up the sending node name
     * 
     * @param aNodeName
     *            The name of a node
     */
    public void setSenderNodeName(final String aNodeName) {

        pNodeName = aNodeName;
    }

    /**
     * Sets up the sending node UID
     * 
     * @param aNodeUID
     *            The UID of a node
     */
    public void setSenderNodeUID(final String aNodeUID) {

        pNodeUID = aNodeUID;
    }

    /**
     * Sets up the sender ID
     * 
     * @param aIsolateId
     *            An isolate Id
     */
    public void setSenderUID(final String aIsolateId) {

        pIsolateUID = aIsolateId;
    }

    /**
     * Sets up the signal content
     * 
     * @param aSignalData
     *            the signal content
     */
    public void setSignalContent(final Object aSignalData) {

        pSignalData = aSignalData;
    }

    /**
     * Sets up the signal time stamp
     * 
     * @param aTimestamp
     *            the signal time stamp
     */
    public void setTimestamp(final long aTimestamp) {

        pTimestamp = aTimestamp;
    }
}
