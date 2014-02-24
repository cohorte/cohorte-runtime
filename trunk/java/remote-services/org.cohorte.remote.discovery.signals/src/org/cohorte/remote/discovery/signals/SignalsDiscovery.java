/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cohorte.remote.discovery.signals;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.ExportEndpoint;
import org.cohorte.remote.IExportEndpointListener;
import org.cohorte.remote.IExportsDispatcher;
import org.cohorte.remote.IImportsRegistry;
import org.cohorte.remote.ImportEndpoint;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalSendResult;

/**
 * Remote services discovery based on COHORTE Remote Services
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-discovery-signals-factory")
@Provides(specifications = { IExportEndpointListener.class,
        IIsolatePresenceListener.class })
@Instantiate(name = "cohorte-remote-discovery-signals")
public class SignalsDiscovery implements IExportEndpointListener,
        ISignalListener, IIsolatePresenceListener {

    /** Signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /** Exported endpoints dispatcher */
    @Requires
    private IExportsDispatcher pDispatcher;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** Signals receiver */
    @Requires
    private ISignalReceiver pReceiver;

    /** Imported services registry */
    @Requires
    private IImportsRegistry pRegistry;

    /** Signals sender */
    @Requires
    private ISignalBroadcaster pSender;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportEndpointListener#endpointRemoved(org.
     * cohorte.remote.pelix.ExportEndpoint)
     */
    @Override
    public void endpointRemoved(final ExportEndpoint aEndpoint) {

        // Prepare the event bean
        final EndpointEventBean event = new EndpointEventBean(
                EEndpointEventType.UNREGISTERED, aEndpoint);

        // Send it
        sendEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportEndpointListener#endpointsAdded(org.cohorte
     * .remote.pelix.ExportEndpoint[])
     */
    @Override
    public void endpointsAdded(final ExportEndpoint[] aEndpoints) {

        // Prepare the event bean
        final EndpointEventBean event = new EndpointEventBean(
                EEndpointEventType.REGISTERED, aEndpoints);

        // Send it
        sendEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportEndpointListener#endpointUpdated(org.
     * cohorte.remote.pelix.ExportEndpoint, java.util.Map)
     */
    @Override
    public void endpointUpdated(final ExportEndpoint aEndpoint,
            final Map<String, Object> aOldProperties) {

        // Prepare the event bean
        final EndpointEventBean event = new EndpointEventBean(
                EEndpointEventType.MODIFIED, aEndpoint);

        // Send it
        sendEvent(event);
    }

    /**
     * Handles an endpoint event
     * 
     * @param aEvent
     *            An endpoint event
     * @param aSenderAddress
     *            The address of the event sender
     */
    private void handleEvent(final EndpointEventBean aEvent,
            final String aSenderAddress) {

        final EndpointDescriptionBean[] endpoints = aEvent.getEndpoints();

        switch (aEvent.getType()) {
        case REGISTERED:
            // Registration of endpoints
            registerEndpoints(endpoints, aSenderAddress);
            break;

        case MODIFIED: {
            // Single endpoint updated
            final EndpointDescriptionBean endpoint = endpoints[0];
            pRegistry.update(endpoint.getUid(), endpoint.getProperties());
            break;
        }

        case UNREGISTERED: {
            // Single endpoint unregistered
            final EndpointDescriptionBean endpoint = endpoints[0];
            pRegistry.remove(endpoint.getUid());
            break;
        }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalListener#handleReceivedSignal(java.lang.String,
     * org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT
                .equals(aSignalName)) {
            // Get the event
            final EndpointEventBean event = (EndpointEventBean) aSignalData
                    .getSignalContent();
            handleEvent(event, aSignalData.getSenderAddress());

            // Return nothing
            return null;

        } else if (ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS
                .equals(aSignalName)) {
            // Get the events
            final EndpointEventBean event = (EndpointEventBean) aSignalData
                    .getSignalContent();

            // Register remove endpoints
            handleEvent(event, aSignalData.getSenderAddress());

            // Return our endpoints as an event
            return makeExportsEvent();
        }

        // Unhandled signal
        return null;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        pReceiver.unregisterListener(
                ISignalsConstants.PREFIX_BROADCASTER_SIGNAL_NAME
                        + ISignalsConstants.MATCH_ALL, this);

        pLogger.log(LogService.LOG_INFO, "Signals Discovery gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.monitoring.IIsolatePresenceListener#isolateLost
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void isolateLost(final String aUID, final String aName,
            final String aNodeUID) {

        // Notify the imports registry
        pRegistry.lostFramework(aUID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.monitoring.IIsolatePresenceListener#isolateReady
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void isolateReady(final String aIsolateUID, final String aName,
            final String aNodeUID) {

        // Request the endpoints of the given isolate
        requestEndpoints(aIsolateUID);
    }

    /**
     * Returns a signle REGISTERED endpoint event, matching the endpoints
     * exported by the local isolate
     * 
     * @return An endpoint event with all endpoints
     */
    private EndpointEventBean makeExportsEvent() {

        return new EndpointEventBean(EEndpointEventType.REGISTERED,
                pDispatcher.getEndpoints());
    }

    /**
     * Registers the discovered endpoints
     * 
     * @param aEndpoints
     *            Registered endpoints
     * @param aSenderAddress
     *            Address of the remote isolate
     */
    private void registerEndpoints(final EndpointDescriptionBean[] aEndpoints,
            final String aSenderAddress) {

        for (final EndpointDescriptionBean endpoint : aEndpoints) {
            // Compute the import endpoint
            final ImportEndpoint importEndpoint = endpoint.toImportEndpoint();
            importEndpoint.setServer(aSenderAddress);

            // Notify the registry
            pRegistry.add(importEndpoint);
        }
    }

    /**
     * Requests the services exported by the given isolate. If no isolate is
     * given, the request is sent to all known isolates.
     * 
     * @param aIsolateUid
     *            An isolate UID (optional, can be null)
     */
    private void requestEndpoints(final String aIsolateUid) {

        // Prepare our event
        final EndpointEventBean event = makeExportsEvent();

        final ISignalSendResult rawResults;
        if (aIsolateUid == null || aIsolateUid.isEmpty()) {
            // Send the signal to all known isolates
            rawResults = pSender.sendGroup(
                    ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                    event, EBaseGroup.OTHERS);

        } else {
            // Send the signal to the given isolate only
            rawResults = pSender.send(
                    ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                    event, aIsolateUid);
        }

        if (rawResults == null) {
            // No result...
            return;
        }

        // Get successful results
        final Map<String, Object[]> sigResults = rawResults.getResults();
        if (sigResults == null || sigResults.isEmpty()) {
            // No result
            return;
        }

        // Handle all events
        for (final Entry<String, Object[]> entry : sigResults.entrySet()) {
            final String isolateUid = entry.getKey();

            // Only use the first result
            EndpointEventBean isolateEvent = null;
            for (final Object rawIsolateResult : entry.getValue()) {
                if (rawIsolateResult instanceof EndpointEventBean) {
                    isolateEvent = (EndpointEventBean) rawIsolateResult;
                    break;
                }
            }

            if (isolateEvent == null) {
                // No valid result for this isolate
                continue;
            }

            // Found a result, compute the host address of the isolate
            final String isolateNode = pDirectory.getIsolateNode(isolateUid);
            final String nodeAddress = pDirectory.getHostForNode(isolateNode);

            // Handle the event
            handleEvent(isolateEvent, nodeAddress);
        }
    }

    /**
     * Sends an endpoint event to all other isolates
     * 
     * @param aEvent
     *            The endpoint event to send
     */
    private void sendEvent(final EndpointEventBean aEvent) {

        pSender.fireGroup(ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT,
                aEvent, EBaseGroup.OTHERS);
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Register to all broadcast signals
        pReceiver.registerListener(
                ISignalsConstants.PREFIX_BROADCASTER_SIGNAL_NAME
                        + ISignalsConstants.MATCH_ALL, this);

        pLogger.log(LogService.LOG_INFO, "Signals Discovery ready");

        // Request existing endpoints
        requestEndpoints(null);
    }
}
