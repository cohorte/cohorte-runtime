/**
 * File:   ThreadTimes.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2011
 */
package org.psem2m.isolates.jmx.monitor.impl;

/**
 * Thread information storage bean
 * 
 * @author Thomas Calmant
 */
public class ThreadTimes {

    /** CPU time, when first seen */
    private final long pInitialCpuTime;

    /** Initial nano time */
    private final long pInitialNanoTime;

    /** User time, when first seen */
    private final long pInitialUserTime;

    /** CPU time, when last seen */
    private long pLastCpuTime;

    /** Last update nano time */
    private long pLastNanoTime;

    /** User time, when last seen */
    private long pLastUserTime;

    /** Previous update CPU time */
    private long pPreviousCpuTime;

    /** Previous update nano time */
    private long pPreviousNanoTime;

    /** Previous update User time */
    private long pPreviousUserTime;

    /** The thread ID */
    private final long pThreadId;

    /**
     * Sets up the bean
     * 
     * @param aThreadId
     *            The thread ID
     * @param aCpuTime
     *            The initial CPU time
     * @param aUserTime
     *            The initial user time
     */
    public ThreadTimes(final long aThreadId, final long aCpuTime,
            final long aUserTime) {

        pInitialNanoTime = System.nanoTime();

        pThreadId = aThreadId;
        pInitialCpuTime = aCpuTime;
        pInitialUserTime = aUserTime;

        pLastNanoTime = pInitialNanoTime;
        pLastCpuTime = aCpuTime;
        pLastUserTime = aUserTime;
    }

    /**
     * Retrieves the average CPU time usage of the thread, between the two last
     * updates. Returns 0 if {@link #updateTimes(long, long)} was never called
     * since the object creation.
     * 
     * @return The average CPU time usage, or 0
     */
    public double getAverageCpuTimeUsage() {

        if (pPreviousNanoTime == pLastNanoTime) {
            // Avoid a division by zero
            return 0;
        }

        return ((pLastCpuTime - pPreviousCpuTime) * 100.0)
                / (pLastNanoTime - pPreviousNanoTime);
    }

    /**
     * Retrieves the average User time usage of the thread, between the two last
     * updates. Returns 0 if {@link #updateTimes(long, long)} was never called
     * since the object creation.
     * 
     * @return The average User time usage, or 0
     */
    public double getAverageUserTimeUsage() {

        if (pPreviousNanoTime == pLastNanoTime) {
            // Avoid a division by zero
            return 0;
        }

        return ((pLastUserTime - pPreviousUserTime) * 100.0)
                / (pLastNanoTime - pPreviousNanoTime);
    }

    /**
     * Retrieves the initial CPU time
     * 
     * @return the initial CPU time
     */
    public long getInitialCpuTime() {

        return pInitialCpuTime;
    }

    /**
     * Retrieves the initial User time
     * 
     * @return the initial User time
     */
    public long getInitialUserTime() {

        return pInitialUserTime;
    }

    /**
     * Retrieves the last CPU time
     * 
     * @return the last CPU time
     */
    public long getLastCpuTime() {

        return pLastCpuTime;
    }

    /**
     * Retrieves the last User time
     * 
     * @return the last User time
     */
    public long getLastUserTime() {

        return pLastUserTime;
    }

    /**
     * Retrieves the CPU time of the previous update
     * 
     * @return the CPU time of the previous update
     */
    public long getPreviousCpuTime() {

        return pPreviousCpuTime;
    }

    /**
     * Retrieves the User time of the previous update
     * 
     * @return the User time of the previous update
     */
    public long getPreviousUserTime() {

        return pPreviousUserTime;
    }

    /**
     * Retrieves the thread ID
     * 
     * @return the thread ID
     */
    public long getThreadId() {

        return pThreadId;
    }

    /**
     * Retrieves the average CPU time usage of the thread, between the initial
     * and the last updates. Returns 0 if {@link #updateTimes(long, long)} was
     * never called since the object creation.
     * 
     * @return The average CPU time usage, or 0
     */
    public double getTotalAverageCpuTimeUsage() {

        if (pInitialNanoTime == pLastNanoTime) {
            // Avoid a division by zero
            return 0;
        }

        return ((pLastCpuTime - pInitialCpuTime) * 100.0)
                / (pLastNanoTime - pInitialNanoTime);
    }

    /**
     * Retrieves the average User time usage of the thread, between the initial
     * and the last updates. Returns 0 if {@link #updateTimes(long, long)} was
     * never called since the object creation.
     * 
     * @return The average User time usage, or 0
     */
    public double getTotalAverageUserTimeUsage() {

        if (pInitialNanoTime == pLastNanoTime) {
            // Avoid a division by zero
            return 0;
        }

        return ((pLastUserTime - pInitialUserTime) * 100.0)
                / (pLastNanoTime - pInitialNanoTime);
    }

    /**
     * Updates the times values
     * 
     * @param aLastCpuTime
     *            the last seen CPU time
     * @param aLastUserTime
     *            the last seen User time
     */
    public void updateTimes(final long aLastCpuTime, final long aLastUserTime) {

        // Store call time
        final long callTime = System.nanoTime();

        // Store previous values
        pPreviousCpuTime = pLastCpuTime;
        pPreviousNanoTime = pLastNanoTime;
        pPreviousUserTime = pLastUserTime;

        // Update values
        pLastNanoTime = callTime;
        pLastCpuTime = aLastCpuTime;
        pLastUserTime = aLastUserTime;
    }
}
