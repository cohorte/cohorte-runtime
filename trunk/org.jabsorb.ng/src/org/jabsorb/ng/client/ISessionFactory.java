package org.jabsorb.ng.client;

import java.net.URI;

/**
 * A factory used to create transport sessions. Register with
 * #registerTransport.
 */
public interface ISessionFactory {
    /**
     * @param uri
     *            URI used to open this session
     */
    ISession newSession(URI uri) throws Exception;
}
