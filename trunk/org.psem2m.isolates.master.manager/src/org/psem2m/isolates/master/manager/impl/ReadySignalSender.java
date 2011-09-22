/**
 * File:   ReadySignalSender.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends READY signal until the forker is started
 * 
 * @author Thomas Calmant
 */
public class ReadySignalSender extends Thread {

    /** Forker started flag */
    private AtomicBoolean pForkerFlag;

    /** The parent manager */
    private CMasterManager pParent;

    /**
     * Sets up the thread
     * 
     * @param aParent
     *            Parent manager
     * @param aForkerFlag
     *            Forker start flag
     */
    public ReadySignalSender(final CMasterManager aParent,
            final AtomicBoolean aForkerFlag) {

        super("Master-Manager-ReadySignalSender");
        setDaemon(true);

        pParent = aParent;
        pForkerFlag = aForkerFlag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        boolean finished = false;
        try {
            do {
                pParent.emitSignal();

                Thread.sleep(500);

                synchronized (pForkerFlag) {
                    finished = pForkerFlag.get();
                }

            } while (!finished && !isInterrupted());

        } catch (InterruptedException ex) {
            // Ignore, stop here
        }
    }
}
