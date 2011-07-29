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

import static org.ow2.chameleon.rose.pub.EndpointDescription.ENDPOINT_CONFIGS;
import static org.ow2.chameleon.rose.pub.EndpointDescription.ENDPOINT_INTENTS;
import static org.ow2.chameleon.rose.pub.EndpointDescription.ENDPOINT_INTERFACES;
import static org.ow2.chameleon.rose.pub.EndpointDescription.ENDPOINT_ID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public final class EndpointDescriptionManager {

    //The EndpointDescription managed by this manager
    private EndpointDescriptionImpl endpointDesc;
    
    //The ServiceRegistration allowing to update 
    //and unregistered the EndpointDescription
    private ServiceRegistration registration;
    
    private EndpointDescriptionManager(Builder builder){
        endpointDesc = new EndpointDescriptionImpl(builder);
    }
    
    /**
     * Update the EndpointDescription properties
     * @param properties
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public void updateProperties(Map<String,Object> properties) throws IllegalArgumentException,IllegalStateException{
        if (registration == null){
            throw new IllegalStateException("The EndpointDescription has not been registered");
        }
        
        //Check the properties
        checkProperties(properties);
        
        endpointDesc.setProperties(properties);
        Hashtable description = new Hashtable(endpointDesc.getProperties());
        registration.setProperties(description);
    }
    
    /**
     * Register the RemoteServiceDescription
     * @param context
     */
    public void register(BundleContext context){
        Hashtable<String, Object> description = new Hashtable<String, Object>(endpointDesc.getProperties());
        description.put(ENDPOINT_ID, endpointDesc.getId());
        description.put(ENDPOINT_CONFIGS, endpointDesc.getConfigs());
        description.put(ENDPOINT_INTENTS, endpointDesc.getIntents());
        description.put(ENDPOINT_INTERFACES, endpointDesc.getInterfaces());
        registration = context.registerService(EndpointDescription.class.getName(), endpointDesc,description);
    }
    
    /**
     * Unregister the RemoteServiceDescription
     */
    public void unRegister() throws IllegalStateException{
        if (registration == null){
            throw new IllegalStateException("The RemoteServiceDescription has not been registered");
        }
        registration.unregister();
    }
    
    //BUILDER
    public static class Builder {
        //required parameters
        private final String[] interfaces;
        private final String[] configs;
        private final String remoteid;
        
        //optional parameters, set to default value
        
        private Map<String,Object> properties = new HashMap<String, Object>();
        public String[] intents = {};
        
        
        public Builder(String[] pInterfaces,String[] pConfigs,String id) {
            this.interfaces = pInterfaces;
            this.configs = pConfigs;
            this.remoteid = id;
        }
        
        public Builder intents(String[] pIntents){
            intents = pIntents;
            return this;
        }
        
        
        public Builder properties(Map<String,Object> pProperties) throws IllegalArgumentException{
            checkProperties(pProperties);
            this.properties.putAll(pProperties);
            return this;
        }
        
        public EndpointDescriptionManager build(){
            return new EndpointDescriptionManager(this);
        }
    }
    
    /*
     * Private implementation of the RemoteServiceDescription managed by this manager.
     * Construct thanks to the builder.
     */
    private class EndpointDescriptionImpl implements EndpointDescription{
        private final String[] interfaces;
        private final String[] configs;
        private final String[] intents;
        private final String id;
        
        
        private Map<String,Object> properties;
        
        public EndpointDescriptionImpl(Builder builder) {
            configs = builder.configs;
            interfaces = builder.interfaces;
            id = builder.remoteid;
            intents = builder.intents;
            setProperties(builder.properties);
        }
        
        public String[] getInterfaces() {
            return interfaces;
        }
        
        public String[] getConfigs() {
            return configs;
        }

        public String getId() {
            return id;
        }
        
        public Map<String, Object> getProperties(){
            return properties;
        }
        
        public Object getProperty(String key){
            return properties.get(key);
        }
        
        private void setProperties(Map<String, Object> pProperties){
            properties = Collections.unmodifiableMap(pProperties);
        }

        public boolean contains(String key) {
            return properties.containsKey(key);
        }

        public String[] getIntents() {
            return intents;
        }

        public Map<String, Object> toMap() {
            Map<String,Object> map = new HashMap<String, Object>(properties);
            map.put(ENDPOINT_CONFIGS, configs);
            map.put(ENDPOINT_INTERFACES, interfaces);
            map.put(ENDPOINT_INTENTS, intents);
            map.put(ENDPOINT_ID, id);
            return map;
        }
    }
    
    /**
     * @param properties
     * @throws IllegalArgumentException if The properties contains invalid properties.
     */
    private static void checkProperties(Map<String, Object> properties) throws IllegalArgumentException {
        if (properties.containsKey(ENDPOINT_ID) || properties.containsKey(ENDPOINT_INTERFACES)
                || properties.containsKey(ENDPOINT_CONFIGS)) {

            throw new IllegalArgumentException("The properties: " + ENDPOINT_CONFIGS + ", " + ENDPOINT_ID + ", " + ENDPOINT_INTERFACES
                    + " are immutable and thus cannot be updated");
        }
    }
}
