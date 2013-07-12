/**
 * File:   IPacketListener.java
 * Author: Thomas Calmant
 * Date:   18 juin 2012
 */
package org.cohorte.remote.multicast.utils;

import java.net.InetSocketAddress;

/**
 * Defines a UDP packets listener
 * 
 * @author Thomas Calmant
 */
public interface IPacketListener {

    /**
     * Handles an exception thrown while waiting or a packet
     * 
     * @param aException
     *            The thrown exception
     * @return True to continue the thread loop, else false
     */
    boolean handleError(Exception aException);

    /**
     * Handles a received UDP packet
     * 
     * @param aSender
     *            Address of the packet sender
     * @param aContent
     *            Content of the packet
     */
    void handlePacket(InetSocketAddress aSender, byte[] aContent);
}
