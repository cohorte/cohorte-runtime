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

import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.psem2m.isolates.tracer.ITraceLine;
import org.psem2m.isolates.tracer.ITraceLineSetter;
import org.psem2m.isolates.tracer.ITraceLinesProvider;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CopyOfTraceLine implements TraceLineMBean, ITraceLineSetter,
		DynamicMBean {

	ITraceLine pTraceLine = null;

	private final ITraceLinesProvider pTraceLineProvider;

	/**
	 * @param aLines
	 */
	public CopyOfTraceLine(final ITraceLinesProvider aTraceLineProvider) {
		super();
		pTraceLineProvider = aTraceLineProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(final String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {

		if (ATT_CONTENT.equalsIgnoreCase(attribute)) {
			return getContent();
		} else if (ATT_WHAT.equalsIgnoreCase(attribute)) {
			return getWhat();
		} else if (ATT_WHEN.equalsIgnoreCase(attribute)) {
			return getWhen();
		} else if (ATT_WHERE.equalsIgnoreCase(attribute)) {
			return getWhere();
		} else if (ATT_WHO.equalsIgnoreCase(attribute)) {
			return getWho();
		} else {
			throw new AttributeNotFoundException(attribute);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	@Override
	public AttributeList getAttributes(final String[] attributes) {

		AttributeList wAttributeList = new AttributeList();
		if (attributes != null && attributes.length > 0) {
			for (String wAttributeName : attributes) {
				try {
					wAttributeList.add(new Attribute(wAttributeName,
							getAttribute(wAttributeName)));
				} catch (Exception e) {
					Exception wEx = new MBeanException(e,
							"Can't get the value of the attribute "
									+ wAttributeName);
					wEx.printStackTrace();
				}
			}
		}
		return wAttributeList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getLine()
	 */
	@Override
	public String getContent() {
		initTraceLine();
		return pTraceLine.getContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	@Override
	public MBeanInfo getMBeanInfo() {
		try {
			return new CTraceLineMXBeanInfo();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getWhat()
	 */
	@Override
	public String getWhat() {
		initTraceLine();
		return pTraceLine.getWhat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getTimeStamp()
	 */
	@Override
	public Long getWhen() {
		initTraceLine();
		return pTraceLine.getWhen();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getWhere()
	 */
	@Override
	public String getWhere() {
		initTraceLine();
		return pTraceLine.getWhere();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getWho()
	 */
	@Override
	public String getWho() {
		initTraceLine();
		return pTraceLine.getWho();
	}

	/**
	 * 
	 */
	private void initTraceLine() {
		if (pTraceLine == null) {
			pTraceLine = pTraceLineProvider.retreiveFirstStackedLine();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.DynamicMBean#invoke(java.lang.String,
	 * java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object invoke(final String actionName, final Object[] params,
			final String[] signature) throws MBeanException,
			ReflectionException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	@Override
	public void setAttribute(final Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {

		if (ATT_CONTENT.equalsIgnoreCase(attribute.getName())) {
			setContent((String) attribute.getValue());
		} else if (ATT_WHAT.equalsIgnoreCase(attribute.getName())) {
			setWhat((String) attribute.getValue());
		} else if (ATT_WHEN.equalsIgnoreCase(attribute.getName())) {
			setWhen((Long) attribute.getValue());
		} else if (ATT_WHERE.equalsIgnoreCase(attribute.getName())) {
			setWhere((String) attribute.getValue());
		} else if (ATT_WHO.equalsIgnoreCase(attribute.getName())) {
			setWho((String) attribute.getValue());
		} else {
			throw new AttributeNotFoundException(attribute.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.management.DynamicMBean#setAttributes(javax.management.AttributeList
	 * )
	 */
	@Override
	public AttributeList setAttributes(final AttributeList attributes) {
		AttributeList wAttributeList = new AttributeList();
		if (attributes != null && attributes.size() > 0) {
			Iterator<Object> wAttributes = attributes.iterator();
			while (wAttributes.hasNext()) {
				Attribute wAttribute = (Attribute) wAttributes.next();
				try {
					setAttribute(wAttribute);
					wAttributeList.add(wAttribute);
				} catch (Exception e) {
					Exception wEx = new MBeanException(e,
							"Can't set the value of the attribute "
									+ wAttribute.getName());
					wEx.printStackTrace();
				}
			}
		}
		return wAttributeList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.ITraceLine#setContent(java.lang.String)
	 */
	@Override
	public void setContent(final String aContent) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.ITraceLine#setWhat(java.lang.String)
	 */
	@Override
	public void setWhat(final String aWhat) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.ITraceLine#setWhen(long)
	 */
	@Override
	public void setWhen(final Long aWhen) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.ITraceLine#setWhere(java.lang.String)
	 */
	@Override
	public void setWhere(final String aWhere) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.ITraceLine#setWho(java.lang.String)
	 */
	@Override
	public void setWho(final String aWho) {
	}
}
