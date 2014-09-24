/**
 * File:   MonitorPollerThread.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2011
 */
package org.psem2m.isolates.slave.agent.threads;

/**
 * The thread that polls the threads monitor
 * 
 * @author Thomas Calmant
 */
public class MonitorPollerThread extends Thread {

    /** The parent monitor */
    private final ThreadsMonitor pMonitor;

    /** The poll interval */
    private final long pPollInterval;

    /**
     * Sets up members
     */
    public MonitorPollerThread(final ThreadsMonitor aParent,
            final long aPollInterval) {

        super("psem2m-threads-monitor-poller");
        setDaemon(true);

        pMonitor = aParent;
        pPollInterval = aPollInterval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        while (!isInterrupted()) {

            // Update the monitor
            pMonitor.updateMonitor();

            try {
                // Make a pause
                Thread.sleep(pPollInterval);

            } catch (InterruptedException e) {
                // Stop working on interruption
                break;
            }
        }
    }
}
