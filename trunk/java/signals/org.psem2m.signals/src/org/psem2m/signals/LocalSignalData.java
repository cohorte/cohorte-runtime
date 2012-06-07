/**
 * File:   LocalSignalData.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;

import org.psem2m.isolates.services.remote.signals.ISignalData;

/**
 * Represents a local signal data.
 * 
 * Always returns localhost as sender host name and the current isolate ID as
 * sender. As it is not intended to be shared between isolates, this object is
 * not serializable.
 * 
 * @author Thomas Calmant
 */
public class LocalSignalData implements ISignalData {

    /** The data associated to the signal */
    private final Object pSignalData;

    /** The signal source isolate */
    private final String pSourceIsolate;

    /** Signal time stamp */
    private final long pTimestamp;

    /**
     * Sets up the local signal data
     * 
     * @param aData
     *            Signal content
     * @param aData2
     */
    public LocalSignalData(final String aIsolateId, final Object aData) {

        pSourceIsolate = aIsolateId;
        pSignalData = aData;
        pTimestamp = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalData#getIsolateSender
     * ()
     */
    @Override
    public String getIsolateSender() {

        return pSourceIsolate;
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

        return "localhost";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalData#getSignalContent
     * ()
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
}
