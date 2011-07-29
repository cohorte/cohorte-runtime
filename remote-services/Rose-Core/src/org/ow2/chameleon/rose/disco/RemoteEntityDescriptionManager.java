package org.ow2.chameleon.rose.disco;

import static org.ow2.chameleon.rose.disco.RemoteEntityDescription.REMOTE_ENTITY_CONFIGS;
import static org.ow2.chameleon.rose.disco.RemoteEntityDescription.REMOTE_ENTITY_ID;
import static org.ow2.chameleon.rose.disco.RemoteEntityDescription.REMOTE_ENTITY_INTERFACES;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class RemoteEntityDescriptionManager {
    
    //The RemoteServiceDescription managed by this manager
    private RemoteServiceDescriptionImpl servidedesc;
    
    //The ServiceRegistration allowing to update 
    //and unregistered the RemoteServiceDescription
    private ServiceRegistration registration;
    
    private RemoteEntityDescriptionManager(Builder builder){
        servidedesc = new RemoteServiceDescriptionImpl(builder);
    }
    
    /**
     * Update the RemoteServiceDescription properties
     * @param properties
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public void updateProperties(Map<String,Object> properties) throws IllegalArgumentException,IllegalStateException{
        if (registration == null){
            throw new IllegalStateException("The RemoteServiceDescription has not been registered");
        }
        
        servidedesc.setProperties(properties);
        Hashtable description = new Hashtable(servidedesc.getProperties());
        registration.setProperties(description);
    }
    
    /**
     * Register the RemoteServiceDescription
     * @param context
     */
    public void register(BundleContext context){
        Hashtable<String, Object> description = new Hashtable<String, Object>(servidedesc.getProperties());
        description.put(REMOTE_ENTITY_ID, servidedesc.getId());
        description.put(REMOTE_ENTITY_CONFIGS, servidedesc.getConfigs());
        description.put(REMOTE_ENTITY_INTERFACES, servidedesc.getInterfaces());
        registration = context.registerService(RemoteEntityDescription.class.getName(), servidedesc,description);
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
        
        //optional parameters, set to default value
        private String remoteid = UUID.randomUUID().toString();
        private Map<String,Object> properties = new HashMap<String, Object>();
        
        
        public Builder(String[] pInterfaces,String[] pConfigs ) {
            this.interfaces = pInterfaces;
            this.configs = pConfigs;
        }
        
        public Builder id(String id){
            this.remoteid = id;
            return this;
        }
        
        public Builder properties(Map<String,Object> pProperties) throws IllegalArgumentException{
            //XXX check properties ?
            this.properties.putAll(pProperties);
            return this;
        }
        
        public RemoteEntityDescriptionManager build(){
            return new RemoteEntityDescriptionManager(this);
        }
    }
    
    /*
     * Private implementation of the RemoteServiceDescription managed by this manager.
     * Construct thanks to the builder.
     */
    private class RemoteServiceDescriptionImpl implements RemoteEntityDescription{
        private final String[] interfaces;
        private final String[] configs;
        private final String id;
        
        private Map<String,Object> properties;
        
        public RemoteServiceDescriptionImpl(Builder builder) {
            configs = builder.configs;
            interfaces = builder.interfaces;
            id = builder.remoteid;
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
            //Check the properties
            Object new_id;
            Object new_configs;
            Object new_interfaces;
            
            try {
                new_id = properties.get(REMOTE_ENTITY_ID);
                if (!id.equals(new_id)){
                    throw new IllegalArgumentException("The properties: " + REMOTE_ENTITY_ID + " is immutable");
                }
            } catch(NullPointerException ne){
                new_id = null;
            }
            
            try {
                new_configs = properties.get(REMOTE_ENTITY_CONFIGS);
                if (!configs.equals(new_configs)){
                    throw new IllegalArgumentException("The properties: " + REMOTE_ENTITY_CONFIGS + " is immutable");
                }
            } catch(NullPointerException ne){
                new_configs = null;
            }
            
            try {
                new_interfaces = properties.get(REMOTE_ENTITY_INTERFACES);
                if (!interfaces.equals(new_interfaces)){
                    throw new IllegalArgumentException("The properties: " + REMOTE_ENTITY_INTERFACES + " is immutable");
                }
            } catch(NullPointerException ne){
                new_interfaces = null;
            }
            
            //ok, set the properties
            properties = Collections.unmodifiableMap(pProperties);
        }
    }
}