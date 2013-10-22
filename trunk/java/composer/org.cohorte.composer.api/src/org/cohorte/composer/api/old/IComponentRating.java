/**
 * File:   IComponentRating.java
 * Author: "Thomas Calmant"
 * Date:   29 janv. 2013
 */
package org.cohorte.composer.api.old;

import java.io.IOException;
import java.util.Map;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IComponentRating {

    boolean load(String aRatingFile);

    void store(String aRatingFile) throws IOException;

    /**
     * Component Factory -&gt; Rating
     */
    void update(Map<String, Integer> aRating);
}
