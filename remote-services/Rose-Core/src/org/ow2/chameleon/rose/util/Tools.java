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

package org.ow2.chameleon.rose.util;

import static org.osgi.framework.Constants.SERVICE_ID;
import static org.ow2.chameleon.rose.RemoteConstants.CONFIGS_NODEFAULT;
import static org.ow2.chameleon.rose.RemoteConstants.OBJECTCLASS;
import static org.ow2.chameleon.rose.RemoteConstants.SERVICE_EXPORTED_CONFIGS;
import static org.ow2.chameleon.rose.RemoteConstants.SERVICE_EXPORTED_INTENTS;
import static org.ow2.chameleon.rose.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA;
import static org.ow2.chameleon.rose.RemoteConstants.SERVICE_EXPORTED_INTERFACES;
import static org.ow2.chameleon.rose.RemoteConstants.SERVICE_INTENTS;
import static org.ow2.chameleon.rose.disco.RemoteEntityDescription.REMOTE_ENTITY_CONFIGS;
import static org.ow2.chameleon.rose.server.EndpointFactory.PROP_ENDPOINT_NAME;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.rose.disco.RemoteEntityDescription;
import org.ow2.chameleon.rose.server.EndpointFactory;

/**
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public final class Tools {

    /**
     * Returned the intents needed by the Service in order to export it.
     * @param sref
     * @return the required intents
     */
    public static Collection<String> getRequiredIntents(final ServiceReference sref) throws ClassCastException {
        Collection<String> requiredintents = new HashSet<String>();

        Object exporObj = sref.getProperty(SERVICE_EXPORTED_INTENTS);
        Object extraObj = sref.getProperty(SERVICE_EXPORTED_INTENTS_EXTRA);

        Collection<String> exportedIntents = null;
        Collection<String> extraIntents = null;

        if (exporObj != null) {
            if (Collection.class.isInstance(exporObj)) {
                exportedIntents = (Collection<String>) exporObj;
            } else {
                exportedIntents = Arrays.asList((String[]) exporObj);
            }

            requiredintents.addAll(exportedIntents);
        }

        if (extraObj != null) {
            if (Collection.class.isInstance(extraObj)) {
                extraIntents = (Collection<String>) extraObj;
            } else {
                extraIntents = Arrays.asList((String[]) extraObj);
            }

            requiredintents.addAll(extraIntents);
        }

        return requiredintents;
    }

    /**
     * Returned the intents provided by the Service and the service Exporter.
     * @param sref
     * @return the required intents
     */
    public static Collection<String> getProvidedIntents(final ServiceReference sref, final Collection<String> exporterIntents) {
        Collection<String> providedintents = new HashSet<String>();

        String[] serviceIntents = (String[]) sref.getProperty(SERVICE_INTENTS);

        if (serviceIntents != null) {
            providedintents.addAll(Arrays.asList(serviceIntents));
        }

        providedintents.addAll(exporterIntents);

        return providedintents;
    }

    /**
     * Return the service properties from its ServiceReference.
     * @param sref the ServiceReference
     * @param blacklistPrefix ignore the property with its key starting by one of those prefix.
     * @return the service properties
     */
    public static Map<String, Object> getServiceProperties(final ServiceReference sref,final String[] blacklistPrefix) {
        Map<String, Object> properties = new HashMap<String, Object>();
        
        boolean ignore;
        for (String key : sref.getPropertyKeys()) {
            ignore = false;
            //ignore remote service related properties
            for (String prefix : blacklistPrefix) {
                if(key.startsWith(prefix)){
                    ignore = true;
                    break;
                }
            }
            if (!ignore){
                properties.put(key, sref.getProperty(key));
            }
        }

        return properties;
    }
    
    /**
     * Get the interface names from the Service Reference. 
     * @param sref the ServiceReference
     * @return The List of interface names
     * @throws IllegalArgumentException
     */
    public static final List<String> getItfNames(final ServiceReference sref) throws IllegalArgumentException {
        final List<String> itfnames;
        Object itfs = sref.getProperty(SERVICE_EXPORTED_INTERFACES);
        
        if (itfs != null && !itfs.equals("*")) {
            try {
                itfnames = Arrays.asList((String[]) itfs);
            } catch (ClassCastException ce) {
                throw new IllegalArgumentException("The property: " + SERVICE_EXPORTED_INTERFACES + " must be an Array of String or *.", ce);
            }
            
        } else {
            itfnames = Arrays.asList((String[]) sref.getProperty(OBJECTCLASS));
        }

        return itfnames;
    }
    
    public static final String filterTrackServiceToBeExported(String[] configs){
        
        StringBuilder orClauses = new StringBuilder();
        //compute config filter
        for (String config : configs) {
            orClauses.append("("+SERVICE_EXPORTED_CONFIGS+"="+config+")");
        }
        
        return
            "(&" +
                "("+SERVICE_EXPORTED_INTERFACES+"=*)" +
                "(|"+
                    "(&"+
                        "("+SERVICE_EXPORTED_CONFIGS+"="+CONFIGS_NODEFAULT+")"+
                        "(|"+
                            orClauses.toString()+
                         ")"+
                     ")"+
                     "(&"+
                         "(!("+SERVICE_EXPORTED_CONFIGS+"="+CONFIGS_NODEFAULT+"))"+
                         "("+SERVICE_EXPORTED_CONFIGS+"=*)"+
                     ")" +
                     "(!("+SERVICE_EXPORTED_CONFIGS+"=*))"+
                ")"+
            ")";
    }
    
    public static final String filterTrackRemoteToBeImported(String[] configs,String[] mandatory){
        
        StringBuilder andClauses = new StringBuilder();
        
        //compute config filter
        andClauses.append("(|");
        for (String config : configs) {
            andClauses.append("("+REMOTE_ENTITY_CONFIGS+"="+config+")");
        }
        andClauses.append(")");
        
        //computer mandatory properties filter
        for (String mandat : mandatory) {
            andClauses.append("("+mandat+"=*)");
        }
        
        return
            "(&" +
                "("+Constants.OBJECTCLASS+"="+RemoteEntityDescription.class.getName()+")" +
                 andClauses +
            ")";
    }
    
    /**
     * Get the endpoint name from the ServiceReference and the endpoint name
     * property list. If the name property if not set, use the instance.name or
     * the service pid or the service id + service as prefix.
     * @param sref
     * @param name_properties
     * @return the endpoint name (String)
     */
    public static final String getEndpointName(final ServiceReference sref, final String[] configs) {
        Object name = sref.getProperty(PROP_ENDPOINT_NAME);

        //get the endpoint name from the given name properties
        int i = 0;
        while (name == null && i < configs.length) {
            name = sref.getProperty(configs[i++]+PROP_ENDPOINT_NAME);
        }
        
        //try with instance.name
        if (name == null) { 
            name = sref.getProperty("instance.name");
        } 
        
        //try with service.pid
        if(name == null) {
            name = sref.getProperty(Constants.SERVICE_PID);
        }
        
        //try with service.id
        if (name == null) { 
            name = "service" + String.valueOf(sref.getProperty(SERVICE_ID));
        }
        
        return String.valueOf(name);
    }
    

    /**
     * Return true if the provided intents contains all the required intents.
     * Which means that the intents are fully resolved.
     * @param providedintents
     * @param requiredintents
     * @return
     */
    public static boolean intentsResolved(final Collection<String> providedintents, final Collection<String> requiredintents) {
        return providedintents.containsAll(requiredintents);
    }
}
