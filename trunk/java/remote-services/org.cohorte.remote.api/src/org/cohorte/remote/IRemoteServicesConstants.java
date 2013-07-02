/**
 * File:   IRemoteServicesConstants.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.cohorte.remote;

/**
 * Remote Services constants
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServicesConstants {

    /** COHORTE: Force/Refuse service export */
    String COHORTE_SERVICE_EXPORT = "cohorte.service.export";

    /**
     * Remote Service Importer exclusion filter
     * 
     * Default : "" Format : "filter1,filter2"
     */
    String FILTERS_EXCLUDE = "org.cohorte.remote.filters.exclude";

    /**
     * Remote Service Importer inclusion filter
     * 
     * Default : "*" Format : "filter1,filter2"
     */
    String FILTERS_INCLUDE = "org.cohorte.remote.filters.include";

    /**
     * Contains the isolate Unique IDentifier, a String.
     * 
     * This might be a Framework property (read-only), or a generated System
     * property.
     */
    String ISOLATE_UID = "cohorte.isolate.uid";

    /** Imported service flag (boolean) */
    String SERVICE_IMPORTED = "service.imported";

    /** ID of the isolate publishing the imported service (String) */
    String SERVICE_IMPORTED_FROM = "service.imported.from";

    /** Exported interfaces synonyms (String[]) */
    String SYNONYM_INTERFACES = "cohorte.remote.synonyms";

    /** Unknown isolate ID constant */
    String UNKNOWN_ISOLATE_ID = "unknown";
}
