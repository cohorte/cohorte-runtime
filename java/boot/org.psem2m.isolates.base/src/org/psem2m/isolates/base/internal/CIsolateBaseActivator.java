/**
 * Copyright 2014 isandlaTech
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

package org.psem2m.isolates.base.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.IIsolateBaseActivator;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.bundles.impl.CBundleFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CFileFinderSvc;
import org.psem2m.isolates.base.dirs.impl.CPlatformDirsSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.logging.CActivityLoggerBasicConsole;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * This is the activator of the bundle "org.psem2m.isolates.base". This bundle
 * is present in each COHORTE Java isolate
 *
 * This bundle aims to put in place the basic services used by all the
 * componenent deployed in the isolate
 *
 * <ul>
 * <li>IPlatformDirsSvc
 *
 * <li>IIsolateLoggerSvc : the main ActivityLogger service of the isolate (the
 * file
 *
 * <li>LogService Factory: the factory of "org.osgi.service.log.LogService". The
 * implementation "CLogServiceFactory" returns an implementation of the OSGi log
 * service (CLogInternal) which wrapps the main ActivityLogger of the isolate.
 *
 * <li>LogReaderService factory : the factory of
 * "org.osgi.service.log.LogReaderService". The implementation
 * "CLogReaderServiceFactory" returns an implementation of the OSGi log reader
 * service (CLogReaderServiceImpl) which adds and removes listener on the
 * CLogInternal instance.
 *
 * <li>IFileFinderSvc : a file finder service. This service tries to find files
 * in the platform folders using their file name. This service uses the
 * IPlatformDirsSvc.
 *
 * <li>IBundleFinderSvc : a bundle file finder service. This service tries the
 * files of the bundles using their simpoblic name. This service uses the
 * IPlatformDirsSvc.
 *
 * </ul>
 *
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * @author Thomas Calmant
 */
@Component(name = "cohorte-isolate-base-activator-factory", factoryMethod = "getSingleton")
@Instantiate(name = "cohorte-isolate-base-activator")
@Provides(specifications = { IIsolateBaseActivator.class })
public class CIsolateBaseActivator extends CXObjectBase implements
		BundleActivator, IIsolateBaseActivator {

	/**
	 * Service Infos bean
	 *
	 * @author ogattaz
	 *
	 */
	private class CServiceInfos {

		/** the name of the service **/
		private final String pServiceName;

		/** the registration info of the service **/
		private final ServiceRegistration<?> pServiceRegistration;

		public CServiceInfos(final ServiceRegistration<?> aServiceRegistration,
				final String aServiceName) {

			pServiceRegistration = aServiceRegistration;
			pServiceName = aServiceName;
		}

		/**
		 * @return
		 */
		public String getServiceName() {

			return pServiceName;
		}

		/**
		 * @return
		 */
		public ServiceRegistration<?> getServiceRegistration() {

			return pServiceRegistration;
		}
	}

	/** Maximum log files for the LogService */
	public static final int LOG_FILES_COUNT = 10;

	/** Maximum log file size (10 Mo) */
	public static final int LOG_FILES_SIZE = 10 * 1024 * 1024;

	private static final String SERVICE_DESCRIPTION = org.osgi.framework.Constants.SERVICE_DESCRIPTION;

	private static final String SERVICE_RANKING = org.osgi.framework.Constants.SERVICE_RANKING;

	private static CIsolateBaseActivator sMe;

	/**
	 * @return
	 */
	public static CIsolateBaseActivator getSingleton() {
		return sMe;
	}

	/** Log instance underlying logger */
	private IActivityLogger pActivityLogger;

	/** The bundle context */
	private BundleContext pBundleContext;

	/** Bundle finder service */
	private CBundleFinderSvc pBundleFinderSvc;

	/** File finder service */
	private CFileFinderSvc pFileFinderSvc;

	/** Log service available for all the bundles of the isolate */
	private CIsolateLoggerSvc pIsolateLoggerSvc;

	/** Internal log handler */
	private CLogInternal pLogInternal;

	/** Log reader service factory */
	private CLogReaderServiceFactory pLogReaderServiceFactory;

	/** Log service factory */
	private CLogServiceFactory pLogServiceFactory;

	/** Platform directories service */
	private CPlatformDirsSvc pPlatformDirsSvc;

	/** OSGi services registration */
	private final List<CServiceInfos> pRegisteredServicesInfos = new ArrayList<CServiceInfos>();

	/** The service listener */
	private ServiceListener pRegistrationListener = null;

	/**
	 *
	 */

	private final String UNKNOWN_ISOLATE_NAME = "UnknownIsolate";

	/**
	 * do nothing ! This Activator is instanciated by the Osgi framework, the
	 * first metod to be called is the method "start()"
	 */
	public CIsolateBaseActivator() {

		super();
		sMe = this;
	}

	/**
	 * @param aReport
	 * @param aFormat
	 * @param aArgs
	 */
	private void addLineInReport(final StringBuilder aReport,
			final String aFormat, final Object... aArgs) {
		aReport.append(String.format('\n' + aFormat, aArgs));
	}

	/**
	 *
	 */
	private void destroyLogger() {

		getLogger().close();
		pActivityLogger = null;
	}

	/**
	 * Creates or retrieves an instance of the bundle finder
	 *
	 * @return A bundle finder instance
	 */
	public IBundleFinderSvc getBundleFinder() {

		if (pBundleFinderSvc == null) {
			pBundleFinderSvc = new CBundleFinderSvc(getPlatformDirs());
		}

		return pBundleFinderSvc;
	}

	/**
	 * Creates or retrieves an instance of the file finder
	 *
	 * @return A file finder instance
	 */
	public IFileFinderSvc getFileFinder() {

		if (pFileFinderSvc == null) {
			pFileFinderSvc = new CFileFinderSvc(getPlatformDirs());
		}

		return pFileFinderSvc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.base.IIsolateBaseActivator#getIsolateLoggerChannel()
	 */
	@Override
	public CIsolateLoggerChannel getIsolateLoggerChannel() {

		return hasIsolateLoggerChannel() ? (CIsolateLoggerChannel) pActivityLogger
				: null;
	}

	/**
	 * Retrieves the log service instance, creates it if needed
	 *
	 * @return The log service instance
	 * @throws Exception
	 *             An error occurred while preparing the logger
	 */
	public CIsolateLoggerSvc getIsolateLoggerSvc() throws Exception {

		if (pIsolateLoggerSvc == null) {
			pIsolateLoggerSvc = new CIsolateLoggerSvc(getLogger());
		}

		return pIsolateLoggerSvc;
	}

	/**
	 * return the activity logger instance.
	 *
	 * @return The activity logger
	 * @throws Exception
	 *             An error occurred while preparing the logger
	 */
	protected IActivityLogger getLogger() {

		// if no logger already created
		if (pActivityLogger == null) {
			initIsolateLoggerChannel(pBundleContext);
		}

		return pActivityLogger;
	}

	/**
	 * Creates or retrieves an instance of the internal log handler
	 *
	 * @return the internal log handler
	 * @throws Exception
	 *             An error occurred while preparing the underlying logger
	 */
	public CLogInternal getLogInternal() throws Exception {

		if (pLogInternal == null) {
			pLogInternal = new CLogInternal(getLogger());
		}

		return pLogInternal;
	}

	/**
	 * Creates or retrieves an instance of the log reader service factory
	 *
	 * @return the log reader service factory
	 * @throws Exception
	 *             An error occurred while preparing the underlying logger
	 */
	public CLogReaderServiceFactory getLogReaderServiceFactory()
			throws Exception {

		if (pLogReaderServiceFactory == null) {
			pLogReaderServiceFactory = new CLogReaderServiceFactory(
					getLogInternal());
		}

		return pLogReaderServiceFactory;
	}

	/**
	 * Creates or retrieves an instance of the log service factory
	 *
	 * @return A log service factory instance
	 * @throws Exception
	 *             An error occurred while preparing the underlying logger
	 */
	public CLogServiceFactory getLogServiceFactory() throws Exception {

		// if no LogServiceFactory already created
		if (pLogServiceFactory == null) {
			pLogServiceFactory = new CLogServiceFactory(getLogInternal());
		}

		return pLogServiceFactory;
	}

	/**
	 * Creates or retrieves an instance of the platform directories registry
	 *
	 * @param aContext
	 *            The bundle context
	 * @return A platform directories registry instance
	 */
	public IPlatformDirsSvc getPlatformDirs() {

		// if no PlatformDirsSvc already created
		if (pPlatformDirsSvc == null) {
			pPlatformDirsSvc = new CPlatformDirsSvc(pBundleContext);
		}

		return pPlatformDirsSvc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.base.IIsolateBaseActivator#hasIsolateLoggerChannel()
	 */
	@Override
	public boolean hasIsolateLoggerChannel() {
		return (pActivityLogger instanceof CIsolateLoggerChannel);
	}

	/**
	 * @return true if the IsolateLoggerSvc is available
	 */
	@Override
	public boolean hasIsolateLoggerSvc() {

		return pIsolateLoggerSvc != null;
	}

	/**
	 * @param aBundleContext
	 */
	private void initIsolateLoggerChannel(final BundleContext aBundleContext) {

		final StringBuilder wReport = new StringBuilder();

		String wIsolateName = UNKNOWN_ISOLATE_NAME;

		final String wStr = aBundleContext
				.getProperty(IPlatformProperties.PROP_ISOLATE_NAME);

		if (wStr == null) {
			addLineInReport(
					wReport,
					"ERROR: The system property [%s] is not available : verify the isolate launcher",
					IPlatformProperties.PROP_ISOLATE_NAME);
		}
		//
		else if (wStr.isEmpty()) {
			addLineInReport(
					wReport,
					"ERROR: The system property [%s] is not empty : verify the isolate launcher",
					IPlatformProperties.PROP_ISOLATE_NAME);

		}
		//
		else {
			wIsolateName = wStr;

			addLineInReport(wReport, "The system property [%s] contains [%s]",
					IPlatformProperties.PROP_ISOLATE_NAME, wIsolateName);
		}

		try {

			// Be sure we have a valid platform service instance
			final IPlatformDirsSvc wPlatformDirsSvc = getPlatformDirs();

			File wLogStorageDir = wPlatformDirsSvc.getIsolateLogDir();

			// the name of the logger
			final String wLoggerName = String.format("cohorte.isolate.%s.%s",
					wIsolateName, wPlatformDirsSvc.getIsolateUID());

			addLineInReport(wReport, "The name of the logger : [%s]",
					wLoggerName);

			final String wLogStoragePath = aBundleContext
					.getProperty(IPlatformProperties.PROP_ISOLATE_LOG_STORAGE);

			if (wLogStoragePath != null) {
				if (wLogStoragePath.isEmpty()) {
					addLineInReport(wReport,
							"ERROR: The system property [%s] is empty",
							IPlatformProperties.PROP_ISOLATE_LOG_STORAGE);
				} else {
					addLineInReport(wReport,
							"The system property [%s] contains [%s]",
							IPlatformProperties.PROP_ISOLATE_LOG_STORAGE,
							wLogStoragePath);

					final File wDir = new File(wLogStoragePath);
					if (!wDir.exists()) {
						addLineInReport(
								wReport,
								"ERROR: The given log storage path [%s] doesn't exist",
								wDir);
					}
					//
					else if (!wDir.isDirectory()) {
						addLineInReport(
								wReport,
								"ERROR: The given log storage path [%s] isn't a directory",
								wDir);

					}
					// The given log storage path is an existing directory
					else {
						wLogStorageDir = wDir;
					}
				}
			}

			addLineInReport(wReport, "The log storage path : [%s]",
					wLogStorageDir.getAbsolutePath());

			// the FilePathPattern of the logger
			final StringBuilder wSB = new StringBuilder();

			wSB.append(wLogStorageDir.getAbsolutePath());
			wSB.append(File.separator);
			wSB.append("Log-");
			wSB.append(wIsolateName);
			wSB.append("-%g.txt");

			final String wFilePathPattern = wSB.toString();

			// level ALL by default !
			pActivityLogger = new CIsolateLoggerChannel(wLoggerName,
					wFilePathPattern, IActivityLoggerBase.ALL, LOG_FILES_SIZE,
					LOG_FILES_COUNT);

			addLineInReport(wReport, "ActivityLogger opened : %s",
					((CIsolateLoggerChannel) pActivityLogger).toDescription());

		} catch (final Exception e) {
			pActivityLogger = CActivityLoggerBasicConsole.getInstance();

			addLineInReport(wReport,
					"ERROR: Can't instanciate a CIsolateLoggerChannel : %s",
					CXException.eInString(e));
		}

		// Logs the report, the java context and the environment context
		pActivityLogger
		.logInfo(this, "initLogger", "Initialized:%s %s %s", wReport,
				CXJvmUtils.getJavaContext(), CXOSUtils.getEnvContext());
	}

	/**
	 * @param aServiceInterface
	 * @param aService
	 * @param aState
	 */
	private void logServiceManipulation(final String aServiceName,
			final String aState) {

		getLogger().logDebug(this, "logServiceRegistering", "Service=",
				aServiceName, aState + '=', true);
	}

	/**
	 * log the registration of a service in the logger of the isolate
	 *
	 * @param aName
	 * @param aService
	 * @throws Exception
	 */
	private void logServiceRegistration(final String aServiceName) {

		logServiceManipulation(aServiceName, "Registered");
	}

	/**
	 * log the unregistration of a service in the logger of the isolate
	 *
	 * @param aName
	 * @param aService
	 * @throws Exception
	 */
	private void logServiceUnregistration(final String aServiceName) {

		logServiceManipulation(aServiceName, "Unregistered");
	}

	/**
	 * @param aBundleContext
	 */
	private void putInPlaceRegistrationListener(
			final BundleContext aBundleContext) {

		pRegistrationListener = new ServiceListener() {

			@Override
			public void serviceChanged(final ServiceEvent aServiceEvent) {

				final ServiceReference<?> wServiceReference = aServiceEvent
						.getServiceReference();

				final String[] types = (String[]) wServiceReference
						.getProperty(Constants.OBJECTCLASS);

				final String wServiceClass = types != null ? Arrays
						.toString(types) : "<null>";

				// FIXME class loader dead lock
				// see:
				// http://underlap.blogspot.fr/2006/11/experimental-fix-for-sunbug-4670071.html
				// // Get the service with OUR bundle context
				// final Object wService = aBundleContext
				// .getService(wServiceReference);
				//
				// // Get the class name
				// final String wServiceClass = wService.getClass().getName();
				//
				// // Release the service
				// aBundleContext.ungetService(wServiceReference);

				switch (aServiceEvent.getType()) {
				case ServiceEvent.REGISTERED:
					logServiceRegistration(wServiceClass);
					break;

				case ServiceEvent.UNREGISTERING:
					logServiceUnregistration(wServiceClass);
					break;

				default:
					break;
				}
			}
		};

		try {
			aBundleContext.addServiceListener(pRegistrationListener, null);

		} catch (final InvalidSyntaxException e) {
			getLogger().logSevere(this, "putInPlaceRegistrationListener",
					"Can't register the service listener.", e);
		}
	}

	/**
	 * @param aServiceInterface
	 * @param aService
	 */
	private <S> void registerOneService(final BundleContext aBundleContext,
			final Class<S> aServiceInterface, final S aService) {

		try {
			final ServiceRegistration<S> registration = aBundleContext
					.registerService(aServiceInterface, aService, null);
			pRegisteredServicesInfos.add(new CServiceInfos(registration,
					aServiceInterface.getName()));
			logServiceRegistration(aServiceInterface.getName());

		} catch (final Exception e) {

			getLogger().logSevere(this, "registerOneService",
					"Can't register the service [%s]. %s.", aServiceInterface,
					e);
		}
	}

	/**
	 * @param aServiceInterface
	 * @param aService
	 */
	private <S> void registerOneServiceFactory(
			final BundleContext aBundleContext,
			final Class<S> aServiceInterface,
			final ServiceFactory<S> aServiceFactory,
			final Dictionary<String, ?> aProps) {

		try {
			final ServiceRegistration<?> registration = aBundleContext
					.registerService(aServiceInterface.getName(),
							aServiceFactory, aProps);
			pRegisteredServicesInfos.add(new CServiceInfos(registration,
					aServiceInterface.getName()));
			logServiceRegistration(aServiceInterface.getName());

		} catch (final Exception e) {

			getLogger().logSevere(this, "registerOneService",
					"Can't register the service [%s]. %s.", aServiceInterface,
					e);
		}
	}

	/**
	 * @param aBundleContext
	 */
	private void removeRegistrationListener(final BundleContext aBundleContext) {

		if (pRegistrationListener != null) {
			try {
				aBundleContext.removeServiceListener(pRegistrationListener);
			} catch (final Exception e) {
				getLogger()
						.logSevere(
								this,
								"removeServiceLogger",
								"Can't remove the listener of all the service registering and unregistering",
								e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext aBundleContext) {

		final Bundle wBundle = aBundleContext.getBundle();
		System.out.printf("%50s | Bundle=[%50s][%s] starting\n",
				"CBundleBaseActivator.start()", wBundle.getSymbolicName(),
				wBundle.getVersion());

		// Store the bundle context
		pBundleContext = aBundleContext;

		// IMPORTANT : this first call to the method "getLogger()" creates the
		// IsolateLoggerChannel instance
		getLogger().logInfo(this, "start", "START", toDescription(),
				((CPlatformDirsSvc) getPlatformDirs()).toDescription());

		// Register platform directories service
		registerOneService(aBundleContext, IPlatformDirsSvc.class,
				getPlatformDirs());

		try {
			// register own LogService service factory

			/**
			 * OSGi Ranking policy : The best way of prioritizing OSGi services
			 * to use SERVICE_RANKING service property.
			 *
			 * A service with a ranking of Integer.MAX_VALUE is very likely to
			 * be returned as the default service
			 *
			 * <ul>
			 * <li>SERVICE_DESCRIPTION "service.description"
			 * <li>SERVICE_RANKING "service.ranking"
			 * </ul>
			 *
			 * @see https
			 *      ://osgi.org/javadoc/r4v42/org/osgi/framework/Constants.html
			 *      #SERVICE_RANKING
			 */

			Dictionary<String, Object> wProps = new Hashtable<String, Object>();
			wProps.put(SERVICE_DESCRIPTION, "cohorte");
			wProps.put(SERVICE_RANKING, Integer.MAX_VALUE);

			registerOneServiceFactory(aBundleContext, LogService.class,
					getLogServiceFactory(), wProps);

		} catch (final Exception e) {
			getLogger()
					.logSevere(
							this,
							"start",
							"Can't get the LogServiceFactory and register it as 'LogService'",
							e);
		}
		try {
			// register own LogReader service factory

			Dictionary<String, Object> wProps = new Hashtable<String, Object>();
			wProps.put(SERVICE_DESCRIPTION, "cohorte");
			wProps.put(SERVICE_RANKING, Integer.MAX_VALUE);

			registerOneServiceFactory(aBundleContext, LogReaderService.class,
					getLogReaderServiceFactory(), wProps);
		} catch (final Exception e) {
			getLogger()
					.logSevere(
							this,
							"start",
							"Can't get the LogReaderServiceFactory and register it as 'LogReaderService'",
							e);
		}
		try {
			/*
			 * Register the service IIsolateLoggerSvc.
			 *
			 * Note: the CIsolateLoggerSvc instance wrapps the
			 * IsolateLoggerChannel created during the first call to the method
			 * "getLogger()" at the begining of the method start().
			 */
			registerOneService(aBundleContext, IIsolateLoggerSvc.class,
					getIsolateLoggerSvc());
		} catch (final Exception e) {
			getLogger()
					.logSevere(
							this,
							"start",
							"Can't get the IsolateLoggerSvc and register it as 'IIsolateLoggerSvc'",
							e);
		}

		// Register the service file finder
		registerOneService(aBundleContext, IFileFinderSvc.class,
				getFileFinder());

		// Register the service bundle finder
		registerOneService(aBundleContext, IBundleFinderSvc.class,
				getBundleFinder());

		// put in place a listner witch logs each service registration and
		// unregistration
		putInPlaceRegistrationListener(aBundleContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext aBundleContext) {

		try {
			getLogger().logInfo(this, "stop", "STOP", toDescription());
		} catch (final Exception e) {
			System.err.println("Can't log the begining of the stop method");
			e.printStackTrace();
		}
		// remove the listner witch logs each service registration and
		// unregistration
		removeRegistrationListener(aBundleContext);

		// Unregister all services
		for (final CServiceInfos wServiceInfos : pRegisteredServicesInfos) {
			wServiceInfos.getServiceRegistration().unregister();
			logServiceUnregistration(wServiceInfos.getServiceName());
		}

		pRegisteredServicesInfos.clear();

		getLogger().logInfo(this, "stop", "STOP ENDED");

		// The end of the isolate
		destroyLogger();

		pBundleContext = null;

		System.out.printf("%50s | Bundle=[%50s] stopped\n",
				"CBundleBaseActivator.stop()", aBundleContext.getBundle()
						.getSymbolicName());
	}
}
