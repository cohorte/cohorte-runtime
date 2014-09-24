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
package org.cohorte.remote.discovery.signals;

import java.util.Arrays;

import org.cohorte.remote.ExportEndpoint;

/**
 * Represents an endpoint event sent using signals
 * 
 * @author Thomas Calmant
 */
public class EndpointEventBean {

    /** Endpoints */
    private EndpointDescriptionBean[] pEndpoints;

    /** Kind of event */
    private EEndpointEventType pType;

    /**
     * Default constructor, for bean marshaling
     */
    public EndpointEventBean() {

    }

    /**
     * Prepares an endpoint event
     * 
     * @param aType
     *            The endpoint event type
     * @param aEndpoint
     *            An export endpoint
     */
    public EndpointEventBean(final EEndpointEventType aType,
            final ExportEndpoint aEndpoint) {

        pType = aType;
        pEndpoints = new EndpointDescriptionBean[] { new EndpointDescriptionBean(
                aEndpoint) };
    }

    /**
     * Prepares an endpoint event with multiple endpoints
     * 
     * @param aType
     *            The endpoint event type
     * @param aEndpoints
     *            The export endpoints
     */
    public EndpointEventBean(final EEndpointEventType aType,
            final ExportEndpoint[] aEndpoints) {

        // Setup the event
        pType = aType;

        // Prepare endpoints beans
        pEndpoints = new EndpointDescriptionBean[aEndpoints.length];
        for (int i = 0; i < aEndpoints.length; i++) {
            pEndpoints[i] = new EndpointDescriptionBean(aEndpoints[i]);
        }
    }

    /**
     * @return the endpoints
     */
    public EndpointDescriptionBean[] getEndpoints() {

        return Arrays.copyOf(pEndpoints, pEndpoints.length);
    }

    /**
     * @return the type of event
     */
    public EEndpointEventType getType() {

        return pType;
    }

    /**
     * @param aEndpoints
     *            the endpoints to set
     */
    public void setEndpoints(final EndpointDescriptionBean[] aEndpoints) {

        pEndpoints = Arrays.copyOf(aEndpoints, aEndpoints.length);
    }

    /**
     * @param aType
     *            the type of event
     */
    public void setType(final EEndpointEventType aType) {

        pType = aType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "EndpointEvent(" + pType + ", " + Arrays.toString(pEndpoints)
                + ")";
    }
}
