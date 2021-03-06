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

package org.psem2m.isolates.base.admin;

import java.io.IOException;
import java.util.List;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.architecture.Architecture;
import org.cohorte.herald.HeraldException;
import org.cohorte.herald.IDirectory;
import org.cohorte.herald.IHerald;
import org.cohorte.herald.IMessageListener;
import org.cohorte.herald.MessageReceived;
//import org.cohorte.herald.http.IHttpServiceAvailabilityChecker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.IIsolateLoggerAdmin;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileText;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * Java implementation of the Admin Agent service.
 *
 *
 * @author bdebbabi
 *
 */
@Component(name = IAdminAgent.FACTORY_ADMIN_AGENT)
@Instantiate(name = "cohorte-isolate-base-admin-agent-java")
public class CAdminAgent implements IAdminAgent, IMessageListener {

	/** List of available Architecture service. */
	@Requires(optional = true, specification = Architecture.class)
	private List<Architecture> pArchs;

	/** OSGi Bundle Context */
	private final BundleContext pBundleContext;

	/** The Herald directory */
	@Requires
	private IDirectory pDirectory;

	/** List of available Factories. */
	@Requires(optional = true, specification = Factory.class)
	private List<Factory> pFactories;

	/** Herald API Service */
	@Requires
	private IHerald pHerald;

	/** Cohorte Isolate Logger */
	@Requires
	private IIsolateLoggerSvc pLogger;

	/** Cohorte Isolate Logger Admin */
	@Requires
	private IIsolateLoggerAdmin pLoggerAdmin;

	/** HttpService Availability Checker service */
	// @Requires
	// private IHttpServiceAvailabilityChecker pHttpServiceAvailabilityChecker;

	public CAdminAgent(final BundleContext aBundleContext) {
		pBundleContext = aBundleContext;
	}

	@Override
	public String getBundleDetail(final int aBundleNumber) {
		// TODO Auto-generated method stub
		JSONObject wResult = new JSONObject();
		return wResult.toString();
	}

	@Override
	public String getBundles() {
		JSONArray wResult = new JSONArray();
		Bundle[] wBundles = pBundleContext.getBundles();
		for (int i = -1; i < wBundles.length; i++) {
			Bundle wBundle = null;
			if (i == -1) {
				wBundle = pBundleContext.getBundle(0);
			} else {
				wBundle = wBundles[i];
			}
			JSONObject wBundleJson = new JSONObject();
			try {
				wBundleJson.put("id", wBundle.getBundleId());
				wBundleJson.put("name", wBundle.getSymbolicName());
				wBundleJson.put("state",
						getBundleStateAsString(wBundle.getState()));
				wBundleJson.put("version", wBundle.getVersion());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wResult.put(wBundleJson);
		}
		return wResult.toString();
	}

	private String getBundleStateAsString(final int aState) {
		switch (aState) {
		case Bundle.ACTIVE: {
			return "ACTIVE";
		}
		case Bundle.INSTALLED: {
			return "INSTALLED";
		}
		case Bundle.RESOLVED: {
			return "RESOLVED";
		}
		case Bundle.STARTING: {
			return "STARTING";
		}
		case Bundle.STOPPING: {
			return "STOPPING";
		}
		case Bundle.UNINSTALLED: {
			return "UNINSTALLED";
		}
		default:
			return "UNKNOWN";
		}
	}

	@Override
	public String getComponentInstanceDetail(final String aInstanceName) {
		JSONObject wResult = new JSONObject();
		wResult.put("kind", "Java");
		for (Architecture arch : pArchs) {
			if (arch.getInstanceDescription().getName()
					.equalsIgnoreCase(aInstanceName)) {
				wResult.put("name", arch.getInstanceDescription().getName());
				wResult.put("factory", arch.getInstanceDescription()
						.getInstance().getFactory().getName());
				wResult.put("state", getInstanceStateAsString(arch
						.getInstanceDescription().getState()));
				wResult.put("bundle-id", new Long(arch.getInstanceDescription()
						.getBundleId()).toString());
				wResult.put("properties", "");
				wResult.put("dependencies", "");
				wResult.put("error-trace", "");
				break;
			}
		}
		return wResult.toString();
	}

	@Override
	public String getComponentInstances() {
		JSONArray wResult = new JSONArray();
		for (Architecture arch : pArchs) {
			JSONObject instance = new JSONObject();
			instance.put("name", arch.getInstanceDescription().getName());
			instance.put("factory", arch.getInstanceDescription()
					.getComponentDescription().getName());
			instance.put("state", getInstanceStateAsString(arch
					.getInstanceDescription().getState()));
			wResult.put(instance);
		}
		return wResult.toString();
	}

	@Override
	public String getFactories() {
		JSONArray wResult = new JSONArray();
		try {
			for (Factory wFactory : pFactories) {
				JSONObject factory = new JSONObject();
				factory.put("name", wFactory.getName());
				JSONObject bundle = new JSONObject();
				bundle.put("id", wFactory.getBundleContext().getBundle()
						.getBundleId());
				bundle.put("name", wFactory.getBundleContext().getBundle()
						.getSymbolicName());
				factory.put("bundle", bundle);
				wResult.put(factory);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wResult.toString();
	}

	@Override
	public String getFactoryDetail(final String aFactoryName) {
		JSONObject wResult = new JSONObject();
		wResult.put("kind", "Java");
		try {
			for (Factory wFactory : pFactories) {
				if (wFactory.getName().equalsIgnoreCase(aFactoryName)) {
					wResult.put("name", wFactory.getName());
					JSONObject wBundle = new JSONObject();
					wBundle.put("id", wFactory.getBundleContext().getBundle()
							.getBundleId());
					wBundle.put("name", wFactory.getBundleContext().getBundle()
							.getSymbolicName());
					wResult.put("bundle", wBundle);
					wResult.put("properties", "");
					wResult.put("handlers", "");
					wResult.put("requirements", "");
					wResult.put("provided-services", "");
					break;
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return wResult.toString();
	}

	private String getInstanceStateAsString(final int aState) {
		switch (aState) {
		case ComponentInstance.VALID:
			return "VALID";
		case ComponentInstance.INVALID:
			return "INVALID";
		case ComponentInstance.DISPOSED:
			return "DISPOSED";
		case ComponentInstance.STOPPED:
			return "STOPPED";
		default:
			return "UNKNOWN";
		}
	}

	@Override
	public String getIsolateAccesses() {
		JSONObject wResult = new JSONObject();
		for (String wAccess : pDirectory.getLocalPeer().getAccesses()) {
			try {
				wResult.put(wAccess,
						pDirectory.getLocalPeer().getAccess(wAccess).dump());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return wResult.toString();
	}

	@Override
	public String getIsolateDetail() {
		JSONObject wResult = new JSONObject();
		try {
			wResult.append("cohorte.isolate.uid", pDirectory.getLocalPeer()
					.getUid());
			wResult.append("cohorte.isolate.name", pDirectory.getLocalPeer()
					.getName());
			wResult.append("cohorte.isolate.http.port", ((Object[]) pDirectory
					.getLocalPeer().getAccess("http").dump())[1].toString());
			wResult.append("cohorte.isolate.kind", "Java");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wResult.toString();
	}

	@Override
	public Object getIsolateDirectory() {
		return pDirectory.dump();
		// JSONObject wResult = new JSONObject(pDirectory.dump());
		// return wResult.toString();
	}

	@Override
	public String getIsolateLog(final String aLogId) {
		JSONObject wResult = new JSONObject();

		String wLoggerInfos = pLoggerAdmin.getLoggerInfos("application/json");

		try {
			JSONObject wLoggerInfosJson = new JSONObject(wLoggerInfos);
			JSONArray wFiles = (JSONArray) wLoggerInfosJson.get("files");
			for (int i = 0; i < wFiles.length(); i++) {
				JSONObject wFileMeta = (JSONObject) wFiles.get(i);
				Object wLogId = wFileMeta.get("path");
				JSONObject wFile = new JSONObject();
				if (wLogId != null) {
					CXFile file = new CXFile(wLogId.toString());
					if (file != null) {
						if (aLogId.equalsIgnoreCase(file
								.getNameWithoutExtension())) {
							CXFileText wFileResult = new CXFileText(file);
							try {
								wResult.put("content", wFileResult.readAll());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return wResult.toString();

	}

	@Override
	public String getIsolateLogs() {
		JSONObject wResult = new JSONObject();

		JSONArray wLogsJson = new JSONArray();
		try {
			String wLoggerInfos = pLoggerAdmin
					.getLoggerInfos("application/json");
			JSONObject wLoggerInfosJson = new JSONObject(wLoggerInfos);
			wResult.put("level", wLoggerInfosJson.get("level"));
			JSONArray wFiles = (JSONArray) wLoggerInfosJson.get("files");
			for (int i = 0; i < wFiles.length(); i++) {
				JSONObject wFileMeta = (JSONObject) wFiles.get(i);
				Object wLogId = wFileMeta.get("path");
				Object wLastModified = wFileMeta.get("lastmodified");
				JSONObject wFile = new JSONObject();
				if (wLogId != null) {
					CXFile file = new CXFile(wLogId.toString());
					if (file != null) {
						wFile.put(file.getNameWithoutExtension(),
								wLastModified.toString());
						wLogsJson.put(wFile);
					}
				}
			}
			wResult.put("log-files", wLogsJson);
		} catch (Throwable e) {
			// e.printStackTrace();
			pLogger.logSevere(this, "getIsolateLogs", "Error! %s",
					e.getMessage());
		}

		return wResult.toString();
	}

	@Override
	public String getServices() {
		// TODO Auto-generated method stub
		JSONObject wResult = new JSONObject();
		return wResult.toString();
	}

	@Override
	public String getThreads() {
		// TODO Auto-generated method stub
		JSONObject wResult = new JSONObject();
		return wResult.toString();
	}

	@Override
	public void heraldMessage(final IHerald aHerald,
			final MessageReceived aMessage) throws HeraldException {
		// get message subject
		String wMessageSubject = aMessage.getSubject();
		Object wReply = null;

		if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_ISOLATE_DETAIL)) {
			wReply = getIsolateDetail();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_BUNDLES)) {
			wReply = getBundles();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_BUNDLE_DETAIL)) {
			Object wBundleNumber = aMessage.getContent();
			wReply = getBundleDetail(new Integer(wBundleNumber.toString()));
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_FACTORIES)) {
			wReply = getFactories();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_FACTORY_DETAIL)) {
			Object wFactoryName = aMessage.getContent();
			wReply = getFactoryDetail(wFactoryName.toString());
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_INSTANCES)) {
			wReply = getComponentInstances();
		} else if (wMessageSubject
				.equalsIgnoreCase(SUBJECT_GET_INSTANCE_DETAIL)) {
			Object wInstanceName = aMessage.getContent();
			wReply = getComponentInstanceDetail(wInstanceName.toString());
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_SERVICES)) {
			wReply = getServices();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_THREADS)) {
			wReply = getThreads();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_ISOLATE_LOGS)) {
			wReply = getIsolateLogs();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_ISOLATE_LOG)) {
			Object wLogId = aMessage.getContent();
			wReply = getIsolateLog(wLogId.toString());
		} else if (wMessageSubject
				.equalsIgnoreCase(SUBJECT_GET_ISOLATE_DIRECTORY)) {
			wReply = getIsolateDirectory();
		} else if (wMessageSubject
				.equalsIgnoreCase(SUBJECT_GET_ISOLATE_ACCESSES)) {
			wReply = getIsolateAccesses();
		} else if (wMessageSubject
				.equalsIgnoreCase(SUBJECT_SET_ISOLATE_LOGS_LEVEL)) {
			Object wLogLevel = aMessage.getContent();
			wReply = setIsolateLogsLevel(wLogLevel.toString());
		}

		if (wReply != null) {
			aHerald.reply(aMessage, wReply);
		} else {
			aHerald.reply(aMessage, "No value!");
		}
	}

	/**
	 * Component invalidated
	 */
	@Invalidate
	public void invalidate() {
		if (pHerald != null) {
			pHerald.removeMessageListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.base.admin.IAdminAgent#setIsolateLogsLevel(java.lang
	 * .String)
	 */
	@Override
	public String setIsolateLogsLevel(final String aLogLevel) {
		JSONObject wResult = new JSONObject();
		String wOldLevel = pLoggerAdmin.setLevel(aLogLevel);
		wResult.put("old_log_level", wOldLevel);
		wResult.put("new_log_level", aLogLevel);
		return wResult.toString();
	}

	/**
	 * Component validated
	 */
	@Validate
	public void validate() {

		// register herald listener
		String[] wFilters = { SUBJECT_MATCH_ALL };
		pHerald.addMessageListener(this, wFilters);

	}

}
