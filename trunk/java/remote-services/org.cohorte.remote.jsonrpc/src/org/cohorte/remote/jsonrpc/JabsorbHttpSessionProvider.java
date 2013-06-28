/**
 * File:   JabsorbHttpSessionProvider.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.cohorte.remote.jsonrpc;

import java.net.MalformedURLException;
import java.net.URI;

import org.jabsorb.ng.client.IHTTPSession;
import org.jabsorb.ng.client.IHTTPSessionProvider;

/**
 * Implementation of an HTTPSession provider for Jabsorb
 * 
 * @author Thomas Calmant
 */
public class JabsorbHttpSessionProvider implements IHTTPSessionProvider {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jabsorb.ng.client.IHTTPSessionProvider#newHTTPSession(java.net.URI)
     */
    @Override
    public IHTTPSession newHTTPSession(final URI aUri)
            throws MalformedURLException {

        return new JabsorbHttpSession(aUri);
    }
}
