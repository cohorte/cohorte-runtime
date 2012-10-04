/**
 * File:   BroadcastSignalHandler.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.remote.broadcaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.IRemoteServiceRepository;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent.ServiceEventType;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Broadcast signals listener
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-rsb-signal-handler-factory", publicFactory = false)
@Instantiate(name = "psem2m-remote-rsb-signal-handler")
@Provides(specifications = IIsolatePresenceListener.class)
public class BroadcastSignalHandler extends CPojoBase implements
        ISignalListener, IIsolatePresenceListener {

    /** Signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /** Log service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Remote service events listeners */
    @Requires(optional = true)
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
     * @see org.psem2m.isolates.services.monitoring.IIsolatePresenceListener#
     * handleIsolatePresence(java.lang.String, java.lang.String,
     * org.psem2m.isolates
     * .services.monitoring.IIsolatePresenceListener.EPresence)
     */
    @Override
    public void handleIsolatePresence(final String aIsolateId,
            final String aNode, final EPresence aPresence) {

        if (aPresence == EPresence.UNREGISTERED) {
            // Isolate lost : Notify all listeners
            for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
                try {
                    listener.handleIsolateLost(aIsolateId);

                } catch (final Exception ex) {
                    // Just log...
                    pLogger.logWarn(
                            this,
                            "handleIsolatePresence",
                            "RemoveServiceEventListener failed to handle lost isolate=",
                            aIsolateId, ex);
                }
            }
        }
    }

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

        if (ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT
                .equals(aSignalName)) {
            // Remote event notification

            if (signalContent instanceof RemoteServiceEvent) {
                // Valid signal content, handle it
                handleRemoteEvent((RemoteServiceEvent) signalContent);

            } else if (signalContent instanceof RemoteServiceEvent[]) {
                /*
                 * Multiple remote service events received, handle them one by
                 * one
                 */
                for (final RemoteServiceEvent event : (RemoteServiceEvent[]) signalContent) {
                    // Update its content and notify listeners
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
                    handleRemoteEvent(event);
                }

            } else if (signalContent instanceof Object[]) {
                /*
                 * Multiple remote service events received, handle them one by
                 * one
                 */
                try {
                    final RemoteServiceEvent[] events = Arrays.asList(
                            (Object[]) signalContent).toArray(
                            new RemoteServiceEvent[0]);
                    for (final RemoteServiceEvent event : events) {
                        // Update its content and notify listeners
                        handleRemoteEvent(event);
                    }

                } catch (final ClassCastException ex) {
                    // Invalid type
                    pLogger.logWarn(this, "handleReceivedSignal",
                            "Uncastable array from isolate=",
                            aSignalData.getSenderId(), "content=",
                            Arrays.toString((Object[]) signalContent));
                }

            } else {
                pLogger.logWarn(this, "handleReceivedSignal", "Unknown class=",
                        signalContent.getClass().getName());
            }

        } else if (ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS
                .equals(aSignalName)) {
            // End point request
            handleRequestEndpoints(aSignalData.getSenderId());
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

        // Prepare the event content
        for (final EndpointDescription endpoint : aEvent
                .getServiceRegistration().getEndpoints()) {

            // Resolve the host name corresponding to the node
            final String node = endpoint.getNode();
            final String host = pDirectory.getHostForNode(node);

            // Store it
            endpoint.resolveHost(host);
        }

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
            pLogger.logInfo(this, "handleRequestEndpoints",
                    "RequestEndpoints received from isolate=",
                    aRequestingIsolateId, "but the RSR is empty");
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
        pLogger.logInfo(this, "invalidatePojo",
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

        pLogger.logInfo(this, "validatePojo",
                "PSEM2M Remote Service Broadcaster Handler Ready");
    }
}
