/**
 * File:   ServletReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.psem2m.isolates.services.remote.signals.ISignalListener;

/**
 * HTTP signal receiver servlet
 * 
 * @author Thomas Calmant
 */
public class ServletReceiver extends HttpServlet {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** Signal reception listener */
    private ISignalListener pSignalListener;

    /** Valid signals caught */
    private int pSignalsCaughtCount;

    /**
     * Sets up the servlet
     * 
     * @param aListener
     *            Main signal reception listener
     */
    public ServletReceiver(final ISignalListener aListener) {

        super();
        pSignalListener = aListener;
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
        writer.println("<li>Main signal listener : <pre>" + pSignalListener
                + "</pre></li>");
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
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Read the inner object
        Object signalData = null;

        try {
            final ObjectInputStream inputStream = new ObjectInputStream(
                    aReq.getInputStream());
            try {
                signalData = inputStream.readObject();

            } catch (ClassNotFoundException e) {
                aResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Class not found : " + e);
                return;
            }

        } catch (EOFException ex) {
            /*
             * Do nothing : ObjectInputStream constructor reached the end of
             * stream before the end of the header, so we consider the signal
             * content as null.
             */
        }

        if (pSignalListener != null) {
            pSignalListener.handleReceivedSignal(signalName, signalData);

            // Valid signal received and handled
            pSignalsCaughtCount++;
        }

        // Return success
        aResp.setStatus(HttpServletResponse.SC_OK);
        aResp.getWriter().println("SUCCESS");
    }
}
