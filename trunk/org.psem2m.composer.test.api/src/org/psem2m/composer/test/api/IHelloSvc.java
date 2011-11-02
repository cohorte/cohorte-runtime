/**
 * File:   IHelloSvc.java
 * Author: Thomas Calmant
 * Date:   2 nov. 2011
 */
package org.psem2m.composer.test.api;

/**
 * Simple hello world service
 * 
 * @author Thomas Calmant
 */
public interface IHelloSvc {

    /**
     * Writes an hello message to the logger
     * 
     * @param aName
     *            A name
     */
    void logHello(String aName);

    /**
     * Prepares an hello message
     * 
     * @param aName
     *            A name
     * @return The message
     */
    String sayHello(String aName);
}
