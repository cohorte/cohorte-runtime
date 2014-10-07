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

import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.pyboot.api.IPyBridge;
import org.psem2m.isolates.services.conf.IConfigurationParser;
import org.psem2m.isolates.services.conf.IStartConfiguration;
import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Calls the bridge to copy the isolate configuration and converts it to Java
 * beans
 *
 * @author Thomas Calmant
 */
@Component(name = "cohorte-pyboot-configuration-start-factory")
@Provides(specifications = IStartConfiguration.class)
public class StartConfiguration implements IStartConfiguration {

    /** The Python bridge dependency ID */
    private static final String IPOJO_ID_BRIDGE = "python-bridge";

    /** The configuration parser dependency ID */
    private static final String IPOJO_ID_PARSER = "config-parser";

    /** The application description */
    private ApplicationDescription pApplication;

    /** The Python bridge */
    @Requires(id = IPOJO_ID_BRIDGE, optional = true, nullable = true)
    private IPyBridge pBridge;

    /** The start configuration */
    private IsolateConf pConfiguration;

    /** The configuration parser */
    @Requires(id = IPOJO_ID_PARSER, optional = true, nullable = true)
    private IConfigurationParser pParser;

    /** Service export flag */
    @ServiceController(value = false)
    private boolean pServiceFlag;

    /**
     * Called when a Python bridge service is bound
     */
    @Bind(id = IPOJO_ID_BRIDGE)
    private synchronized void bindBridge() {

        if (pConfiguration == null) {
            pServiceFlag = loadConfiguration();
        }
    }

    /**
     * Called when a configuration parser service is bound
     */
    @Bind(id = IPOJO_ID_PARSER)
    private synchronized void bindParser() {

        if (pConfiguration == null) {
            pServiceFlag = loadConfiguration();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.services.conf.IStartConfiguration#getApplication()
     */
    @Override
    public ApplicationDescription getApplication() {

        return pApplication;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.services.conf.IStartConfiguration#getConfiguration()
     */
    @Override
    public IsolateConf getConfiguration() {

        return pConfiguration;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        pApplication = null;
        pConfiguration = null;
    }

    /**
     * Loads the configuration from the Python bridge
     *
     * @return True if a configuration has been loaded
     */
    private synchronized boolean loadConfiguration() {

        if (pBridge == null || pParser == null) {
            // Dependency missing
            return false;
        }

        // Get the raw configuration
        final Map<String, Object> rawConfig = pBridge.getStartConfiguration();
        if (rawConfig == null) {
            // No configuration
            return false;
        }

        // Compute the isolate configuration
        pConfiguration = pParser.loadIsolateMap(rawConfig);

        return pConfiguration != null;
    }

    /**
     * Component validated.
     *
     * WARNING: with the development version of iPOJO (1.9.0.SNAPSHOT), this
     * method is mandatory, else @Bind will only be called after @Invalidate.
     */
    @Validate
    public void validate() {

        // Try to load the configuration
        loadConfiguration();
    }
}
