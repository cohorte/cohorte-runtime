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
package org.psem2m.isolates.base;

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
     * contains the absolute path of the BASE folder of the current application
     */
    String PROP_PLATFORM_BASE = "org.psem2m.platform.base";

    /**
     * contains the absolute path of the HOME folder of the current application
     */
    String PROP_PLATFORM_HOME = "org.psem2m.platform.home";

    /**
     * contains the id of the current isolates.
     * 
     * <pre>
     * -Dorg.psem2m.platform.isolate.id=development
     * </pre>
     */
    String PROP_PLATFORM_ISOLATE_ID = "org.psem2m.platform.isolate.id";

    /**
     * if "true", allows the redirection of the output of the osgi logservice in
     * the logger of the current isolate.
     * 
     * <pre>
     * -Dorg.psem2m.platform.isolate.redirect.logservice=true
     * </pre>
     */
    String PROP_PLATFORM_REDIRECT_LOGSVC = "org.psem2m.platform.isolate.redirect.logservice";

    /**
     * Forker isolate ID
     */
    String SPECIAL_ISOLATE_ID_FORKER = "org.psem2m.internals.isolate.forker";
}
