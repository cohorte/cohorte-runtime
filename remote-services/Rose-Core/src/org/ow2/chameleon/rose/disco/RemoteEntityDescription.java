package org.ow2.chameleon.rose.disco;

import static org.osgi.framework.Constants.SERVICE_PID;

import java.util.Map;


public interface RemoteEntityDescription {
    String REMOTE_ENTITY_CONFIGS = "remote.enity.configs";
    String REMOTE_ENTITY_INTERFACES = "remote.entity.interfaces";
    String REMOTE_ENTITY_ID = SERVICE_PID;
    
    /**
     * @return The RemoteEntity interfaces name.
     */
    String[] getInterfaces();
    
    /**
     * @return The RemoteEntity configurations. 
     */
    String[] getConfigs();
    
    /**
     * @return The RemoteEntity id.
     */
    String getId();
    
    /**
     * @return The RemoteEntiry properties.
     */
    Map<String, Object> getProperties();
    
    /**
     * @param key
     * @return The value of the property linked to the given key.
     */
    Object getProperty(String key);
}
