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

import java.lang.reflect.Method;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;


/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CTraceLineMXBeanAttributeInfo extends MBeanAttributeInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6788636152880728386L;

	/**
	 * @param aAttributeName
	 * @return
	 * @throws Exception
	 */
	private static Method getGetter(final String aAttributeName)
			throws Exception {
		return getMethod("get" + aAttributeName);
	}

	/**
	 * @param aMethodName
	 * @return
	 * @throws Exception
	 */
	private static Method getMethod(final String aMethodName,
			final Class<?>... aParamClasses) throws Exception {
		return TraceLine.class.getMethod(aMethodName, aParamClasses);
	}

	/**
	 * @param aAttributeName
	 * @return
	 * @throws Exception
	 */
	private static Method getSetter(final String aAttributeName,
			final Class<?>... aParamClasses) throws Exception {
		return getMethod("set" + aAttributeName, aParamClasses);
	}

	/**
	 * <pre>
	 * String getContent();
	 * 
	 * long getWhen();
	 * 
	 * String getWhat();
	 * 
	 * String getWhere();
	 * 
	 * String getWho();
	 * </pre>
	 * 
	 * @return
	 */
	static CTraceLineMXBeanAttributeInfo[] getTraceLineMBeanAttributeInfos()
			throws Exception {

		CTraceLineMXBeanAttributeInfo[] attributes = new CTraceLineMXBeanAttributeInfo[5];
		attributes[0] = new CTraceLineMXBeanAttributeInfo(
				TraceLineMBean.ATT_CONTENT, "Trace line content", String.class);
		attributes[1] = new CTraceLineMXBeanAttributeInfo(
				TraceLineMBean.ATT_WHEN, "Trace line timestamp", Long.class);
		attributes[2] = new CTraceLineMXBeanAttributeInfo(
				TraceLineMBean.ATT_WHAT, "The method", String.class);
		attributes[3] = new CTraceLineMXBeanAttributeInfo(
				TraceLineMBean.ATT_WHERE, "The thread origin", String.class);
		attributes[4] = new CTraceLineMXBeanAttributeInfo(
				TraceLineMBean.ATT_WHO, "The object origin", String.class);

		return attributes;

	}

	/**
	 * @param name
	 * @param description
	 * @param getter
	 * @param setter
	 * @throws IntrospectionException
	 */
	public CTraceLineMXBeanAttributeInfo(final String aAttributeName,
			final String aDescription, final Class<?>... aParamClasses)
			throws Exception {
		super(aAttributeName, aDescription, getGetter(aAttributeName),
				getSetter(aAttributeName, aParamClasses));
	}
}
