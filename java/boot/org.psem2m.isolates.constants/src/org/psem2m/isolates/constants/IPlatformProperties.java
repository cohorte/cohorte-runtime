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

package org.psem2m.isolates.constants;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public interface IPlatformProperties {

	/**
	 * The debug port used by the forker
	 *
	 * If present, the platform is ran in debug mode and the isolates must be
	 * started in remote debug mode, connecting to localhost:debug_port+n, with
	 * n > 0.
	 */
	String PROP_BASE_DEBUG_PORT = "org.psem2m.debug.port";

	/**
	 * URL to the configuration broker
	 */
	String PROP_BROKER_URL = "cohorte.configuration.broker.url";

	/**
	 * MOD_0G_20150821
	 *
	 * Path to localize a directory where the isolate log file are stored
	 *
	 * Accept an absolute path or relative path to the "user.dir" one of the
	 * isolate.
	 *
	 * Allows the isolates to store their log file outside their isolate
	 * directory when they are a singleton one.
	 *
	 * Note: the isolate directory is in .../base/var which is clean each time
	 * the node is launched
	 */
	String PROP_ISOLATE_LOG_STORAGE = "cohorte.isolate.log.storage";

	/**
	 * contains the name of the current isolates.
	 *
	 * <pre>
	 * -Dcohorte.isolate.name=development
	 * </pre>
	 */
	String PROP_ISOLATE_NAME = "cohorte.isolate.name";

	/**
	 * contains the uuid of the current isolates.
	 *
	 * <pre>
	 * -Dcohorte.isolate.uid=POSD-UEZS-NBG9-JA72
	 * </pre>
	 */
	String PROP_ISOLATE_UID = "cohorte.isolate.uid";

	/**
	 * contains the node name of the current isolates.
	 *
	 * <pre>
	 * -Dcohorte.node.uid=GFRY-HYRZ-LOIS-85HY
	 * </pre>
	 */
	String PROP_NODE_NAME = "cohorte.node.name";

	/**
	 * contains the node uuid of the current isolates.
	 *
	 * <pre>
	 * -Dcohorte.node.uid=GFRY-HYRZ-LOIS-85HY
	 * </pre>
	 */
	String PROP_NODE_UID = "cohorte.node.uid";

	/**
	 * contains the absolute path of the BASE folder of the current application
	 */
	String PROP_PLATFORM_BASE = "cohorte.base";

	/**
	 * contains the absolute path of the HOME folder of the current application
	 */
	String PROP_PLATFORM_HOME = "cohorte.home";

	/**
	 * Contains the name (or IP) of the current isolate host machine
	 */
	String PROP_PLATFORM_HOST_NAME = "org.psem2m.platform.host.name";

	/**
	 * if "true", allows the redirection of the output of the OSGi log service
	 * in the logger of the current isolate.
	 *
	 * <pre>
	 * -Dorg.psem2m.platform.isolate.redirect.logservice=true
	 * </pre>
	 */
	String PROP_PLATFORM_REDIRECT_LOGSVC = "org.psem2m.platform.isolate.redirect.logservice";

	/**
	 * Special isolate name: forker
	 */
	String SPECIAL_NAME_FORKER = "cohorte.internals.forker";
}
