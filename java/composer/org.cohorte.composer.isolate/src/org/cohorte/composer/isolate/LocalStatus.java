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

package org.cohorte.composer.isolate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.cohorte.composer.api.RawComponent;
import org.osgi.service.log.LogService;

/**
 * Isolate composer status storage
 *
 * @author Thomas Calmant
 */
public class LocalStatus {

    /** Component name -&gt; Component bean */
    private final Map<String, RawComponent> pComponents = new LinkedHashMap<String, RawComponent>();

    /** Instantiated components */
    private final Set<RawComponent> pInstantiated = new LinkedHashSet<RawComponent>();

    /** The log service */
    private final LogService pLogger;

    /** Component to be instantiated */
    private final Set<RawComponent> pRemaining = new LinkedHashSet<RawComponent>();

    /**
     * Sets up the status
     *
     * @param aLogger
     *            A log service
     */
    public LocalStatus(final LogService aLogger) {

        pLogger = aLogger;
    }

    /**
     * Retrieves the set of all components associated to this isolate
     *
     * @return A set of RawComponent beans
     */
    public Collection<RawComponent> getComponents() {

        return new LinkedHashSet<RawComponent>(pComponents.values());
    }

    /**
     * Returns the components that still need to be instantiated
     *
     * @return A set of RawComponent beans
     */
    public Collection<RawComponent> getRemaining() {

        return new LinkedHashSet<RawComponent>(pRemaining);
    }

    /**
     * Returns the components that are instantiated
     *
     * @return A set of RawComponent beans
     */
    public Collection<RawComponent> getRunning() {

        return new LinkedHashSet<RawComponent>(pInstantiated);
    }

    /**
     * Removes the given components from the storage
     *
     * @param aNames
     *            A set of names of components
     */
    public void remove(final Collection<String> aNames) {

        for (final String name : aNames) {
            final RawComponent component = pComponents.remove(name);
            if (component != null) {
                pRemaining.remove(component);
                pInstantiated.remove(component);

            } else {
                if (pLogger != null) {
                    pLogger.log(LogService.LOG_WARNING, "Unknown component: "
                            + name);
                }
            }
        }
    }

    /**
     * Considers the component killed. Does nothing if the component was already
     * considered killed.
     *
     * @param aName
     *            The name of the instantiated component
     * @return True if the component was known else false
     */
    public boolean setKilled(final String aName) {

        final RawComponent component = pComponents.get(aName);
        if (component != null) {
            pInstantiated.remove(component);
            pRemaining.add(component);
            return true;
        }

        return false;
    }

    /**
     * Considers the component instantiated. Does nothing if the component was
     * already considered running.
     *
     * @param aName
     *            The name of the instantiated component
     * @return True if the component was known else false
     */
    public boolean setRunning(final String aName) {

        final RawComponent component = pComponents.get(aName);
        if (component != null) {
            pRemaining.remove(component);
            pInstantiated.add(component);
            return true;
        }

        return false;
    }

    /**
     * Stores the given components in the storage. Ignores already stored beans.
     *
     * @param aComponents
     *            A set of RawComponent beans
     * @return The set of components that wasn't already known
     */
    public Collection<RawComponent> store(
            final Collection<RawComponent> aComponents) {

        final Set<RawComponent> addedComponents = new LinkedHashSet<RawComponent>();

        for (final RawComponent component : aComponents) {
            final String name = component.getName();
            if (!pComponents.containsKey(name)) {
                // Store if not yet known
                pComponents.put(name, component);
                pInstantiated.add(component);
                addedComponents.add(component);
            }
        }

        return addedComponents;
    }
}
