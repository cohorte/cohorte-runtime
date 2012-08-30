/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 22 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.composer;

/**
 * @author ogattaz
 * 
 */
public enum EComponentState {
    /** Component up and running */
    COMPLETE,

    /** Component instantiation requested */
    INSTANTIATING,

    /** Component removed of the components set */
    REMOVED,

    /** Component resolved (can be instantiated by an agent) */
    RESOLVED,

    /** Component not yet resolved */
    WAITING,
}
