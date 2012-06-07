package org.jabsorb.ng.client;

import java.net.URI;

/**
 * @author ogattaz
 * 
 */
public class HTTPSessionFactory implements ISessionFactory {

    private static IHTTPSessionProvider sHTTPSessionProvider = null;

    /**
     * 
     */
    public static void removeHTTPSessionProvider() {

        sHTTPSessionProvider = null;
    }

    /**
     * @param aHTTPSessionProvider
     */
    public static void setHTTPSessionProvider(
            final IHTTPSessionProvider aHTTPSessionProvider) {

        sHTTPSessionProvider = aHTTPSessionProvider;
    }

    /**
     * 
     */
    HTTPSessionFactory() {

        super();
    }

    /**
     * @return
     */
    public IHTTPSessionProvider getHTTPSessionProvider() {

        return sHTTPSessionProvider;
    }

    /**
     * @return
     */
    public boolean hasHTTPSessionProvider() {

        return sHTTPSessionProvider != null;
    }

    @Override
    public ISession newSession(final URI uri) throws Exception {

        if (!hasHTTPSessionProvider()) {
            throw new Exception(
                    "No IHTTPSessionProvider available to provide an IHTTPSession");
        }

        return getHTTPSessionProvider().newHTTPSession(uri);
    }

}
