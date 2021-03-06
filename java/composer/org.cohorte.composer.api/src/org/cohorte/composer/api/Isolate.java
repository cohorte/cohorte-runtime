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

package org.cohorte.composer.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an isolate to be instantiated
 *
 * @author Thomas Calmant
 */
public class Isolate {

    /** JSON class name, for Cohorte in Python */
    public static final String JSON_CLASS = "cohorte.composer.beans.Isolate";

    /** Components hosted by the isolate */
    private final Set<RawComponent> pComponents = new LinkedHashSet<RawComponent>();

    /** Language of components hosted by this isolate */
    private String pLanguage;

    /** Name of the isolate */
    private String pName;

    /**
     * Default constructor
     */
    public Isolate() {

        // Do nothing
    }

    /**
     * Configured bean
     *
     * @param aName
     *            Isolate name
     * @param aLanguage
     *            Language of hosted components
     * @param aComponents
     *            Hosted components
     */
    public Isolate(final String aName, final String aLanguage,
            final Collection<RawComponent> aComponents) {

        pName = aName;
        pLanguage = aLanguage;

        if (aComponents != null) {
            pComponents.addAll(aComponents);
        }
    }

    /**
     * Adds a new component to this isolate
     *
     * @param aComponent
     *            The component to be added
     */
    public void addComponent(final RawComponent aComponent) {

        if (pLanguage == null) {
            pLanguage = aComponent.getLanguage();
        }

        pComponents.add(aComponent);
    }

    /**
     * Returns the JSON class for Python
     *
     * @return
     */
    public Object[] get__jsonclass__() {

        return new Object[] { JSON_CLASS, new Object[] {} };
    }

    /**
     * @return the components
     */
    public Set<RawComponent> getComponents() {

        return new HashSet<RawComponent>(pComponents);
    }

    /**
     * @return the language
     */
    public String getLanguage() {

        return pLanguage;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * Compatibility method
     */
    public void set__jsonclass__(final Object[] aValue) {

        // Do nothing
    }

    /**
     * @param aLanguage
     *            the language to set
     */
    public void setLanguage(final String aLanguage) {

        if (pLanguage == null) {
            pLanguage = aLanguage;
        }
    }

    /**
     * @param aName
     *            the name to set
     */
    public void setName(final String aName) {

        if (pName == null) {
            pName = aName;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Isolate(" + pName + ", " + pLanguage + ")";
    }
}
