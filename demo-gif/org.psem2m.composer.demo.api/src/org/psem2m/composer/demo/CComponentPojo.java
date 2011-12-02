/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 1 déc. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.composer.demo;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public abstract class CComponentPojo extends CPojoBase {

    /**
     * @return
     */
    public abstract String getName();

    /**
     * @return
     */
    public String getShortName() {

        return CXStringUtils.strAdjustRight(getName(), 24, ' ');
    }

    /**
     * @param aLogger
     * @param aContext
     */
    public void logContextError(final IIsolateLoggerSvc aLogger,
            final IComponentContext aContext) {

        aLogger.logSevere(this, "computeResult", "cpnt=[%25s] ERROR : %s",
                getShortName(), aContext.getErrors());
    }
}
