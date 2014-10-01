/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

            } catch (final InterruptedException e) {
                // Stop working on interruption
                break;
            }
        }
    }
}
