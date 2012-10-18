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
package org.psem2m.isolates.tracer;

import java.util.List;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface ITracerSvc {

    /**
     * @return the list of the ids of the tracable channels
     */
    List<String> getTracableChannelsIds();

    /**
     * @param aIds
     * @return
     */
    boolean traceChannels(List<String> aIds);
}
