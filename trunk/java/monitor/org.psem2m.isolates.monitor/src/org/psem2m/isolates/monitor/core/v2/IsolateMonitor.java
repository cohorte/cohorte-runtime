/**
 * 
 */
package org.psem2m.isolates.monitor.core.v2;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.isolates.IIsolateStatusEventListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.monitor.IPlatformMonitor;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Isolate monitor base component.
 * 
 * Notifies monitor signals listeners
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-monitor-base-factory", publicFactory = false)
@Instantiate(name = "psem2m-monitor-base")
public class IsolateMonitor implements ISignalListener {

    /** Isolate status events listeners */
    @Requires(optional = true)
    private IIsolateStatusEventListener[] pIsolateStatusListeners;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The platform monitor */
    @Requires
    private IPlatformMonitor pMonitor;

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            A signal receiver
     */
    @Bind
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        // Subscribe to the full-platform stop signal
        aSignalReceiver.registerListener(
                ISignalsConstants.MONITOR_SIGNAL_STOP_PLATFORM, this);

        // Subscribe to 'isolate lost' signal
        aSignalReceiver.registerListener(ISignalsConstants.ISOLATE_LOST_SIGNAL,
                this);

        // Subscribe to isolate status
        aSignalReceiver.registerListener(
                ISignalsConstants.ISOLATE_STATUS_SIGNAL, this);
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

        // Get the signal information
        final String senderId = aSignalData.getSenderId();
        final Object signalContent = aSignalData.getSignalContent();

        // Signal status
        if (aSignalName.equals(ISignalsConstants.ISOLATE_STATUS_SIGNAL)) {
            // Test if the signal content is an isolate status
            if (signalContent instanceof IsolateStatus) {
                notifyIsolateStatusEvent(senderId,
                        (IsolateStatus) signalContent);

            } else {
                pLogger.logWarn(this, "handleReceivedSignal",
                        "Received signal=", aSignalName,
                        "but content can't type be handled=", signalContent);
            }

        } else if (aSignalName.equals(ISignalsConstants.ISOLATE_LOST_SIGNAL)) {
            // Isolate lost -> ID in the signal content
            if (signalContent instanceof CharSequence) {

                // Notify listeners
                notifyIsolateLost(signalContent.toString());

            } else {
                pLogger.logWarn(this, "handleReceivedSignal",
                        "Received signal=", aSignalName,
                        "but content can't type be handled=", signalContent);
            }

        } else if (aSignalName
                .equals(ISignalsConstants.MONITOR_SIGNAL_STOP_PLATFORM)) {
            // Stop everything
            pMonitor.stopPlatform();
        }

        return null;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        pLogger.logInfo(this, "invalidate", "Isolate Monitor gone");
    }

    /**
     * Notifies all listeners that an isolate has been lost
     * 
     * @param aIsolateId
     *            The lost isolate ID
     */
    private void notifyIsolateLost(final String aIsolateId) {

        for (final IIsolateStatusEventListener listener : pIsolateStatusListeners) {

            try {
                listener.handleIsolateLost(aIsolateId);

            } catch (final Exception ex) {
                pLogger.logWarn(this, "notifyIsolateLost",
                        "Error notifying an isolate lost listener:", ex);
            }
        }
    }

    /**
     * Notifies all listeners that an isolate status has been received
     * 
     * @param aSenderId
     *            The isolate that sent the ID
     * @param aStatus
     *            The isolate status
     */
    private void notifyIsolateStatusEvent(final String aSenderId,
            final IsolateStatus aStatus) {

        for (final IIsolateStatusEventListener listener : pIsolateStatusListeners) {

            try {
                listener.handleIsolateStatusEvent(aSenderId, aStatus);

            } catch (final Exception ex) {
                pLogger.logWarn(this, "notifyIsolateStatusEvent",
                        "Error notifying an isolate status event listener:", ex);
            }
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        pLogger.logInfo(this, "validate", "Isolate Monitor ready");
    }
}
