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

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;

import org.psem2m.isolates.tracer.ITraceLine;
import org.psem2m.isolates.tracer.ITraceLinesProvider;


/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class Tracer extends NotificationBroadcasterSupport implements
		TracerMBean {

	/**
	 * 
	 */
	private ITraceLinesProvider pTraceLineProvider;

	/**
	 * 
	 */
	public Tracer() {
		super();
	}

	/**
	 * @param aLines
	 */
	public Tracer(final ITraceLinesProvider aTraceLineProvider) {
		this();
		pTraceLineProvider = aTraceLineProvider;
	}

	@Override
	public int getNbTraceLines() {
		return pTraceLineProvider.getNbStackedLines();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.management.NotificationBroadcasterSupport#getNotificationInfo()
	 */
	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		String[] types = new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE };
		String name = AttributeChangeNotification.class.getName();
		String description = "Un attribut du MBean a ete modifie";
		MBeanNotificationInfo info = new MBeanNotificationInfo(types, name,
				description);
		return new MBeanNotificationInfo[] { info };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLines#getLines()
	 */
	@Override
	public ITraceLine[] getTraceLines() {
		return pTraceLineProvider.retreiveStackedLines();
	}

}
