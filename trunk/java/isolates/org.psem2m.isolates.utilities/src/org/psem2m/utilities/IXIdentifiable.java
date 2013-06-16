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
package org.psem2m.utilities;

/**
 * generique interface that define a methode getId()
 * 
 * @param <E>
 */
public interface IXIdentifiable<E extends Comparable<E>> extends Comparable<IXIdentifiable<?>> {

	public static String LIB_ID = IXIdentifier.LIB_ID;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	int compareTo(IXIdentifiable<?> aIdentifiable);

	/**
	 * @return
	 */
	public E getIdentifier();

}
