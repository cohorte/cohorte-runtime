/**
 * File:   IRemoteServiceRepository.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.cohorte.remote;

import org.cohorte.remote.beans.RemoteServiceRegistration;

/**
 * Description of an RSR (Remote Service Repository)
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceRepository {

    /**
     * Retrieves all local end points registered to this RSR.
     * 
     * @return All exported service known by this RSR
     */
    RemoteServiceRegistration[] getLocalRegistrations();

    /**
     * Registers an exported service
     * 
     * @param aRegistration
     *            An exported service registration
     * @return True if the registration succeeded
     */
    boolean registerExportedService(RemoteServiceRegistration aRegistration);

    /**
     * Unregisters an exported service
     * 
     * @param aRegistration
     *            An exported service registration
     * @return True if the registration succeeded
     */
    boolean unregisterExportedService(RemoteServiceRegistration aRegistration);
}
