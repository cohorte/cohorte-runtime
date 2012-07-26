/**
 * File:   ISignalSerializer.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2012
 */
package org.psem2m.signals;

/**
 * Constants for serializers
 * 
 * @author Thomas Calmant
 */
public interface ISignalSerializerConstants {

    /** JSON content type */
    String CONTENT_TYPE_JSON = "application/json";

    /** Java Serializable HTTP Content type */
    String CONTENT_TYPE_SERIALIZABLE = "application/x-java-serialized-object";

    /** Default POST content type (url encoded) */
    String DEFAULT_POST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    /** Default error code to return in case of error */
    int HTTP_INTERNAL_ERROR = 505;

    /** Serializer priority service property */
    String PROPERTY_PRIORITY = "psem2m.signals.serializer.priority";
}
