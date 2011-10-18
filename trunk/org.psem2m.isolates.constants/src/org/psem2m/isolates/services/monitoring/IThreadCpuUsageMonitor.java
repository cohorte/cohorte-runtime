/**
 * File:   IThreadCpuUsageMonitor.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2011
 */
package org.psem2m.isolates.services.monitoring;

/**
 * Represents a CPU Thread monitor
 * 
 * @author Thomas Calmant
 */
public interface IThreadCpuUsageMonitor {

    /**
     * Retrieves the average CPU usage for all threads
     * 
     * @return The average CPU usage of all running processes
     */
    double getAverageCpuUsage();

    /**
     * Retrieves all known threads IDs
     * 
     * @return All known thread IDs
     */
    long[] getKnownThreadsIds();

    /**
     * Retrieves the average CPU time of the given thread. Returns -1 if the
     * thread is unknown (dead or never seen), 0 if the average CPU time
     * couldn't be computed.
     * 
     * @param aThreadId
     *            A thread ID
     * @return The thread average CPU usage, 0 if not computable, -1 if the
     *         thread is unknown.
     */
    double getThreadAverageCpuUsage(final long aThreadId);

    /**
     * Updates the monitor informations
     */
    void update();
}
