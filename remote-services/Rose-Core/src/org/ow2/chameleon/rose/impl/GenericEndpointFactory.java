package org.ow2.chameleon.rose.impl;

import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.service.log.LogService.LOG_ERROR;
import static org.osgi.service.log.LogService.LOG_WARNING;
import static org.ow2.chameleon.rose.server.EndpointFactory.PROP_ENDPOINT_NAME;
import static org.ow2.chameleon.rose.server.EndpointFactory.PROP_INTERFACE_NAME;
import static org.ow2.chameleon.rose.util.Tools.getEndpointName;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ow2.chameleon.rose.RemoteConstants;
import org.ow2.chameleon.rose.RemoteIdGenerator;
import org.ow2.chameleon.rose.pub.EndpointDescriptionManager;
import org.ow2.chameleon.rose.server.EndpointFactory;
import org.ow2.chameleon.rose.util.Tools;

public class GenericEndpointFactory implements ServiceTrackerCustomizer{

    private static final String[] BLACKLIST = {"service.exported"};
    
    private String filter; //injected
    
    private String[] configs;
    
    private EndpointFactory exporter;
    
    private LogService logger;

    private BundleContext context;
    
    private ServiceTracker tracker;

    private Map<Long, EndpointDescriptionManager> sidToEndpointDescription;
    
    private RemoteIdGenerator idGenerator; //injected

    public GenericEndpointFactory(BundleContext pContext) {
        context = pContext;
        sidToEndpointDescription = new HashMap<Long, EndpointDescriptionManager>();
    }
    
    @SuppressWarnings("unused")
    private void start(){
    	if (tracker == null){ //singleton
			//filterTrackServiceToBeExported(configs);
	        
	        //create the tracker
	        try {
	            tracker = new ServiceTracker(context, context.createFilter(filter), this);
	        } catch (InvalidSyntaxException e) {
	            logger.log(LOG_ERROR, "The given filter: "+filter+" is not a valid filter." ,e);
	            throw new RuntimeException(e);
	        }
		}
    	
    	tracker.open();
    }
    

	@SuppressWarnings("unused")
    private void stop(){
		if (tracker != null) {
			tracker.close();
		}
    }
    
    
    /**
     * TODO catch exception
     * @param factory
     * @param refs
     */
    @SuppressWarnings("unused")
	private void bindEndpointFactory(EndpointFactory factory, ServiceReference refs){
    	exporter = factory;
    	configs = (String[]) refs.getProperty(RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
    }
    
    
    
    /**
     * 
     */
    public Object addingService(ServiceReference sref) {
        final Object service = context.getService(sref);
        final String name = getEndpointName(sref, configs);
        final List<String> itfnames = Tools.getItfNames(sref);
        // set the properties
        Map<String, String> properties = new Hashtable<String, String>();
        
        try {

            // If one and only one interface has been specified, set the
            //  interface name property to it.
            if (itfnames.size() == 1) {
                properties.put(PROP_INTERFACE_NAME, itfnames.get(0));

            }
            properties.put(PROP_ENDPOINT_NAME, name);
            
            exporter.createEndpoint(service, service.getClass().getClassLoader(),properties);
        } catch (Exception e) {
            logger.log(LOG_ERROR, "Cannot create an endpoint for the service: " + String.valueOf(sref.getProperty(SERVICE_ID)), e);
            return null;
        }
        
        try {
            Map<String, Object> optioProp = new HashMap<String, Object>(properties);
            optioProp.putAll(Tools.getServiceProperties(sref,BLACKLIST));
            //XXX add generic properties to the EndpointFactory service
            //optioProp.put(PROP_ENDPOINT_URI, "/jaxws");

            String[] interfaces = itfnames.toArray(new String[itfnames.size()]);
            String id = idGenerator.generateRemoteId(sref, configs[0]);

            EndpointDescriptionManager manager = new EndpointDescriptionManager.Builder(interfaces, configs, id).properties(optioProp).build();

            manager.register(context);

            sidToEndpointDescription.put((Long) sref.getProperty(SERVICE_ID), manager);
        } catch (Exception e) {
            logger.log(LOG_WARNING, "Cannot publish the created endpoint for service: " + String.valueOf(sref.getProperty(SERVICE_ID)), e);
        }
        
        return service;
        
    }

    public void modifiedService(ServiceReference sref, Object service) {
        // unpublished the endpoint
        try {
            final long id = (Long) sref.getProperty(SERVICE_ID);

            if (sidToEndpointDescription.containsKey(id)) {
                sidToEndpointDescription.get(id).updateProperties(Tools.getServiceProperties(sref,BLACKLIST));
            }
        } catch (Exception e) {
            logger.log(LOG_WARNING, "Cannot update the endpoint properties for service: " + String.valueOf(sref.getProperty(SERVICE_ID)), e);
        }
    }

    public void removedService(ServiceReference sref, Object service) {
        // unpublished the endpoint
        try {
            final long id = (Long) sref.getProperty(SERVICE_ID);

            if (sidToEndpointDescription.containsKey(id)) {
                sidToEndpointDescription.remove(id).unRegister();
            }
        } catch (Exception e) {
            logger.log(LOG_WARNING, "Cannot unpublished the endpoint for service: " + String.valueOf(sref.getProperty(SERVICE_ID)), e);
        }

        try {
            final String name = getEndpointName(sref, configs);
            // destroy the endpoint
            exporter.destroyEndpoint(name);
        } catch (Exception e) {
            logger.log(LOG_ERROR, "Cannot destroy the endpoint for service: " + String.valueOf(sref.getProperty(SERVICE_ID)), e);
        }
    }

}
