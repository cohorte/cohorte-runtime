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

    /** Exported service configurations (OSGi Remote Services) */
    String SERVICE_EXPORTED_CONFIGS = IRemoteServicesConstants.SERVICE_EXPORTED_PREFIX
            + "configs";

    /** Exported service interfaces (OSGi Remote Services) */
    String SERVICE_EXPORTED_INTERFACES = IRemoteServicesConstants.SERVICE_EXPORTED_PREFIX
            + "interfaces";

    /** Service export properties prefix */
    String SERVICE_EXPORTED_PREFIX = "service.exported.";

    /** Imported service flag (boolean) */
    String SERVICE_IMPORTED = "service.imported";

    /** Imported service configurations (String[]) */
    String SERVICE_IMPORTED_CONFIGS = "service.imported.configs";

    /** ID of the isolate publishing the imported service (String) */
    String SERVICE_IMPORTED_FROM = "service.imported.from";

    /** Exported interfaces synonyms (String[]) */
    String SYNONYM_INTERFACES = "cohorte.remote.synonyms";

    /** Unknown isolate ID constant */
    String UNKNOWN_ISOLATE_ID = "unknown";
}
