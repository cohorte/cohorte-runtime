/**
 * File:   ForkerWatchThread.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.LogRecord;

import org.psem2m.isolates.base.isolates.IIsolateOutputListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * Thread reading the forker process output. Notifies an
 * {@link IIsolateOutputListener} on each understood read object.
 * 
 * @author Thomas Calmant
 */
public class ForkerWatchThread extends Thread {

    /** Thread name */
    public static final String THREAD_NAME = "PSEM2M-Monitor-ForkerWatcher";

    /** The forker output stream */
    private ObjectInputStream pForkerOutput;

    /** Forker process */
    private Process pForkerProcess;

    /** Process output listener */
    private IIsolateOutputListener pOutputListener;

    /**
     * Sets up the forker watcher
     * 
     * @param aOutputListener
     *            Process output listener
     * @param aForkerProcess
     *            The forker isolate process
     * @throws IOException
     *             Invalid forker output format
     */
    public ForkerWatchThread(final IIsolateOutputListener aOutputListener,
            final Process aForkerProcess) throws IOException {

        super(THREAD_NAME);
        setDaemon(true);

        pOutputListener = aOutputListener;
        pForkerProcess = aForkerProcess;
        pForkerOutput = new ObjectInputStream(pForkerProcess.getInputStream());
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
                final Object readObject = pForkerOutput.readObject();

                if (readObject instanceof IsolateStatus) {
                    // Let the manager handle this
                    pOutputListener.handleIsolateStatus(
                            IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER,
                            (IsolateStatus) readObject);

                } else if (readObject instanceof LogRecord) {
                    // Log it
                    pOutputListener.handleIsolateLogRecord(
                            IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER,
                            (LogRecord) readObject);
                }

                // Ignore other objects

            } catch (final IOException e) {
                // IO Exception are fatal : destroy the forker process
                pForkerProcess.destroy();
                pOutputListener.handleIsolateStatus(
                        IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER, null);

                return;

            } catch (final ClassNotFoundException e) {
                // Print error
                e.printStackTrace();
            }
        }
    }
}
