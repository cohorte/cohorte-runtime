/**
 * File:   HttpSignalSender.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remotes.signals.http.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.remotes.signals.http.IHttpSignalsConstants;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcastProvider;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalSerializer;
import org.psem2m.signals.InvalidDataException;
import org.psem2m.signals.SignalContent;
import org.psem2m.signals.UnsendableDataException;

/**
 * Implementation of a signal sender. Uses HTTP Sender Thread to do the job.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-sender-http-factory")
@Provides(specifications = ISignalBroadcastProvider.class)
public class HttpSignalSender extends CPojoBase implements
        ISignalBroadcastProvider {

    /** Log service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Signal data serializers */
    @Requires
    private ISignalSerializer[] pSerializers;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "HTTP Signal Sender Gone");
    }

    /**
     * Converts the given ISignalData object to a byte array, ready to be
     * written in a POST request body.
     * 
     * The result map contains only one entry
     * 
     * @param aData
     *            A signal complete content
     * @return Content-Type -&gt; Raw content map
     * @throws UnsendableDataException
     *             The given signal content can't be serialized
     */
    private SignalContent makeRequestBody(final ISignalData aData)
            throws UnsendableDataException {

        // The really sent data
        byte[] sentData = null;

        // The sent data content type
        String contentType = null;

        final SortedMap<Number, ISignalSerializer> pSortedSerializers = new TreeMap<Number, ISignalSerializer>();

        // Find the serializers that can handle this object
        for (final ISignalSerializer serializer : pSerializers) {

            if (serializer.canSerialize(aData)
                    && serializer.canSerialize(aData)) {
                // Valid serializer found
                pSortedSerializers.put(serializer.getPriority(), serializer);
            }
        }

        // Make the conversion
        for (final ISignalSerializer serializer : pSortedSerializers.values()) {

            try {
                sentData = serializer.serializeData(aData);
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

        // Use a map to return the result...
        return new SignalContent(contentType, sentData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalBroadcastProvider#sendSignal(org.psem2m.signals
     * .HostAccess, java.lang.String, java.lang.String,
     * org.psem2m.signals.ISignalData)
     */
    @Override
    public Object[] sendSignal(final HostAccess aAccess, final String aMode,
            final String aSignalName, final ISignalData aData)
            throws UnsendableDataException {

        // Prepare the request content
        final SignalContent content = makeRequestBody(aData);

        // Forge the URL
        final StringBuilder signalUri = new StringBuilder(
                IHttpSignalsConstants.RECEIVER_SERVLET_ALIAS);
        signalUri.append(aSignalName);

        final URL url;
        try {
            url = new URL("http", aAccess.getAddress(), aAccess.getPort(),
                    signalUri.toString());

        } catch (final MalformedURLException ex) {
            pLogger.logSevere(this, "sendSignal",
                    "Error forging URL for access=", aAccess, "signal=",
                    aSignalName, "exception=", ex);
            return null;
        }

        // Open the connection
        HttpURLConnection httpConnection = null;

        try {
            httpConnection = (HttpURLConnection) url.openConnection();

            // POST message
            httpConnection.setRequestMethod("POST");
            httpConnection.setUseCaches(false);
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            // Headers
            httpConnection.setRequestProperty(
                    IHttpSignalsConstants.HEADER_CONTENT_TYPE,
                    content.getType());
            httpConnection.setRequestProperty(
                    IHttpSignalsConstants.HEADER_CONTENT_LENGTH,
                    Integer.toString(content.getLength()));
            httpConnection.setRequestProperty(
                    IHttpSignalsConstants.HEADER_SIGNAL_MODE, aMode);

            // After fields, before content
            httpConnection.connect();

            // Write the event in the request body, if any
            if (content.getContent() != null) {
                final OutputStream outStream = httpConnection.getOutputStream();

                try {
                    outStream.write(content.getContent());
                    outStream.flush();

                } finally {
                    // Always be nice...
                    outStream.close();
                }
            }

            // Flush the request
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                pLogger.logWarn(this, "sendSignal",
                        "Incorrect response for signal=", aSignalName,
                        "access=", aAccess, "code=", responseCode);
            }

            // Get the result content type
            final String resultType = httpConnection
                    .getHeaderField(IHttpSignalsConstants.HEADER_CONTENT_TYPE);

            // Read the result
            final byte[] rawResult = Utilities
                    .inputStreamToBytes(httpConnection.getInputStream());

            // Un-serialize the result
            final Object result = unserializeData(resultType, rawResult);
            if (result != null) {
                if (result instanceof Map) {
                    // We have a map
                    final Object rawResults = ((Map<?, ?>) result)
                            .get("results");
                    if (rawResults instanceof Collection) {
                        // List
                        return ((Collection<?>) rawResults).toArray();

                    } else if (rawResults instanceof Object[]) {
                        // Array
                        return (Object[]) rawResults;

                    } else if (rawResults == null) {
                        // Nothing
                        return null;

                    } else {
                        pLogger.logWarn(this, "sendSignal",
                                "Unknown result type=", rawResults.getClass()
                                        .getName());
                    }

                } else if (result.getClass().isArray()) {
                    // We have an array (old case)
                    return (Object[]) result;
                }
            }

            // Unknown result
            pLogger.logDebug(this, "sendSignal", "Unknown result for signal=",
                    aSignalName, "access=", aAccess, "result=", result);

        } catch (final IOException ex) {
            pLogger.logSevere(this, "sendSignal", "Error sending signal=",
                    aSignalName, "to access=", aAccess, "exception=", ex);

        } finally {
            // Clean up
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * Uses the known serialization services to un-serialize the given data
     * 
     * @param aContentType
     *            Content type
     * @param aData
     *            RAW data
     * @return The un-serialized data, or null
     */
    private Object unserializeData(final String aContentType, final byte[] aData) {

        if (aData == null) {
            // Nothing to do
            return null;
        }

        for (final ISignalSerializer serializer : pSerializers) {

            if (serializer.canHandleType(aContentType)) {
                // Handled content type
                try {
                    return serializer.unserializeData(aData);

                } catch (final InvalidDataException e) {
                    pLogger.logDebug(this, "unserializeData",
                            "Invalid data found with content-type=",
                            aContentType, ":", e.getMessage());
                }
            }
        }

        pLogger.logWarn(this, "unserializeData",
                "Couldn't decode value of content-type=", aContentType);

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

        pLogger.logInfo(this, "validatePojo", "HTTP Signal Sender Ready");
    }
}
