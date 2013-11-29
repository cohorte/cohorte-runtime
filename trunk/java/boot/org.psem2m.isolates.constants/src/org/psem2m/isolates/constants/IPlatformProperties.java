/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *    tcalmant (isandlaTech) - Debug constant
 *******************************************************************************/
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
     * contains the name of the current isolates.
     */
    String PROP_ISOLATE_NAME = "cohorte.isolate.name";

    /**
     * contains the id of the current isolates.
     * 
     * <pre>
     * -Dorg.psem2m.platform.isolate.id=development
     * </pre>
     */
    String PROP_ISOLATE_UID = "cohorte.isolate.uid";

    /**
     * Name of the node hosting this isolate
     */
    String PROP_NODE_NAME = "cohorte.node.name";

    /**
     * UID of the node hosting this isolate
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
     * Special isolates ID prefix
     */
    String SPECIAL_INTERNAL_ISOLATES_PREFIX = "org.psem2m.internals.isolates.";

    /**
     * Forkers isolate ID prefix
     */
    String SPECIAL_ISOLATE_ID_FORKER = SPECIAL_INTERNAL_ISOLATES_PREFIX
            + "forker";

    /**
     * Monitors isolate ID prefix
     */
    String SPECIAL_ISOLATE_ID_MONITOR = SPECIAL_INTERNAL_ISOLATES_PREFIX
            + "monitor";

    /**
     * Special isolate name: forker
     */
    String SPECIAL_NAME_FORKER = "cohorte.internals.forker";
}
