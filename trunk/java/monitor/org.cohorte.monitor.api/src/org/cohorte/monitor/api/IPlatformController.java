/**
 * File:   IMonitor.java
 * Author: Thomas Calmant
 * Date:   28 janv. 2013
 */
package org.cohorte.monitor.api;

import java.util.Collection;
import java.util.Map;

import org.psem2m.isolates.services.conf.beans.BundleConf;
import org.psem2m.isolates.services.conf.beans.ComponentConf;

/**
 * Represents a monitor
 * 
 * @author Thomas Calmant
 */
public interface IPlatformController {

    /**
     * Tests if the isolate with the given UID is running
     * 
     * @param UID
     *            An isolate UID
     * @return True if the isolate is known and running
     */
    boolean isAlive(String UID);

    /**
     * Starts an isolate according to the given parameters.
     * 
     * If aNode is null, the current one will be used.
     * 
     * If aStack is false, the method blocks until the isolate has been started.
     * Else, the method returns immediately and the isolate will be started as
     * soon as possible.
     * 
     * @param aKind
     *            Kind of isolate
     * @param aLevel
     *            Level of configuration (boot, Java, Python, ...)
     * @param aSubLevel
     *            Category of configuration (monitor, isolate, ...)
     * @param aName
     *            Name of the isolate
     * @param aNode
     *            Node hosting the isolate
     * @param aProperties
     *            Isolate properties
     * @param aBundles
     *            List of bundles to install in the isolate
     * @param aComponents
     *            List of components to instantiate
     * @return The isolate unique identifier (UID)
     */
    String startIsolate(String aKind, String aLevel, String aSubLevel,
            String aName, String aNode, Map<String, Object> aProperties,
            Collection<BundleConf> aBundles,
            Collection<ComponentConf> aComponents);

    /**
     * Stops the isolate with the given UID
     * 
     * @param aUID
     *            An isolate UID
     * @return True if the isolate has been stopped
     */
    boolean stopIsolate(String aUID);

    /**
     * Stops all isolates and monitors.
     * 
     * @param aStopForkers
     *            If true, stops the forkers too.
     */
    void stopPlatform(boolean aStopForkers);
}
