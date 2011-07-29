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
package org.psem2m.isolates.tracer.test.impl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.tracer.ITracerSvc;
import org.psem2m.utilities.CXStringUtils;

/**
 * 
 * @see http://felix.apache.org/site/apache-felix-shell.html
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CCommandTraceChannels extends CPojoBase implements Command {

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 * 
	 * This service is the logger of the current bundle
	 **/
	private IIsolateLoggerSvc pIsolateLoggerSvc;

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 * 
	 * This service is the logger of the current bundle
	 **/
	private ITracerSvc pTracerSvc;

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
	 * @see org.apache.felix.shell.Command#execute(java.lang.String,
	 * java.io.PrintStream, java.io.PrintStream)
	 */
	@Override
	public void execute(final String aArgs, final PrintStream out,
			final PrintStream err) {

		pIsolateLoggerSvc.logInfo(this, "execute", "cmde=[%s] args=[%s]",
				getName(), aArgs);

		List<String> wIds = new ArrayList<String>();
		wIds.add("toto");
		wIds.add("tata");

		out.println(getName() + " begin");
		boolean wOK = pTracerSvc.traceChannels(wIds);

		out.println(String.format("TraceChannels=[%s] ok=[%b]",
				CXStringUtils.stringListToString(wIds), wOK));

		out.println(getName() + " end");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.felix.shell.Command#getName()
	 */
	@Override
	public String getName() {
		return "traceChannels";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.felix.shell.Command#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return "return the list of the tracable log channels.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.felix.shell.Command#getUsage()
	 */
	@Override
	public String getUsage() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() throws BundleException {
		// logs in the bundle output
		pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
				toDescription());

		List<String> wIds = pTracerSvc.getTracableChannelsIds();

		pIsolateLoggerSvc.logInfo(this, null, "getTracableChannelsIds=[%s]",
				CXStringUtils.stringListToString(wIds));

	}

	@Override
	public void validatePojo() throws BundleException {
		// logs in the bundle output
		pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
				toDescription());
	}
}
