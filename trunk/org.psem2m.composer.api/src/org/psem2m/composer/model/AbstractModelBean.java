/**
 * File:   AbstractModelBean.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

import java.io.Serializable;

/**
 * Common code for Composer model beans
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractModelBean implements IModelBean, Serializable {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** Name of the bean */
    protected String pName;

    /** Name of the parent of the bean */
    protected String pParentName;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#getName()
     */
    @Override
    public String getName() {

        return pName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#getParentName()
     */
    @Override
    public String getParentName() {

        return pParentName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#setName(java.lang.String)
     */
    @Override
    public void setName(final String aName) {

        pName = aName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.model.IModelBean#setParent(org.psem2m.composer.model
     * .ComponentsSetBean)
     */
    @Override
    public void setParentName(final String aParentName) {

        pParentName = aParentName;
    }
}
