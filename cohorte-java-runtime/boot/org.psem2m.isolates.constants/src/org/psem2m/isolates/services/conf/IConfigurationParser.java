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

package org.psem2m.isolates.services.conf;

import java.util.Collection;
import java.util.Map;

import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Parses configuration files
 *
 * @author Thomas Calmant
 */
public interface IConfigurationParser {

    /**
     * Parses the description of an isolate in the given file
     *
     * @param aFileName
     *            A configuration file
     * @return The description of the isolate
     */
    IsolateConf loadIsolateFile(String aFileName);

    /**
     * Parses the description map of an isolate
     *
     * @param aConfiguration
     *            A configuration map
     * @return The description of the isolate
     */
    IsolateConf loadIsolateMap(Map<String, Object> aConfiguration);

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
}
