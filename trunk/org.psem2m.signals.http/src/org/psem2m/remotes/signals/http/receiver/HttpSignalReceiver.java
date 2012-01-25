/**
 * File:   HttpSignalReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.util.HashSet;
import java.util.Set;

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
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalDataSerializer;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceptionProvider;
import org.psem2m.isolates.services.remote.signals.ISignalRequestReader;
import org.psem2m.isolates.services.remote.signals.InvalidDataException;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;

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

    /** HTTP service, injected by iPOJO */
    @Requires(filter = "(org.osgi.service.http.port=*)")
    private HttpService pHttpService;

    /** Signal listeners */
    private final Set<ISignalListener> pListeners = new HashSet<ISignalListener>();

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** A service flag to indicate that the servlet is ready */
    @ServiceProperty(name = ISignalReceptionProvider.PROPERTY_READY, value = "false", mandatory = true)
    private boolean pPropertyReady;

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
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Get listeners set
        synchronized (pListeners) {
            // Notify listeners
            for (final ISignalListener listener : pListeners) {
                listener.handleReceivedSignal(aSignalName, aSignalData);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.remotes.signals.http.receiver.ISignalRequestHandler#
     * handleSignalRequest(java.lang.String, byte[])
     */
    @Override
    public ISignalData handleSignalRequest(final String aContentType,
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
                    pLogger.log(LogService.LOG_WARNING,
                            "Error during de-serialization", e);
                    continue;
                }

                if (object instanceof ISignalData) {
                    // We're good
                    return (ISignalData) object;

                } else {
                    pLogger.log(LogService.LOG_INFO,
                            "Received something that is not an ISignalData");
                }
            }
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("Can't de-serialize data (content-type: ");
        builder.append(aContentType);
        builder.append(')');
        pLogger.log(LogService.LOG_INFO, builder.toString());

        // No de-serializer found
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
        synchronized (pListeners) {
            pListeners.clear();
        }

        pLogger.log(LogService.LOG_INFO, "HTTP Signal Sender Gone");
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
     * org.psem2m.isolates.services.remote.signals.ISignalReceptionProvider#
     * registerListener
     * (org.psem2m.isolates.services.remote.signals.ISignalListener)
     */
    @Override
    public void registerListener(final ISignalListener aListener) {

        if (aListener == null) {
            // Invalid call
            return;
        }

        synchronized (pListeners) {
            pListeners.add(aListener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalReceptionProvider#
     * unregisterListener
     * (org.psem2m.isolates.services.remote.signals.ISignalListener)
     */
    @Override
    public void unregisterListener(final ISignalListener aListener) {

        if (aListener == null) {
            // Invalid call
            return;
        }

        synchronized (pListeners) {
            pListeners.remove(aListener);
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

            pLogger.log(LogService.LOG_INFO, "HTTP Signal Receiver Ready");

        } catch (final Exception ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Can't register the HTTP Signal Receiver servlet", ex);
        }
    }
}
