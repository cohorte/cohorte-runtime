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
import org.psem2m.isolates.tracer.ITraceLinesProvider;


/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class TraceLine implements TraceLineMBean {

	private String pContent;

	private final ITraceLinesProvider pTraceLineProvider;

	private String pWhat;

	private Long pWhen;

	private String pWhere;

	private String pWho;

	/**
	 * @param aLines
	 */
	public TraceLine(final ITraceLinesProvider aTraceLineProvider) {
		super();
		pTraceLineProvider = aTraceLineProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getLine()
	 */
	@Override
	public String getContent() {
		initTraceLine();
		return pContent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getWhat()
	 */
	@Override
	public String getWhat() {
		return pWhat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getTimeStamp()
	 */
	@Override
	public Long getWhen() {
		return pWhen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getWhere()
	 */
	@Override
	public String getWhere() {
		return pWhere;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLine#getWho()
	 */
	@Override
	public String getWho() {
		return pWho;
	}

	/**
	 * 
	 */
	private void initTraceLine() {
		ITraceLine wTraceLine = pTraceLineProvider.retreiveFirstStackedLine();
		pContent = wTraceLine.getContent();
		pWhat = wTraceLine.getWhat();
		pWhen = wTraceLine.getWhen();
		pWhere = wTraceLine.getWhere();
		pWho = wTraceLine.getWho();
	}

}
