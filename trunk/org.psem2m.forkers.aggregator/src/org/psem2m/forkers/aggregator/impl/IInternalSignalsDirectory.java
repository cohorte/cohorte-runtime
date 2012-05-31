/**
 * File:   IInternalSignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

/**
 * Interface implemented by internal directories
 * 
 * @author Thomas Calmant
 */
public interface IInternalSignalsDirectory {

    /** Get the forker ID for the given host name */
    String getForkerForHost(String aHostName);
}
