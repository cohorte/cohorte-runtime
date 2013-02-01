/**
 * File:   ComponentRating.java
 * Author: "Thomas Calmant"
 * Date:   30 janv. 2013
 */
package org.cohorte.composer.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.cohorte.composer.api.IComponentRating;

/**
 * @author "Thomas Calmant"
 * 
 */
@Component(name = "cohorte-component-rating-factory")
@Provides(specifications = IComponentRating.class)
public class ComponentRating implements IComponentRating {

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IComponentRating#load(java.lang.String)
     */
    @Override
    public boolean load(final String aRatingFile) {

        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IComponentRating#store(java.lang.String)
     */
    @Override
    public void store(final String aRatingFile) throws IOException {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IComponentRating#update(java.util.Map)
     */
    @Override
    public void update(final Map<String, Integer> aRating) {

        // TODO Auto-generated method stub

    }
}
