/**
 * File:   IAggregator.java
 * Author: Thomas Calmant
 * Date:   10 juil. 2012
 */
package org.psem2m.demo.sensors;

import java.util.List;
import java.util.Map;

/**
 * Description of a sensor Aggregator
 * 
 * @author Thomas Calmant
 */
public interface IAggregator {

    /**
     * Retrieves the whole known history
     * 
     * @return the unit of the value
     */
    Map<String, List<HistoryEntry>> getHistory();

    /**
     * Retrieves the history of the given sensor, or null
     * 
     * @param aSensorName
     *            Name of the sensor
     * @return The history of the sensor or null
     */
    List<HistoryEntry> getSensorHistory(String aSensorName);

    /**
     * Retrieves the names of the sensors visible in the history
     * 
     * @return the names of the sensors
     */
    String[] getSensors();
}
