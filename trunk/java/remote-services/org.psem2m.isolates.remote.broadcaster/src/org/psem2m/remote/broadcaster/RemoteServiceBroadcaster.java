/**
 * File:   RemoteServiceBroadcaster.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remote.broadcaster;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.EEmitterTargets;

/**
 * Implementation of an RSB
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-rsb-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceBroadcaster.class)
@Instantiate(name = "psem2m-remote-rsb")
public class RemoteServiceBroadcaster extends CPojoBase implements
        IRemoteServiceBroadcaster {

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** Platform properties */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Signal sender service, inject by iPOJO */
    @Requires
    private ISignalBroadcaster pSignalEmitter;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO,
                "PSEM2M Remote Service Broadcaster Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster#
     * requestAllEndpoints()
     */
    @Override
    public void requestAllEndpoints() {

        // Ask for monitors and isolates services
        pSignalEmitter.sendData(EEmitterTargets.ALL,
                ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS, null);

        // Internal isolates also request for forker services
        final String isolateId = pPlatformDirsSvc.getIsolateId();
        if (isolateId != null
                && isolateId
                        .startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
            // Send the signal
            pSignalEmitter.sendData(EEmitterTargets.FORKER,
                    ISignalsConstants.BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                    null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster#
     * sendNotification
     * (org.psem2m.isolates.services.remote.beans.RemoteServiceEvent)
     */
    @Override
    public void sendNotification(final RemoteServiceEvent aEvent) {

        pSignalEmitter.sendData(EEmitterTargets.ALL,
                ISignalsConstants.BROADCASTER_SIGNAL_REMOTE_EVENT, aEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO,
                "PSEM2M Remote Service Broadcaster Ready");
    }
}
