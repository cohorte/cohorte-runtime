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

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface ITraceLine {

	public final static String ATT_CONTENT = "Content";
	public final static String ATT_WHAT = "What";
	public final static String ATT_WHEN = "When";
	public final static String ATT_WHERE = "Where";
	public final static String ATT_WHO = "Who";

	public final static String[] ATTRIBUTES = { ATT_CONTENT, ATT_WHAT,
			ATT_WHEN, ATT_WHERE, ATT_WHO };

	/**
	 * @return the content of this trace line
	 */
	public String getContent();

	/**
	 * @return the name of the method from where this trace line was sent
	 */
	public String getWhat();

	/**
	 * @return the timestamp of this trace line
	 */
	public Long getWhen();

	/**
	 * @return the identifier of the thread from where this trace line was sent
	 */
	public String getWhere();

	/**
	 * @return the identifier of the object which send this trace line
	 */
	public String getWho();

}
