/**
 * File:   EventNotifier.java
 * Author: Thomas Calmant
 * Date:   15 déc. 2011
 */
package org.psem2m.signals.eventadmin;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.ISignalsEventsConstants;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Receives the EventAdmin bridge signals and posts them in the local isolate
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-eventadmin-signals-notifier-factory", publicFactory = false)
@Instantiate(name = "psem2m-eventadmin-signals-notifier")
public class EventNotifier extends CPojoBase implements ISignalListener {

    /** EventAdmin service */
    @Requires
    private EventAdmin pEventAdmin;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            A signal receiver
     */
    @Bind
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        // Register to all event admin signals
        aSignalReceiver.registerListener(ISignalsEventsConstants.SIGNAL_PREFIX
                + "*", this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        // Extract the topic
        final String topic = aSignalName
                .substring(ISignalsEventsConstants.SIGNAL_PREFIX.length());

        // Get the properties
        final Map<?, ?> properties = (Map<?, ?>) aSignalData.getSignalContent();

        if (properties != null
                && properties.containsKey(ISignalsEventsConstants.EXPORTED)) {
            // Avoid infinite event export loop
            properties.remove(ISignalsEventsConstants.EXPORTED);
        }

        // Post the event
        pEventAdmin.postEvent(new Event(topic, properties));

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

        pLogger.logInfo(this, "invalidatePojo", "EventAdmin notifier gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "EventAdmin notifier ready");
    }
}
