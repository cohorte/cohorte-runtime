/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.services.conf.beans;

import java.io.Serializable;

/**
 * Description of an application
 *
 * @author Thomas Calmant
 */
public class ApplicationDescription implements Serializable {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** The application ID */
    private String pApplicationId;

    /** The multicast address */
    private String pMulticast;

    /** The application multicast port (default: 42000) */
    private int pMulticastPort = 42000;

    /**
     * Default constructor
     */
    public ApplicationDescription() {

        // Do nothing
    }

    /**
     * Configuration constructor
     *
     * @param aApplicationId
     *            The application ID
     */
    public ApplicationDescription(final String aApplicationId) {

        pApplicationId = aApplicationId;
    }

    /**
     * Retrieves the ID of this described application
     *
     * @return The application ID
     */
    public String getApplicationId() {

        return pApplicationId;
    }

    /**
     * Retrieves the application multicast address
     *
     * @return the application multicast address
     */
    public String getMulticastGroup() {

        return pMulticast;
    }

    /**
     * Retrieves the application multicast port
     *
     * @return the application multicast port
     */
    public int getMulticastPort() {

        return pMulticastPort;
    }

    /**
     * @param aApplicationId
     *            the applicationId to set
     */
    public void setApplicationId(final String aApplicationId) {

        pApplicationId = aApplicationId;
    }

    /**
     * Sets up the application multicast address
     *
     * @param aMulticast
     *            the application multicast address
     */
    public void setMulticastGroup(final String aMulticast) {

        pMulticast = aMulticast;
    }

    /**
     * Sets up the application multicast port
     *
     * @param aMulticastPort
     *            the application multicast port
     */
    public void setMulticastPort(final int aMulticastPort) {

        if (aMulticastPort > 0) {
            pMulticastPort = aMulticastPort;
        }
    }
}
