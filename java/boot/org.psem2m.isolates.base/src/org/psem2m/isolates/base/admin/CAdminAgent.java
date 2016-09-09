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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
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
	private BundleContext pBundleContext;

	/** The Herald directory */
	@Requires
	private IDirectory pDirectory;

	/** List of available Factories. */
	@Requires(optional = true, specification = Factory.class)
	private List<Factory> pFactories;

	/** Herald API Service */
	@Requires
	private IHerald pHerald;

	public CAdminAgent(BundleContext aBundleContext) {
		pBundleContext = aBundleContext;
	}

	@Override
	public String getBundleDetail(int aBundleNumber) {
		// TODO Auto-generated method stub
		return null;
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

	private String getBundleStateAsString(int aState) {
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
	public String getComponentInstanceDetail(String aInstanceName) {
		// TODO Auto-generated method stub
		return null;
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
	public String getFactoryDetail(String aFactoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getInstanceStateAsString(int aState) {
		switch (aState) {
		case ComponentInstance.VALID:
			return "valid";
		case ComponentInstance.INVALID:
			return "invalid";
		case ComponentInstance.DISPOSED:
			return "disposed";
		case ComponentInstance.STOPPED:
			return "stopped";
		default:
			return "unknown";
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
			wResult.append("cohorte.isolate.http.port", "NOT YET IMPLEMENTED!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wResult.toString();
	}

	@Override
	public String getIsolateDirectory() {
		JSONObject wResult = new JSONObject(pDirectory.dump());
		return wResult.toString();
	}

	@Override
	public String getIsolateLog(String aLogId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsolateLogs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getThreads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void heraldMessage(IHerald aHerald, MessageReceived aMessage)
			throws HeraldException {
		// get message subject
		String wMessageSubject = aMessage.getSubject();
		String wReply = null;

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
