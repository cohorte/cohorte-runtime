/**
 * File:   HttpSignalReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDataSerializer;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalReceptionProvider;
import org.psem2m.signals.ISignalRequestReader;
import org.psem2m.signals.InvalidDataException;
import org.psem2m.signals.SignalContent;
import org.psem2m.signals.SignalResult;

/**
 * Implementation of a signal receiver. Uses an HTTP servlet to do the job.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-signal-receiver-http-factory", publicFactory = false)
@Provides(specifications = ISignalReceptionProvider.class)
@Instantiate(name = "psem2m-remote-signal-http-receiver")
public class HttpSignalReceiver extends CPojoBase implements
        ISignalReceptionProvider, ISignalRequestReader {

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** Configuration service */
    @Requires
    private ISvcConfig pConfig;

    /** HTTP service, injected by iPOJO */
    @Requires(filter = "(org.osgi.service.http.port=*)")
    private HttpService pHttpService;

    /** Log service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** A service flag to indicate that the servlet is ready */
    @ServiceProperty(name = ISignalReceptionProvider.PROPERTY_READY, value = "false", mandatory = true)
    private boolean pPropertyReady;

    /** Main signals receiver */
    private ISignalReceiver pReceiver;

    /** The signal data serializers */
    @Requires
    private ISignalDataSerializer[] pSerializers;

    /**
     * Sets up the signal receiver
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public HttpSignalReceiver(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalReceptionProvider#getAccessInfo()
     */
    @Override
    public HostAccess getAccessInfo() {

        try {
            return new HostAccess(InetAddress.getLocalHost().getHostName(),
                    pConfig.getCurrentIsolate().getPort());

        } catch (final UnknownHostException ex) {
            pLogger.logWarn(this, "", "Could not get local host name, ex=", ex);
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

        synchronized (pReceiver) {
            if (pReceiver != null) {
                return pReceiver.handleReceivedSignal(aSignalName, aSignalData,
                        aMode);
            }
        }

        return null;
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

        // Clear listeners
        pReceiver = null;

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

        // Try to serialize with the preferred type
        for (final ISignalDataSerializer serializer : pSerializers) {

            if (serializer.canHandleType(aPreferredContentType)) {
                // Content-Type understood by serializer
                try {
                    rawData = serializer.serializeData(aResult.getResult());

                } catch (final Exception e) {
                    // Error during serialization, ignore
                    pLogger.logDebug(this, "serializeSignalResult",
                            "Error serializing data, ex=", e);
                    continue;
                }

                if (rawData != null) {
                    // Sucess...
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
            for (final ISignalDataSerializer serializer : pSerializers) {
                if (!serializer.canHandleType(aPreferredContentType)) {
                    // We already tested the preferred Content-Type, use others
                    try {
                        rawData = serializer.serializeData(aResult.getResult());

                    } catch (final Exception e) {
                        // Error during serialization, ignore
                        pLogger.logDebug(this, "serializeSignalResult",
                                "Error serializing data, ex=", e);
                        continue;
                    }

                    if (rawData != null) {
                        // Sucess...
                        contentType = serializer.getContentType();
                        break;
                    }
                }
            }
        }

        if (rawData != null) {
            return new SignalContent(contentType, rawData);

        } else {
            pLogger.logWarn(this, "serializeSignalResult",
                    "Coudln't serialize data");
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalReceptionProvider#setReceiver(org.psem2m.signals
     * .ISignalReceiver)
     */
    @Override
    public boolean setReceiver(final ISignalReceiver aReceiver) {

        synchronized (pReceiver) {

            if (pReceiver == null && aReceiver != null) {
                pReceiver = aReceiver;
                return true;
            }
        }

        return false;
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
        for (final ISignalDataSerializer serializer : pSerializers) {

            if (serializer.canHandleType(aContentType)) {
                // Content-Type understood by serializer
                final Object object;
                try {
                    object = serializer.unserializeData(aData);

                } catch (final Exception e) {
                    // Error during de-serialization
                    pLogger.logWarn(this, "unserializeSignalContent",
                            "Error during de-serialization : ex=", e);
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
     * @see
     * org.psem2m.signals.ISignalReceptionProvider#unsetReceiver(org.psem2m.
     * signals.ISignalReceiver)
     */
    @Override
    public void unsetReceiver(final ISignalReceiver aReceiver) {

        synchronized (pReceiver) {
            if (pReceiver != null && pReceiver.equals(aReceiver)) {
                pReceiver = null;
            }
        }
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
        final ServletReceiver servlet = new ServletReceiver(pBundleContext,
                this);
        try {
            pHttpService.registerServlet(
                    IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS, servlet,
                    null, null);

            // Ready to work
            pPropertyReady = true;

            pLogger.logInfo(this, "validatePojo", "HTTP Signal Receiver Ready");

        } catch (final Exception ex) {
            pLogger.logSevere(this, "validatePojo",
                    "Can't register the HTTP Signal Receiver servlet ex=", ex);
        }
    }
}
