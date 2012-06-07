/**
 * File:   ForkerAggregator.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.forker.IForkerEventListener;
import org.psem2m.isolates.services.forker.IForkerEventListener.EForkerEventType;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * The forker aggregator
 * 
 * Provides the forker interface and sends signals to real forkers to start/stop
 * isolates.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-forker-aggregator-factory", publicFactory = false)
@Provides(specifications = IForker.class)
@Instantiate(name = "psem2m-forker-aggregator")
public class ForkerAggregator implements IForker, ISignalListener, Runnable {

    /** Maximum time without forker notification : 5 seconds */
    public static final long FORKER_TTL = 5000;

    /** The command ID generator */
    private static final AtomicInteger sCmdId = new AtomicInteger();

    /** Forkers ID -&gt; Last seen time (LST) */
    private final Map<String, Long> pForkersLST = new HashMap<String, Long>();

    /** The forkers directory */
    @Requires
    private IInternalSignalsDirectory pInternalDirectory;

    /** Isolate -&gt; Associated forker map */
    private final Map<String, String> pIsolateForkers = new HashMap<String, String>();

    /** The forker events listeners */
    private final Set<IForkerEventListener> pListeners = new HashSet<IForkerEventListener>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Order results */
    private final Map<Integer, Integer> pResults = new HashMap<Integer, Integer>();

    /** The signal sender */
    @Requires
    private ISignalBroadcaster pSender;

    /** The TTL thread */
    private Thread pThread;

    /** The thread stopper */
    private boolean pThreadRunning = false;

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

        aReceiver.registerListener(IForkerOrders.SIGNAL_MATCH_ALL, this);
    }

    /**
     * Notifies listeners of a forker event
     * 
     * @param aEventType
     *            The kind of event
     * @param aForkerId
     *            The forker isolate ID
     * @param aForkerHost
     *            The forker host
     */
    protected void fireForkerEvent(final EForkerEventType aEventType,
            final String aForkerId, final String aForkerHost) {

        synchronized (pListeners) {
            for (final IForkerEventListener listener : pListeners) {
                try {
                    listener.handleForkerEvent(aEventType, aForkerId,
                            aForkerHost);
                } catch (final Exception e) {
                    // A listener failed
                    pLogger.logSevere(this, "fireForkerEvent",
                            "A forker event listener failed:\n", e);
                }
            }
        }
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

    /**
     * Handles a forker heart beat
     * 
     * @param aSignalData
     *            The heart beat signal data (content and meta data)
     */
    protected void handleHeartBeat(final ISignalData aSignalData) {

        // The current forker ID is the one used to send the signal
        final String forkerId = aSignalData.getIsolateSender();

        // Registration flag
        final boolean needsRegistration;

        synchronized (pForkersLST) {
            // Test if it's a new forker
            needsRegistration = !pForkersLST.containsKey(forkerId);

            // Update the Last Seen Time
            pForkersLST.put(forkerId, System.currentTimeMillis());
        }

        if (needsRegistration) {
            // Register the forker in the internal directory
            registerForker(aSignalData);
        }
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
            // A forker answers a request
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

        } else if (IForkerOrders.SIGNAL_HEART_BEAT.equals(aSignalName)) {
            // A forker heart beat has been received
            handleHeartBeat(aSignalData);
        }
    }

    /**
     * Component invalidation
     */
    @Invalidate
    public void invalidate() {

        // Clear all collections
        pForkersLST.clear();
        pWaiters.clear();
        pResults.clear();
        pIsolateForkers.clear();

        // Wait a second for the thread to stop
        pThreadRunning = false;
        try {
            pThread.join(1000);

        } catch (final InterruptedException e) {
            // Join interrupted
        }

        if (pThread.isAlive()) {
            // Force interruption if necessary
            pThread.interrupt();
        }
        pThread = null;

        pLogger.logInfo(this, "invalidate", "Forker Aggregator invalidated");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#isOnHost(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean isOnHost(final String aForkerId, final String aHostName) {

        if (aForkerId == null) {
            return false;
        }

        return aForkerId.equals(pInternalDirectory.getForkerForHost(aHostName));
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
     * Registers a forker in the internal directory, using a heart beat signal
     * data, and notitfies listeners on success.
     * 
     * @param aSignalData
     *            A heart beat signal data
     */
    protected void registerForker(final ISignalData aSignalData) {

        // Extract the signal content
        final Object rawSignalContent = aSignalData.getSignalContent();
        if (!(rawSignalContent instanceof Map)) {
            // Invalid signal
            pLogger.logWarn(this, "registerForker",
                    "Invalid forker heart beat content");
            return;
        }

        // Cast the signal content
        final Map<?, ?> signalContent = (Map<?, ?>) rawSignalContent;

        // Extract signal data
        final String forkerId = aSignalData.getIsolateSender();
        final String forkerSignalHost = aSignalData.getSenderHostName();
        final String forkerContentHost = (String) signalContent.get("host");
        final Integer forkerContentPort = (Integer) signalContent.get("port");

        // Validate the given host names
        if (forkerSignalHost == null || forkerContentHost == null) {
            pLogger.logSevere(this, "registerForker",
                    "Invalid host name for forker=", forkerId,
                    "found in signal: signal=", forkerSignalHost, "content=",
                    forkerContentHost, "-> Forker not registered");
            return;
        }

        // Get the access port
        int port;
        if (forkerContentPort == null) {
            // Default port
            port = 8080;
            pLogger.logWarn(this, "registerForker",
                    "Port information missing for forker=", forkerId,
                    "-> Using the default port.");

        } else {
            port = forkerContentPort.intValue();
        }

        // Register the alias if different host names given in the signal
        if (!forkerSignalHost.equals(forkerContentHost)) {
            pLogger.logDebug(this, "registerForker",
                    "Different host names found in the heart beat: sender=",
                    forkerSignalHost, "content=", forkerContentHost,
                    "for forker=", forkerId,
                    "-> Using the content host as an alias.");

            pInternalDirectory
                    .setHostAlias(forkerSignalHost, forkerContentHost);
        }

        // Register the forker
        if (pInternalDirectory.addIsolate(forkerId, forkerSignalHost, port)) {

            pLogger.logInfo(this, "registerForker", "Registered forker ID=",
                    forkerId, "Host=", forkerSignalHost, "Port=", port);

            // Notify listeners
            fireForkerEvent(EForkerEventType.REGISTERED, forkerId,
                    forkerSignalHost);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#registerListener(org.psem2m
     * .isolates.services.forker.IForkerEventListener)
     */
    @Override
    public boolean registerListener(final IForkerEventListener aListener) {

        return pListeners.add(aListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        final List<String> toDelete = new ArrayList<String>();

        while (pThreadRunning) {

            synchronized (pForkersLST) {
                for (final Entry<String, Long> entry : pForkersLST.entrySet()) {
                    // First loop to detect forkers to delete
                    final String forkerId = entry.getKey();
                    final long lastSeen = entry.getValue();

                    if (System.currentTimeMillis() - lastSeen > FORKER_TTL) {
                        // TTL reached
                        toDelete.add(forkerId);
                    }
                }

                for (final String forkerId : toDelete) {
                    // Unregister the forker
                    pForkersLST.remove(forkerId);
                    unregisterForker(forkerId);
                }
            }

            toDelete.clear();

            try {
                Thread.sleep(1000);

            } catch (final InterruptedException e) {
                // Interrupted
                return;
            }
        }
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

        final String[] forkers = pInternalDirectory.getForkers();
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
     * Unregisters the given forker and notifies listeners on success
     * 
     * @param aForkerId
     *            The ID of the forker to unregister
     */
    protected synchronized void unregisterForker(final String aForkerId) {

        // Get the forker host
        final String forkerHost = pInternalDirectory
                .getHostForIsolate(aForkerId);

        pLogger.logDebug(this, "unregisterForker", "Unregistering forker=",
                aForkerId, "for host=", forkerHost);

        if (pInternalDirectory.removeIsolate(aForkerId)) {
            // Forker has been removed
            fireForkerEvent(EForkerEventType.UNREGISTERED, aForkerId,
                    forkerHost);
        }

        // Clean up corresponding isolates
        final String[] isolates = pInternalDirectory
                .getIsolatesForHost(forkerHost);
        if (isolates != null) {

            for (final String isolate : isolates) {
                // Forget this isolate
                pIsolateForkers.remove(isolate);
                pInternalDirectory.removeIsolate(isolate);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#registerListener(org.psem2m
     * .isolates.services.forker.IForkerEventListener)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#unregisterListener(org.psem2m
     * .isolates.services.forker.IForkerEventListener)
     */
    @Override
    public boolean unregisterListener(final IForkerEventListener aListener) {

        return pListeners.remove(aListener);
    }

    /** Component validation */
    @Validate
    public void validate() {

        // Start the TTL thread
        pThreadRunning = true;
        pThread = new Thread(this);
        pThread.start();

        pLogger.logInfo(this, "validate", "Forker Aggregator validated");
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
