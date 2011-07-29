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
package org.ow2.chameleon.rose.pub;

import java.util.Map;

/**
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public interface EndpointDescription {
    
    String ENDPOINT_ID = "endpoint.id";
        
    String LOCAL_SERVICE_ID = "endpoint.osgi.service.id";
    
    String LOCAL_SERVICE_PID = "endpoint.osgi.service.pid";
    
    
    /*--------------------------------*
     *  R4.2 RemoteService properties *
     *--------------------------------*/

    /**
     * @optional
     * @type String[]
     * @see getIntents()
     */
    String ENDPOINT_INTENTS = "endpoint.intents";
    
    /**
     * @type String[] , at least one elem
     * @see getConfigs()
     */
    String ENDPOINT_CONFIGS = "endpoint.configs";
    
    /**
     * @type String[]
     * @see getInterfaceNames()
     */
    String ENDPOINT_INTERFACES = "endpoint.interfaces";
    
    /**
     * The list cannot be null or empty.
     * @return A list of configuration types that should be used to import the service. Usually, it refers to the protocol name (e.g jsonrpc, org.jabsorb.jsonrpc, org.apache.xmlrpc)
     */
    String[] getConfigs();
    
    /**
     * An intent is a name for an abstract distribution capability.
     * @return A list of intents that this service implements.
     */
    String[] getIntents();
    
    /**
     * This property defines under which interface this Endpoint can be accessed.
     * @return A subset of the type listed in the objectClass service property. 
     */
    String[] getInterfaces();
    
    /**
     * @return The Endpoint unique Id.
     */
    String getId();
    
    /**
     * @return The Endpoint properties.
     */
    Map<String, Object> getProperties();
    
    /**
     * @param key, the property name
     * @return The value of the property of given name if it has been set.
     */
    Object getProperty(String key);
    
    /**
     * @param key, the property name
     * @return true if this EndpointDescription contains this property
     */
    boolean contains(String key);
    
    /**
     * @return a Map view of this EndpointDescription. (defensive copy)
     */
    Map<String,Object> toMap();
}
