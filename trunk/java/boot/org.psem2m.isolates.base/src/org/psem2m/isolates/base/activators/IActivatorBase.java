/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.base.activators;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface IActivatorBase {

    /**
     * Retrieves all available service references
     * 
     * @return an array containing all the available service references.
     */
    ServiceReference[] getAllServiceReferences();

    /**
     * Retrieves the bundle ID
     * 
     * @return The bundle ID
     */
    String getBundleId();

    /**
     * Retrieves the bundle context of the activator
     * 
     * @return The bundle context
     */
    BundleContext getContext();

    /**
     * Retrieves the service reference of a service using the unique service id
     * 
     * @param aServiceId
     *            the unique service id
     * @return an instance of ServiceReference
     * 
     * @throws Exception
     *             Something went wrong
     */
    ServiceReference getServiceReference(Long aServiceId) throws Exception;

}
