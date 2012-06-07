/**
 * File:   CProcessReaderThread.java
 * Author: Thomas Calmant
 * Date:   14 sept. 2011
 */
package org.psem2m.isolates.forker.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.LogRecord;

import org.psem2m.isolates.base.isolates.IIsolateOutputListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;

/**
 * Thread reading an isolate process output. Notifies an
 * {@link IIsolateOutputListener} on each understood read object.
 * 
 * @author Thomas Calmant
 */
public class CProcessWatcherThread extends Thread {

    /** Thread name prefix */
    public static final String THREAD_NAME_PREFIX = "PSEM2M-Forker-IsolateWatcher-";

    /** Watched isolate ID */
    private String pIsolateId;

    /** Process output listener */
    private IIsolateOutputListener pOutputListener;

    /** Watched process */
    private Process pProcess;

    /** The process output stream */
    private ObjectInputStream pProcessOutput;

    /**
     * Sets up the isolate process watcher
     * 
     * @param aOutputListener
     *            The isolate process output listener
     * @param aIsolateId
     *            The watched isolate ID
     * @param aProcess
     *            The isolate process
     * 
     * @throws IOException
     *             Invalid isolate output format
     */
    public CProcessWatcherThread(final IIsolateOutputListener aOutputListener,
            final String aIsolateId, final Process aProcess) throws IOException {

        // Prepare the thread
        super(THREAD_NAME_PREFIX + aIsolateId);
        setDaemon(true);

        pOutputListener = aOutputListener;
        pIsolateId = aIsolateId;
        pProcess = aProcess;

        pProcessOutput = new ObjectInputStream(pProcess.getInputStream());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        while (!isInterrupted()) {

            try {
                final Object readObject = pProcessOutput.readObject();

                if (readObject instanceof IsolateStatus) {
                    // Let the manager handle this
                    pOutputListener.handleIsolateStatus(pIsolateId,
                            (IsolateStatus) readObject);

                } else if (readObject instanceof LogRecord) {
                    // Log it
                    pOutputListener.handleIsolateLogRecord(pIsolateId,
                            (LogRecord) readObject);
                }

                // Ignore other objects

            } catch (IOException e) {
                // IO Exception are fatal : destroy the forker process
                pProcess.destroy();
                pOutputListener.handleIsolateStatus(pIsolateId, null);

                return;

            } catch (ClassNotFoundException e) {
                // Print error
                e.printStackTrace();
            }
        }
    }
}
