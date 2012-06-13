/**
 * File:   SignalBroadcaster.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcastProvider;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalSendResult;
import org.psem2m.signals.SignalData;
import org.psem2m.signals.SignalResult;

/**
 * Base signal sender logic
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signal-broadcaster-factory", publicFactory = true)
@Provides(specifications = ISignalBroadcaster.class)
@Instantiate(name = "psem2m-signal-broadcaster")
public class SignalBroadcaster extends CPojoBase implements ISignalBroadcaster {

    /** Broadcast providers */
    @Requires(id = "providers")
    private ISignalBroadcastProvider[] pBroadcasters;

    /** A directory service */
    @Requires
    private ISignalDirectory pDirectory;

    /** The thread pool */
    private ExecutorService pExecutor;

    /** Logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Signal receiver (for local only communication) */
    @Requires
    private ISignalReceiver pReceiver;

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
     * @return A SignalSendResult object per reached isolate, null on error
     */
    protected ISignalSendResult commonGroupSignalHandling(
            final String aSignalName, final Object aContent,
            final String aMode, final String... aGroups) {

        final Map<String, HostAccess> accesses = new HashMap<String, HostAccess>();
        for (final String group : aGroups) {
            // Find all accesses
            final Map<String, HostAccess> isolates = pDirectory
                    .getGroupAccesses(group.toLowerCase());
            if (isolates != null) {
                accesses.putAll(isolates);
            }
        }

        if (accesses.isEmpty()) {
            // No known isolate found
            return null;
        }

        // Forge the signal content
        final SignalData signalData = new SignalData();
        signalData.setIsolateId(pDirectory.getIsolateId());
        signalData.setIsolateNode(pDirectory.getIsolateNode());
        signalData.setSignalContent(aContent);
        signalData.setTimestamp(System.currentTimeMillis());

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
    protected ISignalSendResult commonSignalHandling(final String aSignalName,
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
        signalData.setIsolateId(pDirectory.getIsolateId());
        signalData.setIsolateNode(pDirectory.getIsolateNode());
        signalData.setSignalContent(aContent);
        signalData.setTimestamp(System.currentTimeMillis());

        // Send the signal
        return sendLoop(accesses, aSignalName, signalData, aMode);
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
     * @see
     * org.psem2m.signals.impl.ISignalBroadcaster#fireGroup(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public String[] fireGroup(final String aSignalName, final Object aContent,
            final String... aGroups) {

        final ISignalSendResult result = commonGroupSignalHandling(aSignalName,
                aContent, MODE_FORGET, aGroups);

        if (result == null) {
            // Unknown targets
            return null;
        }

        // Only return reached isolates
        return result.getResults().keySet().toArray(new String[0]);
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
    protected Object[] internalSend(final HostAccess aAccess,
            final String aSignalName, final ISignalData aSignalData,
            final String aMode) throws Exception {

        if (aAccess.getAddress() == null && aAccess.getPort() == 0) {
            // Special case : local signal
            final SignalResult localResult = pReceiver.localReception(
                    aSignalName, aSignalData, aMode);

            if (localResult != null) {
                return localResult.getResults();
            }

            return null;
        }

        final List<Object> results = new ArrayList<Object>();
        for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {
            // Call all broadcasters
            final Object[] result = broadcaster.sendSignal(aAccess, aMode,
                    aSignalName, aSignalData);
            if (result != null) {
                results.addAll(Arrays.asList(result));
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

        pExecutor.shutdown();
        pExecutor = null;

        pLogger.logInfo(this, "invalidatePojo", "Base Signal Broadcaster Gone");
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
            public ISignalSendResult call() throws Exception {

                return commonSignalHandling(aSignalName, aContent, MODE_SEND,
                        aIsolates);
            }
        };

        // Submit the operation
        return pExecutor.submit(method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.impl.ISignalBroadcaster#postGroup(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public Future<ISignalSendResult> postGroup(final String aSignalName,
            final Object aContent, final String... aGroups) {

        final Callable<ISignalSendResult> method = new Callable<ISignalSendResult>() {

            @Override
            public ISignalSendResult call() throws Exception {

                return commonGroupSignalHandling(aSignalName, aContent,
                        MODE_SEND, aGroups);
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
     * @see
     * org.psem2m.signals.impl.ISignalBroadcaster#sendGroup(java.lang.String,
     * java.lang.Object, java.lang.String[])
     */
    @Override
    public ISignalSendResult sendGroup(final String aSignalName,
            final Object aContent, final String... aGroups) {

        return commonGroupSignalHandling(aSignalName, aContent, MODE_SEND,
                aGroups);
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
    protected ISignalSendResult sendLoop(
            final Map<String, HostAccess> aAccesses, final String aSignalName,
            final SignalData aSignalData, final String aMode) {

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
            final String aHost, final int aPort) throws Exception {

        // Prepare the signal data
        final SignalData signalData = new SignalData();
        signalData.setIsolateId(pDirectory.getIsolateId());
        signalData.setIsolateNode(pDirectory.getIsolateNode());
        signalData.setSignalContent(aContent);
        signalData.setTimestamp(System.currentTimeMillis());

        // Use all broadcasters
        final List<Object> results = new ArrayList<Object>();
        for (final ISignalBroadcastProvider broadcaster : pBroadcasters) {

            final Object[] result = broadcaster.sendSignal(new HostAccess(
                    aHost, aPort), MODE_SEND, aSignalName, signalData);
            if (result != null) {
                results.addAll(Arrays.asList(result));
            }
        }

        return results.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pExecutor = Executors.newCachedThreadPool();
        pLogger.logInfo(this, "validatePojo", "Base Signal Broadcaster Ready");
    }
}
