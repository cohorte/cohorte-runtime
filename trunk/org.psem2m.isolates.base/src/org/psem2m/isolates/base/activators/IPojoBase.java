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

import org.osgi.framework.BundleException;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface IPojoBase {
    /**
     * @return the id of the bundle
     */
    public String getPojoId();

    /**
     * Called when the POJO has been invalidated by iPOJO (dependency gone, ...)
     * 
     * @throws BundleException
     *             An error occurred while stopping the POJO
     */
    public void invalidatePojo() throws BundleException;

    /**
     * Called when iPOJO starts the POJO.
     * 
     * @throws BundleException
     *             An error occurred while starting the POJO
     */
    public void validatePojo() throws BundleException;
}
