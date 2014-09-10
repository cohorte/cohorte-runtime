package org.psem2m.isolates.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ogattaz
 *
 */
public class CSnapshotNode extends CSnapshotAbstract {

    final List<CSnapshotIsolate> pIsolates = new ArrayList<CSnapshotIsolate>();

    /** Node name */
    private final String pNodeName;

    /** Node UID */
    private final String pNodeUid;

    /**
     * @param aState
     */
    public CSnapshotNode(final String aNodeUid, final String aNodeName) {

        super(aNodeUid);
        pNodeName = aNodeName;
        pNodeUid = aNodeUid;
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
            return pNodeUid.equals(other.pNodeUid);
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
     *            The UID of the isolate to look for
     * @return
     */
    public int getIndexOfChild(final String aUID) {

        int wIdx = 0;
        for (final CSnapshotIsolate wChild : pIsolates) {
            if (wChild.getIsolateUid().equals(aUID)) {
                return wIdx;
            }
            wIdx++;
        }
        return -1;
    }

    /**
     * @return the nodeName
     */
    public String getNodeName() {

        return pNodeName;
    }

    /**
     * @return the nodeUid
     */
    public String getNodeUid() {

        return pNodeUid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.CSnapshotAbstract#getTextInfo()
     */
    @Override
    public String getTextInfo() {

        return String.format("Node:\nName=[%s]\nUID=[%s]", pNodeName, pNodeUid);
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Node: %s" + pNodeUid;
    }
}
