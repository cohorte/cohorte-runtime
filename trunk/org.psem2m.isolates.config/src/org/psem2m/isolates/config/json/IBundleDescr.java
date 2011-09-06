/**
 * File:   IBundleDescr.java
 * Author: "Thomas Calmant"
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.config.json;

import java.io.Serializable;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IBundleDescr extends Serializable {

    String getFile();

    String getSymbolicName();

    String getVersion();

    boolean isOptional();
}
