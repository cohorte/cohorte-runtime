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

package org.cohorte.pyboot.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a Python-side boot orders service
 *
 * @author Thomas Calmant
 */
public interface IPyBridge {

    /**
     * Logs the given message at debug level
     *
     * @param aMessage
     *            A message
     * @param aValues
     *            Values to inject in the message
     */
    void debug(String aMessage, String... aValues);

    /**
     * Logs the given message at error level
     *
     * @param aMessage
     *            A message
     * @param aValues
     *            Values to inject in the message
     */
    void error(String aMessage, String... aValues);

    /**
     * Retrieves the components to instantiate
     *
     * @return An array of components, or null
     */
    List<ComponentBean> getComponents();

    /**
     * Retrieves the process ID (PID) of this isolate
     *
     * @return the isolate PID
     */
    int getPid();

    /**
     * Returns the port used by the Pelix remote shell, or -1 if the shell is
     * not active
     *
     * @return the port used by the Pelix remote shell or -1
     */
    int getRemoteShellPort();

    /**
     * Retrieves the configuration used to start this isolate as a map
     *
     * @return The configuration used to start this isolate
     */
    Map<String, Object> getStartConfiguration();

    /**
     * Called when a component has been started
     *
     * @param aName
     *            Name of the component
     */
    void onComponentStarted(String aName);

    /**
     * Called when an error has occurred
     *
     * @param aError
     *            An error message
     */
    void onError(String aError);

    /**
     * Prepares the configuration dictionary of an isolate
     *
     * @param aUID
     *            Isolate UID
     * @param aName
     *            Isolate name
     * @param aNode
     *            Isolate node
     * @param aKind
     *            Isolate kind
     * @param aLevel
     *            Level of configuration (boot, Java, Python, ...)
     * @param aSubLevel
     *            Category of configuration (monitor, isolate, ...)
     * @param aBundles
     *            Isolate bundles
     * @param aComposition
     *            Isolate components
     * @return The configuration dictionary
     */
    Map<String, Object> prepareIsolate(String aUID, String aName, String aNode,
            String aKind, String aLevel, String aSubLevel,
            Collection<Map<String, Object>> aBundles,
            Collection<Map<String, Object>> aComposition);

    /**
     * Reads the given configuration file
     *
     * @param aFileName
     *            A configuration file name
     * @return The parsed configuration map
     */
    Map<String, Object> readConfiguration(String aFileName);
}
