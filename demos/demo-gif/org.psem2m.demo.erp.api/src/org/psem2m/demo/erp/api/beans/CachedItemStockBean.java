/**
 * File:   CachedItemStockBean.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.api.beans;

/**
 * Represents an item stock and its information quality level
 * 
 * @author Thomas Calmant
 */
public class CachedItemStockBean implements ICachedBean {

    /** The item ID */
    private String pItemId;

    /** The information quality level */
    private int pQualityLevel;

    /** The stock */
    private long pStock;

    /**
     * Sets up the cached stock bean
     * 
     * @param aItemId
     *            The item
     * @param aStock
     *            The item stock
     * @param aQualityLevel
     *            The information quality level
     */
    public CachedItemStockBean(final String aItemId, final long aStock,
            final int aQualityLevel) {

        pItemId = aItemId;
        pStock = aStock;
        pQualityLevel = aQualityLevel;
    }

    /**
     * Sets up the cached stock bean
     * 
     * @param aItemId
     *            The item
     * @param aStock
     *            The item stock
     * @param aStockAge
     *            The age of the stock information
     */
    public CachedItemStockBean(final String aItemId, final long aStock,
            final long aStockAge) {

        pItemId = aItemId;
        pStock = aStock;

        // Compute the quality
        pQualityLevel = QualityUtilities.computeCacheQuality(aStockAge);
    }

    /**
     * @return the item ID
     */
    public String getId() {

        return pItemId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.beans.ICachedBean#getQualityLevel()
     */
    @Override
    public int getQualityLevel() {

        return pQualityLevel;
    }

    /**
     * @return the stock
     */
    public long getStock() {

        return pStock;
    }

    /**
     * @param aItemId
     *            the item ID
     */
    public void setId(final String aItemId) {

        pItemId = aItemId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.beans.ICachedBean#setQualityLevel(int)
     */
    @Override
    public void setQualityLevel(final int aQualityLevel) {

        pQualityLevel = aQualityLevel;
    }

    /**
     * @param aStock
     *            the stock
     */
    public void setStock(final long aStock) {

        pStock = aStock;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("CachedItemStockBean(");
        builder.append("id=").append(pItemId);
        builder.append(", stock=").append(pStock);
        builder.append(", quality=").append(
                QualityUtilities.qualityToString(pQualityLevel));
        builder.append(")");

        return builder.toString();
    }
}
