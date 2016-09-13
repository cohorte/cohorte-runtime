/**
 * Copyright 2016 Cohorte Technologies (ex. isandlaTech)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cohorte.isolates.discovery.local.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.herald.Access;
import org.cohorte.herald.HeraldException;
import org.cohorte.herald.IDirectory;
import org.cohorte.herald.IDirectoryListener;
import org.cohorte.herald.IHerald;
import org.cohorte.herald.IMessageListener;
import org.cohorte.herald.ITransport;
import org.cohorte.herald.Message;
import org.cohorte.herald.MessageReceived;
import org.cohorte.herald.Peer;
import org.cohorte.herald.http.HTTPExtra;
import org.cohorte.herald.http.IHttpConstants;
import org.cohorte.herald.http.impl.IHttpReceiver;
import org.cohorte.herald.transport.IDiscoveryConstants;
import org.cohorte.isolates.discovery.local.IConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * Discovery of local peers (of the same node) based on the forker.
 * 
 * @author Bassem Debbabi
 *
 */
@Component(name = IConstants.FACTORY_DISCOVERY_LOCAL)
@Instantiate(name = "cohorte-local-discovery")
public class CLocalDiscovery implements IMessageListener, IDirectoryListener {
	
	/** The Herald directory */
	@Requires
	private IDirectory pDirectory;
	
	/** The HTTP transport implementation */
	@Requires(filter = "(" + org.cohorte.herald.IConstants.PROP_ACCESS_ID + "="
			+ IHttpConstants.ACCESS_ID + ")")
	private ITransport pHttpTransport;
	
	/** Herald API Service */
	@Requires
	private IHerald pHerald;
	
	/** Local peer bean */
	private Peer pLocalPeer;
	
	/** Forker peer bean */
	private Peer pForkerPeer;
	
	/** The logger */
	@Requires(optional = true)
	private LogService pLogger;
	
	/** The HTTP reception part */
	@Requires
	private IHttpReceiver pReceiver;
	
	/** OSGi Bundle Context */
	private BundleContext pBundleContext;
	
	/** The contacting Forker thread */
    private Thread pDiscoverForkerThread;
    
    /** The contacting Neighbors thread */
    private Thread pDiscoverNeighborsThread;
	
	private int pForkerPort = 8080;
	private String pForkerHost = "127.0.0.1";
	private String pForkerPath = "/herald";
	
	public CLocalDiscovery(BundleContext aBundleContext) {
		pBundleContext = aBundleContext;
	}
	
	/**
	 * Grab the description of a peer using the Herald servlet
	 *
	 * @param aHostAddress
	 *            Address which sent the heart beat
	 * @param aPort
	 *            Port of the Herald HTTP server
	 * @param aPath
	 *            Path to the Herald HTTP servlet
	 */
	private void discoverPeer(final String aHostAddress, final int aPort,
			final String aPath) {		
		// Prepare extra information like for a reply
		final HTTPExtra extra = new HTTPExtra(aHostAddress, aPort, aPath, null);

		try {
			// Fire the message, using the HTTP transport directly
			// Peer registration will be done after it responds
			pHttpTransport.fire(null, new Message(
					IDiscoveryConstants.SUBJECT_DISCOVERY_STEP_1, pDirectory
							.getLocalPeer().dump()), extra);

		} catch (final HeraldException ex) {
			pLogger.log(LogService.LOG_ERROR, "Error contacting peer: " + ex,
					ex);
		}
	}
	
	
	/**
	 * Component invalidated
	 */
	@Invalidate
	public void invalidate() {
		if (pDiscoverForkerThread != null) {
			pDiscoverForkerThread.interrupt();
			try {
				pDiscoverForkerThread.join(500);
			} catch (final InterruptedException e) {
				// Ignore
			}			
		}
		
		// Clean up
		pDiscoverForkerThread = null;
		pLocalPeer = null;
	}
	
	/**
	 * Component validated
	 */
	@Validate
	public void validate() {
				
		// Get the local peer
		pLocalPeer = pDirectory.getLocalPeer();
		
		// register herald listener
		String[] wFilters = { IConstants.SUBJECT_GET_NEIGHBORS_LIST };
		pHerald.addMessageListener(this, wFilters);
	
		// get concrete http path servlet of HTTP Receiver component
		pForkerPath = pReceiver.getAccessInfo().getPath();
		
		// get forker's http port and create the thread that discovers the forker
		try {
			pForkerPort = new Integer(pBundleContext.getProperty(IPlatformProperties.PROP_FORKER_HTTP_PORT));
			pDiscoverForkerThread = new Thread(new Runnable() {				
				@Override
				public void run() {
					discoverPeer(pForkerHost, pForkerPort, pForkerPath);
				}	
			});
			pDiscoverForkerThread.start();		
		}
		catch (Exception ex) {
			if (ex instanceof NumberFormatException) {
				pLogger.log(LogService.LOG_ERROR, "The provided forker's Http port is incorrect!: " + ex,
						ex);
			} else {
				pLogger.log(LogService.LOG_ERROR, "Error retrieving the forker Http port: " + ex,
						ex);
			}
		}
	}

	@Override
	public void heraldMessage(IHerald aHerald, MessageReceived aMessage)
			throws HeraldException {
		// TODO
	}

	@Override
	public void peerRegistered(Peer aPeer) {
		if (aPeer != null) {
			// applies only for local discovery components of isolates not forker
			if (pLocalPeer.getName().equalsIgnoreCase(IPlatformProperties.SPECIAL_NAME_FORKER) == false) {				
				// monitor only local peers (same node)
				if (aPeer.getNodeUid().equalsIgnoreCase(pLocalPeer.getNodeUid())) {
					// if the registered peer is the forker
					if (aPeer.getName().equalsIgnoreCase(IPlatformProperties.SPECIAL_NAME_FORKER)) {
						pForkerPeer = aPeer;
						pDiscoverNeighborsThread = new Thread(new Runnable() {
							
							@Override
							public void run() {
								discoverNeighbors();
							}

							private void discoverNeighbors() {
								try {
									Object wReply = pHerald.send(pForkerPeer.getUid(), 
											new Message(IConstants.SUBJECT_GET_NEIGHBORS_LIST));
									if (wReply != null) {
										pLogger.log(LogService.LOG_ERROR, "##### NEIGHBORS: " + wReply);
									} else {
										pLogger.log(LogService.LOG_ERROR, "##### No Reply!");
									}
								} catch (HeraldException e) {
									pLogger.log(LogService.LOG_ERROR, "Couldn't retrieve the list of neighbors from the forker. "
											+ "Impossible to send the message!");
								}
							}
						});
						pDiscoverNeighborsThread.start();
					}
				}
			}
		}
	}

	@Override
	public void peerUnregistered(Peer aPeer) {
		// nothing to do!	
	}

	@Override
	public void peerUpdated(Peer aPeer, String aAccessId, Access aData,
			Access aPrevious) {
		// nothing to do!
	}
}
