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
package org.psem2m.isolates.tracer.impl;

import java.util.List;

import org.apache.felix.ipojo.Pojo;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.activators.IIsolateLoggerSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.isolates.tracer.ITracerSvc;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CTracerSvc extends CPojoBase implements ITracerSvc {

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 * 
	 * This service is the logger of the current bundle
	 **/
	private IIsolateLoggerSvc pIsolateLoggerSvc;

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 * 
	 * This service is used to control the opened logging channels : set level,
	 * redirect to the tracer, ...
	 **/
	private ILogChannelsSvc pLogChannelsSvc;

	/**
	 * Explicite default constructor
	 */
	public CTracerSvc() {
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
	 * @see org.psem2m.isolates.osgi.CPojoBase#getPojoId()
	 */
	@Override
	public String getPojoId() {
		return ((Pojo) this).getComponentInstance().getInstanceName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.tracer.ITracerSvc#getTracableChannelsIds()
	 */
	@Override
	public List<String> getTracableChannelsIds() {
		return pLogChannelsSvc.getChannelsIds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() {
		// logs in the bundle output
		pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
				toDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.tracer.ITracerSvc#traceChannels(java.util.List)
	 */
	@Override
	public boolean traceChannels(final List<String> aIds) {

		for (String wId : aIds) {
			// pLogChannelsSvc.getLogChannel(wId).setHandler(this);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.CPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {
		// logs in the bundle output
		pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
				toDescription());

		List<String> wIds = pLogChannelsSvc.getChannelsIds();

		pIsolateLoggerSvc.logInfo(this, null, "getChannelsIds=[%s]",
				CXStringUtils.stringListToString(wIds));
	}
}
