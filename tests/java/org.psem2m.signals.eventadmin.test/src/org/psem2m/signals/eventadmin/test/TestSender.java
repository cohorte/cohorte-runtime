/**
 * File:   TestSender.java
 * Author: Thomas Calmant
 * Date:   15 déc. 2011
 */
package org.psem2m.signals.eventadmin.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsEventsConstants;

/**
 * Sends an "Hello" event throught the EventAdmin service, with the PSEM2M
 * export flag
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-eventadmin-test-sender-factory")
@Instantiate(name = "psem2m-eventadmin-test-sender")
public class TestSender {

    /** The EventAdmin service */
    @Requires
    private EventAdmin pEventAdmin;

    /**
     * Schedules the post of an event to an isolate
     */
    @Validate
    public void validate() {

        final ScheduledExecutorService exec = Executors
                .newSingleThreadScheduledExecutor();

        exec.schedule(new Runnable() {

            @Override
            public void run() {

                final Map<String, Object> map = new HashMap<String, Object>();
                map.put("From",
                        System.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID));
                map.put("To", "Billou");
                map.put(ISignalsEventsConstants.EXPORTED, true);
                map.put(ISignalsEventsConstants.EXPORT_ISOLATES,
                        "isolate-cache");

                pEventAdmin.postEvent(new Event("hello", map));
            }
        }, 2, TimeUnit.SECONDS);
    }
}
