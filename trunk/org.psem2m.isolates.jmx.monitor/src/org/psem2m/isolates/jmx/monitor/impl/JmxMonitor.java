/**
 * File:   JmxMonitor.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2011
 */
package org.psem2m.isolates.jmx.monitor.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.psem2m.isolates.services.monitoring.IThreadCpuUsageMonitor;

/**
 * Uses JMX to poll the Thread MXBean and compute
 * 
 * @author Thomas Calmant
 */
public class JmxMonitor implements IThreadCpuUsageMonitor {

    /** JMX Thread MXBean */
    private ThreadMXBean pThreadBean;

    /** The threads times */
    private final Map<Long, ThreadTimes> pTimes = new HashMap<Long, ThreadTimes>();

    /**
     * Prepares the beans
     */
    public JmxMonitor() {

        pThreadBean = ManagementFactory.getThreadMXBean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.jmx.monitor.impl.ICpuThreadMonitor#getAverageCpuUsage
     * ()
     */
    @Override
    public double getAverageCpuUsage() {

        double averageCpuTime = 0;
        int validThreads = 0;

        for (ThreadTimes threadTime : pTimes.values()) {

            // Sum all average CPU usage
            double threadAverageCpuTime = threadTime.getAverageCpuTimeUsage();

            if (threadAverageCpuTime > 0) {
                // Only use valid thread times
                validThreads++;
                averageCpuTime += threadTime.getAverageCpuTimeUsage();
            }
        }

        if (validThreads == 0) {
            // No valid data...
            return 0;
        }

        return averageCpuTime / validThreads;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.jmx.monitor.impl.ICpuThreadMonitor#getKnownThreadsIds
     * ()
     */
    @Override
    public long[] getKnownThreadsIds() {

        long[] result = new long[pTimes.size()];
        int i = 0;

        for (Long threadId : pTimes.keySet()) {
            // Convert all Long objects to primitive
            result[i] = threadId;
            i++;
        }

        // Sort the array
        Arrays.sort(result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.jmx.monitor.impl.ICpuThreadMonitor#
     * getThreadAverageCpuUsage(long)
     */
    @Override
    public double getThreadAverageCpuUsage(final long aThreadId) {

        final ThreadTimes threadTimes = pTimes.get(aThreadId);
        if (threadTimes == null) {
            return -1;
        }

        return threadTimes.getTotalAverageCpuTimeUsage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.monitoring.IThreadCpuUsageMonitor#
     * getThreadCpuUsage(long)
     */
    @Override
    public double getThreadCpuUsage(final long aThreadId) {

        final ThreadTimes threadTimes = pTimes.get(aThreadId);
        if (threadTimes == null) {
            return -1;
        }

        return threadTimes.getAverageCpuTimeUsage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.jmx.monitor.impl.ICpuThreadMonitor#update()
     */
    @Override
    public void update() {

        if (!pThreadBean.isThreadCpuTimeSupported()) {
            // Thread CPU Time not supported, do nothing
            return;
        }

        // Work on all live thread
        final long[] threadsIds = pThreadBean.getAllThreadIds();
        for (long threadId : threadsIds) {

            // Get thread times
            final long cpuTime = pThreadBean.getThreadCpuTime(threadId);
            final long userTime = pThreadBean.getThreadCpuTime(threadId);

            if (cpuTime == -1 || userTime == -1) {
                // Dead thread
                pTimes.remove(threadId);
                continue;
            }

            // Get the stored thread time
            final ThreadTimes threadTimes = pTimes.get(threadId);

            if (threadTimes == null) {
                // New thread
                pTimes.put(threadId, new ThreadTimes(threadId, cpuTime,
                        userTime));

            } else {
                // Update the thread times
                threadTimes.updateTimes(cpuTime, userTime);
            }
        }
    }
}
