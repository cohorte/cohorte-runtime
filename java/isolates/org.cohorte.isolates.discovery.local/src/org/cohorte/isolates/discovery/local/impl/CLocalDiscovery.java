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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
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
import org.cohorte.herald.UnknownPeer;
import org.cohorte.herald.http.HTTPExtra;
import org.cohorte.herald.http.IHttpConstants;
import org.cohorte.herald.http.IHttpServiceAvailabilityChecker;
import org.cohorte.herald.http.impl.IHttpReceiver;
import org.cohorte.herald.transport.IDiscoveryConstants;
import org.cohorte.isolates.discovery.local.IConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;

/**
 * Discovery of local peers (of the same node) based on the forker.
 *
 * @author Bassem Debbabi
 *
 */
@Component(name = IConstants.FACTORY_DISCOVERY_LOCAL)
@Provides
@Instantiate(name = IConstants.INSTANCE_DISCOVERY_LOCAL)
public class CLocalDiscovery implements IMessageListener, IDirectoryListener {

	/** OSGi Bundle Context */
	private final BundleContext pBundleContext;

	/** The Herald directory */
	@Requires
	private IDirectory pDirectory;

	/** The contacting Forker thread */
	private Thread pDiscoverForkerThread;

	/** The contacting Neighbors thread */
	private Thread pDiscoverNeighborsThread;

	/** Forker peer host -- will be updated when forker is known */
	private final String pForkerHost = "127.0.0.1";

	/** Forker peer herald path -- will be updated when forker is known */
	private String pForkerPath = "/herald";

	/** Forker peer bean */
	private Peer pForkerPeer;

	/** Forker peer port -- will be updated when forker is known */
	private int pForkerPort = 8080;

	/** Herald API Service */
	@Requires
	private IHerald pHerald;

	@Requires
	private IHttpServiceAvailabilityChecker pHttpServiceAvailabilityChecker;

	/** The HTTP transport implementation */
	@Requires(filter = "(" + org.cohorte.herald.IConstants.PROP_ACCESS_ID + "="
			+ IHttpConstants.ACCESS_ID + ")")
	private ITransport pHttpTransport;

	/** Local peer bean */
	private Peer pLocalPeer;

	/** The logger */
	@Requires(optional = true)
	private LogService pLogger;

	/** The HTTP reception part */
	@Requires
	private IHttpReceiver pReceiver;

	/**
	 * Constructor.
	 *
	 * @param aBundleContext
	 */
	public CLocalDiscovery(final BundleContext aBundleContext) {
		pBundleContext = aBundleContext;
	}

	/**
	 * Discover neighbor peer using the dump received from the forker.
	 *
	 * @param aPeerDump
	 */
	private void discoverNeighbor(final HashMap aPeerDump) {
		String wHost = ((String[]) ((HashMap) aPeerDump.get("accesses"))
				.get("http"))[0];
		String wPort = ((String[]) ((HashMap) aPeerDump.get("accesses"))
				.get("http"))[1];
		String wPath = "/"
				+ ((String[]) ((HashMap) aPeerDump.get("accesses")).get("http"))[2];
		discoverPeer(wHost, new Integer(wPort), wPath);
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
		pLogger.log(LogService.LOG_DEBUG, String.format(
				"Discover peer : host=[%s], port=[%s] path=[%s]",
				extra.getHost(), extra.getPort(), extra.getPath()));
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cohorte.herald.IMessageListener#heraldMessage(org.cohorte.herald.
	 * IHerald, org.cohorte.herald.MessageReceived)
	 */
	@Override
	public void heraldMessage(final IHerald aHerald,
			final MessageReceived aMessage) throws HeraldException {
		String wSubject = aMessage.getSubject();
		String wReply = null;
		if (wSubject.equals(ISignalsConstants.ISOLATE_LOST_SIGNAL)) {
			try {
				Peer wLostPeer = pDirectory.getPeer(aMessage.getContent()
						.toString());
				if (wLostPeer != null
						&& wLostPeer.getNodeUid().equalsIgnoreCase(
								pLocalPeer.getNodeUid())) {
					// we have just to nuset the http access from the peer.
					// if it will have no access, it will be automatically
					// removed by Herald!
					wLostPeer.unsetAccess(IHttpConstants.ACCESS_ID);
				}
			} catch (UnknownPeer e) {
				// do nothing!
			}
		}
		// No need to respond to SUBJECT_GET_NEIGHBORS as java isolates are
		// never forker
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cohorte.herald.IDirectoryListener#peerRegistered(org.cohorte.herald
	 * .Peer)
	 */
	@Override
	public void peerRegistered(final Peer aPeer) {
		if (aPeer != null) {
			pLogger.log(LogService.LOG_DEBUG,
					"New Peer registred: " + aPeer.getName());
			// applies only for local discovery components of isolates not
			// forker
			if (pLocalPeer.getName().equalsIgnoreCase(
					IPlatformProperties.SPECIAL_NAME_FORKER) == false) {
				// monitor only local peers (same node)
				if (aPeer.getNodeUid()
						.equalsIgnoreCase(pLocalPeer.getNodeUid())) {
					// if the registered peer is the forker
					if (aPeer.getName().equalsIgnoreCase(
							IPlatformProperties.SPECIAL_NAME_FORKER)) {
						pForkerPeer = aPeer;
						pDiscoverNeighborsThread = new Thread(new Runnable() {

							private void discoverNeighbors() {
								try {
									Object wReply = pHerald.send(
											pForkerPeer.getUid(),
											new Message(
													IConstants.SUBJECT_GET_NEIGHBORS_LIST,
													pLocalPeer.getUid()));
									if (wReply != null) {
										HashMap wReplyDict = (HashMap) wReply;
										Iterator it = wReplyDict.entrySet()
												.iterator();
										while (it.hasNext()) {
											Map.Entry pair = (Map.Entry) it
													.next();
											discoverNeighbor((HashMap) pair
													.getValue());
										}
									} else {
										pLogger.log(LogService.LOG_INFO,
												"##### No Reply!");
									}
								} catch (HeraldException e) {
									pLogger.log(
											LogService.LOG_ERROR,
											"Couldn't retrieve the list of neighbors from the forker. "
													+ "Impossible to send the message!");
								}
							}

							@Override
							public void run() {
								discoverNeighbors();
							}
						});
						pDiscoverNeighborsThread.start();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cohorte.herald.IDirectoryListener#peerUnregistered(org.cohorte.herald
	 * .Peer)
	 */
	@Override
	public void peerUnregistered(final Peer aPeer) {
		// nothing to do!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cohorte.herald.IDirectoryListener#peerUpdated(org.cohorte.herald.
	 * Peer, java.lang.String, org.cohorte.herald.Access,
	 * org.cohorte.herald.Access)
	 */
	@Override
	public void peerUpdated(final Peer aPeer, final String aAccessId,
			final Access aData, final Access aPrevious) {
		// nothing to do!
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

		// get forker's http port and create the thread that discovers the
		// forker
		try {
			pForkerPort = new Integer(
					pBundleContext
							.getProperty(IPlatformProperties.PROP_FORKER_HTTP_PORT));
			pDiscoverForkerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					discoverPeer(pForkerHost, pForkerPort, pForkerPath);
				}
			});
			pDiscoverForkerThread.start();
		} catch (Exception ex) {
			if (ex instanceof NumberFormatException) {
				pLogger.log(LogService.LOG_ERROR,
						"The provided forker's Http port is incorrect!: " + ex,
						ex);
			} else {
				pLogger.log(LogService.LOG_ERROR,
						"Error retrieving the forker Http port: " + ex, ex);
			}
		}
	}
}
