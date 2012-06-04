/**
 * File:   ForkerAggregator.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.EEmitterTargets;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * @author Thomas Calmant
 */
@Component(name = "psem2m-forker-aggregator-factory", publicFactory = false)
@Provides(specifications = IForker.class)
@Instantiate(name = "psem2m-forker-aggregator")
public class ForkerAggregator implements IForker, ISignalListener {

    /** The command ID generator */
    private static final AtomicInteger sCmdId = new AtomicInteger();

    /** The forkers directory */
    @Requires
    private IInternalSignalsDirectory pInternalDirectory;

    /** Isolate -&gt; Associated forker map */
    private final Map<String, String> pIsolateForkers = new HashMap<String, String>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Order results */
    private final Map<Integer, Integer> pResults = new HashMap<Integer, Integer>();

    /** The signal sender */
    @Requires
    private ISignalBroadcaster pSender;

    /** Signal response waiters */
    private final Map<Integer, Semaphore> pWaiters = new HashMap<Integer, Semaphore>();

    /**
     * Called when the signal service is up
     * 
     * @param aReceiver
     *            The signal service
     */
    @Bind
    protected void bindSignalReceiver(final ISignalReceiver aReceiver) {

        aReceiver.registerListener(IForkerOrders.SIGNAL_PREFIX_MATCH_ALL, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#getHostName()
     */
    @Override
    public String getHostName() {

        return Utilities.getHostName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (IForkerOrders.SIGNAL_RESPONSE.equals(aSignalName)) {

            final Object rawData = aSignalData.getSignalContent();
            if (rawData instanceof Map) {

                final Map<?, ?> data = (Map<?, ?>) rawData;
                final Integer cmdId = (Integer) data.get(IForkerOrders.CMD_ID);
                if (cmdId == null) {
                    // Unusable data
                    return;
                }

                pResults.put(cmdId,
                        (Integer) data.get(IForkerOrders.RESULT_CODE));

                final Semaphore cmdWaiter = pWaiters.get(cmdId);
                if (cmdWaiter != null) {
                    // Release the waiter
                    cmdWaiter.release();
                }
            }
        }
    }

    /**
     * Component invalidation
     */
    @Invalidate
    public void invalidate() {

        pWaiters.clear();
        pResults.clear();
        pIsolateForkers.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#ping(java.lang.String)
     */
    @Override
    public int ping(final String aIsolateId) {

        final String forker = pIsolateForkers.get(aIsolateId);
        if (forker == null) {
            pLogger.logSevere(this, "ping", "No forker known for isolate ID=",
                    aIsolateId);
            return -1;
        }

        // Prepare the order
        final Map<String, Object> order = new HashMap<String, Object>();
        order.put("isolateId", aIsolateId);

        // Send the order (don't care about the result)
        final int cmdId = sendOrder(forker, IForkerOrders.SIGNAL_PING_ISOLATE,
                order);

        // Wait a second...
        Integer result;
        try {
            result = waitForResult(cmdId, 1000);

        } catch (final InterruptedException e) {
            // Time out
            pLogger.logWarn(this, "ping", "Forker didn't respond in time");
            return -21;
        }

        if (result == null) {
            // No result
            pLogger.logWarn(this, "ping", "Forker didn't respond");
            return -22;
        }

        return result;
    }

    /**
     * Sends an order to the given isolate
     * 
     * @param aIsolate
     *            The target isolate ID
     * @param aOrderName
     *            The order signal name
     * @param aOrder
     *            The order content
     * @return The request ID
     */
    protected int sendOrder(final String aIsolate, final String aOrderName,
            final Map<String, Object> aOrder) {

        // Prepare the command id
        final int cmdId = sCmdId.incrementAndGet();
        aOrder.put(IForkerOrders.CMD_ID, cmdId);

        // Set up the waiter
        final Semaphore waiter = new Semaphore(0);
        pWaiters.put(cmdId, waiter);

        // Send the signal
        pSender.sendData(aIsolate, aOrderName, aOrder);

        return cmdId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#setPlatformStopping()
     */
    @Override
    public void setPlatformStopping() {

        final String[] forkers = pInternalDirectory
                .getIsolates(EEmitterTargets.FORKER);
        if (forkers == null) {
            return;
        }

        for (final String forker : forkers) {
            pSender.sendData(forker, IForkerOrders.SIGNAL_PLATFORM_STOPPING,
                    null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#startIsolate(java.util.Map)
     */
    @Override
    public int startIsolate(final Map<String, Object> aIsolateConfiguration) {

        String hostName = (String) aIsolateConfiguration.get("host");
        if (hostName == null || hostName.isEmpty()) {
            hostName = getHostName();
        }

        final String forker = pInternalDirectory.getForkerForHost(hostName);
        if (forker == null) {
            pLogger.logSevere(this, "startIsolate",
                    "No forker known for host=", hostName);
            return -20;
        }

        // Prepare the order
        final Map<String, Object> order = new HashMap<String, Object>();
        order.put("isolateDescr", aIsolateConfiguration);

        // Associate the isolate to the found forker
        pIsolateForkers.put((String) aIsolateConfiguration.get("id"), forker);

        // Send the order
        final int cmdId = sendOrder(forker, IForkerOrders.SIGNAL_START_ISOLATE,
                order);

        // Wait a second...
        Integer result;
        try {
            result = waitForResult(cmdId, 1000);

        } catch (final InterruptedException e) {
            // Time out
            pLogger.logWarn(this, "startIsolate",
                    "Forker didn't respond in time");
            return -21;
        }

        if (result == null) {
            // No result
            pLogger.logWarn(this, "startIsolate", "Forker didn't respond");
            return -22;
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#stopIsolate(java.lang.String)
     */
    @Override
    public void stopIsolate(final String aIsolateId) {

        final String forker = pIsolateForkers.get(aIsolateId);
        if (forker == null) {
            pLogger.logSevere(this, "stopIsolate",
                    "No forker known for isolate ID=", aIsolateId);
            return;
        }

        // Prepare the order
        final Map<String, Object> order = new HashMap<String, Object>();
        order.put("isolateId", aIsolateId);

        // Send the order (don't care about the result)
        sendOrder(forker, IForkerOrders.SIGNAL_STOP_ISOLATE, order);
    }

    /**
     * Waits for the result of the given command
     * 
     * @param aCmdId
     *            A command ID
     * @param aTimeout
     *            A wait timeout in milliseconds
     * @return The command result, or null
     * @throws InterruptedException
     *             Timeout raised
     */
    protected Integer waitForResult(final int aCmdId, final long aTimeout)
            throws InterruptedException {

        final Semaphore waiter = pWaiters.get(aCmdId);

        if (waiter == null) {
            return pResults.get(aCmdId);
        }

        if (!waiter.tryAcquire(aTimeout, TimeUnit.MILLISECONDS)) {
            // Failed to acquire the semaphore before timeout
            throw new InterruptedException("Timeout raised");
        }

        // Store the result
        final Integer result = pResults.get(aCmdId);

        // Now, we can clean up
        pWaiters.remove(aCmdId);
        pResults.remove(aCmdId);

        return result;
    }
}
