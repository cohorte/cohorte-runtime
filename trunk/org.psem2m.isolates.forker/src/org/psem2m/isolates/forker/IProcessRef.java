/**
 * File:   IProcessRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker;

/**
 * Describes an OS process
 * 
 * @author Thomas Calmant
 */
public interface IProcessRef {

    /**
     * Retrieves the OS process identifier
     * 
     * @return The Pid
     */
    int getPid();
}
