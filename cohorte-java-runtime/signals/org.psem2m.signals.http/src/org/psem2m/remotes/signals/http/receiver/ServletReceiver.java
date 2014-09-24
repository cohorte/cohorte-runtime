/**
 * File:   ServletReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.psem2m.isolates.base.Utilities;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalRequestReader;
import org.psem2m.signals.InvalidDataException;
import org.psem2m.signals.SignalContent;
import org.psem2m.signals.SignalData;
import org.psem2m.signals.SignalResult;

/**
 * HTTP signal receiver servlet
 * 
 * @author Thomas Calmant
 */
public class ServletReceiver extends HttpServlet {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** Signal reception listener */
    private final ISignalRequestReader pSignalRequestHandler;

    /**
     * Sets up the servlet
     * 
     * @param aHandler
     *            Main signal reception listener
     */
    public ServletReceiver(final ISignalRequestReader aHandler) {

        super();
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

        // Prepare the page
        final StringBuilder pageBuilder = new StringBuilder();
        pageBuilder.append("<html>\n<head>\n");
        pageBuilder.append("<title>HTTP Signal Receiver</title>\n");
        pageBuilder.append("</head>\n<body>\n");
        pageBuilder.append("<h1>HTTP Signal Receiver</h1>\n<ul>\n");
        pageBuilder.append("<li>Main signal handler : <pre>");
        pageBuilder.append(pSignalRequestHandler);
        pageBuilder.append("</pre></li>\n");
        pageBuilder.append("</ul>\n</body>\n</html>\n");

        // Setup headers
        aResp.setStatus(HttpServletResponse.SC_OK);
        aResp.setContentLength(pageBuilder.length());
        aResp.setContentType("text/html");

        // Write the page
        final PrintWriter writer = aResp.getWriter();
        writer.print(pageBuilder.toString());
        writer.flush();
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

        // Get the request mode
        String requestMode = aReq
                .getHeader(IHttpSignalsConstants.HEADER_SIGNAL_MODE);
        if (requestMode == null) {
            // Default mode
            requestMode = ISignalBroadcaster.MODE_SEND;
        }

        // Get the content type (allow null content type)
        final String contentType = aReq
                .getHeader(IHttpSignalsConstants.HEADER_CONTENT_TYPE);

        // Get the content
        final byte[] data = Utilities.inputStreamToBytes(aReq.getInputStream());

        ISignalData signalData;
        try {
            // Try to un-serialize the message
            signalData = pSignalRequestHandler.unserializeSignalContent(
                    contentType, data);

        } catch (final InvalidDataException e) {
            // Exception thrown...
            aResp.sendError(e.getErrorCode(), e.getMessage());
            return;
        }

        if (signalData == null) {
            // No content, prepare an empty signal data
            // WARNING: the sender in the signal data will be invalid
            signalData = new SignalData();
        }

        if (signalData instanceof SignalData) {
            // Set up the sender address now
            ((SignalData) signalData).setSenderAddress(aReq.getRemoteAddr());
        }

        // Notify listeners
        final SignalResult result = pSignalRequestHandler.handleSignal(
                signalName, signalData, requestMode);

        // Return the result
        aResp.setStatus(result.getCode());

        // Convert the result
        final SignalContent responseContent = pSignalRequestHandler
                .serializeSignalResult(contentType, result);

        if (responseContent != null) {
            // Send headers
            aResp.setContentLength(responseContent.getLength());
            aResp.setContentType(responseContent.getType());

            // Write data
            aResp.getOutputStream().write(responseContent.getContent());

        } else {
            // No result
            aResp.setContentLength(0);
        }
    }
}
