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

    String ATT_CONTENT = "Content";

    String ATT_WHAT = "What";

    String ATT_WHEN = "When";

    String ATT_WHERE = "Where";

    String ATT_WHO = "Who";

    String[] ATTRIBUTES = { ATT_CONTENT, ATT_WHAT, ATT_WHEN, ATT_WHERE, ATT_WHO };

    /**
     * @return the content of this trace line
     */
    String getContent();

    /**
     * @return the name of the method from where this trace line was sent
     */
    String getWhat();

    /**
     * @return the timestamp of this trace line
     */
    Long getWhen();

    /**
     * @return the identifier of the thread from where this trace line was sent
     */
    String getWhere();

    /**
     * @return the identifier of the object which send this trace line
     */
    String getWho();
}
