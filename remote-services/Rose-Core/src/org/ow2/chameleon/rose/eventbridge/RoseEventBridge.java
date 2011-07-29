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

import java.util.Collection;

/**
 * A simple bridge to do a mapping between the OSGi event (with the Event Admin)
 * and an ohter Message protocol (such as JMS, cometd). Then you can simply
 * import a remote topic on the event admin or export an OSGi topic with an
 * other message middleware.
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface RoseEventBridge {

    /**
     * Rose Event Bridge Protocol Name property.
     */
    String PROP_ROSE_EVENTBRIDGE_PROTOCOL_NAME = "rose.eventbridge.protocol.name";

    /**
     * Import an topic from a message oriented middleware to the EventAdmin
     * @param topicname
     * @return key
     */
    public REBKey importTopic(String topicname) throws RoseEventBridgeException;

    /**
     * Import an topic from JMS to the EventAdmin, and specified the OSGi topic
     * target name
     * @param jmstopic the JMS topic to map
     * @param osgitopic the OSGI topic where the jms message are mapped
     * @return key
     */
    public REBKey importTopic(String jmstopic, String osgitopic)
            throws RoseEventBridgeException;

    /**
     * Export a topic from the EventAdmin to a JMS topic
     * @param topicname
     * @return key use to remove the imported topic
     */
    public REBKey exportTopic(String topicname) throws RoseEventBridgeException;

    /**
     * Export a topic from the EventAdmin to a JMS topic, and specified the JMS
     * topic name
     * @param osgitopic name of the OSGi topic
     * @param jmstopic name of the JMS topic
     * @return key use to remove the imported topic
     * @throws RoseEventBridgeException
     */
    public REBKey exportTopic(String osgitopic, String jmstopic)
            throws RoseEventBridgeException;

    /**
     * Remove an Imported topic
     * @param key use to stop the export of this topic
     */
    public void removeImportTopic(REBKey key) throws RoseEventBridgeException;

    /**
     * Remove an Exported topic
     * @param key use to stop the import of this topic
     */
    public void removeExportTopic(REBKey key) throws RoseEventBridgeException;

    /**
     * Get the imported topics
     * @return the enumeration of the imported topics
     */
    public Collection<String> getImportedTopics();

    /**
     * Get the exported topics
     * @return the enumeration of the exported topics
     */
    public Collection<String> getExportedTopics();

}
