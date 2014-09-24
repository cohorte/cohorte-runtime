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
package org.psem2m.isolates.tracer.impl;

import org.psem2m.isolates.tracer.ITraceLineSetter;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CTraceLine implements ITraceLineSetter {

    private String pContent;

    private String pWhat;

    private Long pWhen;

    private String pWhere;

    private String pWho;

    /**
	 * 
	 */
    public CTraceLine() {

        super();
    }

    /**
     * @param aWhen
     * @param aWho
     * @param aWhat
     * @param aWhere
     * @param aContent
     */
    public CTraceLine(final Long aWhen, final String aWho, final String aWhat,
            final String aWhere, final String aContent) {

        this();
        setWhen(aWhen);
        setWho(aWho);
        setWhat(aWhat);
        setWhere(aWhere);
        setContent(aContent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.tracer.IJmxTraceLine#getLine()
     */
    @Override
    public String getContent() {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.tracer.ITraceLine#setContent(java.lang.String)
     */
    @Override
    public void setContent(final String aContent) {

        pContent = aContent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.tracer.ITraceLine#setWhat(java.lang.String)
     */
    @Override
    public void setWhat(final String aWhat) {

        pWhat = aWhat;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.tracer.ITraceLine#setWhen(long)
     */
    @Override
    public void setWhen(final Long aWhen) {

        pWhen = aWhen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.tracer.ITraceLine#setWhere(java.lang.String)
     */
    @Override
    public void setWhere(final String aWhere) {

        pWhere = aWhere;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.tracer.ITraceLine#setWho(java.lang.String)
     */
    @Override
    public void setWho(final String aWho) {

        pWho = aWho;
    }
}
