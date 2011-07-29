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
package org.ow2.chameleon.rose.eventbridge;

import java.util.UUID;

/**
 * The RoseEventBridge Key
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public class REBKey {

    private String osgitopic;

    private String rosetopic;

    private boolean isAnexport;

    private UUID uuid;

    /**
     * Create a new REBKey.
     * @param osgitopic
     * @param rosetopic
     * @param isanexport
     */
    public REBKey(String osgitopic, String rosetopic, Boolean isanexport) {
        this.osgitopic = osgitopic;
        this.rosetopic = rosetopic;
        this.isAnexport = isanexport;
        uuid = UUID.randomUUID();
    }

    /**
     * Get the OSGi name of the topic linked to this key
     * @return osgitopic
     */
    public String getOSGiTopicName() {
        return osgitopic;
    }

    /**
     * Get the Rose name of the topic linked to this key For example if this Key
     * revers to an RoseEventBridge based on JMS, the Rose name of the topic
     * will be the jms topic name.
     * @return rosetopic name
     */
    public String getRoseTopicName() {
        return rosetopic;
    }

    /**
     * Is true if this Key is related to a mapping osgi to rose
     * @return boolean
     */
    public boolean isAnExport() {
        return isAnexport;
    }

    /**
     * Is true if this Key is related to a mapping rose to osgi
     * @return boolean
     */
    public boolean isAnImport() {
        return !isAnexport;
    }

    /**
     * Get the id of this JEBkey
     * @return String
     */
    public String getId() {
        return uuid.toString();
    }
}
