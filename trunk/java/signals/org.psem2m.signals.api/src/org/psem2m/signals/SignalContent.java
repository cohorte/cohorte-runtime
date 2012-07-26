/**
 * File:   SignalContent.java
 * Author: "Thomas Calmant"
 * Date:   12 juin 2012
 */
package org.psem2m.signals;

/**
 * Represents the content of a request
 * 
 * @author Thomas Calmant
 */
public class SignalContent {

    /** The RAW content */
    private final byte[] pContent;

    /** The content type */
    private final String pContentType;

    /**
     * Sets up the request content
     * 
     * @param aType
     *            MIME Content type
     * @param aContent
     *            RAW content
     */
    public SignalContent(final String aType, final byte[] aContent) {

        pContent = aContent;
        pContentType = aType;
    }

    /**
     * Retrieves the RAW content
     * 
     * @return the content
     */
    public byte[] getContent() {

        return pContent;
    }

    /**
     * Retrieves the content length
     * 
     * @return the content length
     */
    public int getLength() {

        if (pContent == null) {
            // No content
            return 0;
        }

        return pContent.length;
    }

    /**
     * Retrieves the content type
     * 
     * @return the content type
     */
    public String getType() {

        return pContentType;
    }
}
