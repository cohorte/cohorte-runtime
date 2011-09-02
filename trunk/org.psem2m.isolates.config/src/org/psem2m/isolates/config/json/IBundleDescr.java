/**
 * File:   IBundleDescr.java
 * Author: "Thomas Calmant"
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.config.json;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IBundleDescr {

    String getFile();

    String getSymbolicName();

    String getVersion();

    boolean isOptional();
}
