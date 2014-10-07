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

package org.cohorte.pyboot.config;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.cohorte.pyboot.api.IPyBridge;
import org.psem2m.isolates.services.conf.IConfigurationParser;
import org.psem2m.isolates.services.conf.beans.BundleConf;
import org.psem2m.isolates.services.conf.beans.ComponentConf;
import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Calls the bridge core to retrieve configuration and converts it to Java beans
 *
 * @author Thomas Calmant
 */
@Component(name = "cohorte-pyboot-configuration-parser-factory")
@Provides(specifications = IConfigurationParser.class)
public class ConfigurationParser implements IConfigurationParser {

    /** The Python bridge */
    @Requires
    private IPyBridge pBridge;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.services.conf.IConfigurationParser#loadIsolateFile
     * (java.lang.String)
     */
    @Override
    public IsolateConf loadIsolateFile(final String aFileName) {

        // Call the PyBridge to read the file
        final Map<String, Object> configurationMap = pBridge
                .readConfiguration(aFileName);

        // Convert the map into a bean
        return loadIsolateMap(configurationMap);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.services.conf.IConfigurationParser#loadIsolateMap
     * (java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public IsolateConf loadIsolateMap(final Map<String, Object> aConfiguration) {

        final IsolateConf isolate = new IsolateConf(
                (String) aConfiguration.get("uid"),
                (String) aConfiguration.get("name"),
                (String) aConfiguration.get("node"),
                (String) aConfiguration.get("kind"),
                (String) aConfiguration.get("level"),
                (String) aConfiguration.get("sublevel"));

        // Convert bundles
        final Collection<Map<String, Object>> rawBundles = (Collection<Map<String, Object>>) aConfiguration
                .get("bundles");
        final List<BundleConf> bundles = new LinkedList<BundleConf>();
        for (final Map<String, Object> rawBundle : rawBundles) {

            final BundleConf bundle = new BundleConf(
                    (String) rawBundle.get("name"),
                    (String) rawBundle.get("version"), false);

            // null doesn't like to be converted to boolean
            final Boolean optional = (Boolean) rawBundle.get("optional");
            if (optional != null) {
                bundle.setOptional(optional);
            }

            bundle.setFilename((String) rawBundle.get("filename"));
            bundle.setProperties((Map<String, Object>) rawBundle
                    .get("properties"));

            // Store the bundle
            bundles.add(bundle);
        }
        isolate.setBundles(bundles);

        // Convert components
        final Collection<Map<String, Object>> rawComponents = (Collection<Map<String, Object>>) aConfiguration
                .get("composition");
        final List<ComponentConf> components = new LinkedList<ComponentConf>();
        for (final Map<String, Object> rawComponent : rawComponents) {
            components.add(new ComponentConf((String) rawComponent
                    .get("factory"), (String) rawComponent.get("name"),
                    (Map<String, Object>) rawComponent.get("properties")));
        }
        isolate.setComponents(components);

        return isolate;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.services.conf.IConfigurationParser#prepareIsolate
     * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.util.Collection,
     * java.util.Collection)
     */
    @Override
    public Map<String, Object> prepareIsolate(final String aUID,
            final String aName, final String aNode, final String aKind,
            final String aLevel, final String aSubLevel,
            final Collection<Map<String, Object>> aBundles,
            final Collection<Map<String, Object>> aComposition) {

        return pBridge.prepareIsolate(aUID, aName, aNode, aKind, aLevel,
                aSubLevel, aBundles, aComposition);
    }
}
