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
abstract class AbstractModelBean implements IModelBean, Serializable {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** Name of the bean */
    private String pName;

    /** Name of the parent of the bean */
    private String pParentName;

    /** Name of the root parent of the hierarchy */
    private String pRootName;

    /**
     * Default constructor
     */
    public AbstractModelBean() {

        // Does nothing
    }

    /**
     * Copy constructor
     * 
     * @param aComponentBean
     */
    public AbstractModelBean(final AbstractModelBean aModelBean) {

        // Copy members
        pName = aModelBean.pName;
        pParentName = aModelBean.pParentName;
        pRootName = aModelBean.pRootName;
    }

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
            if (!safeEquals(pName, other.pName)) {
                return false;
            }

            // Compare parent names
            return safeEquals(pParentName, other.pParentName);

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

    /**
     * Safe call to aObject.compareTo().
     * 
     * Returns 0 if both objects are null, -1 if aOther is not null while
     * aObject is null
     * 
     * @param aObject
     *            An object
     * @param aOther
     *            Another object
     * @return The result of compareTo()
     */
    protected <T> int safeCompareTo(final Comparable<T> aObject, final T aOther) {

        if (aObject == null) {
            if (aOther == null) {
                // Both objects are null
                return 0;

            } else {
                // The other is greater than the object
                return -1;
            }
        }

        return aObject.compareTo(aOther);
    }

    /**
     * Tests arguments equality, treating the special "null" case
     * 
     * @param aObject
     *            An object
     * @param aOther
     *            Another object
     * @return True if objects are equals
     */
    protected boolean safeEquals(final Object aObject, final Object aOther) {

        if (aObject == null) {
            return aOther == null;
        }

        return aObject.equals(aOther);
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
