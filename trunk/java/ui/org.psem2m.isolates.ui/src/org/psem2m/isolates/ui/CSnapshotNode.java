package org.psem2m.isolates.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ogattaz
 * 
 */
public class CSnapshotNode extends CSnapshotAbstract {

    private String pHostName;

    final List<CSnapshotIsolate> pIsolates = new ArrayList<CSnapshotIsolate>();

    /**
     * @param aState
     */
    public CSnapshotNode(final String aName) {

        super(aName);
    }

    /**
     * @param aIsolate
     */
    public void add(final CSnapshotIsolate aIsolate) {

        pIsolates.add(aIsolate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof CSnapshotNode) {

            final CSnapshotNode other = (CSnapshotNode) aObj;
            return pHostName.equals(other.pHostName);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.CSnapshotAbstract#getChild(int)
     */
    @Override
    public CSnapshotIsolate getChild(final int aIdx) {

        return pIsolates.get(aIdx);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.CSnapshotAbstract#getChildCount()
     */
    @Override
    public int getChildCount() {

        return pIsolates.size();
    }

    /**
     * @return
     */
    String getHostName() {

        return pHostName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.CSnapshotAbstract#getIndexOfChild(org.psem2m.isolates
     * .ui.CSnapshotAbstract)
     */
    @Override
    public int getIndexOfChild(final CSnapshotAbstract aChild) {

        return getIndexOfChild(aChild.getName());
    }

    /**
     * @param aUID
     * @return
     */
    public int getIndexOfChild(final String aUID) {

        int wIdx = 0;
        for (final CSnapshotIsolate wChild : pIsolates) {
            if (wChild.getUID().equals(aUID)) {
                return wIdx;
            }
            wIdx++;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.CSnapshotAbstract#getTextInfo()
     */
    @Override
    public String getTextInfo() {

        return String.format("Node:\nName=[%s]\nHostName=[%s]", getName(),
                pHostName);
    }

    /**
     * @param aUID
     */
    void removeChild(final String aUID) {

        final int wIdx = getIndexOfChild(aUID);
        if (wIdx > -1) {
            pIsolates.remove(wIdx);
        }
    }

    /**
     * @param aHostName
     */
    void setHostName(final String aHostName) {

        pHostName = aHostName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("node: %s", getName());
    }
}
