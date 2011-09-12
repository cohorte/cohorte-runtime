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
package org.psem2m.isolates.forker.impl;

import org.psem2m.isolates.base.activators.CBundleLoggerBase;
import org.psem2m.isolates.forker.CBundleForkerActivator;
import org.psem2m.isolates.forker.IBundleForkerLoggerSvc;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleForkerLoggerSvc extends CBundleLoggerBase implements
	IBundleForkerLoggerSvc {

    /**
     * 
     */
    public CBundleForkerLoggerSvc() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
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
    public void validatePojo() {
	setActivityLoggerBase(CBundleForkerActivator.getInstance());

	// logs in the bundle output
	logInfo(this, "validatePojo", "VALIDATE", toDescription());
    }

}
