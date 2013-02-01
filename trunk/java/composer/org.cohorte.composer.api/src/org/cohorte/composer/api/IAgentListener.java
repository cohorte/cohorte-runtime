/**
 * File:   IAgentListener.java
 * Author: "Thomas Calmant"
 * Date:   29 janv. 2013
 */
package org.cohorte.composer.api;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IAgentListener {

    void agentGone(String aIsolateUID);

    void agentReady(String aIsolateUID);

    void componentGone(String aIsolateUID, String aCompositionUID,
            String aComponentUID);

    void componentInstantiated(String aIsolateUID, String aCompositionUID,
            String aComponentUID);

    void componentInvalidated(String aIsolateUID, String aCompositionUID,
            String aComponentUID);

    void componentValidated(String aIsolateUID, String aCompositionUID,
            String aComponentUID);

    void factoryGone(String aIsolateUID, String aFactoryName);

    void factoryReady(String aIsolateUID, String aFactoryName);
}
