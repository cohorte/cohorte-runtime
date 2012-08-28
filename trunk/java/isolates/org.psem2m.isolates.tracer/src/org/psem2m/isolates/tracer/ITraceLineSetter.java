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
public interface ITraceLineSetter extends ITraceLine {

    /**
     * @param aContent
     *            the content of this trace line
     */
    void setContent(String aContent);

    /**
     * @param aWhat
     *            the name of the method from where this trace line was sent
     */
    void setWhat(String aWhat);

    /**
     * @param aWhen
     *            the timestamp of this trace line
     */
    void setWhen(Long aWhen);

    /**
     * @param aWhere
     *            the identifier of the thread from where this trace line was
     *            sent
     */
    void setWhere(String aWhere);

    /**
     * @param aWho
     *            the identifier of the object which send this trace line
     */
    void setWho(String aWho);
}
