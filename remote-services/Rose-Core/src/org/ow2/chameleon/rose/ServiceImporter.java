/*
 * Copyright 2009 OW2 Chameleon Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.ow2.chameleon.rose;

import org.ow2.chameleon.rose.disco.RemoteEntityDescriptionManager;




/**
 * Components which provides this service handle the import of a Remote Service.
 * Generally a Proxy is created for the remote service, in order to import it.
 * The ServiceImporter manage the proxy corresponding to the Remote Service
 * Description.
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface ServiceImporter {

    /**
     * Return true if this ServiceImporter is able to create a proxy for The
     * remoteService of the given description.
     * @param description
     * @return true if match , false else
     * @throws NullPointerException if parameter null
     */
    boolean match(RemoteEntityDescriptionManager pDescription) throws NullPointerException;

    /**
     * True if this ServiceImporter has create the Proxy of the given
     * service.pid
     * @param pid
     * @return true if managed the RemoteService of Pid pid
     * @throws NullPointerException if parameter null
     */
    boolean managedProxy(String pid) throws NullPointerException;
    
    /**
     * 
     * @param pDescription
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     */
    void createProxy(RemoteEntityDescriptionManager pDescription) throws ServiceImporterException,IllegalArgumentException;

    /**
     * 
     * @param pNewDescription
     * @throws ServiceImporterException
     * @throws IllegalArgumentException
     */
    void updateProxy(RemoteEntityDescriptionManager pNewDescription) throws ServiceImporterException,IllegalArgumentException;

    /**
     * 
     * @param pPid
     * @throws ServiceImporterException
     * @throws IllegalArgumentException
     */
    void destroyProxy(String pPid) throws ServiceImporterException,IllegalArgumentException;

}
