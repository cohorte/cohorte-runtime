/**
 * File:   LocalSignalData.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;

import java.io.Serializable;

import org.psem2m.isolates.constants.IPlatformProperties;
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
    private Serializable pSignalData;

    /**
     * Sets up the local signal data
     * 
     * @param aData
     *            Signal content
     */
    public LocalSignalData(final Serializable aData) {

        pSignalData = aData;
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

        return System.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
                "<unknown>");
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
    public Serializable getSignalContent() {

        return pSignalData;
    }
}
