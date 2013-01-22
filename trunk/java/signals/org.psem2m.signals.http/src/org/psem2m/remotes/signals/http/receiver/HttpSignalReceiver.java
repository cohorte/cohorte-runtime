/**
 * File:   HttpSignalReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalReceptionProvider;
import org.psem2m.signals.ISignalRequestReader;
import org.psem2m.signals.ISignalSerializer;
import org.psem2m.signals.InvalidDataException;
import org.psem2m.signals.SignalContent;
import org.psem2m.signals.SignalResult;

/**
 * Implementation of a signal receiver. Uses an HTTP servlet to do the job.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-receiver-http-factory")
@Provides(specifications = ISignalReceptionProvider.class)
public class HttpSignalReceiver extends CPojoBase implements
        ISignalReceptionProvider, ISignalRequestReader {

    /** HTTP service port property */
    private static final String HTTP_SERVICE_PORT = "org.osgi.service.http.port";

    /** HTTPService dependency ID */
    private static final String IPOJO_ID_HTTP = "http.service";

    /** HTTP service port */
    private int pHttpPort;

    /** HTTP service, injected by iPOJO */
    @Requires(id = IPOJO_ID_HTTP, filter = "(" + HTTP_SERVICE_PORT + "=*)")
    private HttpService pHttpService;

    /** Log service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** A service flag to indicate that the servlet is ready */
    @ServiceProperty(name = ISignalReceptionProvider.PROPERTY_READY, value = "false", mandatory = true)
    private boolean pPropertyReady;

    /** Main signals receiver */
    @Requires
    private ISignalReceiver pReceiver;

    /** The signal data serializers */
    @Requires
    private ISignalSerializer[] pSerializers;

    /**
     * HTTP service ready
     * 
     * @param aHttpService
     *            The bound service
     * @param aServiceProperties
     *            The HTTP service properties
     */
    @Bind(id = IPOJO_ID_HTTP)
    private void bindHttpService(final HttpService aHttpService,
            final Map<?, ?> aServiceProperties) {

        final Object rawPort = aServiceProperties.get(HTTP_SERVICE_PORT);

        if (rawPort instanceof Number) {
            // Get the integer
            pHttpPort = ((Number) rawPort).intValue();

        } else if (rawPort instanceof CharSequence) {
            // Parse the string
            pHttpPort = Integer.parseInt(rawPort.toString());

        } else {
            // Unknown port type
            pLogger.logWarn(this, "getAccessInfo",
                    "Couldn't read access port=", rawPort);
            pHttpPort = -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalReceptionProvider#getAccessInfo()
     */
    @Override
    public HostAccess getAccessInfo() {

        if (pHttpPort < 0) {
            pLogger.logWarn(this, "getAccessInfo",
                    "HTTP service has no port yet");
        }

        try {
            // Read the host name
            return new HostAccess(InetAddress.getLocalHost().getHostName(),
                    pHttpPort);

        } catch (final UnknownHostException ex) {
            pLogger.logWarn(this, "getAccessInfo",
                    "Could not get local host name:", ex);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalRequestReader#handleSignal(java.lang.String,
     * org.psem2m.signals.ISignalData, java.lang.String)
     */
    @Override
    public SignalResult handleSignal(final String aSignalName,
            final ISignalData aSignalData, final String aMode) {

        return pReceiver.handleReceivedSignal(aSignalName, aSignalData, aMode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // We're not ready anymore
        pPropertyReady = false;

        // Unregister the servlet
        pHttpService.unregister(IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS);

        pLogger.logInfo(this, "invalidatePojo", "HTTP Signal Sender Gone");
    }

    /**
     * Tests if the receiver is ready.
     * 
     * @return True if the receiver is ready
     */
    public boolean isReady() {

        return pPropertyReady;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalRequestReader#serializeSignalResult(java.lang
     * .String, org.psem2m.signals.SignalResult)
     */
    @Override
    public SignalContent serializeSignalResult(
            final String aPreferredContentType, final SignalResult aResult) {

        byte[] rawData = null;
        String contentType = null;

        // Object to serialize
        final Object toSerialize;
        if (aResult != null) {
            toSerialize = aResult.getResult();
        } else {
            toSerialize = null;
        }

        // Try to serialize with the preferred type
        for (final ISignalSerializer serializer : pSerializers) {

            if (serializer.canHandleType(aPreferredContentType)) {
                // Content-Type understood by serializer
                try {
                    rawData = serializer.serializeData(toSerialize);

                } catch (final Exception ex) {
                    // Error during serialization, ignore
                    pLogger.logDebug(this, "serializeSignalResult",
                            "Error serializing data:", ex);
                    continue;
                }

                if (rawData != null) {
                    // Success...
                    contentType = serializer.getContentType();
                    break;
                }
            }
        }

        if (rawData == null) {
            // Couldn't use the preferred content type
            pLogger.logWarn(
                    this,
                    "serializeSignalResult",
                    "Coudln't use a serializer for the preferred content-type=",
                    aPreferredContentType);

            // Try to serialize with the preferred type
            for (final ISignalSerializer serializer : pSerializers) {
                if (!serializer.canHandleType(aPreferredContentType)) {
                    // We already tested the preferred Content-Type, use others
                    try {
                        rawData = serializer.serializeData(toSerialize);

                    } catch (final Exception e) {
                        // Error during serialization, ignore
                        pLogger.logDebug(this, "serializeSignalResult",
                                "Error serializing data:", e);
                        continue;
                    }

                    if (rawData != null) {
                        // Success...
                        contentType = serializer.getContentType();
                        break;
                    }
                }
            }
        }

        if (rawData != null) {
            // Done
            return new SignalContent(contentType, rawData);

        } else {
            pLogger.logWarn(this, "serializeSignalResult",
                    "Coudln't serialize data");
            return null;
        }
    }

    /**
     * HTTP service gone
     */
    @Unbind(id = IPOJO_ID_HTTP)
    private void unbindHttpService() {

        // Forget the port
        pHttpPort = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.remotes.signals.http.receiver.ISignalRequestHandler#
     * handleSignalRequest(java.lang.String, byte[])
     */
    @Override
    public ISignalData unserializeSignalContent(final String aContentType,
            final byte[] aData) throws InvalidDataException {

        // Try to de-serialize
        for (final ISignalSerializer serializer : pSerializers) {

            if (serializer.canHandleType(aContentType)) {
                // Content-Type understood by serializer
                final Object object;
                try {
                    object = serializer.unserializeData(aData);

                } catch (final Exception e) {
                    // Error during de-serialization
                    pLogger.logWarn(this, "unserializeSignalContent",
                            "Error during de-serialization:", e);
                    continue;
                }

                if (object instanceof ISignalData) {
                    // We're good
                    return (ISignalData) object;

                } else {
                    pLogger.logWarn(this, "unserializeSignalContent",
                            "Received something that is not an ISignalData");
                }
            }
        }

        pLogger.logWarn(this, "unserializeSignalContent",
                "Can't de-serialize data, content-type=", aContentType);

        // No de-serializer found
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Prepare and register the servlet
        final ServletReceiver servlet = new ServletReceiver(this);
        try {
            pHttpService.registerServlet(
                    IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS, servlet,
                    null, null);

            // Ready to work
            pPropertyReady = true;

            pLogger.logInfo(this, "validatePojo", "HTTP Signal Receiver Ready");

        } catch (final Exception ex) {
            pLogger.logSevere(this, "validatePojo",
                    "Can't register the HTTP Signal Receiver servlet:", ex);
        }
    }
}
