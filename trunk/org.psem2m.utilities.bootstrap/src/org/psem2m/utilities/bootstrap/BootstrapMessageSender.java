/**
 * File:   BootstrapMessageSender.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.util.logging.LogRecord;

import org.psem2m.isolates.base.boot.IBootstrapMessageSender;
import org.psem2m.isolates.base.boot.IsolateStatus;

/**
 * The bootstrap message sender service implementation
 * 
 * @author Thomas Calmant
 */
public class BootstrapMessageSender implements IBootstrapMessageSender {

    /** The underlying message sender */
    private IMessageSender pMessageSender;

    /**
     * Sets up the message sender service
     * 
     * @param aMessageSender
     *            The underlying message sender
     */
    public BootstrapMessageSender(final IMessageSender aMessageSender) {
	pMessageSender = aMessageSender;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.boot.IBootstrapMessageSender#sendLog(java.util
     * .logging.LogRecord)
     */
    @Override
    public synchronized void sendLog(final LogRecord aLogRecord) {
	pMessageSender.sendLog(aLogRecord);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.boot.IBootstrapMessageSender#sendStatus(int,
     * double)
     */
    @Override
    public void sendStatus(final int aState, final double aProgress) {
	pMessageSender.sendStatus(aState, aProgress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.boot.IBootstrapMessageSender#sendStatus(org.
     * psem2m.isolates.base.boot.IsolateStatus)
     */
    @Override
    public synchronized void sendStatus(final IsolateStatus aIsolateStatus) {
	pMessageSender.sendStatus(aIsolateStatus);
    }

    /**
     * Changes the underlying message sender
     * 
     * @param aMessageSender
     *            The new message sender
     */
    public synchronized void setMessageSender(
	    final IMessageSender aMessageSender) {
	pMessageSender = aMessageSender;
    }
}
