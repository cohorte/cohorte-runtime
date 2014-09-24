package org.psem2m.isolates.ui;

/**
 * @author ogattaz
 *
 */
public abstract class CSnapshotAbstract {

    /** The tree item name */
    private final String pName;

    /**
     * @param aState
     */
    public CSnapshotAbstract(final String aName) {

        pName = aName;
    }

    /**
     * @return
     */
    public abstract CSnapshotAbstract getChild(final int aIdx);

    /**
     * @return
     */
    public abstract int getChildCount();

    /**
     * @param aChild
     * @return
     */
    public abstract int getIndexOfChild(final CSnapshotAbstract aChild);

    /**
     * @return
     */
    public String getName() {

        return pName;
    }

    /**
     * @return
     */
    public abstract String getTextInfo();

}
