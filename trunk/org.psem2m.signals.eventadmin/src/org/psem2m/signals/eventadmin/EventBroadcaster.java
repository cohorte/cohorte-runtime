/**
 * File:   EventBroadcaster.java
 * Author: Thomas Calmant
 * Date:   15 déc. 2011
 */
package org.psem2m.signals.eventadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.ISignalsEventsConstants;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;

/**
 * An EventHandler that transmits received events to other isolates
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-eventadmin-signals-sender", publicFactory = false)
@Provides(specifications = EventHandler.class, properties = {
        /* Register to all topics */
        @StaticServiceProperty(name = EventConstants.EVENT_TOPIC, value = "*", type = "String"),
        /* Register to exported events */
        @StaticServiceProperty(name = EventConstants.EVENT_FILTER, value = "("
                + ISignalsEventsConstants.EXPORTED + "=true)", type = "String") })
@Instantiate(name = "psem2m-eventadmin-signals-sender")
public class EventBroadcaster extends CPojoBase implements EventHandler {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The signal broadcaster */
    @Requires
    private ISignalBroadcaster pSender;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event
     * .Event)
     */
    @Override
    public void handleEvent(final Event aEvent) {

        // Prepare the signal name
        final String signalName = ISignalsEventsConstants.SIGNAL_PREFIX
                + aEvent.getTopic();

        // Copy properties in a map
        final String[] propertiesNames = aEvent.getPropertyNames();
        if (propertiesNames == null || propertiesNames.length == 0) {
            // Nothing to do
            pLogger.logInfo(this, "handleEvent", "No properties : no export");
            return;
        }

        final HashMap<String, Object> signalData = new HashMap<String, Object>(
                propertiesNames.length);

        for (final String propertyName : propertiesNames) {

            if (propertyName.equals(ISignalsEventsConstants.EXPORTED)) {
                signalData.put(ISignalsEventsConstants.IMPORTED, true);

            } else {
                final Object propertyValue = aEvent.getProperty(propertyName);
                if (propertyValue != null) {
                    // null values are not allowed
                    signalData.put(propertyName, propertyValue);
                }
            }
        }

        // Is the target given from the enumeration
        final String targetStr = (String) aEvent
                .getProperty(ISignalsEventsConstants.EXPORT_TARGET);
        if (targetStr != null) {

            try {
                pSender.sendData(
                        ISignalBroadcaster.EEmitterTargets.valueOf(targetStr),
                        signalName, signalData);

                // Done
                return;

            } catch (final IllegalArgumentException e) {
                // Invalid target
                pLogger.logWarn(this, "handleEvent", "Invalid target :",
                        targetStr);
            }
        }

        // Invalid export target : try to grab a list of isolates
        Object isolatesObj = aEvent
                .getProperty(ISignalsEventsConstants.EXPORT_ISOLATES);

        if (isolatesObj.getClass().isArray()) {
            // Transform the array in a collection
            isolatesObj = Arrays.asList(isolatesObj);
        }

        if (isolatesObj instanceof Collection) {

            final List<String> isolates = new ArrayList<String>(
                    ((Collection<?>) isolatesObj).size());

            for (final Object isolateObj : (Collection<?>) isolatesObj) {
                if (isolateObj instanceof CharSequence) {
                    isolates.add(isolateObj.toString());
                }
            }

            // Send to isolates
            pSender.sendData(isolates, signalName, signalData);
            return;

        } else if (isolatesObj instanceof CharSequence) {
            // Only one isolate given
            pSender.sendData(isolatesObj.toString(), signalName, signalData);
            return;
        }

        // No isolate ID given : send to all
        pSender.sendData(ISignalBroadcaster.EEmitterTargets.ALL, signalName,
                signalData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() {

        pLogger.logInfo(this, "invalidatePojo", "EventAdmin broadcaster gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() {

        pLogger.logInfo(this, "validatePojo", "EventAdmin broadcaster ready");
    }
}
