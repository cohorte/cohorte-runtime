/**
 * File:   ServletReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;

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
    private final ISignalListener pSignalListener;

    /** Valid signals caught */
    private int pSignalsCaughtCount;

    /**
     * Sets up the servlet
     * 
     * @param aBundleContext
     *            The bundle context
     * @param aListener
     *            Main signal reception listener
     */
    public ServletReceiver(final BundleContext aBundleContext,
            final ISignalListener aListener) {

        super();
        pBundleContext = aBundleContext;
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
        final ISignalData signalData;

        final ObjectInputStream inputStream = new OsgiObjectInputStream(
                pBundleContext, aReq.getInputStream());
        try {
            final Object readData = inputStream.readObject();

            if (readData instanceof ISignalData) {
                // Valid object found
                signalData = (ISignalData) readData;

            } else {
                // Bad content
                aResp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Bad request content. Only ISignalData objects are allowed.");
                return;
            }

        } catch (ClassNotFoundException e) {
            aResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Class not found : " + e);
            return;
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
