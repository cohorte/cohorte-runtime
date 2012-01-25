/**
 * File:   HttpSignalSender.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remotes.signals.http.sender;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcastProvider;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalDataSerializer;
import org.psem2m.isolates.services.remote.signals.ISignalsDirectory;
import org.psem2m.isolates.services.remote.signals.UnsendableDataException;
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

    /** Signal directory */
    @Requires
    private ISignalsDirectory pDirectory;

    /** Thread pool */
    private ExecutorService pExecutor;

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** Flag to indicate if the logger is accessible or not */
    private AtomicBoolean pLoggerAccessible = new AtomicBoolean(false);

    /** Signal data serializers */
    @Requires
    private ISignalDataSerializer[] pSerializers;

    /**
     * Data sending core method. Starts as many threads as URLs to avoid time
     * outs, etc
     * 
     * @param aUrls
     *            Target URLs
     * @param aData
     *            Signal content
     * @throws UnsendableDataException
     *             The given data can't be sent
     */
    protected void internalSendData(final URL[] aUrls, final Object aData)
            throws UnsendableDataException {

        if (aUrls == null) {
            return;
        }

        // Prepare the real signal data object
        final HttpSignalData sentObject = new HttpSignalData(
                pDirectory.getCurrentIsolateId(), aData);

        // The really sent data
        byte[] sentData = null;

        // The sent data content type
        String contentType = null;

        final SortedMap<Number, ISignalDataSerializer> pSortedSerializers = new TreeMap<Number, ISignalDataSerializer>();

        // Find the serializers that can handle this object
        for (final ISignalDataSerializer serializer : pSerializers) {

            if (serializer.canSerialize(aData)
                    && serializer.canSerialize(sentObject)) {
                // Valid serializer found
                pSortedSerializers.put(serializer.getPriority(), serializer);
            }
        }

        // Make the conversion
        for (final ISignalDataSerializer serializer : pSortedSerializers
                .values()) {

            try {
                sentData = serializer.serializeData(sentObject);
                contentType = serializer.getContentType();

                // We're good
                break;

            } catch (final UnsendableDataException ex) {
                /*
                 * Can't serialize the data with this serializer, do nothing
                 * special and try another one
                 */
            }
        }

        // No serialization done
        if (sentData == null) {
            final StringBuilder builder = new StringBuilder();
            builder.append("No serializer found to prepare a signal with an instance of ");

            if (aData != null) {
                builder.append(aData.getClass());

            } else {
                builder.append("<null>");
            }

            throw new UnsendableDataException(builder.toString());
        }

        for (final URL targetUrl : aUrls) {
            // Use threads to parallelize the sending process
            pExecutor.execute(new HttpSenderThread(this, targetUrl, sentData,
                    contentType));
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

        pLogger.log(LogService.LOG_INFO, "HTTP Signal Sender Gone");
        pLoggerAccessible.set(false);
    }

    /**
     * Joins the isolate access string and the signal name and converts the
     * result to an URL object.
     * 
     * @param aAccessString
     *            An isolate access string
     * @param aSignalName
     *            A signal name
     * @return The corresponding URL, null on error
     */
    protected URL isolateAccessToUrl(final String aAccessString,
            final URI aSignalName) {

        if (aAccessString == null || aSignalName == null) {
            return null;
        }

        // Construct the URI behind the URL
        final StringBuilder urlBuilder = new StringBuilder(aAccessString);

        // Add the servlet alias URI
        urlBuilder.append(IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS);

        // Add the signal name
        urlBuilder.append(aSignalName.normalize());

        try {
            // Return the corresponding URL
            return new URL(urlBuilder.toString());

        } catch (final MalformedURLException e) {
            pLogger.log(LogService.LOG_ERROR, "Can't prepare access URL '"
                    + urlBuilder + "'", e);
        }

        return null;
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

        // Get the access string
        final String isolateAccess = pDirectory.getIsolate(aIsolateId);
        if (isolateAccess == null || isolateAccess.trim().isEmpty()) {
            return null;
        }

        return isolateAccessToUrl(isolateAccess, aSignalName);
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

        if (aIsolatesIds == null) {
            return null; // new URL[0];
        }

        final List<URL> isolateUrls = new ArrayList<URL>(aIsolatesIds.length);

        // Compute isolates URL list
        for (final String isolateId : aIsolatesIds) {
            final URL isolateUrl = isolateIdToUrl(isolateId, aSignalName);
            if (isolateUrl != null) {
                isolateUrls.add(isolateUrl);
            }
        }

        return isolateUrls.toArray(new URL[isolateUrls.size()]);
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

        if (pLoggerAccessible.get()) {
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
            final String aSignalName, final Object aData)
            throws UnsendableDataException {

        // Find the URLs corresponding to targets
        final URL[] targetsUrl;
        try {
            targetsUrl = targetsToUrl(aTargets, new URI(aSignalName));

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
            final Object aData) throws UnsendableDataException {

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
     * Generates the signal URLs for the given predefined targets. Ignores
     * errors of URL generation. Returns null if the parameters are invalid or
     * if no isolate corresponds to the target.
     * 
     * @param aTargets
     *            Predefined targets
     * @param aSignalName
     *            The signal name
     * @return The generated URLs, null on error.
     */
    protected URL[] targetsToUrl(
            final ISignalBroadcaster.EEmitterTargets aTargets,
            final URI aSignalName) {

        if (aTargets == null || aSignalName == null) {
            return null;
        }

        final String[] isolatesAccesses = pDirectory.getIsolates(aTargets);
        if (isolatesAccesses == null) {
            return null;
        }

        final List<URL> isolateUrls = new ArrayList<URL>();

        // Prepare all URls
        for (final String isolateAccess : isolatesAccesses) {

            final URL isolateUrl = isolateAccessToUrl(isolateAccess,
                    aSignalName);
            if (isolateUrl != null) {
                isolateUrls.add(isolateUrl);
            }
        }

        return isolateUrls.toArray(new URL[isolateUrls.size()]);
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

        pLoggerAccessible.set(true);
        pLogger.log(LogService.LOG_INFO, "HTTP Signal Sender Ready");
    }
}
