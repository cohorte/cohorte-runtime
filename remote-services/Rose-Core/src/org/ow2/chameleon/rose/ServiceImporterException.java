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
package org.ow2.chameleon.rose;

/**
 * A ServiceImporterException occured while a ServiceImporter cannot create,
 * update or destroy a Proxy for a valid RemoteService description
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public class ServiceImporterException extends Exception {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ServiceImporterException() {
        super();
    }

    public ServiceImporterException(String msg) {
        super(msg);
    }

}
