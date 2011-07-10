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

import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;


/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CTraceLineMXBeanInfo extends MBeanInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3733194964273120377L;

	/**
	 * @throws Exception
	 */
	public CTraceLineMXBeanInfo() throws Exception {
		super(TraceLine.class.getName(), "Trace line",
				CTraceLineMXBeanAttributeInfo.getTraceLineMBeanAttributeInfos(),
				new MBeanConstructorInfo[0], new MBeanOperationInfo[0],
				new MBeanNotificationInfo[0]);
	}

}
