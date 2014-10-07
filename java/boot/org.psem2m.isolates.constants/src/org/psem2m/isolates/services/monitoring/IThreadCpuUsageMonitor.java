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
     * @return The average CPU usage of the given thread, 0 if not computable,
     *         -1 if the thread is unknown.
     */
    double getThreadAverageCpuUsage(final long aThreadId);

    /**
     * Retrieves the average CPU usage of the given thread between the two last
     * calls to {@link #update()}.Returns -1 if the thread is unknown (dead or
     * never seen), 0 if the average CPU usage couldn't be computed.
     *
     * @param aThreadId
     *            A thread ID
     * @return The average CPU usage of the given thread, 0 if not computable,
     *         -1 if the thread is unknown.
     */
    double getThreadCpuUsage(final long aThreadId);

    /**
     * Updates the monitor informations.
     */
    void update();
}
