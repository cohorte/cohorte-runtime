/**
 * File:   Composer.java
 * Author: "Thomas Calmant"
 * Date:   30 janv. 2013
 */
package org.cohorte.composer.impl;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.cohorte.composer.api.IAgentListener;
import org.cohorte.composer.api.IComposer;
import org.cohorte.monitor.api.IIsolatePresenceListener;

/**
 * Core logic component for Cohorte compositions
 * 
 * @author "Thomas Calmant"
 */
@Component(name = "cohorte-composer-core-factory")
@Provides(specifications = { IComposer.class, IAgentListener.class,
        IIsolatePresenceListener.class })
public class ComposerCore implements IComposer, IAgentListener,
        IIsolatePresenceListener {

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IAgentListener#agentGone(java.lang.String)
     */
    @Override
    public void agentGone(final String aIsolateUID) {

        // TODO: consider all components on this isolate as lost

        // TODO: compute a new repartition of components
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IAgentListener#agentReady(java.lang.String)
     */
    @Override
    public void agentReady(final String aIsolateUID) {

        // TODO: request instantiation of components for this isolate
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IAgentListener#componentGone(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void componentGone(final String aIsolateUID,
            final String aCompositionUID, final String aComponentUID) {

        // TODO: if requested: do nothing

        /*
         * TODO: else, call composer rules to down the component rating and
         * compute a new repartition
         */
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * composer.interfaces.IAgentListener#componentInstantiated(java.lang.String
     * , java.lang.String, java.lang.String)
     */
    @Override
    public void componentInstantiated(final String aIsolateUID,
            final String aCompositionUID, final String aComponentUID) {

        // TODO: update composition state => remove from "instantiating list"

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * composer.interfaces.IAgentListener#componentInvalidated(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void componentInvalidated(final String aIsolateUID,
            final String aCompositionUID, final String aComponentUID) {

        // TODO: if requested: do nothing; else: log
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * composer.interfaces.IAgentListener#componentValidated(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void componentValidated(final String aIsolateUID,
            final String aCompositionUID, final String aComponentUID) {

        // TODO: log

    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IAgentListener#factoryGone(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void factoryGone(final String aIsolateUID, final String aFactoryName) {

        // TODO: consider all isolates from the given factory as gone
        // TODO: if not requested, compute a new repartition of isolates
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IAgentListener#factoryReady(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void factoryReady(final String aIsolateUID, final String aFactoryName) {

        // TODO: instantiate waiting components of this type
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IComposer#getComposition(java.lang.String)
     */
    @Override
    public Map<String, Object> getComposition(final String aCompositionUID) {

        // TODO: retrieve a snapshot of the composition: configuration & state
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * monitor.interfaces.IIsolatePresenceListener#isolateLost(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void isolateLost(final String aUID, final String aName,
            final String aNode) {

        // TODO: consider all components & factories lost for this isolate
        // TODO: if not requested, update components rating
        // TODO: compute a new repartition
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * monitor.interfaces.IIsolatePresenceListener#isolateReady(java.lang.String
     * , java.lang.String, java.lang.String)
     */
    @Override
    public void isolateReady(final String aUID, final String aName,
            final String aNode) {

        // Do nothing (wait for the agent)
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IComposer#load(java.util.Map)
     */
    @Override
    public String load(final Map<String, Object> aCompositionMap) {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see composer.interfaces.IComposer#unload(java.lang.String)
     */
    @Override
    public boolean unload(final String aCompositionUID) {

        // TODO: Stop/delete all components of the given composition
        return false;
    }
}
