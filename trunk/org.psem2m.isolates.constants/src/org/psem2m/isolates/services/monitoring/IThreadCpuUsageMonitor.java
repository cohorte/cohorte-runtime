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
     * Retrieves the average CPU usage of the given thread, between the first
     * and the last time it was seen by {@link #update()}. Returns -1 if the
     * thread is unknown (dead or never seen), 0 if the average CPU usage
     * couldn't be computed.
     * 
     * @param aThreadId
     *            A thread ID
     * @return The thread average CPU usage, 0 if not computable, -1 if the
     *         thread is unknown.
     */
    double getThreadAverageCpuUsage(final long aThreadId);

    /**
     * Retrieves the average CPU usage of the given thread between the two last
     * calls to {@link #update()}.Returns -1 if the thread is unknown (dead or
     * never seen), 0 if the average CPU usage couldn't be computed.
     * 
     * @param aThreadId
     * @return
     */
    double getThreadCpuUsage(final long aThreadId);

    /**
     * Updates the monitor informations.
     */
    void update();
}
