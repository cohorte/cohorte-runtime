/**
 * File:   ListeningServlet.java
 * Author: Thomas Calmant
 * Date:   25 juil. 2011
 */
package org.psem2m.isolates.remote.importer;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * @author Thomas Calmant
 * 
 */
public class ListeningServlet extends HttpServlet {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The remote service event handler */
    private IRemoteServiceEventListener pRemoteServiceListener;

    /**
     * Stores the bundle context
     * 
     * @param aRemoteServiceListener
     *            The remote service event handler
     */
    public ListeningServlet(
	    final IRemoteServiceEventListener aRemoteServiceListener) {

	super();
	pRemoteServiceListener = aRemoteServiceListener;
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

	// TODO Handle GET as human readable pollings (XML & co)
	aResp.sendError(HttpServletResponse.SC_FORBIDDEN);
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
	    final HttpServletResponse aResp) throws ServletException {

	final String sender = aReq.getRemoteHost();

	try {
	    ObjectInputStream inStream = new ObjectInputStream(
		    aReq.getInputStream());

	    // Read the stream content
	    try {
		Object readObject = inStream.readObject();
		if (readObject instanceof RemoteServiceEvent) {
		    // Notify the handler
		    RemoteServiceEvent event = (RemoteServiceEvent) readObject;
		    if (event.getEndpoints() != null) {

			for (EndpointDescription endpoint : event
				.getEndpoints()) {
			    if (endpoint.getHost() == null) {
				endpoint.setHost(sender);
			    }
			}
		    }

		    pRemoteServiceListener
			    .handleRemoteEvent((RemoteServiceEvent) readObject);
		}

		// Do nothing in other cases...

		// Send a "response"
		aResp.setStatus(HttpServletResponse.SC_OK);

	    } catch (ClassNotFoundException e) {
		// "Internal error" for unknown classes
		aResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		throw new ServletException("Can't deserialize the message", e);

	    } finally {
		// Don't forget to close the stream...
		inStream.close();
	    }

	} catch (IOException ex) {
	    // Internal error on IOException => can't send an answer...
	    throw new ServletException("Error reading the POST content", ex);
	}
    }
}
