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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a basic component
 *
 * @author Thomas Calmant
 */
public class ComponentBean {

    /** ComponentBean factory name (type) */
    private final String pFactory;

    /** Instance name */
    private final String pName;

    /** Instance properties */
    private final Map<String, Object> pProperties = new HashMap<String, Object>();

    /**
     * Setup the component bean
     *
     * @param aFactory
     *            ComponentBean type
     * @param aName
     *            Instance name
     * @param aProperties
     *            Instance properties
     */
    public ComponentBean(final String aFactory, final String aName,
            final Map<String, Object> aProperties) {

        pFactory = aFactory;
        pName = aName;
        if (aProperties != null) {
            // Copy given properties
            pProperties.putAll(aProperties);
        }
    }

    /**
     * @return the factory
     */
    public String getFactory() {

        return pFactory;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {

        return new HashMap<String, Object>(pProperties);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Component(name='" + pName + "', factory='" + pFactory + "')";
    }
}
