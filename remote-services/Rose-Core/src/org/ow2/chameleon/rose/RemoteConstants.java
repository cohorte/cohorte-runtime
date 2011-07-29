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

import org.osgi.framework.Constants;

/**
 * Commonly used constants in ROSE
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface RemoteConstants {

    /*--------------------------------------------------------*
     *  Distribution Provider Properties                      *
     *  Part of the Remote Services specification (OSGi R4.2) *
     *--------------------------------------------------------*/

    /**
     * Registered by the distribution provider on one of its services to
     * indicate the supported configuration types.
     */
    String REMOTE_CONFIGS_SUPPORTED = "remote.configs.supported";

    String REMOTE_INTENTS_SUPPORTED = "remote.intents.supported";

    /*--------------------------------------------------------*
     *  Properties related to the export                      *
     *  Part of the Remote Services specification (OSGi R4.2) *
     *--------------------------------------------------------*/
    
    String CONFIGS_NODEFAULT = "nodefault";
    
    String SERVICE_EXPORTED_CONFIGS = "service.exported.configs";

    String SERVICE_EXPORTED_INTENTS = "service.exported.intents";

    String SERVICE_EXPORTED_INTENTS_EXTRA = "service.exported.intents.extra";

    String SERVICE_EXPORTED_INTERFACES = "service.exported.interfaces";

    /*--------------------------------------------------------*
     *  Properties related to the import                      *
     *  Part of the Remote Services specification (OSGi R4.2) *
     *--------------------------------------------------------*/

    String SERVICE_IMPORTED = "service.imported";

    String SERVICE_IMPORTED_CONFIGS = "service.imported.configs";

    String SERVICE_INTENTS = "service.intents";

    String SERVICE_PID = "rose.remote.service.id";

    String OBJECTCLASS = Constants.OBJECTCLASS;

    /*---------------------------------------------------------*
     *  Remote Service Properties added                        *
     *  bye the Rose ImportServiceBroker                       *
     *---------------------------------------------------------*/
    
    String SERVICE_IMPORTER_PID = "rose.service.importer.pid";
    
    /*---------------------------------------------------------*
     *  The Discovery and Publication Topic name               *
     *                                                         *
     *---------------------------------------------------------*/
    
    String TOPIC_DISCOVERY_AVAILABLE = "rose/discovery/available";
    
    String TOPIC_DISCOVERY_UNAVAILABLE = "rose/discovery/unavailable";
    
    String TOPIC_DISCOVERY_UPDATED = "rose/discovery/updated";
    
    String TOPIC_PUBLICATION_AVAILABLE = "rose/publication/available";
    
    String TOPIC_PUBLICATION_UNAVAILABLE = "rose/publication/unavailable";
    
    String TOPIC_PUBLICATION_UPDATED = "rose/publication/updated";   
    
    /*---------------------------------------------------------*
     *  Remote Related Intents                                 *
     *                                                         *
     *---------------------------------------------------------*/
    
    String INTENTS_NOPUBLICATION ="confidentiality.nopublication";
    
}
