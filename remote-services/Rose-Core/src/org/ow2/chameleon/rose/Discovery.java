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

/**
 * Discovery registers must implement this interface. This service is registered
 * with extra properties identified at the beginning of this interface to denote
 * the name of the product providing Discovery functionality, its version,
 * vendor, used protocols etc.. Discovery allows to search for remote services.
 * Register a {@link RemoteServiceTracker} service in order to search for remote
 * services.<BR>
 * Discovery service implementations usually rely on some discovery protocols or
 * other information distribution means.
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface Discovery {

    /**
     * Service Registration property for the name of the Discovery product.
     */
    String PROP_KEY_PRODUCT_NAME = "rose.discovery.name";

    /**
     * Service Registration property that lists the discovery protocols used by
     * this Discovery service. Value of this property is of type String+
     */
    String PROP_KEY_SUPPORTED_PROTOCOLS = "rose.discovery.protocols";

}
