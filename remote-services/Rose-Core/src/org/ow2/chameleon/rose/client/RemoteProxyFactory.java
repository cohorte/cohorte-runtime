/*
 * Copyright 2009 OW2 Chameleon
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
 */
package org.ow2.chameleon.rose.client;

import java.util.Map;

/**
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public interface RemoteProxyFactory extends RemoteClientFactory {

    /**
     * Return a Proxy of the endpoint of given properties available through this
     * factory. The Class is loaded from its own classloader,
     * pClass.getClassLoader().
     * @param pEndpointProp, the endpoint properties
     * @param pClass
     * @return The proxy
     * @throws IllegalArgumentException
     */
    Object getRemoteProxy(Map<String, String> pEndpointProp, Class<?> pClass) throws IllegalArgumentException;

    /**
     * Return a Proxy of the endpoint of given properties available through this
     * factory.
     * @param pEndpointProp, the endpoint properties
     * @param pLoader
     * @param pClass
     * @return the proxy
     * @throws IllegalArgumentException
     */
    Object getRemoteProxy(Map<String, String> pEndpointProp, ClassLoader pLoader, Class<?> pClass) throws IllegalArgumentException;

    /**
     * Destroy the proxy (optional).
     * @param proxy
     * @throws IllegalArgumentException
     */
    void destroyRemoteProxy(Object proxy) throws IllegalArgumentException;

}
