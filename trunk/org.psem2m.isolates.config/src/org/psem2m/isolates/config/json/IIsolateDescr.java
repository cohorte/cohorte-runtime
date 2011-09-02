/**
 * File:   IIsolateDescr.java
 * Author: "Thomas Calmant"
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.config.json;

import java.util.List;
import java.util.Set;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IIsolateDescr {

    Set<IBundleDescr> getBundles();

    String getId();

    List<String> getVMArgs();
}
