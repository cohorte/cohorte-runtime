/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas Calmant (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.master.manager;

import org.psem2m.isolates.base.CActivatorBase;

/**
 * PSEM2M isolate manager (launcher)
 * 
 * @author Thomas Calmant
 */
public class CBundleMasterManagerActivator extends CActivatorBase {

    /** Valid instance **/
    private static CBundleMasterManagerActivator sInstance = null;

    /**
     * Retrieves a valid activator instance, null if the activator wasn't called
     * 
     * @return A valid activator instance
     */
    public static CBundleMasterManagerActivator getInstance() {
	return sInstance;
    }

    /**
     * Explicit default constructor
     */
    public CBundleMasterManagerActivator() {
	super();

	if (sInstance == null) {
	    sInstance = this;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {
	// Invalidate the reference
	sInstance = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CActivatorBase#getBundleId()
     */
    @Override
    public String getBundleId() {
	return getClass().getPackage().getName();
    }
}
