/**
 * File:   IHttpSignalsConstants.java
 * Author: Thomas Calmant
 * Date:   20 sept. 2011
 */
package org.psem2m.remotes.signals.http;

/**
 * Common constants for HTTP Sender and Receiver components.
 * 
 * @author Thomas Calmant
 */
public interface IHttpSignalsConstants {

    /** HTTP Header: Content-Length */
    String HEADER_CONTENT_LENGTH = "Content-Length";

    /** HTTP Header: Content-Type */
    String HEADER_CONTENT_TYPE = "Content-Type";

    /** HTTP Header: Signal request mode */
    String HEADER_SIGNAL_MODE = "psem2m-mode";

    /** HTTP Servlet alias */
    String RECEIVER_SERVLET_ALIAS = "/cohorte-signal-receiver";
}
