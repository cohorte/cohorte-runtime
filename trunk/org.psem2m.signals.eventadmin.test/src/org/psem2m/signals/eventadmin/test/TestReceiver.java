/**
 * File:   TestReceiver.java
 * Author: Thomas Calmant
 * Date:   15 déc. 2011
 */
package org.psem2m.signals.eventadmin.test;

import javax.swing.JOptionPane;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsEventsConstants;

/**
 * An EventHandler service that pops up a message dialog when an imported
 * "hello" event is notified by the EventAdmin service
 * 
 * @author Thomas Calmant
 */
@Component
@Instantiate
@Provides(specifications = EventHandler.class, properties = { @StaticServiceProperty(name = EventConstants.EVENT_TOPIC, value = "hello", type = "String") })
public class TestReceiver implements EventHandler {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event
     * .Event)
     */
    @Override
    public void handleEvent(final Event aEvent) {

        final Object imported = aEvent
                .getProperty(ISignalsEventsConstants.IMPORTED);
        if (imported == null) {
            return;
        }

        final StringBuilder msg = new StringBuilder();
        msg.append("Received signal :");
        msg.append("\nImported :").append(imported);
        msg.append("\nTopic : ").append(aEvent.getTopic());
        msg.append("\nFrom  : ").append(aEvent.getProperty("From"));
        msg.append("\nTo    : ").append(aEvent.getProperty("To"));

        new Thread(new Runnable() {

            @Override
            public void run() {

                JOptionPane.showMessageDialog(
                        null,
                        msg.toString(),
                        System.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }).start();
    }
}
