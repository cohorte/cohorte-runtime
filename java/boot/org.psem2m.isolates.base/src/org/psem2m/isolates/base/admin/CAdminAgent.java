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


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.herald.HeraldException;
import org.cohorte.herald.IHerald;
import org.cohorte.herald.IMessageListener;
import org.cohorte.herald.MessageReceived;
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
@Instantiate(name = "admin-agent-java")
public class CAdminAgent implements IAdminAgent, IMessageListener {

	/** Herald API Service */
	@Requires
	private IHerald pHerald;
	
	@Override
	public String getIsolateDetail() {
		JSONObject wResult = new JSONObject();
		try {
			wResult.append("cohorte.isolate.http.port", "");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wResult.toString();
	}

	@Override
	public String getBundles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBundleDetail(int aBundleNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFactories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFactoryDetail(String aFactoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getComponentInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getComponentInstanceDetail(String aInstanceName) {
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
	public String getIsolateLogs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsolateLog(String aLogId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsolateDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsolateAccesses() {
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
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_INSTANCE_DETAIL)) {
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
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_ISOLATE_DIRECTORY)) {
			wReply = getIsolateDirectory();
		} else if (wMessageSubject.equalsIgnoreCase(SUBJECT_GET_ISOLATE_ACCESSES)) {
			wReply = getIsolateAccesses();
		} 
		
		if (wReply != null) {
			aHerald.reply(aMessage, wReply);
		} else {
			aHerald.reply(aMessage, "No value!");
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
	
	/**
	 * Component invalidated
	 */
	@Invalidate
	public void invalidate() {
		if (pHerald != null) {
			pHerald.removeMessageListener(this);
		}
	}
	
}
