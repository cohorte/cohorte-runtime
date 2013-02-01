/**
 * File:   PlatformController.java
 * Author: Thomas Calmant
 * Date:   28 janv. 2013
 */
package org.cohorte.monitor.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.cohorte.monitor.api.IForkerAggregator;
import org.cohorte.monitor.api.IForkerPresenceListener;
import org.cohorte.monitor.api.IIsolatePresenceListener;
import org.cohorte.monitor.api.IMonitorStatus;
import org.cohorte.monitor.api.IPlatformController;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.services.conf.IConfigurationParser;
import org.psem2m.isolates.services.conf.beans.BundleConf;
import org.psem2m.isolates.services.conf.beans.ComponentConf;
import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Prepares isolates configuration and ask the forker aggregator to send the
 * start order
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-platform-controller-factory")
@Provides(specifications = { IPlatformController.class,
        IIsolatePresenceListener.class, IForkerPresenceListener.class })
public class PlatformController implements IPlatformController,
        IIsolatePresenceListener, IForkerPresenceListener {

    /** Forkers directory */
    @Requires
    private IForkerAggregator pForkers;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Python configuration bridge */
    @Requires
    private IConfigurationParser pParser;

    /** Current platform status */
    @Requires
    private IMonitorStatus pStatus;

    /*
     * (non-Javadoc)
     * 
     * @see
     * monitor.interfaces.IForkerPresenceListener#forkerGone(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void forkerLost(final String aUID, final String aNode) {

        final String[] uids = pStatus.getIsolatesOnNode(aNode);
        if (uids != null) {
            for (final String uid : uids) {
                // TODO Test the state of the isolate

                // Declare the isolate stopped
                pStatus.isolateStopped(uid);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * monitor.interfaces.IForkerPresenceListener#forkerReady(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void forkerReady(final String aUID, final String aNode) {

        // Start isolates waiting for this node
        final IsolateConf[] nodeIsolates = pStatus
                .getIsolatesWaitingForNode(aNode);
        if (nodeIsolates == null || nodeIsolates.length == 0) {
            // Nothing to do
            pLogger.logInfo(this, "startIsolatesForNode",
                    "No isolate configured for node=", aNode);
        }

        pLogger.logDebug(this, "startIsolatesForNode", "Node=", aNode,
                "isolate=", nodeIsolates);

        for (final IsolateConf isolate : nodeIsolates) {
            // Try to start the isolates
            startIsolate(isolate);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.monitor.IPlatformController#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String UID) {

        return pForkers.isAlive(UID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * monitor.interfaces.IIsolatePresenceListener#isolateGone(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void isolateLost(final String aUID, final String aName,
            final String aNode) {

        // Update status
        pStatus.isolateStopped(aUID);

        // TODO Notify the isolate rules handler -> restart, do nothing
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

        // Clear the isolate out of the waiting list
        pStatus.isolateReady(aUID);
    }

    /**
     * Starts an isolate with the given configuration
     * 
     * @param aConfiguration
     *            An isolate configuration
     * @return The isolate UID, or null
     */
    public String startIsolate(final IsolateConf aConfiguration) {

        // Generate the UID if necessary
        if (aConfiguration.getUID() == null) {
            aConfiguration.setUID(UUID.randomUUID().toString());
        }

        // Prepare the list of bundles
        final List<Map<String, Object>> bundles = new LinkedList<Map<String, Object>>();
        if (aConfiguration.getBundles() != null) {
            for (final BundleConf bundle : aConfiguration.getBundles()) {
                bundles.add(bundle.toMap());
            }
        }

        // Prepare the list of components
        final List<Map<String, Object>> components = new LinkedList<Map<String, Object>>();
        if (aConfiguration.getComponents() != null) {
            for (final ComponentConf component : aConfiguration.getComponents()) {
                components.add(component.toMap());
            }
        }

        // Prepare the configuration of the isolate
        final String uid = aConfiguration.getUID();
        final String node = aConfiguration.getNode();
        final String kind = aConfiguration.getKind();
        final String level = aConfiguration.getLevel();
        final String sublevel = aConfiguration.getSublevel();

        final Map<String, Object> configuration = pParser.prepareIsolate(uid,
                aConfiguration.getName(), node, kind, level, sublevel, bundles,
                components);

        // TODO: add extra configuration (boot arguments, ...)

        // Store the configuration
        if (!pStatus.prepareIsolate(uid, aConfiguration)) {
            pLogger.logSevere(this, "startIsolate",
                    "Couldn't store the isolate configuration. Abandon.");
            return null;
        }

        // Ask the forker to start the isolate
        final int result = pForkers
                .startIsolate(uid, node, kind, configuration);
        pLogger.logDebug(this, "startIsolate", "Result=", result, "for UID=",
                uid);

        /*
         * TODO: handle result code:
         * 
         * Success -> put in "instantiating list"
         * 
         * No forker found -> put in "waiting list"
         * 
         * Error -> call the "isolate rules"
         */

        return uid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.api.IPlatformController#startIsolate(java.lang.String
     * , java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.util.Map, java.util.Collection, java.util.Collection)
     */
    @Override
    public String startIsolate(final String aKind, final String aLevel,
            final String aSubLevel, final String aName, final String aNode,
            final Map<String, Object> aProperties,
            final Collection<BundleConf> aBundles,
            final Collection<ComponentConf> aComponents) {

        // Generate a UID
        final String uid = UUID.randomUUID().toString();

        // Prepare the configuration bean
        final IsolateConf isolate = new IsolateConf(uid, aName, aNode, aKind,
                aLevel, aSubLevel);
        isolate.setBundles(aBundles);
        isolate.setComponents(aComponents);

        // Start the isolate
        return startIsolate(isolate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.monitor.IPlatformController#stopIsolate(java.lang.String)
     */
    @Override
    public boolean stopIsolate(final String aUID) {

        return pForkers.stopIsolate(aUID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.monitor.IPlatformController#stopPlatform(boolean)
     */
    @Override
    public void stopPlatform(final boolean aStopForkers) {

        // Set the forkers in stop mode
        pForkers.setPlatformStopping();

        // TODO Send the stop signal to every isolate

        if (aStopForkers) {
            // TODO Send the stop signal to every forker
        }

        // TODO Stop the monitors
    }
}
