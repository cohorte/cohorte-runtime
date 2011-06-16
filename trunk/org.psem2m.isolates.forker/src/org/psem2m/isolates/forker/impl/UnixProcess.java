/**
 * 
 */
package org.psem2m.isolates.forker.impl;

/**
 * Specific description of an Unix Process
 */
public class UnixProcess extends AbstractProcess {

    public UnixProcess(final Process aProcess) {
	// ...
    }

    /**
     * Sends a signal to the process using the kill method
     * 
     * @param aSignal
     *            The signal to send
     * @return The process exit code
     */
    public int kill(final int aSignal) {
	// TODO use JNA to call the real kill() method
	return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IProcess#stop()
     */
    @Override
    public int stop() {
	// TODO Auto-generated method stub
	return 0;
    }
}