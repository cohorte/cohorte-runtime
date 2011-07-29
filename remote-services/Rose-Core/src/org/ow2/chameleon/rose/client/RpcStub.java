/*
 * Copyright 2009 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.chameleon.rose.client;


/**
 * Part of the ROSE communication framework. This stub allow to call a
 * RemoteService in an RPC way. This stub must be created through a
 * RoseRpcStubFactory. Idealy, this stub must be put in a decorator in order to
 * provide an OSGi service.
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public interface RpcStub {

    /**
     * Performs a request with the given method name and parameters.
     * @param pMethodName
     * @param pParams
     * @return result of the call
     */
    Object call(String pMethodName, Object[] pParams) throws RemoteClientException;

    /**
     * @return The Endpoint URI.
     */
    String getEndpointURI();
}
