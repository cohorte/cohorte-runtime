/**
 * File:   HttpSignalReceiver.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http.receiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;

/**
 * Implementation of a signal receiver. Uses an HTTP servlet to do the job.
 * 
 * @author Thomas Calmant
 */
@Component(name = "remote-signal-receiver-http-factory", publicFactory = false)
@Provides(specifications = ISignalReceiver.class)
@Instantiate(name = "remote-signal-http-receiver")
public class HttpSignalReceiver extends CPojoBase implements ISignalReceiver,
        ISignalListener {

    /** HTTP service, injected by iPOJO */
    @Requires(filter = "(org.osgi.service.http.port=*)")
    private HttpService pHttpService;

    /** Signal listeners */
    private final Map<String, Set<ISignalListener>> pListeners = new HashMap<String, Set<ISignalListener>>();

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /**
     * Default constructor
     */
    public HttpSignalReceiver() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String, java.io.Serializable)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        System.out.println("Received signal '" + aSignalName + "' : "
                + aSignalData);

        final Set<ISignalListener> signalListeners = new HashSet<ISignalListener>();

        // Get listeners set
        synchronized (pListeners) {

            for (String signal : pListeners.keySet()) {

                // Take care of jokers ('*' and '?')
                if (Utilities.matchFilter(aSignalName, signal)) {
                    signalListeners.addAll(pListeners.get(signal));
                }
            }
        }

        // Notify listeners (with a different lock, as other signals may come)
        for (ISignalListener listener : signalListeners) {
            listener.handleReceivedSignal(aSignalName, aSignalData);
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

        pHttpService.unregister(IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS);
        pLogger.log(LogService.LOG_INFO, "HTTP Signal Sender Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalReceiver#registerListener
     * (java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalListener)
     */
    @Override
    public void registerListener(final String aSignalName,
            final ISignalListener aListener) {

        if (aSignalName == null || aSignalName.isEmpty() || aListener == null) {
            // Invalid call
            return;
        }

        synchronized (pListeners) {

            // Get or create the signal listeners set
            Set<ISignalListener> signalListeners = pListeners.get(aSignalName);

            if (signalListeners == null) {
                signalListeners = new LinkedHashSet<ISignalListener>();
                pListeners.put(aSignalName, signalListeners);
            }

            synchronized (signalListeners) {
                signalListeners.add(aListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalReceiver#
     * unregisterListener(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalListener)
     */
    @Override
    public void unregisterListener(final String aSignalName,
            final ISignalListener aListener) {

        if (aSignalName == null || aSignalName.isEmpty() || aListener == null) {
            // Invalid call
            return;
        }

        synchronized (pListeners) {

            // Get or create the signal listeners set
            final Set<ISignalListener> signalListeners = pListeners
                    .get(aSignalName);

            if (signalListeners != null) {
                synchronized (signalListeners) {
                    signalListeners.remove(aListener);
                }
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

        final ServletReceiver servlet = new ServletReceiver(this);
        try {
            pHttpService.registerServlet(
                    IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS, servlet,
                    null, null);

            pLogger.log(LogService.LOG_INFO, "HTTP Signal Receiver Ready");

        } catch (Exception ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Can't register the HTTP Signal Receiver servlet", ex);
        }
    }
}
