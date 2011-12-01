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

    /** Name of the root parent of the hierarchy */
    protected String pRootName;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof AbstractModelBean) {
            // The other object is also a model bean
            final AbstractModelBean other = (AbstractModelBean) aObj;

            // Compare names
            boolean sameNames = false;

            if (pName != null) {
                sameNames = pName.equals(other.pName);

            } else {
                sameNames = (other.pName == null);
            }

            if (!sameNames) {
                return false;
            }

            // Compare parent names
            if (pParentName != null) {
                sameNames = pParentName.equals(other.pParentName);
            } else {
                sameNames = (other.pParentName == null);
            }

            return sameNames;

        } else if (aObj instanceof CharSequence) {
            // The other object is a string, compare only the name
            return aObj.equals(pName);
        }

        return false;
    }

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
     * @see org.psem2m.composer.model.IModelBean#getRootName()
     */
    @Override
    public String getRootName() {

        return pRootName;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#setRootName(java.lang.String)
     */
    @Override
    public void setRootName(final String aRootName) {

        pRootName = aRootName;
    }
}
