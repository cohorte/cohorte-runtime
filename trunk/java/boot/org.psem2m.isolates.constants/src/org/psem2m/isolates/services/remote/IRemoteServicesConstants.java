/**
 * File:   IRemoteServicesConstants.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.isolates.services.remote;

/**
 * Remote Services constants
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServicesConstants {

    /** PSEM2M Force service export */
    String PSEM2M_SERVICE_EXPORT = "psem2m.service.export";

    /** Exported service configurations (OSGi Remote Services) */
    String SERVICE_EXPORTED_CONFIGS = IRemoteServicesConstants.SERVICE_EXPORTED_PREFIX
            + "configs";

    /** Exported service interfaces (OSGi Remote Services) */
    String SERVICE_EXPORTED_INTERFACES = IRemoteServicesConstants.SERVICE_EXPORTED_PREFIX
            + "interfaces";

    /** Service export properties prefix */
    String SERVICE_EXPORTED_PREFIX = "service.exported.";

    /** Imported service flag */
    String SERVICE_IMPORTED = "service.imported";

    /** Imported service configurations */
    String SERVICE_IMPORTED_CONFIGS = "service.imported.configs";
}
