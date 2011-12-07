/**
 * File:   SignalBroadcaster.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcastProvider;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * Base signal sender logic
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signal-broadcaster-factory", publicFactory = true)
@Provides(specifications = ISignalBroadcaster.class)
@Instantiate(name = "psem2m-signal-broadcaster")
public class SignalBroadcaster extends CPojoBase implements ISignalBroadcaster {

    /** Broadcast providers */
    @Requires(id = "providers", optional = true)
    private ISignalBroadcastProvider[] pBroadcasters;

    /** Log service */
    @Requires
    // private LogService pLogger;
    private ILogChannelsSvc pChannels;

    /** Logger */
    private ILogChannelSvc pLogger;

    /** Signal receiver (for local only communication) */
    @Requires
    private ISignalReceiver pReceiver;

    /** Signals targeting a specific isolate */
    private final List<StoredSignal> pStoredSpecificSignals = new LinkedList<StoredSignal>();

    /** Signals for multiple targets */
    private final List<StoredSignal> pStoredTargetedSignals = new LinkedList<StoredSignal>();

    /**
     * Called by iPOJO when a broadcast provider is bound. Flushes currently
     * stored signals to the provider.
     * 
     * @param aProvider
     *            A new broadcast provider
     */
    @Bind(id = "providers")
    protected void bindProvider(final ISignalBroadcastProvider aProvider) {

        // Use it to send all of our stored signals
        synchronized (pStoredTargetedSignals) {
            for (final StoredSignal signal : pStoredTargetedSignals) {
                // Multiple targets
                aProvider.sendData(signal.getTargets(), signal.getSignalName(),
                        signal.getSignalData());
            }
        }

        synchronized (pStoredSpecificSignals) {
            for (final StoredSignal signal : pStoredSpecificSignals) {
                // Specific isolate ID
                aProvider.sendData(signal.getTargetId(),
                        signal.getSignalName(), signal.getSignalData());
            }
        }
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
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Base Signal Broadcaster Gone");
        pLogger = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalBroadcaster#sendData
     * (org
     * .psem2m.isolates.services.remote.signals.ISignalBroadcaster.EEmitterTargets
     * , java.lang.String, java.io.Serializable)
     */
    @Override
    public void sendData(final EEmitterTargets aTargets,
            final String aSignalName, final Serializable aData) {

        pLogger.logInfo(this, "SEND TO TARGETS", "Signal=", aSignalName,
                "- Target=", aTargets, "- Data=", aData);

        if (aTargets == EEmitterTargets.LOCAL) {
            // Only case to send signal directly to the receiver
            final ISignalData signalData = new LocalSignalData(aData);
            pReceiver.localReception(aSignalName, signalData);

        } else {
            // Real broadcast
            if (pBroadcasters.length == 0) {
                // Store the signal, waiting to send it
                synchronized (pStoredTargetedSignals) {
                    pStoredTargetedSignals.add(new StoredSignal(aTargets,
                            aSignalName, aData));
                }

            } else {
                // Use current providers
                for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {
                    broadcaster.sendData(aTargets, aSignalName, aData);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalBroadcaster#sendData
     * (java.lang.String, java.lang.String, java.io.Serializable)
     */
    @Override
    public boolean sendData(final String aIsolateId, final String aSignalName,
            final Serializable aData) {

        pLogger.logInfo(this, "SEND TO ISOLATE", "Signal=", aSignalName,
                "- isolate=", aIsolateId, "- Data=", aData);

        if (pBroadcasters.length == 0) {
            // No providers, store the signal to emit
            synchronized (pStoredSpecificSignals) {
                pStoredSpecificSignals.add(new StoredSignal(aIsolateId,
                        aSignalName, aData));
            }
            return false;

        } else {
            // At least one of the current providers must succeed
            boolean atLeastOneSuccess = false;

            // Use current providers
            for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {

                atLeastOneSuccess |= broadcaster.sendData(aIsolateId,
                        aSignalName, aData);
            }

            return atLeastOneSuccess;
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

        try {
            pLogger = pChannels.getLogChannel("RemoteServices");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        pLogger.logInfo(this, "validatePojo", "Base Signal Broadcaster Ready");
    }
}
