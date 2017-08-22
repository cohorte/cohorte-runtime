/**
 * File:   StreamInput.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.core.resolver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;

import org.psem2m.utilities.files.CXFileText;
import org.w3c.dom.ls.LSInput;

/**
 * @author Thomas Calmant
 */
public class StreamInput implements LSInput {

    /** The base ID */
    private String pBaseUri;

    /** The public ID */
    private String pPublicId;

    /** The input stream */
    private BufferedInputStream pStream;

    /** The system ID */
    private String pSystemId;

    /**
     * Sets up the input
     * 
     * @param aPublicId
     *            The public identifier of the external entity being referenced
     * @param aSystemId
     *            The system identifier, a URI reference, of the external
     *            resource being referenced
     * @param aBaseURI
     *            The absolute base URI of the resource being parsed
     * @param aStream
     *            The input stream to read this input
     */
    public StreamInput(final String aPublicId, final String aSystemId,
            final String aBaseURI, final InputStream aStream) {

        pPublicId = aPublicId;
        pSystemId = aSystemId;
        pBaseUri = aBaseURI;
        pStream = new BufferedInputStream(aStream);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getBaseURI()
     */
    @Override
    public String getBaseURI() {

        return pBaseUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getByteStream()
     */
    @Override
    public InputStream getByteStream() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getCertifiedText()
     */
    @Override
    public boolean getCertifiedText() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getCharacterStream()
     */
    @Override
    public Reader getCharacterStream() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getEncoding()
     */
    @Override
    public String getEncoding() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getPublicId()
     */
    @Override
    public String getPublicId() {

        return pPublicId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getStringData()
     */
    @Override
    public String getStringData() {

        int readChar = 0;
        final byte[] bom = new byte[8];
        boolean xmlStartFound = false;

        for (int i = 0; i < bom.length; i++) {
            try {
                readChar = pStream.read();
                if (readChar == -1 || readChar == '<') {
                    // XML start Found
                    xmlStartFound = true;
                    break;
                }

                // Reading BOM...
                bom[i] = (byte) readChar;

            } catch (final IOException e) {
                // Error...
                e.printStackTrace();
                return null;
            }
        }

        if (readChar == -1 || !xmlStartFound) {
            // To long data
            System.err.println("Can't read the prologue of " + pSystemId);
            return null;
        }

        // Compute the file encoding
        final Scanner scanner = new Scanner(pStream,
                CXFileText.readEncoding(bom));
        scanner.useDelimiter("\\Z");

        int streamSize = 0;
        try {
            streamSize = pStream.available() + 1;

        } catch (final IOException e) {
            // Do nothing...
        }

        final StringBuilder xmlBuilder = new StringBuilder(streamSize);
        // Don't forget the eaten "<" character
        xmlBuilder.append('<');
        xmlBuilder.append(scanner.next());
        scanner.close();

        return xmlBuilder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#getSystemId()
     */
    @Override
    public String getSystemId() {

        return pSystemId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setBaseURI(java.lang.String)
     */
    @Override
    public void setBaseURI(final String aBaseURI) {

        pBaseUri = aBaseURI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setByteStream(java.io.InputStream)
     */
    @Override
    public void setByteStream(final InputStream aByteStream) {

        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setCertifiedText(boolean)
     */
    @Override
    public void setCertifiedText(final boolean aCertifiedText) {

        // Do nothing...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setCharacterStream(java.io.Reader)
     */
    @Override
    public void setCharacterStream(final Reader aCharacterStream) {

        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setEncoding(java.lang.String)
     */
    @Override
    public void setEncoding(final String aEncoding) {

        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setPublicId(java.lang.String)
     */
    @Override
    public void setPublicId(final String aPublicId) {

        pPublicId = aPublicId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setStringData(java.lang.String)
     */
    @Override
    public void setStringData(final String aStringData) {

        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSInput#setSystemId(java.lang.String)
     */
    @Override
    public void setSystemId(final String aSystemId) {

        pSystemId = aSystemId;
    }
}
