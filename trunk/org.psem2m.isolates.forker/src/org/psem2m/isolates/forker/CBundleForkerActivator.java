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
package org.psem2m.isolates.forker;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleForkerActivator extends CActivatorBase implements
        IBundleForkerActivator {

    /** The current activator instance **/
    private static IBundleForkerActivator sBundleForkerActivator = null;

    /**
     * Retrieves the forker activator instance
     * 
     * @return The Forker activator instance
     */
    public static IBundleForkerActivator getInstance() {

        return sBundleForkerActivator;
    }

    /**
     * Explicit default constructor
     */
    public CBundleForkerActivator() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.utilities.osgi.CActivatorBase#getBundleId()
     */
    @Override
    public String getBundleId() {

        return getClass().getPackage().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext aBundleContext) throws Exception {

        // Always keep the version of the activator
        sBundleForkerActivator = this;

        super.start(aBundleContext);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext aBundleContext) throws Exception {

        super.stop(aBundleContext);

        // Remove the reference
        sBundleForkerActivator = null;
    }
}
