/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 6 oct. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.demo.erp.api.beans;

import java.net.HttpURLConnection;

/**
 * 
 * Bean representing the response returned by the ERP after executing an action
 * (eg. a submission of a set of cart lines).
 * 
 * The bean contains a quality level which represents the fact that the ERP
 * could respond directly or not :
 * 
 * => SYNC : the ERP responds
 * 
 * => ACCEPTABLE : the request is stored to be played later.
 * 
 * => CRITICAL : the request is stored to be played later, but the ERP doesn't
 * reply for a long time.
 * 
 * 
 * @author ogattaz
 * 
 */
public class CErpActionReport implements ICachedBean {

    private String pMessage = "Not Implemented.";

    /** The response quality level. */
    private int pQualityLevel;

    private String pReasonInfos = "No reason.";

    /** the same as http status code. **/
    private int pStatus = HttpURLConnection.HTTP_NOT_IMPLEMENTED;

    /**
     * Explicit public constructor
     */
    public CErpActionReport() {

        super();
    }

    /**
     * @param aStatus
     * @param aMessage
     */
    public CErpActionReport(final int aStatus, final String aMessage) {

        this();
        setMessage(aMessage);
        setStatus(aStatus);
    }

    /**
     * @param aStatus
     * @param aMessage
     * @param aReasonInfos
     */
    public CErpActionReport(final int aStatus, final String aMessage,
            final String aReasonInfos) {

        this(aStatus, aMessage);
        setReasonInfos(aReasonInfos);
    }

    /**
     * @return the Message
     */
    public String getMessage() {

        return pMessage;
    }

    /**
     * @return the QualityLevel
     */
    @Override
    public int getQualityLevel() {

        return pQualityLevel;
    }

    /**
     * @return the ReasonInfos
     */
    public String getReasonInfos() {

        return pReasonInfos;
    }

    /**
     * @return the Status
     */
    public int getStatus() {

        return pStatus;
    }

    /**
     * @param aMessage
     *            the pMessage to set
     */
    public void setMessage(final String aMessage) {

        this.pMessage = aMessage;
    }

    /**
     * @param aQualityLevel
     *            the pQualityLevel to set
     */
    @Override
    public void setQualityLevel(final int aQualityLevel) {

        this.pQualityLevel = aQualityLevel;
    }

    /**
     * @param aReasonInfos
     *            the pReasonInfos to set
     */
    public void setReasonInfos(final String aReasonInfos) {

        this.pReasonInfos = aReasonInfos;
    }

    /**
     * @param aStatus
     *            the pStatusCode to set
     */
    public void setStatus(final int aStatus) {

        this.pStatus = aStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("CErpActionReport(");
        builder.append("status=").append(pStatus);
        builder.append(", quality=").append(pQualityLevel);
        builder.append(", message=").append(pMessage);

        if (pReasonInfos != null && !pReasonInfos.isEmpty()) {
            builder.append(", reason=").append(pReasonInfos);
        }
        builder.append(")");

        return builder.toString();
    }
}
