/**
 * File:   ServletReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.OsgiObjectInputStream;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalRequestReader;
import org.psem2m.isolates.services.remote.signals.InvalidDataException;
import org.psem2m.remotes.signals.http.HttpSignalData;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;

/**
 * HTTP signal receiver servlet
 * 
 * @author Thomas Calmant
 */
public class ServletReceiver extends HttpServlet {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** Signal reception listener */
    private final ISignalRequestReader pSignalRequestHandler;

    /** Valid signals caught */
    private int pSignalsCaughtCount;

    /**
     * Sets up the servlet
     * 
     * @param aBundleContext
     *            The bundle context
     * @param aHandler
     *            Main signal reception listener
     */
    public ServletReceiver(final BundleContext aBundleContext,
            final ISignalRequestReader aHandler) {

        super();
        pBundleContext = aBundleContext;
        pSignalRequestHandler = aHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest aReq,
            final HttpServletResponse aResp) throws ServletException,
            IOException {

        aResp.setStatus(HttpServletResponse.SC_OK);
        final PrintWriter writer = aResp.getWriter();

        writer.println("<html><head><title>HTTP Signal Receiver</title></head>");
        writer.println("<body><h1>HTTP Signal Receiver</h1><ul>");
        writer.println("<li>Main signal listener : <pre>"
                + pSignalRequestHandler + "</pre></li>");
        writer.println("<li>Signals caugth : " + pSignalsCaughtCount + "</li>");
        writer.println("</ul></body></html>");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(final HttpServletRequest aReq,
            final HttpServletResponse aResp) throws ServletException,
            IOException {

        // Get the signal name
        final String signalName = aReq.getPathInfo();
        if (signalName == null) {
            // Invalid name, send a 404
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No signal name in URI");
            return;
        }

        // Get the content type (allow null content type)
        final String contentType = aReq
                .getHeader(IHttpSignalsConstants.HEADER_CONTENT_TYPE);

        // Get the content
        final byte[] data = inputStreamToBytes(aReq.getInputStream());

        ISignalData signalData;
        try {
            // Try to de-serialize the message
            signalData = pSignalRequestHandler.handleSignalRequest(contentType,
                    data);

        } catch (final InvalidDataException e) {
            // Exception thrown...
            aResp.sendError(e.getErrorCode(), e.getMessage());
            return;
        }

        if (signalData == null) {
            // No content, prepare an empty signal data
            // WARNING: the sender in the signal data will be invalid
            signalData = new HttpSignalData(null, null);
        }

        // Special case : HTTP signal data needs to known the remote host name
        if (signalData instanceof HttpSignalData) {
            // Set the sender if we can
            ((HttpSignalData) signalData).setHostName(aReq.getRemoteHost());
        }

        // Return success
        aResp.setStatus(HttpServletResponse.SC_OK);
        aResp.getWriter().println("SUCCESS");
    }

    /**
     * Converts an input stream into a byte array
     * 
     * @param aInputStream
     *            An input stream
     * @return The input stream content, null on error
     * @throws IOException
     *             Something went wrong
     */
    protected byte[] inputStreamToBytes(final InputStream aInputStream)
            throws IOException {

        if (aInputStream == null) {
            return null;
        }

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];
        int read = 0;

        do {
            read = aInputStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }

        } while (read > 0);

        outStream.close();
        return outStream.toByteArray();
    }

    /**
     * Reads a serialized stream content
     * 
     * @param aRemoteHost
     *            The host that sent the signal
     * @param aInputStream
     *            An input stream
     * @return The read data
     * @throws InvalidDataException
     *             The request body is invalid
     */
    protected ISignalData readSerialized(final String aRemoteHost,
            final InputStream aInputStream) throws InvalidDataException {

        ISignalData signalData;

        try {
            final ObjectInputStream inputStream = new OsgiObjectInputStream(
                    pBundleContext, aInputStream);

            final Object readData = inputStream.readObject();

            if (readData instanceof ISignalData) {
                // Valid object found
                signalData = (ISignalData) readData;

                if (signalData instanceof HttpSignalData) {
                    // Set the sender if we can
                    ((HttpSignalData) signalData).setHostName(aRemoteHost);
                }

            } else {
                // Bad content
                throw new InvalidDataException(
                        "Bad request content. Only ISignalData objects are allowed.",
                        HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (final ClassNotFoundException e) {
            throw new InvalidDataException("Class not found : "
                    + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);

        } catch (final EOFException e) {
            // End Of File Exception : the POST body was empty
            // WARNING: the sender in the signal data will be invalid
            signalData = new HttpSignalData(null, null);
            ((HttpSignalData) signalData).setHostName(aRemoteHost);

        } catch (final IOException e) {
            // Other I/O Exceptions
            throw new InvalidDataException("Error reading the request body",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

        return signalData;
    }
}
