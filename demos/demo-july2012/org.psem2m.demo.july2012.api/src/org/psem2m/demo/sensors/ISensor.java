/**
 * File:   ISensor.java
 * Author: Thomas Calmant
 * Date:   10 juil. 2012
 */
package org.psem2m.demo.sensors;

/**
 * Definition of sensor
 * 
 * @author Thomas Calmant
 */
public interface ISensor {

    /**
     * Retrieves the name of the sensor
     * 
     * @return the name of the sensor
     */
    String getName();

    /**
     * Retrieves the unit of the sensor values
     * 
     * @return the unit of the sensor values
     */
    String getUnit();

    /**
     * Retrieves the current value of the sensor
     * 
     * @return the current value of the sensor
     */
    double getValue();
}
