/**
 * File:   ThreadsMonitor.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2011
 */
package org.psem2m.isolates.slave.agent.threads;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.services.monitoring.IThreadCpuUsageMonitor;

/**
 * Calls the JMX monitor to get the average isolate CPU load
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-threads-monitor-factory")
public class ThreadsMonitor {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The thread monitor */
    @Requires
    private IThreadCpuUsageMonitor pMonitor;

    /** The poll thread */
    private ScheduledExecutorService pScheduledExecutor;

    /**
     * Enumerates all threads in the given group and sub-groups
     * 
     * @param aResultMap
     *            The result id -&gt; thread map (must not be null)
     * @param aThreadGroup
     *            A root thread group
     */
    protected void enumerateThreadGroup(final Map<Long, Thread> aResultMap,
            final ThreadGroup aThreadGroup) {

        if (aResultMap == null || aThreadGroup == null) {
            // Forbid invalid parameters
            return;
        }

        try {
            // Count group threads
            final int nbThreads = aThreadGroup.activeCount() * 2;
            final Thread[] threads = new Thread[nbThreads];

            // Retrieve all threads in group
            aThreadGroup.enumerate(threads, false);

            // Fill the map
            for (final Thread thread : threads) {
                if (thread != null) {
                    aResultMap.put(thread.getId(), thread);
                }
            }

        } catch (final SecurityException ex) {
            // Ignore security exception...
            pLogger.logWarn(this, "enumerateThreadGroup",
                    "Not allowed to read thread group ",
                    aThreadGroup.getName(), " :", ex);
        }

        // Count sub-groups
        final int nbThreadGroups = aThreadGroup.activeGroupCount() * 2;
        final ThreadGroup[] threadGroups = new ThreadGroup[nbThreadGroups];

        // Retrieve all sub-groups
        aThreadGroup.enumerate(threadGroups, false);

        // Recursive calls on sub-groups
        for (final ThreadGroup group : threadGroups) {
            enumerateThreadGroup(aResultMap, group);
        }
    }

    protected String findThreadStarter(final Thread aThread) {

        // final ClassLoader classLoader = aThread.getContextClassLoader();
        //
        // StackTraceElement[] stack = aThread.getStackTrace();
        // for (StackTraceElement s : stack) {
        // System.out.println("Trace : " + s);
        // }
        //
        // System.out.println(aThread + " - CL=" + classLoader + " - CLClass="
        // + (classLoader != null ? classLoader.getClass() : "null")
        // + "Class=" + aThread.getClass() + "- ClassCL="
        // + aThread.getClass().getClassLoader() + " - Bundle : "
        // + FrameworkUtil.getBundle(aThread.getClass()));

        return "<unknown>";
    }

    /**
     * Retrieves all running threads in the JVM.
     * 
     * Code from
     * http://stackoverflow.com/questions/1323408/get-a-list-of-all-threads
     * -currently-running-in-java
     * 
     * @return all running threads in the JVM
     */
    protected Map<Long, Thread> getAllThreads() {

        // Find the root thread group
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }

        // List all threads
        final Map<Long, Thread> resultMap = new HashMap<Long, Thread>();
        enumerateThreadGroup(resultMap, rootGroup);

        return resultMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Invalidate
    public void invalidatePojo() {

        pScheduledExecutor.shutdownNow();
        pLogger.logInfo(this, "invalidatePojo", "Threads Monitor Gone");
    }

    /**
     * Update the monitor values
     */
    protected void updateMonitor() {

        // pLogger.logInfo(this, "updateMonitor", "Update Monitor...");

        synchronized (pMonitor) {

            // Update the monitor
            pMonitor.update();

            // Get the list of active threads
            final Map<Long, Thread> allThreads = getAllThreads();

            pLogger.logInfo(this, "updateMonitor",
                    "Average CPU Usage : %3.2f for %d threads",
                    pMonitor.getAverageCpuUsage(), allThreads.size());

            for (final long id : pMonitor.getKnownThreadsIds()) {

                // Find the correspond JVM thread
                final Thread thread = allThreads.get(id);

                // Compute the thread name
                final String threadName;
                if (thread == null) {
                    threadName = "<null>";
                } else {
                    threadName = thread.getName();
                }

                // Get thread CPU usage information
                final double threadCpuUsage = pMonitor.getThreadCpuUsage(id);

                final double threadAverageCpuUsage = pMonitor
                        .getThreadAverageCpuUsage(id);

                if (threadCpuUsage > 80) {
                    // > 80% CPU usage
                    pLogger.logWarn(
                            this,
                            "updateMonitor",
                            "HEAVY  ACTIVITY FOR %3d (%15.15s) - %5.2f / %5.2f - %s",
                            id, threadName, threadCpuUsage,
                            threadAverageCpuUsage, findThreadStarter(thread));

                } else if (threadCpuUsage > 50) {
                    // > 50% CPU usage
                    pLogger.logWarn(
                            this,
                            "updateMonitor",
                            "HIGH   activity for %3d (%15.15s) - %5.2f / %5.2f - %s",
                            id, threadName, threadCpuUsage,
                            threadAverageCpuUsage, findThreadStarter(thread));
                }
            }
        }

        // pLogger.logInfo(this, "updateMonitor", "Update Monitor DONE");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Validate
    public void validatePojo() {

        // Prepare the scheduled update
        pScheduledExecutor = Executors.newScheduledThreadPool(1);
        pScheduledExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {

                updateMonitor();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        pLogger.logInfo(this, "validatePojo", "Threads Monitor Ready");
    }
}
