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
package org.psem2m.isolates.osgi;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CIsolateLoggerListener implements LogListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.log.LogListener#logged(org.osgi.service.log.LogEntry)
	 */
	@Override
	public void logged(final LogEntry entry) {
		// Invoked by the log service implementation for each log entry
		System.out.println(entry.getMessage());
	}

}
