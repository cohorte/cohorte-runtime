/**
 * 
 */
package org.psem2m.isolates.forker;

/**
 * Description of the an isolate process
 * 
 */
public interface IProcess {

    /**
     * Retrieves the isolate ID
     * 
     * @return The isolate ID
     */
    public String getIsolateId();

    /**
     * Retrieves the process ID (depending on the system)
     * 
     * @return The process ID
     */
    public int getProcessId();

    /**
     * Tries to exit the given process gracefully
     * 
     * @return The process exit value
     */
    public int stop();
}
