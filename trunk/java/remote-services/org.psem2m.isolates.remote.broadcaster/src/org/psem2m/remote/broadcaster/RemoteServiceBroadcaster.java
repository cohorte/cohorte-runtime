/**
 * File:   RemoteServiceBroadcaster.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remote.broadcaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalSendResult;

/**
 * Implementation of an RSB
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-rsb-factory", publicFactory = false)
@Provides(specifications = { IRemoteServiceBroadcaster.class,
        IIsolatePresenceListener.class })
@Instantiate(name = "psem2m-remote-rsb")
public class RemoteServiceBroadcaster extends CPojoBase implements
        IRemoteServiceBroadcaster, IIsolatePresenceListener {

    /** Signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /** Log service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Signal sender service, inject by iPOJO */
    @Requires
    private ISignalBroadcaster pSignalEmitter;

    /**
     * Extracts the remote service events from the given results
     * 
     * @param aIsolateSendResults
     *            The result of a signal sent to an isolate
     * @return The found events, or null
     */
    private Collection<RemoteServiceEvent> getRemoteServiceEvents(
            final Object[] aIsolateSendResults) {

        // Isolate results
        if (aIsolateSendResults == null || aIsolateSendResults.length == 0) {
            return null;
        }

        // Find all remote service events in the result
        final List<RemoteServiceEvent> events = new ArrayList<RemoteServiceEvent>();
        for (final Object result : aIsolateSendResults) {

            if (result == null || !result.getClass().isArray()) {
                // Unhandled result
                continue;
            }

            final RemoteServiceEvent[] eventsArray = Utilities
                    .arrayObjectToArray(result, RemoteServiceEvent.class);
            if (eventsArray != null) {
                // Found the events
                events.addAll(Arrays.asList(eventsArray));
            }
        }

        if (events.isEmpty()) {
            return null;
        }

        return events;
    }

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

        if (aPresence == EPresence.REGISTERED) {
            // Component registered: request its end points
            pSignalEmitter.fire(
                    ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                    null, aIsolateId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo",
                "PSEM2M Remote Service Broadcaster Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster#
     * requestAllEndpoints()
     */
    @Override
    public RemoteServiceEvent[] requestAllEndpoints() {

        // Ask for monitors and isolates services too
        final ISignalSendResult sendResult = pSignalEmitter.sendGroup(
                ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS, null,
                EBaseGroup.OTHERS);

        // Signal result
        if (sendResult == null) {
            pLogger.logWarn(this, "requestEndpoints",
                    "No answer from other isolates");
            return null;
        }

        // Aggregated results
        final Collection<RemoteServiceEvent> allEvents = new ArrayList<RemoteServiceEvent>();

        for (final Entry<String, Object[]> entry : sendResult.getResults()
                .entrySet()) {

            // Isolate results
            final String isolate = entry.getKey();
            final Object[] results = entry.getValue();

            final Collection<RemoteServiceEvent> events = getRemoteServiceEvents(results);
            if (events != null) {
                allEvents.addAll(events);

            } else {
                pLogger.logDebug(this, "requestEndpoints",
                        "No end points in isolate=", isolate);
            }
        }

        return allEvents.toArray(new RemoteServiceEvent[allEvents.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster#
     * requestEndpoints(java.lang.String)
     */
    @Override
    public RemoteServiceEvent[] requestEndpoints(final String aIsolateId) {

        final ISignalSendResult sendResult = pSignalEmitter.send(
                ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS, null,
                aIsolateId);

        // Signal result
        if (sendResult == null) {
            pLogger.logWarn(this, "requestEndpoints",
                    "No answer from isolate=", aIsolateId);
            return null;
        }

        // Isolate results
        final Collection<RemoteServiceEvent> events = getRemoteServiceEvents(sendResult
                .getResults().get(aIsolateId));
        if (events == null) {
            pLogger.logDebug(this, "requestEndpoints",
                    "No end points in isolate=", aIsolateId);
            return null;
        }

        return events.toArray(new RemoteServiceEvent[events.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster#
     * sendNotification
     * (org.psem2m.isolates.services.remote.beans.RemoteServiceEvent)
     */
    @Override
    public void sendNotification(final RemoteServiceEvent aEvent) {

        // Set the node name for all end points
        for (final EndpointDescription endpoints : aEvent
                .getServiceRegistration().getEndpoints()) {

            endpoints.setNode(pDirectory.getLocalNode());
        }

        // Send the signal
        pSignalEmitter.fireGroup(
                ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT, aEvent,
                EBaseGroup.OTHERS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo",
                "PSEM2M Remote Service Broadcaster Ready");
    }
}
