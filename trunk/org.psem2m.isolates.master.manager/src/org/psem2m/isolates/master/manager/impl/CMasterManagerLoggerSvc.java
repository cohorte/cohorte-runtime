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
package org.psem2m.isolates.master.manager.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.activators.CBundleLoggerBase;
import org.psem2m.isolates.master.manager.CBundleMasterManagerActivator;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
@Component(name = "isolates-master-manager-logger-factory", publicFactory = false)
@Provides(specifications = IActivityLoggerBase.class)
@Instantiate(name = "isolates-master-manager-logger")
public class CMasterManagerLoggerSvc extends CBundleLoggerBase implements
        IActivityLoggerBase {

    public CMasterManagerLoggerSvc() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() {

        // logs in the bundle output
        logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());

        setActivityLoggerBase(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() {

        setActivityLoggerBase(CBundleMasterManagerActivator.getInstance());

        // logs in the bundle output
        logInfo(this, "validatePojo", "VALIDATE", toDescription());
    }
}
