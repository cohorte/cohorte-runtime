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
package org.psem2m.isolates.tracer.essais;

import org.psem2m.isolates.tracer.ITraceLine;


/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface TracerMBean {

	/**
	 * @return
	 */
	public int getNbTraceLines();

	/**
	 * @return the array of the embeded trace lines
	 */
	public ITraceLine[] getTraceLines();

}
