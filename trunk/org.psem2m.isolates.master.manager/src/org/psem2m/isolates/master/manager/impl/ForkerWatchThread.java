/**
 * File:   ForkerWatchThread.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.master.manager.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.LogRecord;

import org.psem2m.isolates.base.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * @author Thomas Calmant
 */
public class ForkerWatchThread extends Thread {

    /** The forker output stream */
    private ObjectInputStream pForkerOutput;

    /** Forker process */
    private Process pForkerProcess;

    /** Parent manager */
    private CMasterManager pParent;

    /**
     * Sets up the forker watcher
     * 
     * @param aForkerOutput
     *            The forker output stream
     * @param aParentManager
     *            Parent master manager
     * @throws IOException
     *             Invalid forker output format
     */
    public ForkerWatchThread(final CMasterManager aParentManager,
	    final Process aForkerProcess) throws IOException {

	super("Forker-Watcher");
	setDaemon(true);

	pParent = aParentManager;
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
		    pParent.handleForkerStatus((IsolateStatus) readObject);

		} else if (readObject instanceof LogRecord) {
		    // Log it
		    pParent.logFromForker((LogRecord) readObject);
		}

		// Ignore other objects

	    } catch (IOException e) {
		// IO Exception are fatal : destroy the forker process
		pForkerProcess.destroy();
		pParent.handleForkerStatus(new IsolateStatus(
			IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER,
			IsolateStatus.STATE_FAILURE, -1));

		return;

	    } catch (ClassNotFoundException e) {
		// Print error
		e.printStackTrace();
	    }
	}
    }
}
