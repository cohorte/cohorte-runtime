/**
 * File:   ISignalBroadcaster.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.signals;

import java.util.concurrent.Future;

/**
 * Represents a signal broadcast service
 * 
 * @author Thomas Calmant
 */
public interface ISignalBroadcaster {

    /** Signals request mode ACK : at least one listener must be there */
    String MODE_ACK = "ack";

    /** Signals request mode FORGET : return immediately */
    String MODE_FORGET = "forget";

    /** Signals request mode SEND : wait for all listeners to return */
    String MODE_SEND = "send";

    String[] fire(String aSignalName, Object aContent, String... aIsolates);

    String[] fireGroup(String aSignalName, Object aContent, String... aGroups);

    Future<ISignalSendResult> post(String aSignalName, Object aContent,
            String... aIsolates);

    Future<ISignalSendResult> postGroup(String aSignalName, Object aContent,
            String... aGroups);

    ISignalSendResult send(String aSignalName, Object aContent,
            String... aIsolates);

    ISignalSendResult sendGroup(String aSignalName, Object aContent,
            String... aGroups);

    Object[] sendTo(String aSignalName, Object aContent, String aHost, int aPort)
            throws Exception;
}
