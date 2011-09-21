/**
 * File:   HttpSignalData.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.remotes.signals.http;

import java.io.Serializable;

import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.remote.signals.ISignalData;

/**
 * Object sent with the signal.
 * 
 * @author Thomas Calmant
 */
public class HttpSignalData implements Serializable, ISignalData {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** ID of the source isolate */
    private String pIsolateSender;

    /** Signal data */
    private Serializable pSignalData;

    /**
     * Sets up the signal data
     * 
     * @param aSignalData
     *            Embedded data (can be null)
     */
    public HttpSignalData(final Serializable aSignalData) {

        pSignalData = aSignalData;
        pIsolateSender = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
    }

    /* (non-Javadoc)
     * @see org.psem2m.remotes.signals.http.ISignalData#getIsolateSender()
     */
    @Override
    public String getIsolateSender() {

        return pIsolateSender;
    }

    /* (non-Javadoc)
     * @see org.psem2m.remotes.signals.http.ISignalData#getSignalData()
     */
    @Override
    public Serializable getSignalContent() {

        return pSignalData;
    }
}
