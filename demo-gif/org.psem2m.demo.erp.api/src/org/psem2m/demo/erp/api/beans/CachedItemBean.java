/**
 * File:   CachedItemBean.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.api.beans;

/**
 * Description of an item, returned by the Data Server
 * 
 * @author Thomas Calmant
 */
public class CachedItemBean extends ItemBean implements ICachedBean {

    /** Quality of the information returned */
    private int pCacheLevel;

    /**
     * Default constructor
     */
    public CachedItemBean() {

        // Does nothing special...
        super();
        pCacheLevel = IQualityLevels.CACHE_LEVEL_SYNC;
    }

    /**
     * Sets up the cached bean, with a forced cached level
     * 
     * @param aBean
     *            The "source" bean
     * @param aCacheLevel
     *            Cache level
     */
    public CachedItemBean(final ItemBean aBean, final int aCacheLevel) {

        super(aBean);
        pCacheLevel = aCacheLevel;
    }

    /**
     * Sets up the cached bean and computes the information quality level using
     * the given age
     * 
     * @param aItem
     *            A "source" bean
     * @param aItemAge
     *            Age of the source bean
     */
    public CachedItemBean(final ItemBean aItem, final long aItemAge) {

        super(aItem);

        // Compute the quality
        pCacheLevel = QualityUtilities.computeCacheQuality(aItemAge);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.beans.ICachedBean#getQualityLevel()
     */
    @Override
    public int getQualityLevel() {

        return pCacheLevel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.beans.ICachedBean#setQualityLevel(int)
     */
    @Override
    public void setQualityLevel(final int aInformationQualityLevel) {

        pCacheLevel = aInformationQualityLevel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("CachedItemBean(");
        builder.append("item=").append(super.toString());
        builder.append(", quality=").append(
                QualityUtilities.qualityToString(pCacheLevel));
        builder.append(")");

        return builder.toString();
    }
}
