/**
 * File:   IsolateFailureHandler.java
 * Author: Thomas Calmant
 * Date:   10 oct. 2011
 */
package org.psem2m.isolates.monitor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Isolate launch retry logic
 * 
 * @author Thomas Calmant
 */
public class IsolateFailureHandler {

    /** Scheduled executor */
    private final ScheduledExecutorService pExecutor = Executors
            .newScheduledThreadPool(1);

    /** Scheduled tasks */
    private final Map<String, ScheduledFuture<?>> pIsolateFutures = new HashMap<String, ScheduledFuture<?>>();

    /** Number of tries for each isolate */
    private final Map<String, Long> pIsolateTries = new HashMap<String, Long>();

    /** Maximum successive tries */
    private final int pMaxTriesStreak;

    /** A Runnable that calls an isolate launcher */
    private final MonitorCore pMonitorCore;

    /** Time to wait before the next streak */
    private final int pTimeWaitBeforeStreak;

    /** Time to wait when in a try streak */
    private final int pTimeWaitInStreak;

    /**
     * Sets up the failure handler
     * 
     * @param aMonitor
     *            The associated monitor
     * @param aMaxTriesStreak
     *            Maximum tries in a try streak
     * @param aWaitInStreak
     *            Time to wait before the next try when in a streak
     * @param aWaitBeforeStreak
     *            Time to wait before the next try streak
     */
    public IsolateFailureHandler(final MonitorCore aMonitor,
            final int aMaxTriesStreak, final int aWaitInStreak,
            final int aWaitBeforeStreak) {

        pMonitorCore = aMonitor;
        pMaxTriesStreak = aMaxTriesStreak;
        pTimeWaitInStreak = aWaitInStreak;
        pTimeWaitBeforeStreak = aWaitBeforeStreak;
    }

    /**
     * Schedules the next launch try.
     * 
     * @param aIsolateId
     *            The isolate that failed
     */
    public void isolateFailed(final String aIsolateId) {

        // Number of launch tries for this isolate
        final long nbTries;

        synchronized (pIsolateTries) {
            // Get the number of failures
            final Long nbTriesObj = pIsolateTries.get(aIsolateId);

            if (nbTriesObj == null) {
                // First fail
                nbTries = 1;

            } else {
                nbTries = nbTriesObj.longValue() + 1;
            }

            // Update the map
            pIsolateTries.put(aIsolateId, nbTries);
        }

        // Time to wait (in seconds)
        final int timeUntilNextTry;

        if (nbTries < pMaxTriesStreak) {
            // Less than N tries, wait a second before a new try
            timeUntilNextTry = pTimeWaitInStreak;

        } else {
            // Wait for more (10 seconds) ...
            timeUntilNextTry = pTimeWaitBeforeStreak;

            // ... and reset the number of tries
            pIsolateTries.remove(aIsolateId);
        }

        // Prepare the runnable
        final Runnable isolateLauncher = new Runnable() {

            @Override
            public void run() {

                pMonitorCore.startIsolate(aIsolateId);
            }
        };

        // Schedule the work
        try {
            pIsolateFutures.put(aIsolateId, pExecutor.schedule(isolateLauncher,
                    timeUntilNextTry, TimeUnit.SECONDS));

        } catch (RejectedExecutionException e) {
            // Ignore error : the executor has been shut down
        }
    }

    /**
     * Stops the failure handler treatment
     */
    public void stop() {

        synchronized (pIsolateFutures) {
            // Cancel all future tasks (and running ones)
            for (ScheduledFuture<?> future : pIsolateFutures.values()) {
                future.cancel(true);
            }
        }
    }
}
