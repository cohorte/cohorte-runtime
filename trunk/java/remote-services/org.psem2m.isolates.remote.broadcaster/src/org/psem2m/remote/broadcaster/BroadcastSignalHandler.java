/**
 * File:   BroadcastSignalHandler.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.remote.broadcaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.IRemoteServiceRepository;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent.ServiceEventType;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Broadcast signals listener
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-rsb-signal-handler-factory", publicFactory = false)
@Instantiate(name = "psem2m-remote-rsb-signal-handler")
public class BroadcastSignalHandler extends CPojoBase implements
        ISignalListener {

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** Remote service events listeners */
    @Requires
    private IRemoteServiceEventListener[] pRemoteEventsListeners;

    /** Remote Service Repository (RSR), injected by iPOJO */
    @Requires
    private IRemoteServiceRepository pRepository;

    /** Signal sender, injected by iPOJO */
    @Requires
    private ISignalBroadcaster pSignalEmitter;

    /** Signal receiver, injected by iPOJO */
    @Requires
    private ISignalReceiver pSignalReceiver;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String, org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        final Object signalContent = aSignalData.getSignalContent();
        final String senderNodeName = aSignalData.getSenderNode();

        if (ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT
                .equals(aSignalName)) {
            // Remote event notification

            if (signalContent instanceof RemoteServiceEvent) {
                // Valid signal content, handle it
                final RemoteServiceEvent event = (RemoteServiceEvent) signalContent;
                event.setSenderHostName(senderNodeName);

                handleRemoteEvent(event);

            } else if (signalContent instanceof RemoteServiceEvent[]) {
                /*
                 * Multiple remote service events received, handle them one by
                 * one
                 */
                for (final RemoteServiceEvent event : (RemoteServiceEvent[]) signalContent) {
                    // Update its content and notify listeners
                    event.setSenderHostName(senderNodeName);
                    handleRemoteEvent(event);
                }

            } else if (signalContent instanceof Collection) {
                /*
                 * Multiple remote service events received, handle them one by
                 * one
                 */
                @SuppressWarnings("unchecked")
                final Collection<RemoteServiceEvent> collection = (Collection<RemoteServiceEvent>) signalContent;

                for (final RemoteServiceEvent event : collection) {
                    // Update its content and notify listeners
                    event.setSenderHostName(senderNodeName);
                    handleRemoteEvent(event);
                }
            }

        } else if (ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS
                .equals(aSignalName)) {
            // End point request
            handleRequestEndpoints(aSignalData.getSenderId());

        } else if (ISignalsConstants.ISOLATE_LOST_SIGNAL.equals(aSignalName)) {
            // Isolate lost : Notify all listeners
            for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
                listener.handleIsolateLost((String) signalContent);
            }
        }

        return null;
    }

    /**
     * Notifies all listeners that a remote event occurred
     * 
     * @param aEvent
     *            A remote service event
     */
    protected void handleRemoteEvent(final RemoteServiceEvent aEvent) {

        // Notify all listeners
        for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
            listener.handleRemoteEvent(aEvent);
        }
    }

    /**
     * Handles end points listing request.
     * 
     * Uses the Signal sender to tell the requesting isolate which local end
     * points are stored in the RSR. Does nothing if the RSR is empty.
     * 
     * @param aRequestingIsolateId
     *            Isolate to answer to.
     */
    protected void handleRequestEndpoints(final String aRequestingIsolateId) {

        final RemoteServiceRegistration[] localRegistrations = pRepository
                .getLocalRegistrations();
        if (localRegistrations == null || localRegistrations.length == 0) {
            // Don't say anything if we have anything to say...
            pLogger.log(LogService.LOG_INFO,
                    "RequestEndpoints received, but the RSR is empty");
            return;
        }

        // Prepare the event list
        final List<RemoteServiceEvent> events = new ArrayList<RemoteServiceEvent>(
                localRegistrations.length);

        // For each exported interface, create an event
        for (final RemoteServiceRegistration registration : localRegistrations) {

            final EndpointDescription[] endpoints = registration.getEndpoints();
            if (endpoints == null || endpoints.length == 0) {
                // Ignore empty services
                continue;
            }

            // Create the corresponding event
            final RemoteServiceEvent remoteEvent = new RemoteServiceEvent(
                    ServiceEventType.REGISTERED, registration);

            events.add(remoteEvent);
        }

        // Use the signal sender to reply to the isolate
        pSignalEmitter
                .fire(ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT,
                        events.toArray(new RemoteServiceEvent[0]),
                        aRequestingIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Unregister the listener
        pSignalReceiver.unregisterListener(ISignalsConstants.MATCH_ALL, this);
        pLogger.log(LogService.LOG_INFO,
                "PSEM2M Remote Service Broadcaster Handler Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Register to all broadcast signals
        pSignalReceiver.registerListener(
                ISignalsConstants.BROADCASTER_SIGNAL_NAME_PREFIX
                        + ISignalsConstants.MATCH_ALL, this);

        // Register to "isolate lost" signal
        pSignalReceiver.registerListener(ISignalsConstants.ISOLATE_LOST_SIGNAL,
                this);

        pLogger.log(LogService.LOG_INFO,
                "PSEM2M Remote Service Broadcaster Handler Ready");
    }
}
