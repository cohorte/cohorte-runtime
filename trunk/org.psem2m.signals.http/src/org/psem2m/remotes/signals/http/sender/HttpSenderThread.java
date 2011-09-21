/**
 * File:   HttpSenderThread.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remotes.signals.http.sender;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP POST request sender
 * 
 * @author Thomas Calmant
 */
public class HttpSenderThread extends Thread {

    /** Data to send in request */
    private Serializable pRequestData;

    /** Target URL */
    private URL pTargetUrl;

    /**
     * Sets up the thread
     * 
     * @param aTargetUrl
     *            Target HTTP URL
     * @param aData
     *            Request content
     */
    public HttpSenderThread(final URL aTargetUrl, final Serializable aData) {

        super();
        pTargetUrl = aTargetUrl;
        pRequestData = aData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        if (pRequestData == null) {
            // Do not send empty signal
            return;
        }

        try {
            // Try to parse the URL and open a connection
            final URLConnection urlConnection = pTargetUrl.openConnection();

            if (urlConnection instanceof HttpURLConnection) {

                // Only handle HTTP streams
                final HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;

                try {
                    // POST message
                    httpConnection.setRequestMethod("POST");
                    httpConnection.setUseCaches(false);
                    httpConnection.setDoInput(true);
                    httpConnection.setDoOutput(true);

                    // Raw content-type
                    httpConnection.setRequestProperty("Content-Type",
                            "application/octet-stream");

                    // After fields, before content
                    httpConnection.connect();

                    // Write the event in the request body, if any
                    final ObjectOutputStream objectStream = new ObjectOutputStream(
                            httpConnection.getOutputStream());

                    objectStream.writeObject(pRequestData);
                    objectStream.flush();
                    objectStream.close();

                    // Flush the request
                    httpConnection.getResponseCode();

                } finally {
                    // In any case, close the connection
                    httpConnection.disconnect();
                }
            }

        } catch (IOException e) {
            // TODO use a logger
            e.printStackTrace();
        }
    }
}
