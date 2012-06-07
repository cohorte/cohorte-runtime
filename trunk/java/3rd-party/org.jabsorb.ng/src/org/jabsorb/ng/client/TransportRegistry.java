/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha at codebistro dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jabsorb.ng.client;

import java.net.URI;
import java.util.HashMap;

/**
 * A registry of transports serving JSON-RPC-Client
 */
public class TransportRegistry {

    /**
     * HashMap<String, SessionFactory>
     */
    static class RegistryMap {

        HashMap<String, ISessionFactory> rep;

        /**
         * 
         */
        public RegistryMap() {

            rep = new HashMap<String, ISessionFactory>();
        }

        /**
         * @param key
         * @return
         */
        public ISessionFactory get(String key) {

            return rep.get(key);
        }

        /**
         * @param key
         * @param value
         * @return
         */
        public ISessionFactory put(String key, ISessionFactory value) {

            return rep.put(key, value);
        }
    }

    private static TransportRegistry singleton = new TransportRegistry();

    /**
     * Use this function when there is no IOC container to rely on creating the
     * factory.
     * 
     * @return singleton instance of the class, created if necessary.
     */
    public synchronized static TransportRegistry i() {

        // C'an't be null !!
        // if (singleton == null) {
        // singleton = new TransportRegistry();
        // }
        return singleton;
    }

    private final RegistryMap registry = new RegistryMap();

    /**
     * Private to be sure
     */
    private TransportRegistry() {

        super();

        //
        HTTPSession.register(this);
    }

    /**
     * Create a session from 'uriString' using one of registered transports.
     */
    public ISession createSession(String uriString) {

        try {
            URI uri = new URI(uriString);
            ISessionFactory found = registry.get(uri.getScheme());
            if (found != null) {
                return found.newSession(uri);
            } else {
                // Fallback
                return new URLConnectionSession(uri.toURL());
            }
        } catch (Exception e) {
            throw new ClientError(e);
        }
    }

    public void registerTransport(String scheme, ISessionFactory factory) {

        registry.put(scheme, factory);
    }

}
