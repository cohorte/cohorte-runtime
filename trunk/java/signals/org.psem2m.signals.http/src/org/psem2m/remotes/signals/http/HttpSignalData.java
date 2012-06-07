/**
 * File:   HttpSignalData.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.remotes.signals.http;

import java.io.Serializable;
import java.util.Arrays;

import org.psem2m.isolates.services.remote.signals.ISignalData;

/**
 * Object sent with the signal.
 * 
 * @author Thomas Calmant
 */
public class HttpSignalData implements Serializable, ISignalData {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The sender host name (default: local host) */
    private String pHostName = "localhost";

    /** ID of the source isolate */
    private String pIsolateSender;

    /** Signal data */
    private Object pSignalData;

    /** Signal time stamp */
    private long pTimestamp;

    /**
     * Default constructor
     */
    public HttpSignalData() {

        // Does nothing
    }

    /**
     * Sets up the signal data
     * 
     * @param aSignalData
     *            Embedded data (can be null)
     */
    public HttpSignalData(final String aSender, final Object aSignalData) {

        pSignalData = aSignalData;
        pIsolateSender = aSender;
        pTimestamp = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.remotes.signals.http.ISignalData#getIsolateSender()
     */
    @Override
    public String getIsolateSender() {

        return pIsolateSender;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalData#getSenderHostName
     * ()
     */
    @Override
    public String getSenderHostName() {

        return pHostName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.remotes.signals.http.ISignalData#getSignalData()
     */
    @Override
    public Object getSignalContent() {

        return pSignalData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalData#getTimestamp()
     */
    @Override
    public long getTimestamp() {

        return pTimestamp;
    }

    /**
     * Sets the signal sender host name
     * 
     * @param aHostName
     *            The signal sender host name
     */
    public void setSenderHostName(final String aHostName) {

        pHostName = aHostName;
    }

    /**
     * Sets the sender
     * 
     * @param aIsolateSender
     *            An isolate ID
     */
    public void setIsolateSender(final String aIsolateSender) {

        pIsolateSender = aIsolateSender;
    }

    /**
     * Sets the signal data
     * 
     * @param aSignalData
     *            the signal data
     */
    public void setSignalContent(final Object aSignalData) {

        pSignalData = aSignalData;
    }

    /**
     * Sets the signal time stamp
     * 
     * @param aTimestamp
     *            the signal time stamp
     */
    public void setTimestamp(final long aTimestamp) {

        pTimestamp = aTimestamp;
    }

    /**
     * return a human readable string
     * 
     * @return an instance of String
     */
    private String signalDataToString() {

        if (pSignalData != null && pSignalData.getClass().isArray()) {
            return Arrays.toString((Object[]) pSignalData);
        } else {
            return String.valueOf(pSignalData);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("HttpSignalData(");
        builder.append("Sender: ").append(pIsolateSender);
        builder.append(", Data: ").append(signalDataToString());
        builder.append(")");

        return builder.toString();
    }
}
