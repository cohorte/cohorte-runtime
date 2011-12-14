/**
 * File:   HttpSignalSender.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remotes.signals.http.sender;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcastProvider;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.remotes.signals.http.HttpSignalData;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;

/**
 * Implementation of a signal sender. Uses HTTP Sender Thread to do the job.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-signal-sender-http-factory", publicFactory = false)
@Provides(specifications = ISignalBroadcastProvider.class)
@Instantiate(name = "psem2m-remote-signal-http-sender")
public class HttpSignalSender extends CPojoBase implements
        ISignalBroadcastProvider {

    /** Configuration service, injected by iPOJO */
    @Requires
    private ISvcConfig pConfiguration;

    /** Thread pool */
    private ExecutorService pExecutor;

    /** Log service, injected by iPOJO */
    @Requires(nullable = false)
    private LogService pLogger;

    /**
     * Default constructor
     */
    public HttpSignalSender() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // ...
    }

    /**
     * Retrieves the list of all non-internal isolates, testing whether the ID
     * starts with {@link IPlatformProperties#SPECIAL_ISOLATE_ID_FORKER} or not.
     * 
     * @return All non-internal isolates ID (never null)
     */
    protected String[] getAllIsolates() {

        final List<String> isolatesList = new ArrayList<String>();

        for (final String isolateId : pConfiguration.getApplication()
                .getIsolateIds()) {

            if (isValidIsolate(isolateId)
                    && !isolateId
                            .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
                // The isolate ID doesn't have the internal prefix
                isolatesList.add(isolateId);
            }
        }

        return isolatesList.toArray(new String[0]);
    }

    /**
     * Retrieves the list of all internal isolates, except the forker, testing
     * whether the ID starts with
     * {@link IPlatformProperties#SPECIAL_ISOLATE_ID_FORKER} or not.
     * 
     * @return All monitors isolates ID (never null)
     */
    protected String[] getAllMonitors() {

        final List<String> monitorsList = new ArrayList<String>();

        for (final String isolateId : pConfiguration.getApplication()
                .getIsolateIds()) {

            if (isValidIsolate(isolateId)
                    && isolateId
                            .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {

                monitorsList.add(isolateId);
            }
        }

        return monitorsList.toArray(new String[0]);
    }

    /**
     * Data sending core method. Starts as many threads as URLs to avoid time
     * outs, etc
     * 
     * @param aUrls
     *            Target URLs
     * @param aData
     *            Signal content
     */
    protected void internalSendData(final URL[] aUrls, final Serializable aData) {

        if (aUrls == null) {
            return;
        }

        // Prepare the real signal data object
        final HttpSignalData sentObject = new HttpSignalData(aData);

        for (final URL targetUrl : aUrls) {
            // Use threads to parallelize the sending process
            pExecutor
                    .execute(new HttpSenderThread(this, targetUrl, sentObject));
        }
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

        if (pLogger != null) {
            pLogger.log(LogService.LOG_INFO, "HTTP Signal Sender Gone");
        }
    }

    /**
     * Retrieves the access URL of the given isolate
     * 
     * @param aIsolateId
     *            Target isolate ID
     * @param aSignalName
     *            A signal name
     * @return The isolate access URL, null if not found
     */
    protected URL isolateIdToUrl(final String aIsolateId, final URI aSignalName) {

        final IIsolateDescr isolate = pConfiguration.getApplication()
                .getIsolate(aIsolateId);
        if (isolate == null || aSignalName == null) {
            return null;
        }

        // Construct the URI behind the URL
        final StringBuilder urlBuilder = new StringBuilder(
                isolate.getAccessUrl());

        // Add the servlet alias URI
        urlBuilder.append(IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS);

        // Add the signal name
        urlBuilder.append(aSignalName.normalize());

        try {
            // Return the corresponding URL
            return new URL(urlBuilder.toString());

        } catch (final MalformedURLException e) {
            pLogger.log(
                    LogService.LOG_ERROR,
                    "Can't prepare access URL for isolate '" + aIsolateId + "'",
                    e);
        }

        return null;
    }

    /**
     * Retrieves all computable access URLs corresponding to the given isolates
     * IDs.
     * 
     * @param aIsolatesIds
     *            Isolates IDs
     * @param aSignalName
     *            Signal name
     * @return The corresponding access URLs
     */
    protected URL[] isolatesIdsToUrl(final String[] aIsolatesIds,
            final URI aSignalName) {

        final List<URL> isolateUrls = new ArrayList<URL>(aIsolatesIds.length);

        // Compute isolates URL list
        for (final String isolateId : aIsolatesIds) {
            final URL isolateUrl = isolateIdToUrl(isolateId, aSignalName);
            if (isolateUrl != null) {
                isolateUrls.add(isolateUrl);
            }
        }

        return isolateUrls.toArray(new URL[0]);
    }

    /**
     * Tests if the given isolate ID can be used in a "getAllXXX" method.
     * Returns false if the isolate ID is the current one or the forker one.
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return True if the isolate ID can be used
     */
    protected boolean isValidIsolate(final String aIsolateId) {

        if (aIsolateId == null || aIsolateId.isEmpty()) {
            return false;
        }

        // Isolate ID can change on slave agent order
        final String currentIsolateId = System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);

        return !aIsolateId
                .equals(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)
                && !aIsolateId.equals(currentIsolateId);
    }

    /**
     * Lets the sending thread access to the logger
     * 
     * @param aMessage
     *            Error message
     * @param aException
     *            Cause of the error, if any
     */
    protected void logSenderThreadError(final String aMessage,
            final Exception aException) {

        if (pLogger != null) {
            pLogger.log(LogService.LOG_WARNING, aMessage, aException);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalBroadcaster#sendData
     * (org
     * .psem2m.isolates.services.remote.signals.ISignalEmitter.EEmitterTargets,
     * java.lang.String, java.io.Serializable)
     */
    @Override
    public void sendData(final ISignalBroadcaster.EEmitterTargets aTargets,
            final String aSignalName, final Serializable aData) {

        // Find the URLs corresponding to targets
        final URL[] targetsUrl;
        try {
            targetsUrl = targetToUrl(aTargets, new URI(aSignalName));

        } catch (final URISyntaxException e) {
            pLogger.log(LogService.LOG_ERROR, "Invalid signal name '"
                    + aSignalName + "' for targets '" + aTargets + "'", e);
            return;
        }

        // Send all
        internalSendData(targetsUrl, aData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalBroadcaster#sendData
     * (java .lang.String, java.lang.String, java.io.Serializable)
     */
    @Override
    public boolean sendData(final String aIsolateId, final String aSignalName,
            final Serializable aData) {

        final URL isolateUrl;
        try {
            isolateUrl = isolateIdToUrl(aIsolateId, new URI(aSignalName));

        } catch (final URISyntaxException e) {
            pLogger.log(LogService.LOG_ERROR, "Invalid signal name '"
                    + aSignalName + "' for isolate '" + aIsolateId + "'", e);
            return false;
        }

        if (isolateUrl == null) {
            return false;
        }

        internalSendData(new URL[] { isolateUrl }, aData);
        return true;
    }

    /**
     * Converts a target value into an access URLs array.
     * 
     * @param aTargets
     *            Signal targets
     * @param aSignalName
     *            Signal name
     * @return Corresponding URLs, null on error
     */
    protected URL[] targetToUrl(
            final ISignalBroadcaster.EEmitterTargets aTargets,
            final URI aSignalName) {

        switch (aTargets) {

        case FORKER: {
            // Special case : the forker
            final URL forkerUrl = isolateIdToUrl(
                    IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER, aSignalName);

            if (forkerUrl == null) {
                pLogger.log(LogService.LOG_ERROR,
                        "Can't compute the forker access URL.");
                return null;
            }

            return new URL[] { forkerUrl };
        }

        case ISOLATES:
            // Non-internal isolates
            return isolatesIdsToUrl(getAllIsolates(), aSignalName);

        case MONITORS:
            // All monitors
            return isolatesIdsToUrl(getAllMonitors(), aSignalName);

        case ALL: {
            // Monitors and isolates
            final Set<String> allIsolates = pConfiguration.getApplication()
                    .getIsolateIds();
            final Set<URL> allUrls = new HashSet<URL>(allIsolates.size());

            for (final String isolateId : allIsolates) {

                // Do not include the forker nor the current isolate
                if (isValidIsolate(isolateId)) {

                    final URL isolateUrl = isolateIdToUrl(isolateId,
                            aSignalName);
                    if (isolateUrl != null) {
                        allUrls.add(isolateUrl);
                    }
                }
            }

            return allUrls.toArray(new URL[0]);
        }
        }

        // Unknown target
        return null;
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
        pLogger.log(LogService.LOG_INFO, "HTTP Signal Sender Ready");
    }
}
