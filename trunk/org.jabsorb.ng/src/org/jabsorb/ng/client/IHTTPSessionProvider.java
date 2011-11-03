package org.jabsorb.ng.client;

import java.net.URI;

/**
 * @author ogattaz
 * 
 */
public interface IHTTPSessionProvider {

    /**
     * @param uri
     * @return
     */
    public IHTTPSession newHTTPSession(URI uri) throws Exception;

}
