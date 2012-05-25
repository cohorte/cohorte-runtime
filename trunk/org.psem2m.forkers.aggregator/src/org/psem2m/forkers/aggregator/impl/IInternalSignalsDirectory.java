/**
 * File:   IInternalSignalsDirectory.java
 * Author: "Thomas Calmant"
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

/**
 * @author Thomas Calmant
 * 
 */
public interface IInternalSignalsDirectory {

    /** Get the forker ID for the given host name */
    public String getForkerForHost(String aHostName);
}
