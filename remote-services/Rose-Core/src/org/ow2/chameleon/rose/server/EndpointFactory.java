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
package org.ow2.chameleon.rose.server;

import java.util.Map;

/**
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public interface EndpointFactory {

    /**
     * Name of the configuration properties related to the endpoint name.
     */
    String PROP_ENDPOINT_NAME = "endpoint.name";

    /**
     * Name of the configuration properties related to the INTERFACE implemented
     * by the given service.
     */
    String PROP_INTERFACE_NAME = "endpoint.interface.name";

    /**
     * @param pService
     * @param pLoader
     * @param properties
     */
    void createEndpoint(Object pService, ClassLoader pLoader,
	    Map<String, String> properties) throws IllegalArgumentException;

    /**
     * Create an endpoint for the given objec with the given properties.
     * 
     * @param pService
     * @param properties
     * @return
     */
    void createEndpoint(Object pService, Map<String, String> properties)
	    throws IllegalArgumentException;

    /**
     * Destroy the endpoint related to the given object.
     * 
     * @param pEndpointName
     *            , the endpoint name
     */
    void destroyEndpoint(String pEndpointName) throws IllegalArgumentException;

    /**
     * @return The configuration properties supported by this ClientFactory
     */
    String[] getConfigs();

    /**
     * Retrieves the base URI to access the end point (/JSON-RPC/, /XML-RPC/,
     * ...)
     * 
     * @param pEndpointName
     *            the endpoint name
     * @return A base URI
     */
    String getEndpointBaseUri(String pEndpointName);

    /**
     * Retrieves the port to access the given end point (80, ...)
     * 
     * @param pEndpointName
     *            the endpoint name
     * 
     * @return An access port, negative if nonsense
     */
    int getEndpointPort(String pEndpointName);

    /**
     * Retrieves the protocol to access the given end point (http, ...)
     * 
     * @param pEndpointName
     *            the endpoint name
     * 
     * @return An access protocol, negative if nonsense
     */
    String getEndpointProtocol(String pEndpointName);
}
