/**
 * File:   IWaitingSignal.java
 * Author: Thomas Calmant
 * Date:   19 juin 2012
 */
package org.psem2m.signals;

import java.util.concurrent.Future;

import org.psem2m.signals.ISignalBroadcaster.ESendMode;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;

/**
 * Represents a waiting signal
 * 
 * @author Thomas Calmant
 */
public interface IWaitingSignal {

    /**
     * Decreases the TimeToLive of the waiting signal
     * 
     * @param aDelta
     *            Decrease delta (in seconds)
     * @return True if the TTL has expired
     */
    boolean decreaseTTL(final long aDelta);

    /**
     * Retrieves the targeted access
     * 
     * @return the targeted access
     */
    HostAccess getAccess();

    /**
     * Retrieves the content of the signal
     * 
     * @return the content of the signal
     */
    Object getContent();

    /**
     * Retrieves the result of a call to fire()
     * 
     * @return the result of a call to fire()
     */
    String[] getFireResult();

    /**
     * Retrieves the targeted directory base group
     * 
     * @return the directory base group
     */
    EBaseGroup getGroup();

    /**
     * Retrieves the target groups
     * 
     * @return the target groups
     */
    String[] getGroups();

    /**
     * Retrieves the target isolates
     * 
     * @return the target isolates
     */
    String[] getIsolates();

    /**
     * Retrieves the mode to be used to send the signal
     * 
     * @return the mode to be used to send the signal
     */
    ESendMode getMode();

    /**
     * Retrieves the name of the signal
     * 
     * @return the name of the signal
     */
    String getName();

    /**
     * Retrieves the result of a call to post()
     * 
     * @return the result of a call to post()
     */
    Future<ISignalSendResult> getPostResult();

    /**
     * Retrieves the result of a call to send()
     * 
     * @return the result of a call to send()
     */
    ISignalSendResult getSendResult();

    /**
     * Retrieves the result of a call to sendTo()
     * 
     * @return the result of a call to sendTo()
     */
    Object[] getSendToResult();
}
