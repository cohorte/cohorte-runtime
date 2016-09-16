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

/**
 * Admin Service (used by webadmin).
 *
 * This interface is implemented by local Agent in each Isolate.
 *
 * @author bdebbabi
 *
 */
public interface IAdminAgent {

	String FACTORY_ADMIN_AGENT = "cohorte-isolate-base-admin-agent-java-factory";

	/** Common prefix to cohorte agent */
	String PREFIX_SUBJECT = "cohorte/debug/agent";

	/** Signal to request the bundle details */
	String SUBJECT_GET_BUNDLE_DETAIL = String.format("%s/get_bundle_detail",
			PREFIX_SUBJECT);

	/** Signal to request the bundles of the isolate */
	String SUBJECT_GET_BUNDLES = String
			.format("%s/get_bundles", PREFIX_SUBJECT);

	/** Signal to request the component factories of the isolate */
	String SUBJECT_GET_FACTORIES = String.format("%s/get_factories",
			PREFIX_SUBJECT);

	/** Signal to request the component factory details */
	String SUBJECT_GET_FACTORY_DETAIL = String.format("%s/get_factory_detail",
			PREFIX_SUBJECT);

	/** Signal to request the detail of one instance */
	String SUBJECT_GET_INSTANCE_DETAIL = String.format(
			"%s/get_instance_detail", PREFIX_SUBJECT);

	/** Signal to request the instances of the isolate */
	String SUBJECT_GET_INSTANCES = String.format("%s/get_instances",
			PREFIX_SUBJECT);

	/** Signal to request the isolate herald accesses */
	String SUBJECT_GET_ISOLATE_ACCESSES = String.format(
			"%s/get_isolate_accesses", PREFIX_SUBJECT);

	/** Signal to request the detail of the local isolate */
	String SUBJECT_GET_ISOLATE_DETAIL = String.format("%s/get_isolate_detail",
			PREFIX_SUBJECT);

	/** Signal to request the isolate herald local directory */
	String SUBJECT_GET_ISOLATE_DIRECTORY = String.format(
			"%s/get_isolate_directory", PREFIX_SUBJECT);

	/** Signal to request the isolate logs */
	String SUBJECT_GET_ISOLATE_LOG = String.format("%s/get_isolate_log",
			PREFIX_SUBJECT);

	/** Signal to request the list of isolate logs */
	String SUBJECT_GET_ISOLATE_LOGS = String.format("%s/get_isolate_logs",
			PREFIX_SUBJECT);

	/** Signal to request the services of the isolate */
	String SUBJECT_GET_SERVICES = String.format("%s/get_services",
			PREFIX_SUBJECT);

	/** Signal to request the current threads of the isolate */
	String SUBJECT_GET_THREADS = String
			.format("%s/get_threads", PREFIX_SUBJECT);

	/** Filter to match agent signals */
	String SUBJECT_MATCH_ALL = String.format("%s/*", PREFIX_SUBJECT);

	/**
	 * <pre>
	 * 	{
	 * 	  "id": "...",
	 * 	  "name": "...",
	 * 	  "state": "...",
	 * 	  "version": "...",
	 * 	  "location": "...",
	 * 	  "published-services": [],
	 * 	  "used-services": [],
	 * 	}
	 * </pre>
	 *
	 * @param aBundleNumber
	 * @return
	 */
	String getBundleDetail(int aBundleNumber);

	/**
	 * <pre>
	 * 	[
	 * 	  {
	 * 		"id": "...",
	 * 		"name": "...",
	 * 		"state": "...",
	 * 		"version": "...",
	 *    }
	 * ]
	 * </pre>
	 *
	 * @return
	 */
	String getBundles();

	/**
	 * <pre>
	 * 	{
	 * 	  "name": "...",
	 * 	  "factory": "...",
	 * 	  "bundle-id": "...",
	 * 	  "state": "...",
	 * 	  "services": [],
	 * 	  "dependencies": [],
	 * 	  "properties": {},
	 * 	  "error-trace": "..."
	 * 	}
	 * </pre>
	 *
	 * @param aInstanceName
	 * @return
	 */
	String getComponentInstanceDetail(String aInstanceName);

	/**
	 * <pre>
	 * 	[
	 * 	  {
	 * 		"name": "...",
	 * 		"factory": "...",
	 * 		"state": "..."
	 * 	  }
	 * 	]
	 * </pre>
	 *
	 * @return
	 */
	String getComponentInstances();

	/**
	 * <pre>
	 * 	[
	 * 	  {
	 * 		"name": "...",
	 * 		"bundle":
	 * 		  {
	 * 			"id": "...",
	 * 			"name": "..."
	 * 		  }
	 * 	  }
	 * 	]
	 * </pre>
	 *
	 * @return
	 */
	String getFactories();

	/**
	 * <pre>
	 * 	{
	 * 	  "name": "...",
	 * 		"bundle":
	 * 		  {
	 * 			"id": "...",
	 * 			"name": "..."
	 * 		  }
	 * 	  "properties": {},
	 * 	  "provided-services": [],
	 * 	  "requirements": [],
	 * 	  "handlers": []
	 * 	}
	 * </pre>
	 *
	 * @param aFactoryName
	 * @return
	 */
	String getFactoryDetail(String aFactoryName);

	String getIsolateAccesses();

	String getIsolateDetail();

	Object getIsolateDirectory();

	String getIsolateLog(String aLogId);

	String getIsolateLogs();

	String getServices();

	String getThreads();

}
