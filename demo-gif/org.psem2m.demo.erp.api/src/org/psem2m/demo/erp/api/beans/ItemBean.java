/**
 * File:   ItemBean.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.api.beans;

import java.io.Serializable;

/**
 * Description of an Item from the ERP
 * 
 * @author Thomas Calmant
 */
public class ItemBean implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** A short description of the item */
    private String pDescription;

    /** Item ID */
    private String pId;

    /** Name of the item */
    private String pName;

    /** Price of the item */
    private String pPrice;

    /**
     * Default constructor
     */
    public ItemBean() {

        // Do nothing
    }

    /**
     * Copy constructor
     * 
     * @param aBean
     *            A source item bean
     */
    public ItemBean(final ItemBean aBean) {

        if (aBean == null) {
            // Do nothing if the other bean is null
            return;
        }

        pDescription = aBean.pDescription;
        pId = aBean.pId;
        pName = aBean.pName;
        pPrice = aBean.pPrice;
    }

    /**
     * @return the description
     */
    public String getDescription() {

        return pDescription;
    }

    /**
     * @return the id
     */
    public String getId() {

        return pId;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * @return the price
     */
    public String getPrice() {

        return pPrice;
    }

    /**
     * @param aDescription
     *            the description to set
     */
    public void setDescription(final String aDescription) {

        pDescription = aDescription;
    }

    /**
     * @param aId
     *            the id to set
     */
    public void setId(final String aId) {

        pId = aId;
    }

    /**
     * @param aName
     *            the name to set
     */
    public void setName(final String aName) {

        pName = aName;
    }

    /**
     * @param aPrice
     *            the price to set
     */
    public void setPrice(final String aPrice) {

        pPrice = aPrice;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("ItemBean(");
        builder.append("id=").append(pId);
        builder.append(", name=").append(pName);
        builder.append(")");

        return builder.toString();
    }
}
