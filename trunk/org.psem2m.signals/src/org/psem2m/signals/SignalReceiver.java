/**
 * File:   SignalReceiver.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;
import org.psem2m.isolates.services.remote.signals.ISignalReceptionProvider;

/**
 * Base signal receiver logic
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signal-receiver-factory", publicFactory = false)
@Provides(specifications = ISignalReceiver.class)
@Instantiate(name = "psem2m-signal-receiver")
public class SignalReceiver extends CPojoBase implements ISignalReceiver,
        ISignalListener {

    /** Log service */
    @Requires
    // private LogService pLogger;
    private ILogChannelsSvc pChannels;

    /** Signal listeners */
    private final Map<String, Set<ISignalListener>> pListeners = new HashMap<String, Set<ISignalListener>>();

    /** Logger */
    private ILogChannelSvc pLogger;

    /** Number of available providers */
    private int pNbProviders = 0;

    /** Notification thread pool */
    private ExecutorService pNotificationExecutor;

    /** On-line service property */
    @ServiceProperty(name = ISignalReceiver.PROPERTY_ONLINE, value = "false", mandatory = true)
    private boolean pPropertyOnline;

    /** Reception providers */
    @Requires(id = "receivers", optional = true)
    private ISignalReceptionProvider[] pReceivers;

    /**
     * Method called by iPOJO when a reception provider is bound
     * 
     * @param aProvider
     *            The new provider
     */
    @Bind(id = "receivers", aggregate = true, filter = "("
            + ISignalReceptionProvider.PROPERTY_READY + "=true)")
    protected void bindProvider(final ISignalReceptionProvider aProvider) {

        // Register to the provider
        aProvider.registerListener(this);

        // Increase the number of available providers
        pNbProviders++;

        // We're now on-line
        pPropertyOnline = true;
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
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        pLogger.logInfo(this, "RECEIVED", "Signal=", aSignalName, "- Data=",
                aSignalData);

        final Set<ISignalListener> signalListeners = new HashSet<ISignalListener>();

        // Get listeners set
        synchronized (pListeners) {

            for (final String signal : pListeners.keySet()) {
                // Take care of jokers ('*' and '?')
                if (Utilities.matchFilter(aSignalName, signal)) {
                    signalListeners.addAll(pListeners.get(signal));
                }
            }
        }

        // Notify listeners in another thread
        pNotificationExecutor.execute(new Runnable() {

            @Override
            public void run() {

                for (final ISignalListener listener : signalListeners) {
                    listener.handleReceivedSignal(aSignalName, aSignalData);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Unregister from all providers
        for (final ISignalReceptionProvider provider : pReceivers) {
            provider.unregisterListener(this);
        }

        // Clear listeners
        synchronized (pListeners) {
            pListeners.clear();
        }

        // Stop the executor
        pNotificationExecutor.shutdown();

        pLogger.logInfo(this, "invalidatePojo", "Base Signal Receiver Gone");
        pLogger = null;
    }

    /**
     * Tests if the receiver is on line.
     * 
     * @return True if the receiver is on-line
     */
    public boolean isOnline() {

        return pPropertyOnline;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalReceiver#localReception
     * (java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void localReception(final String aSignalName, final ISignalData aData) {

        // Simulate a normal reception
        handleReceivedSignal(aSignalName, aData);
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

    @Unbind(id = "receivers", aggregate = true)
    protected void unbindProvider(final ISignalReceptionProvider aProvider) {

        aProvider.unregisterListener(this);

        // Decrease the number of available providers
        pNbProviders--;

        if (pNbProviders == 0) {
            // No more provider, we're not on-line anymore
            pPropertyOnline = false;
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

        // Set up the thread pool
        pNotificationExecutor = Executors.newCachedThreadPool();

        try {
            pLogger = pChannels.getLogChannel("RemoteServices");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        pLogger.logInfo(this, "validatePojo", "Base Signal Receiver Ready");
    }
}
