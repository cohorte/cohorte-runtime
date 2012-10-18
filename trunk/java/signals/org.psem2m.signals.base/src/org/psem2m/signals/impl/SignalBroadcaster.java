/**
 * File:   SignalBroadcaster.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcastProvider;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalSendResult;
import org.psem2m.signals.IWaitingSignalListener;
import org.psem2m.signals.SignalData;
import org.psem2m.signals.SignalResult;
import org.psem2m.signals.UnsendableDataException;
import org.psem2m.signals.WaitingSignal;

/**
 * Base signal sender logic
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signal-broadcaster-factory", publicFactory = true)
@Provides(specifications = ISignalBroadcaster.class)
@Instantiate(name = "psem2m-signal-broadcaster")
public class SignalBroadcaster extends CPojoBase implements ISignalBroadcaster {

    /** Receivers dependency ID */
    private static final String ID_PROVIDERS = "providers";

    /**
     * Maximum time to wait for the waiting thread when invalidating
     * (milliseconds)
     */
    private static final long TIMEOUT_WAITING_THREAD = 500;

    /** Time to wait before polling the waiting list again (milliseconds) */
    private static final long WAITING_LIST_POLL_INTERVAL = 1000;

    /** Broadcast providers */
    @Requires(id = ID_PROVIDERS, optional = true)
    private ISignalBroadcastProvider[] pBroadcasters;

    /** A directory service */
    @Requires
    private ISignalDirectory pDirectory;

    /** The thread pool */
    private ExecutorService pExecutor;

    /** Logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Number of available providers */
    private int pNbProviders = 0;

    /** On-line service property */
    @ServiceProperty(name = ISignalBroadcaster.PROPERTY_ONLINE, value = "false", mandatory = true)
    private boolean pPropertyOnline;

    /** Signal receiver (for local only communication) */
    @Requires
    private ISignalReceiver pReceiver;

    /** The list of all signals waiting to be sent */
    private final List<WaitingSignal> pWaitingList = new ArrayList<WaitingSignal>();

    /** The thread that handles the waiting queue */
    private Thread pWaitingThread;

    /** The loop control of the waiting queue thread */
    private boolean pWaitingThreadRun;

    /**
     * Method called by iPOJO when a broadcast provider is bound
     * 
     * @param aProvider
     *            The new provider
     */
    @Bind(id = ID_PROVIDERS, aggregate = true)
    protected void bindProvider(final ISignalBroadcastProvider aProvider) {

        // Increase the number of available providers
        pNbProviders++;

        // We're now on-line
        pPropertyOnline = true;
    }

    /**
     * Common code to send a signal to groups : accesses resolution, signal
     * content forge...
     * 
     * @param aSignalName
     *            Name of the signal
     * @param aContent
     *            Signal content
     * @param aMode
     *            Signal request mode
     * @param aGroup
     *            Target directory group
     * @param aExcluded
     *            Excluded isolates
     * @return A SignalSendResult object per reached isolate, null on error
     */
    private ISignalSendResult commonGroupSignalHandling(
            final String aSignalName, final Object aContent,
            final String aMode, final EBaseGroup aGroup,
            final String[] aExcluded) {

        final Map<String, HostAccess> accesses = pDirectory
                .getGroupAccesses(aGroup);

        if (accesses != null && aExcluded != null) {
            // Remove excluded isolates
            for (final String excluded : aExcluded) {
                accesses.remove(excluded);
            }
        }

        if (accesses == null || accesses.isEmpty()) {
            // No known isolate found
            pLogger.logDebug(this, "commonGroupSignalHandling",
                    "No access found for directory group=", aGroup);
            return null;
        }

        // Forge the signal content
        final SignalData signalData = new SignalData();
        signalData.setSenderId(pDirectory.getIsolateId());
        signalData.setSenderNode(pDirectory.getLocalNode());
        signalData.setSignalContent(aContent);

        // Send the signal
        return sendLoop(accesses, aSignalName, signalData, aMode);
    }

    /**
     * Common code to send a signal to groups : accesses resolution, signal
     * content forge...
     * 
     * @param aSignalName
     *            Name of the signal
     * @param aContent
     *            Signal content
     * @param aMode
     *            Signal request mode
     * @param aGroups
     *            Target groups
     * @param aExcluded
     *            Excluded isolates
     * @return A SignalSendResult object per reached isolate, null on error
     */
    private ISignalSendResult commonGroupSignalHandling(
            final String aSignalName, final Object aContent,
            final String aMode, final String[] aGroups, final String[] aExcluded) {

        final Map<String, HostAccess> accesses = new HashMap<String, HostAccess>();
        for (final String group : aGroups) {
            // Find all accesses
            final Map<String, HostAccess> isolates = pDirectory
                    .getGroupAccesses(group.toLowerCase());
            if (isolates != null) {
                accesses.putAll(isolates);
            }
        }

        if (aExcluded != null) {
            // Remove excluded isolates
            for (final String excluded : aExcluded) {
                accesses.remove(excluded);
            }
        }

        if (accesses.isEmpty()) {
            // No known isolate found
            pLogger.logDebug(this, "commonGroupSignalHandling",
                    "No access found for groups=", aGroups);
            return null;
        }

        // Forge the signal content
        final SignalData signalData = new SignalData();
        signalData.setSenderId(pDirectory.getIsolateId());
        signalData.setSenderNode(pDirectory.getLocalNode());
        signalData.setSignalContent(aContent);

        // Send the signal
        return sendLoop(accesses, aSignalName, signalData, aMode);
    }

    /**
     * Common code to send a signal : accesses resolution, signal content
     * forge...
     * 
     * @param aSignalName
     *            Name of the signal
     * @param aContent
     *            Signal content
     * @param aMode
     *            Signal request mode
     * @param aIsolates
     *            Target isolates
     * @return A SignalSendResult object per reached isolate, null on error
     */
    private ISignalSendResult commonSignalHandling(final String aSignalName,
            final Object aContent, final String aMode,
            final String... aIsolates) {

        final Map<String, HostAccess> accesses = new HashMap<String, HostAccess>();
        for (final String isolate : aIsolates) {
            // Find all accesses
            final HostAccess access = pDirectory.getIsolateAccess(isolate);
            if (access != null) {
                accesses.put(isolate, access);
            }
        }

        if (accesses.isEmpty()) {
            // No known isolate found
            return null;
        }

        // Forge the signal content
        final SignalData signalData = new SignalData();
        signalData.setSenderId(pDirectory.getIsolateId());
        signalData.setSenderNode(pDirectory.getLocalNode());
        signalData.setSignalContent(aContent);

        // Send the signal
        return sendLoop(accesses, aSignalName, signalData, aMode);
    }

    /**
     * Common code to add a signal to the waiting list
     * 
     * @param aWaitingSignal
     *            The waiting signal...
     * @return A future signal result
     */
    private boolean commonStackHandling(final WaitingSignal aWaitingSignal) {

        if (aWaitingSignal == null) {
            return false;
        }

        synchronized (pWaitingList) {
            // Add the
            pWaitingList.add(aWaitingSignal);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalBroadcaster#fire(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public String[] fire(final String aSignalName, final Object aContent,
            final String... aIsolates) {

        final ISignalSendResult result = commonSignalHandling(aSignalName,
                aContent, MODE_FORGET, aIsolates);
        if (result == null) {
            // Unknown targets
            return null;
        }

        // Only return reached isolates
        return result.getResults().keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#fireGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.ISignalDirectory.EBaseGroup)
     */
    @Override
    public String[] fireGroup(final String aSignalName, final Object aContent,
            final EBaseGroup aGroup) {

        return fireGroup(aSignalName, aContent, aGroup, (String[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#fireGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.ISignalDirectory.EBaseGroup,
     * java.lang.String[])
     */
    @Override
    public String[] fireGroup(final String aSignalName, final Object aContent,
            final EBaseGroup aGroup, final String... aExcluded) {

        final ISignalSendResult result = commonGroupSignalHandling(aSignalName,
                aContent, MODE_FORGET, aGroup, aExcluded);
        if (result == null) {
            // Unknown targets
            return null;
        }

        final Map<String, Object[]> results = result.getResults();
        if (results == null || results.isEmpty()) {
            // No valid results
            return null;
        }

        // Only return reached isolates
        return results.keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#fireGroup(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public String[] fireGroup(final String aSignalName, final Object aContent,
            final String... aGroups) {

        return fireGroup(aSignalName, aContent, aGroups, (String[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#fireGroup(java.lang.String,
     * java.lang.Object, java.lang.String[], java.lang.String[])
     */
    @Override
    public String[] fireGroup(final String aSignalName, final Object aContent,
            final String[] aGroups, final String[] aExcluded) {

        final ISignalSendResult result = commonGroupSignalHandling(aSignalName,
                aContent, MODE_FORGET, aGroups, aExcluded);
        if (result == null) {
            // Unknown targets
            return null;
        }

        // Only return reached isolates
        return result.getResults().keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#fireTo(java.lang.String,
     * java.lang.Object, java.lang.String, int)
     */
    @Override
    public boolean fireTo(final String aSignalName, final Object aContent,
            final String aHost, final int aPort) {

        // Prepare the signal data
        final SignalData signalData = new SignalData();
        signalData.setSenderId(pDirectory.getIsolateId());
        signalData.setSenderNode(pDirectory.getLocalNode());
        signalData.setSignalContent(aContent);

        if (pBroadcasters.length == 0) {
            pLogger.logWarn(this, "fireTo", "No signals broadcasters yet");
            return false;
        }

        // Use all broadcasters
        final HostAccess access = new HostAccess(aHost, aPort);
        for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {

            try {
                if (broadcaster.sendSignal(access, MODE_FORGET, aSignalName,
                        signalData) != null) {
                    // Success
                    return true;
                }

            } catch (final UnsendableDataException ex) {
                pLogger.logWarn(this, "fireTo", "Couldn't fire signal=",
                        aSignalName, ":", ex);
            }
        }

        return false;
    }

    /**
     * Handles a waiting signal using the FIRE mode
     * 
     * @param aSignal
     *            A waiting signal
     * @return True if the signal has been handled correctly
     */
    private boolean handleWaitingFire(final WaitingSignal aSignal) {

        final String name = aSignal.getName();
        final Object content = aSignal.getContent();
        final HostAccess access = aSignal.getAccess();
        final String[] isolates = aSignal.getIsolates();
        final EBaseGroup baseGroup = aSignal.getGroup();
        final String[] groups = aSignal.getGroups();

        if (access != null) {
            // fireTo: no result, as we don't event know the isolate ID
            return fireTo(name, content, access.getAddress(), access.getPort());

        } else {
            // fire, fireGroup
            String[] result = null;
            if (isolates != null) {
                // Fire
                result = fire(name, content, isolates);

            } else if (baseGroup != null) {
                // Send group
                result = fireGroup(name, content, baseGroup);

            } else if (groups != null) {
                // Fire group
                result = fireGroup(name, content, groups);
            }

            if (result != null) {
                // Success
                aSignal.setFireResult(result);
                return true;
            }
        }

        return false;
    }

    /**
     * Handles a waiting signal using the POST mode
     * 
     * @param aSignal
     *            A waiting signal
     * @return True if the signal has been handled correctly
     */
    private boolean handleWaitingPost(final WaitingSignal aSignal) {

        final String name = aSignal.getName();
        final Object content = aSignal.getContent();
        final HostAccess access = aSignal.getAccess();
        final String[] isolates = aSignal.getIsolates();
        final EBaseGroup baseGroup = aSignal.getGroup();
        final String[] groups = aSignal.getGroups();

        if (access != null) {
            // postTo
            final Future<Object[]> result = postTo(name, content,
                    access.getAddress(), access.getPort());
            if (result != null) {
                aSignal.setPostToResult(result);
                return true;
            }

        } else {
            // postTo, postGroup
            Future<ISignalSendResult> result = null;
            if (isolates != null) {
                // Post
                result = post(name, content, isolates);

            } else if (baseGroup != null) {
                // Directory group
                result = postGroup(name, content, baseGroup);

            } else if (groups != null) {
                // Group
                result = postGroup(name, content, groups);
            }

            if (result != null) {
                // Success
                aSignal.setPostResult(result);
                return true;
            }
        }

        return false;
    }

    /**
     * Handles a waiting signal using the SEND mode
     * 
     * @param aSignal
     *            A waiting signal
     * @return True if the signal has been handled correctly
     */
    private boolean handleWaitingSend(final WaitingSignal aSignal) {

        final String name = aSignal.getName();
        final Object content = aSignal.getContent();
        final HostAccess access = aSignal.getAccess();
        final String[] isolates = aSignal.getIsolates();
        final EBaseGroup baseGroup = aSignal.getGroup();
        final String[] groups = aSignal.getGroups();

        if (access != null) {
            // Send to
            Object[] results = null;
            try {
                results = sendTo(name, content, access.getAddress(),
                        access.getPort());

            } catch (final Exception e) {
                // Ignore errors (try later)
                pLogger.logDebug(this, "handleWaitingSend",
                        "Error trying to send signal=", name, "to=", access, e);
            }

            if (results != null) {
                // Success
                aSignal.setSendToResult(results);
                return true;
            }

        } else {
            ISignalSendResult result = null;
            if (isolates != null) {
                // Send
                result = send(name, content, isolates);

            } else if (baseGroup != null) {
                // Send group
                result = sendGroup(name, content, baseGroup);

            } else if (groups != null) {
                // Send group
                result = sendGroup(name, content, groups, null);
            }

            if (result != null) {
                // Success
                aSignal.setSendResult(result);
                return true;
            }
        }

        return false;
    }

    /**
     * Sends a signal from the waiting list
     * 
     * @param aSignal
     *            A waiting signal
     * @return True if the signal has been sent by at least one isolate
     */
    private boolean handleWaitingSignal(final WaitingSignal aSignal) {

        switch (aSignal.getMode()) {
        case SEND:
            // send, sendGroup, sendTo
            return handleWaitingSend(aSignal);

        case POST:
            // post, postGroup, postTo
            return handleWaitingPost(aSignal);

        case FIRE:
            // fire, fireGroup, fireTo
            return handleWaitingFire(aSignal);

        default:
            // Unknown type
            return false;
        }
    }

    /**
     * Handles the signal sender using the signal broadcaster.
     * 
     * @param aAccess
     *            The access to send the signal to
     * @param aSignalName
     *            The signal name
     * @param aSignalData
     *            The complete signal content
     * @param aMode
     *            The request mode
     * @return A result array, may be null
     * @throws Exception
     *             Something wrong happened
     */
    private Object[] internalSend(final HostAccess aAccess,
            final String aSignalName, final ISignalData aSignalData,
            final String aMode) {

        if (ISignalDirectory.LOCAL_ACCESS.equals(aAccess)) {
            // Special case : signal for the local isolate
            final SignalResult localResult = pReceiver.localReception(
                    aSignalName, aSignalData, aMode);

            if (localResult != null) {
                return localResult.getResults();
            }

            return null;
        }

        if (pBroadcasters.length == 0) {
            pLogger.logWarn(this, "internalSend", "No signals broadcasters yet");
            return null;
        }

        final List<Object> results = new ArrayList<Object>();
        for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {
            // Call all broadcasters
            try {
                final Object[] result = broadcaster.sendSignal(aAccess, aMode,
                        aSignalName, aSignalData);
                if (result != null) {
                    results.addAll(Arrays.asList(result));
                }

            } catch (final UnsendableDataException ex) {
                pLogger.logWarn(this, "internalSend",
                        "Couldn't send data for signal=", aSignalName, ":", ex);
            }
        }

        return results.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Stop the sending thread
        pExecutor.shutdownNow();
        pExecutor = null;

        // Stop the waiting queue thread
        pWaitingThreadRun = false;

        synchronized (pWaitingList) {
            // Clear the list
            pWaitingList.clear();
        }

        // Wait for the thread
        try {
            pWaitingThread.join(TIMEOUT_WAITING_THREAD);

        } catch (final InterruptedException ex) {
            // Ignore
        }

        // Interrupt it if necessary
        pWaitingThread.interrupt();
        pWaitingThread = null;

        pLogger.logInfo(this, "invalidatePojo", "Base Signal Broadcaster Gone");
    }

    /**
     * Tests if the broadcaster is on line.
     * 
     * @return True if the broadcaster is on-line
     */
    public boolean isOnline() {

        return pPropertyOnline;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalBroadcaster#post(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public Future<ISignalSendResult> post(final String aSignalName,
            final Object aContent, final String... aIsolates) {

        final Callable<ISignalSendResult> method = new Callable<ISignalSendResult>() {

            @Override
            public ISignalSendResult call() {

                // Re-use existing code...
                return send(aSignalName, aContent, aIsolates);
            }
        };

        // Submit the operation
        return pExecutor.submit(method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#postGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.ISignalDirectory.EBaseGroup)
     */
    @Override
    public Future<ISignalSendResult> postGroup(final String aSignalName,
            final Object aContent, final EBaseGroup aGroup) {

        final Callable<ISignalSendResult> method = new Callable<ISignalSendResult>() {

            @Override
            public ISignalSendResult call() {

                // Re-use existing code...
                return sendGroup(aSignalName, aContent, aGroup);
            }
        };

        // Submit the operation
        return pExecutor.submit(method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#postGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.ISignalDirectory.EBaseGroup,
     * java.lang.String[])
     */
    @Override
    public Future<ISignalSendResult> postGroup(final String aSignalName,
            final Object aContent, final EBaseGroup aGroup,
            final String... aExcluded) {

        return postGroup(aSignalName, aContent, aGroup, (String[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#postGroup(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public Future<ISignalSendResult> postGroup(final String aSignalName,
            final Object aContent, final String... aGroups) {

        return postGroup(aSignalName, aContent, aGroups, (String[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#postGroup(java.lang.String,
     * java.lang.Object, java.lang.String[], java.lang.String[])
     */
    @Override
    public Future<ISignalSendResult> postGroup(final String aSignalName,
            final Object aContent, final String[] aGroups,
            final String[] aExcluded) {

        final Callable<ISignalSendResult> method = new Callable<ISignalSendResult>() {

            @Override
            public ISignalSendResult call() {

                // Re-use existing code...
                return sendGroup(aSignalName, aContent, aGroups, aExcluded);
            }
        };

        // Submit the operation
        return pExecutor.submit(method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#postTo(java.lang.String,
     * java.lang.Object, java.lang.String, int)
     */
    @Override
    public Future<Object[]> postTo(final String aSignalName,
            final Object aContent, final String aHost, final int aPort) {

        final Callable<Object[]> method = new Callable<Object[]>() {

            @Override
            public Object[] call() {

                // Use existing code...
                try {
                    return sendTo(aSignalName, aContent, aHost, aPort);

                } catch (final UnsendableDataException ex) {
                    pLogger.logWarn(this, "postTo",
                            "Couldn't send data for signal=", aSignalName, ":",
                            ex);
                }

                return null;
            }
        };

        // Submit the operation
        return pExecutor.submit(method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalBroadcaster#send(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public ISignalSendResult send(final String aSignalName,
            final Object aContent, final String... aIsolates) {

        return commonSignalHandling(aSignalName, aContent, MODE_SEND, aIsolates);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#sendGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.ISignalDirectory.EBaseGroup)
     */
    @Override
    public ISignalSendResult sendGroup(final String aSignalName,
            final Object aContent, final EBaseGroup aGroup) {

        return sendGroup(aSignalName, aContent, aGroup, (String[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#sendGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.ISignalDirectory.EBaseGroup,
     * java.lang.String[])
     */
    @Override
    public ISignalSendResult sendGroup(final String aSignalName,
            final Object aContent, final EBaseGroup aGroup,
            final String... aExcluded) {

        return commonGroupSignalHandling(aSignalName, aContent, MODE_SEND,
                aGroup, aExcluded);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#sendGroup(java.lang.String,
     * java.lang.Object, java.lang.String[], java.lang.String[])
     */
    @Override
    public ISignalSendResult sendGroup(final String aSignalName,
            final Object aContent, final String[] aGroups,
            final String[] aExcluded) {

        return commonGroupSignalHandling(aSignalName, aContent, MODE_SEND,
                aGroups, aExcluded);
    }

    /**
     * The main send loop
     * 
     * @param aAccesses
     *            Isolates to access
     * @param aSignalName
     *            Signal name
     * @param aSignalData
     *            Complete signal data
     * @param aMode
     *            Request mode
     * @return The result of each isolate and failed isolates
     */
    private ISignalSendResult sendLoop(final Map<String, HostAccess> aAccesses,
            final String aSignalName, final SignalData aSignalData,
            final String aMode) {

        // Prepare results storage
        final Map<String, Object[]> results = new HashMap<String, Object[]>();
        final List<String> failed = new ArrayList<String>();

        // Loop on each access
        for (final Entry<String, HostAccess> entry : aAccesses.entrySet()) {
            final String isolateId = entry.getKey();
            final HostAccess access = entry.getValue();

            try {
                final Object[] result = internalSend(access, aSignalName,
                        aSignalData, aMode);
                results.put(isolateId, result);

            } catch (final Exception ex) {
                pLogger.logWarn(this, "sendLoop", "Error sending signal=",
                        aSignalName, "to id=", isolateId, "access=", access);

                failed.add(isolateId);
            }
        }

        return new SignalSendResult(results, failed.toArray(new String[0]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalBroadcaster#sendTo(java.lang.String,
     * java.lang.Object, java.lang.String, int)
     */
    @Override
    public Object[] sendTo(final String aSignalName, final Object aContent,
            final String aHost, final int aPort) throws UnsendableDataException {

        // Prepare the signal data
        final SignalData signalData = new SignalData();
        signalData.setSenderId(pDirectory.getIsolateId());
        signalData.setSenderNode(pDirectory.getLocalNode());
        signalData.setSignalContent(aContent);

        if (pBroadcasters.length == 0) {
            pLogger.logWarn(this, "internalSend", "No signals broadcasters yet");
            return null;
        }

        // Use all broadcasters
        final HostAccess access = new HostAccess(aHost, aPort);
        final List<Object> results = new ArrayList<Object>();
        for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {

            final Object[] result = broadcaster.sendSignal(access, MODE_SEND,
                    aSignalName, signalData);
            if (result != null) {
                results.addAll(Arrays.asList(result));
            }
        }

        return results.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#stack(java.lang.String,
     * java.lang.Object, org.psem2m.signals.IWaitingSignalListener,
     * org.psem2m.signals.ISignalBroadcaster.ESendMode, long,
     * org.psem2m.signals.HostAccess)
     */
    @Override
    public boolean stack(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final HostAccess aAccess) {

        return commonStackHandling(new WaitingSignal(aSignalName, aContent,
                aListener, aMode, aTTL, aAccess));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#stack(java.lang.String,
     * java.lang.Object, org.psem2m.signals.IWaitingSignalListener,
     * org.psem2m.signals.ISignalBroadcaster.ESendMode, long,
     * java.lang.String[])
     */
    @Override
    public boolean stack(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final String... aIsolates) {

        return commonStackHandling(new WaitingSignal(aSignalName, aContent,
                aListener, aMode, aTTL, aIsolates, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#stackGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.IWaitingSignalListener,
     * org.psem2m.signals.ISignalBroadcaster.ESendMode, long,
     * org.psem2m.signals.ISignalDirectory.EBaseGroup)
     */
    @Override
    public boolean stackGroup(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final EBaseGroup aGroup) {

        return commonStackHandling(new WaitingSignal(aSignalName, aContent,
                aListener, aMode, aTTL, aGroup));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalBroadcaster#stackGroup(java.lang.String,
     * java.lang.Object, org.psem2m.signals.IWaitingSignalListener,
     * org.psem2m.signals.ISignalBroadcaster.ESendMode, long,
     * java.lang.String[])
     */
    @Override
    public boolean stackGroup(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final String... aGroups) {

        return commonStackHandling(new WaitingSignal(aSignalName, aContent,
                aListener, aMode, aTTL, null, aGroups));
    }

    /**
     * Called by iPOJO when a broadcast provider is gone
     * 
     * @param aProvider
     *            A broadcast provider service
     */
    @Unbind(id = ID_PROVIDERS, aggregate = true)
    protected void unbindProvider(final ISignalBroadcastProvider aProvider) {

        // Decrease the number of available providers
        pNbProviders--;

        if (pNbProviders == 0) {
            // No more provider, we're not on-line anymore
            pPropertyOnline = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Start the signal sender thread pool
        pExecutor = Executors.newCachedThreadPool();

        // Start the waiting queue thread
        pWaitingThread = new Thread(new Runnable() {

            @Override
            public void run() {

                waitingListThread();
            }
        }, "broadcaster-waiting-list");

        pWaitingThreadRun = true;
        pWaitingThread.start();

        pLogger.logInfo(this, "validatePojo", "Base Signal Broadcaster Ready");
    }

    /**
     * Waiting list handling loop
     */
    private void waitingListThread() {

        while (pWaitingThreadRun) {

            synchronized (pWaitingList) {
                // Use an iterator, do be able to remove items during the loop
                final Iterator<WaitingSignal> iter = pWaitingList.iterator();

                while (iter.hasNext()) {
                    final WaitingSignal signal = iter.next();
                    if (handleWaitingSignal(signal)) {
                        // Signal sent
                        iter.remove();

                        // Call back the owner
                        signal.fireSuccessEvent();

                    } else if (signal.decreaseTTL(1)) {
                        // TTL expired
                        iter.remove();

                        // Call back the owner
                        signal.fireTimeoutEvent();
                    }
                }
            }

            try {
                Thread.sleep(WAITING_LIST_POLL_INTERVAL);

            } catch (final InterruptedException ex) {
                // Thread interrupted, go away
                return;
            }
        }
    }
}
