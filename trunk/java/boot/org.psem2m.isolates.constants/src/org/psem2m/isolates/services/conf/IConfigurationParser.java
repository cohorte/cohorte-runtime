/**
 * File:   IConfigurationParser.java
 * Author: Thomas Calmant
 * Date:   23 janv. 2013
 */
package org.psem2m.isolates.services.conf;

import java.util.Map;

import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Parses configuration files
 * 
 * @author Thomas Calmant
 */
public interface IConfigurationParser {

    /**
     * Parses the description of an isolate in the given file
     * 
     * @param aFileName
     *            A configuration file
     * @return The description of the isolate
     */
    IsolateConf loadIsolateFile(String aFileName);

    /**
     * Parses the description map of an isolate
     * 
     * @param aConfiguration
     *            A configuration map
     * @return The description of the isolate
     */
    IsolateConf loadIsolateMap(Map<String, Object> aConfiguration);
}
