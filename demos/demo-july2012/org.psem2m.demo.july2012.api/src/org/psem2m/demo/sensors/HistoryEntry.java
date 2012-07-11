/**
 * File:   HistoryEntry.java
 * Author: Thomas Calmant
 * Date:   10 juil. 2012
 */
package org.psem2m.demo.sensors;


/**
 * Definition of an entry in the history of a sensor
 * 
 * @author Thomas Calmant
 */
public class HistoryEntry {

    /** The sensor name */
    private String pName;

    /** The entry time stamp */
    private long pTimestamp;

    /** The unit of the value */
    private String pUnit;

    /** The sensor value */
    private double pValue;

    /**
     * Default constructor (for bean transmission)
     */
    public HistoryEntry() {

        // Do nothing
    }

    /**
     * Retrieves the name of the sensor
     * 
     * @return the sensor name
     */
    public String getName() {

        return pName;
    }

    /**
     * Retrieves the time stamp of the history entry
     * 
     * @return the entry time stamp
     */
    public long getTimestamp() {

        return pTimestamp;
    }

    /**
     * Retrieves the unit of the value
     * 
     * @return the unit
     */
    public String getUnit() {

        return pUnit;
    }

    /**
     * Retrieves the value
     * 
     * @return the value
     */
    public double getValue() {

        return pValue;
    }

    /**
     * Sets the name of the sensor
     * 
     * @param aName
     *            the sensor name
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * Sets the time stamp of the history entry
     * 
     * @param aTimestamp
     *            the time stamp of the history entry
     */
    public void setTimestamp(final long aTimestamp) {

        pTimestamp = aTimestamp;
    }

    /**
     * Sets the unit of the value
     * 
     * @param aUnit
     *            the unit of the value
     */
    public void setUnit(final String aUnit) {

        pUnit = aUnit;
    }

    /**
     * Sets the value
     * 
     * @param aValue
     *            the value
     */
    public void setValue(final double aValue) {

        pValue = aValue;
    }
}
